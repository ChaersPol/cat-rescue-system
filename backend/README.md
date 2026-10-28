# 后端服务（backend）

流浪动物救助管理系统的后端，Spring Boot 3.5 + Spring Security + JWT + MyBatis + MySQL 8 + Redis。

## 模块划分

| 模块 | 职责 |
| --- | --- |
| `ruoyi-admin` | 启动入口、Web 层；业务 Controller 在 `com.ruoyi.web.controller.cat` |
| `ruoyi-framework` | 安全认证、权限校验、拦截器、全局配置 |
| `ruoyi-system` | 系统管理（用户/角色/菜单/字典）+ 业务领域层 `com.ruoyi.cat`（domain / mapper / service / task） |
| `ruoyi-quartz` | 定时任务（提醒通知扫描等） |
| `ruoyi-generator` | 代码生成工具 |
| `ruoyi-common` | 通用工具、注解、异常、常量 |

## 目录说明

- `sql/`：数据库。`cat_rescue.sql` 是全项目唯一一份完整库文件（46 张表），`upgrade/` 只放生产环境的增量脚本
- `bin/`：Windows 下的打包与启动脚本
- `uploadPath/`：上传文件存放目录（照片、缩略图、二维码），启动时会自动规范成绝对路径
- `logs/`：运行日志

数据库改动规则见 [docs/DATABASE.md](../docs/DATABASE.md)。

## 构建与启动

完整步骤（含数据库初始化、端口注意事项）见仓库根目录的 `README.md`。
