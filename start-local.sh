#!/bin/bash
# 本地一键起前后端（开发用）
#
# 用法：./start-local.sh     （Ctrl+C 同时关掉两个服务）
#
# 说明：后端优先用 8080；被别的项目占用就自动顺延（8090、8091…），
#       前端固定 80 端口，代理自动指向实际的后端端口，所以浏览器地址始终是 http://localhost

ROOT="$(cd "$(dirname "$0")" && pwd)"
JAR="$ROOT/backend/ruoyi-admin/target/ruoyi-admin.jar"

if [ ! -f "$JAR" ]; then
  echo "找不到 $JAR"
  echo "请先在 backend 目录执行：mvn -o clean install -DskipTests"
  exit 1
fi

if [ ! -d "$ROOT/frontend/node_modules" ]; then
  echo "前端依赖还没装，请先执行：cd frontend && npm install"
  exit 1
fi

port=8080
tries=0
while lsof -nP -iTCP:$port -sTCP:LISTEN >/dev/null 2>&1; do
  tries=$((tries + 1))
  if [ $tries -gt 20 ]; then
    echo "8080-${port} 都被占用了，先清理一下再试"
    exit 1
  fi
  echo "端口 $port 已被占用，换一个"
  port=$((port + 1))
done

echo "后端：http://localhost:$port"
echo "前端：http://localhost  （/dev-api 代理到 :$port）"

(cd "$ROOT/backend" && exec java -jar "$JAR" --server.port=$port) &
BACK_PID=$!

# 等后端真的能应答再起前端，免得首屏接口全 502
for _ in $(seq 1 60); do
  if curl -s --noproxy '*' -o /dev/null "http://127.0.0.1:$port/captchaImage"; then
    break
  fi
  sleep 1
done

(cd "$ROOT/frontend" && VITE_PROXY_TARGET=http://localhost:$port npm run dev) &
FRONT_PID=$!

trap 'kill $BACK_PID $FRONT_PID 2>/dev/null' INT TERM
wait
