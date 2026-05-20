🐟 大鱼吃小鱼

> 基于 Spring Boot + MySQL + JWT 的全栈 Web 游戏，支持用户系统与全服排行榜
<img width="1849" height="993" alt="6de37aac-a224-4ea4-ac83-59b21fde1c99" src="https://github.com/user-attachments/assets/da60ee3a-4236-4dff-813f-e1adf71fca56" />
<img width="1856" height="990" alt="3787fbe9-6abf-4189-b8ea-f7750f36d620" src="https://github.com/user-attachments/assets/a6775710-1998-4cbb-add9-ee83cd16c4b2" />
<img width="1845" height="986" alt="91d1f9e2-7758-424f-bac7-66cfbf6c7e97" src="https://github.com/user-attachments/assets/f475dd82-1642-4ffa-b118-dd56b6673296" />




 🔗 在线体验
游戏地址：https://fishgame-production-2c91.up.railway.app/login.html

> 支持 PC 和手机浏览器直接游玩，无需下载

 ✨ 功能特性

- 🐠 游戏核心：HTML5 Canvas 实现游戏主循环、精灵图帧动画、碰撞检测
- 📱 移动适配：支持触屏操作，自适应多端屏幕
- 🎵 背景音乐：游戏内 BGM 播放与切换
- 👤 用户系统：注册 / 登录，JWT 无状态认证，BCrypt 密码加密
- 🏆 全服排行榜：分数云端持久化，展示全服 Top 20 历史最高分
---
🛠 技术栈
| 层级 | 技术 |
|------|------|
| 后端框架 | Spring Boot 3、Spring Security、Spring Data JPA |
| 认证方案 | JWT（jjwt）+ BCrypt |
| 数据库 | MySQL（Railway 云数据库）|
| 前端 | HTML5 Canvas、原生 JavaScript、CSS3 |
| 构建工具 | Maven、Java 17、Lombok |
| 部署平台 | Railway |

📡 主要 API

| 方法 | 路径 | 说明 | 是否需要认证 |
|------|------------------|----------------------|----|
| POST | api/register     | 用户注册             | 否 |
| POST | /api/login       | 登录，返回 JWT Token | 否 |
| POST | /api/score       | 上传本局分数         | 是 |
| GET  | /api/leaderboard | 获取排行榜 Top 20    | 否 |

---

 🚀 本地运行
1. 克隆仓库
 bash
git clone https://github.com/nonamewolf0116/fishgame.git
cd fishgame
2. 配置数据库（src/main/resources/application.yml）
 yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fishgame
    username: root
    password: 123456


3. 启动项目
4. bash
./mvnw spring-boot:run

4. 打开浏览器访问 `http://localhost:8080/login.html
---

 📝 开发说明

本项目在开发过程中借助了 Claude Coded 进行辅助编码，核心逻辑（JWT 认证流程、Canvas 游戏循环、Spring Security 配置）均经过逐行阅读与手动调整。
