#!/usr/bin/env bash
# ============================================================================
#  宠物救助系统 · 阿里云 ECS 一键部署脚本（单机全栈，用 IP 直接访问）
# ============================================================================
#  用法（在服务器上以 root 执行）：
#     bash deploy.sh            # 全量安装并启动
#     bash deploy.sh app        # 只更新后端 + 前端（改了代码后重发版）
#     bash deploy.sh status     # 看各服务状态
#
#  前置条件（脚本不会自己做的部分）：
#    1. 已把这三个东西放到 /opt/cat-rescue/ 下：
#         app/ruoyi-admin.jar       后端包
#         dist/                     前端打包产物（frontend/dist 整个目录）
#         sql/cat_rescue.sql        数据库基线
#         uploadPath/               演示图片目录（可省略，省略则相册空白）
#    2. 安全组已放通 80（443 不用，本项目走 http）
#
#  架构：
#     80  →  nginx（静态 dist + /prod-api 反代到 8080 + /profile/upload 反代到 8080）
#     8080→  Spring Boot（systemd 托管，开机自启）
#     3306→  MySQL 8（本机 root，口令见 application-druid.yml）
#     6379→  Redis（本机无密码）
# ============================================================================
set -euo pipefail

APP_DIR=/opt/cat-rescue
JAR=$APP_DIR/app/ruoyi-admin.jar
DIST=$APP_DIR/dist
SQL=$APP_DIR/sql/cat_rescue.sql
UPLOAD=$APP_DIR/uploadPath
DB_NAME=cat_rescue
DB_USER=root
DB_PASS=123456          # 与 application-druid.yml 保持一致；上线后建议改强口令并同步改 yml 重发
PROFILE=/opt/cat-rescue/uploadPath
# 1.7G 小内存机器：堆上限压到 640m + 串行 GC，避免和 MySQL 抢内存把 sshd 拖死
JAVA_OPTS="-Xms256m -Xmx640m -XX:+UseSerialGC -Duser.timezone=Asia/Shanghai -Dfile.encoding=UTF-8"

log()  { printf '\n\033[1;32m==> %s\033[0m\n' "$*"; }
warn() { printf '\033[1;33m[!] %s\033[0m\n' "$*"; }

need_root() { [ "$(id -u)" -eq 0 ] || { echo "请用 root 执行"; exit 1; }; }

# ---------------------------------------------------------------- 包管理器
pkg_install() {
  if command -v dnf >/dev/null 2>&1; then
    dnf install -y "$@"
  elif command -v yum >/dev/null 2>&1; then
    yum install -y "$@"
  else
    export DEBIAN_FRONTEND=noninteractive
    apt-get update -y && apt-get install -y "$@"
  fi
}

install_deps() {
  log "安装 JDK 17 / MySQL / Redis / Nginx"
  if command -v dnf >/dev/null 2>&1 || command -v yum >/dev/null 2>&1; then
    pkg_install java-17-openjdk-headless mysql-server redis nginx tar
  else
    pkg_install openjdk-17-jre-headless mysql-server redis-server nginx tar
  fi

  # MySQL 8 装完不一定自启，统一拉起并设开机自启
  for s in mysqld mysql; do
    systemctl enable --now "$s" 2>/dev/null && break || true
  done
  for s in redis redis-server; do
    systemctl enable --now "$s" 2>/dev/null && break || true
  done
  systemctl enable --now nginx

  java -version 2>&1 | head -1
}

# ---------------------------------------------------------------- 数据库
init_db() {
  [ -f "$SQL" ] || { warn "缺少 $SQL，跳过建库"; return; }

  log "初始化数据库 $DB_NAME（会 DROP 同名表，首次部署用）"
  # Ubuntu 的 mysql root 默认走 auth_socket，先确保能用口令登录
  if ! mysql -uroot -p"$DB_PASS" -e "select 1" >/dev/null 2>&1; then
    warn "root 口令不是 $DB_PASS，尝试设置为该值"
    mysql -uroot <<SQLSTMT 2>/dev/null || true
ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY '$DB_PASS';
FLUSH PRIVILEGES;
SQLSTMT
  fi
  mysql -uroot -p"$DB_PASS" < "$SQL" || { echo "导入基线失败"; exit 1; }
  mysql -uroot -p"$DB_PASS" -N -e \
    "select concat('表数：', count(*)) from information_schema.tables where table_schema='$DB_NAME';"
}

# ---------------------------------------------------------------- 后端服务
install_app() {
  log "部署后端 jar 与前端静态资源"
  [ -f "$JAR" ] || { echo "缺少 $JAR"; exit 1; }
  [ -d "$DIST" ] || { echo "缺少 $DIST"; exit 1; }
  mkdir -p "$PROFILE" "$APP_DIR/logs"
  [ -d "$UPLOAD" ] && cp -rn "$UPLOAD/." "$PROFILE/" 2>/dev/null || true

  cat > /etc/systemd/system/cat-rescue.service <<UNIT
[Unit]
Description=Cat Rescue System (Spring Boot)
After=network.target mysqld.service redis.service

[Service]
Type=simple
User=root
WorkingDirectory=$APP_DIR/app
ExecStart=/usr/bin/java $JAVA_OPTS -jar $JAR --server.port=8080 --ruoyi.profile=$PROFILE
SuccessExitStatus=143
Restart=always
RestartSec=5
StandardOutput=append:$APP_DIR/logs/stdout.log
StandardError=append:$APP_DIR/logs/stderr.log

[Install]
WantedBy=multi-user.target
UNIT

  systemctl daemon-reload
  systemctl enable cat-rescue
  systemctl restart cat-rescue
}

# ---------------------------------------------------------------- Nginx
install_nginx() {
  log "配置 Nginx（80 端口直接对外）"
  cat > /etc/nginx/conf.d/cat-rescue.conf <<'NGINX'
server {
    listen 80 default_server;
    server_name _;
    charset utf-8;
    client_max_body_size 200m;

    root /opt/cat-rescue/dist;
    index index.html;

    # 前端 history 路由
    location / {
        try_files $uri $uri/ /index.html;
    }

    # 后端接口
    location /prod-api/ {
        proxy_pass http://127.0.0.1:8080/;
        proxy_set_header Host              $host;
        proxy_set_header X-Real-IP         $remote_addr;
        proxy_set_header X-Forwarded-For   $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_read_timeout 300s;
    }

    # 上传的图片/视频（后端 /profile/upload/** 静态映射）
    # 必须只匹配上传目录：前端菜单里也有一级路由 /profile（猫咪档案），
    # 若写成 location /profile/ ，刷新 /profile/profile 会被转给后端，
    # 返回 {"msg":"No static resource profile.","code":500} 白屏。
    location ^~ /profile/upload/ {
        proxy_pass http://127.0.0.1:8080/profile/upload/;
        proxy_set_header Host $host;
        expires 7d;
    }

    gzip on;
    gzip_min_length 1k;
    gzip_comp_level 5;
    gzip_types text/plain text/css application/javascript application/json image/svg+xml;
}
NGINX

  # 去掉发行版默认站点，避免抢 default_server
  rm -f /etc/nginx/sites-enabled/default 2>/dev/null || true
  # RHEL 系默认 server 块在 nginx.conf 里，去掉它的 default_server 冲突
  sed -i 's/listen\s*80 default_server;/listen 80;/; s/listen\s*\[::\]:80 default_server;/listen [::]:80;/' /etc/nginx/nginx.conf 2>/dev/null || true

  nginx -t && systemctl restart nginx
}

# ---------------------------------------------------------------- 防火墙
open_fw() {
  log "放通本机防火墙 80/443（云安全组仍需在控制台放通）"
  if systemctl is-active --quiet firewalld; then
    firewall-cmd --permanent --add-service=http 2>/dev/null || true
    firewall-cmd --permanent --add-service=https 2>/dev/null || true
    firewall-cmd --reload 2>/dev/null || true
  fi
  # Ubuntu ufw 一般不启用，启用时也放通
  if command -v ufw >/dev/null 2>&1 && ufw status 2>/dev/null | grep -q "Status: active"; then
    ufw allow 80/tcp; ufw allow 443/tcp
  fi
}

health() {
  log "健康检查"
  sleep 5
  printf '后端 8080  : '; curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1:8080/captchaImage || true
  printf 'Nginx 80   : '; curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1/ || true
  printf '接口代理   : '; curl -s -o /dev/null -w '%{http_code}\n' http://127.0.0.1/prod-api/captchaImage || true
  # 前端页面刷新不能落到后端（历史上 /profile/ 代理把 /profile/profile 吞掉，刷新白屏）
  printf '页面刷新   : '; curl -s -o /dev/null -w '%{http_code} %{content_type}\n' http://127.0.0.1/profile/profile || true
  IP=$(curl -s --max-time 5 http://100.100.100.200/latest/meta-data/eipv4 2>/dev/null || hostname -I | awk '{print $1}')
  echo
  echo "访问地址： http://$IP/"
  echo "后端日志： tail -f $APP_DIR/logs/stdout.log    或  journalctl -u cat-rescue -f"
}

case "${1:-all}" in
  all)    need_root; install_deps; init_db; install_app; install_nginx; open_fw; health ;;
  app)    need_root; install_app; systemctl reload nginx; health ;;
  status) systemctl --no-pager status cat-rescue nginx mysqld 2>/dev/null | head -40; health ;;
  *)      echo "用法： bash $0 [all|app|status]" ;;
esac
