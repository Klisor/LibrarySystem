# 图书馆管理系统 

## 1. 项目简介

### 项目名称
图书馆管理系统微服务版本 (Library Management System - Microservices)

### 版本
v1.1.0

### 项目描述
本项目基于单机版图书馆管理系统进行微服务化改造，将原单体应用拆分为三个独立的微服务：用户服务、图书服务和借阅服务。每个服务拥有独立的数据库，服务间通过 RESTful API 进行通信。

### 微服务架构说明
- **用户服务 (User Service)**: 处理用户注册、登录、信息管理等功能
- **图书服务 (Book Service)**: 处理图书的增删改查、库存管理等功能
- **借阅服务 (Borrow Service)**: 处理图书借阅、归还、续借等业务流程，通过 Feign 客户端调用用户服务和图书服务
- **Nacos 服务注册中心 (v1.1.0 新增)**: 提供服务注册与发现功能，实现服务间通过**服务名**进行调用

## 2. 架构图

```
客户端 (浏览器/API 客户端)
      ↓
┌─────────────────────────────────────────┐
│             API 网关（可选）             │
└─────────────────────────────────────────┘
        ↓           ↓           ↓
┌───────────┐ ┌───────────┐ ┌───────────┐
│  用户服务  │ │  图书服务  │ │  借阅服务  │
│  (8083)   │ │  (8081)   │ │  (8082)   │
└───────────┘ └───────────┘ └───────────┘
        ↓           ↓           ↓
┌─────────────────────────────────────────┐
│          Nacos 服务注册中心 (新增)       │
│               (8848端口)                 │
└─────────────────────────────────────────┘
        ↓           ↓           ↓
┌───────────┐ ┌───────────┐ ┌───────────┐
│  用户数据库 │ │  图书数据库 │ │  借阅数据库 │
│  (3307)   │ │  (3308)   │ │  (3309)   │
└───────────┘ └───────────┘ └───────────┘
```

**服务间调用关系 (v1.1.0 更新)**：

- 所有微服务启动时向 **Nacos** 注册。
- 借阅服务 → **通过服务名 `user-service` 调用** → 用户服务：获取用户信息，更新用户借阅数量
- 借阅服务 → **通过服务名 `book-service` 调用** → 图书服务：获取图书信息，更新图书库存

## 3. 技术栈

### 后端技术

- **Spring Boot**: 3.1.5
- **Java**: 17
- **Spring Cloud Alibaba Nacos Discovery (新增)**: 服务注册与发现
- **Spring Cloud OpenFeign**: 服务间通信
- **Spring Cloud LoadBalancer (新增)**: 客户端负载均衡
- **Spring Data JPA**: 数据持久化
- **MySQL**: 8.0
- **Docker & Docker Compose**: 容器化部署
- **JWT**: 用户认证和授权

### 开发工具
- **Maven**: 3.8+
- **Git**: 版本控制

## 4. 环境要求

### 开发环境
- **JDK**: 17 或更高版本
- **Maven**: 3.8 或更高版本
- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本

### 生产环境
- **Docker**: 20.10 或更高版本
- **Docker Compose**: 2.0 或更高版本
- **至少 4GB 可用内存**

## 5. 构建和运行步骤

### 5.1 克隆项目
```bash
git clone <项目地址>
cd LibrarySystem
```

### 5.2 配置环境变量
在项目根目录创建 `.env` 文件（如果不存在）：
```bash
# 如果 .env 文件不存在，可以使用以下命令创建
cp .env.example .env
# 然后编辑 .env 文件，根据需要修改配置
```

### 5.3 使用 Docker Compose 部署
```bash
# 构建并启动所有服务(包括新增的 Nacos)
docker-compose up -d --build

# 查看服务状态
docker-compose ps

# 查看服务日志
docker-compose logs -f [服务名]

# 停止所有服务
docker-compose down

# 停止服务并清理数据卷
docker-compose down -v
```

### 5.4 访问服务 (v1.1.0 更新)

服务启动后，可以通过以下地址访问：

| 服务             | 访问地址                    | 说明                                        |
| :--------------- | :-------------------------- | :------------------------------------------ |
| **Nacos 控制台** | http://localhost:8848/nacos | 服务注册与发现管理 (账号/密码: nacos/nacos) |
| 用户服务         | http://localhost:8083/api   | 用户管理相关API                             |
| 图书服务         | http://localhost:8081/api   | 图书管理相关API                             |
| 借阅服务         | http://localhost:8082/api   | 借阅管理相关API                             |

### 5.5 健康检查
```bash
# 检查用户服务健康状态
curl http://localhost:8083/api/actuator/health

# 检查图书服务健康状态
curl http://localhost:8081/api/actuator/health

# 检查借阅服务健康状态
curl http://localhost:8082/api/actuator/health
```

## 6. API 文档

### 6.1 通用响应格式
所有 API 接口都遵循以下响应格式：
```json
{
    "code": 200,
    "message": "成功",
    "data": {}, // 具体数据
    "timestamp": "2025-12-23 15:22:58"
}
```

### 6.2 主要 API 接口

#### 用户服务
| 方法   | 端点                  | 描述         | 权限            |
| ------ | --------------------- | ------------ | --------------- |
| POST   | `/api/users/register` | 用户注册     | 公开            |
| POST   | `/api/auth/login`     | 用户登录     | 公开            |
| GET    | `/api/users`          | 获取用户列表 | ADMIN           |
| GET    | `/api/users/{id}`     | 获取用户详情 | 用户自己或ADMIN |
| PUT    | `/api/users/{id}`     | 更新用户信息 | 用户自己或ADMIN |
| DELETE | `/api/users/{id}`     | 删除用户     | ADMIN           |

#### 图书服务
| 方法   | 端点                | 描述         | 权限  |
| ------ | ------------------- | ------------ | ----- |
| GET    | `/api/books`        | 获取图书列表 | 公开  |
| GET    | `/api/books/{id}`   | 获取图书详情 | 公开  |
| POST   | `/api/books`        | 添加图书     | ADMIN |
| PUT    | `/api/books/{id}`   | 更新图书信息 | ADMIN |
| DELETE | `/api/books/{id}`   | 删除图书     | ADMIN |
| GET    | `/api/books/search` | 搜索图书     | 公开  |

#### 借阅服务
| 方法 | 端点                       | 描述             | 权限            |
| ---- | -------------------------- | ---------------- | --------------- |
| POST | `/api/borrows`             | 借阅图书         | ADMIN           |
| POST | `/api/borrows/{id}/return` | 归还图书         | ADMIN           |
| POST | `/api/borrows/{id}/renew`  | 续借图书         | 借阅者          |
| GET  | `/api/borrows`             | 获取借阅记录     | 相关用户或ADMIN |
| GET  | `/api/borrows/{id}`        | 获取借阅记录详情 | 相关用户或ADMIN |
| GET  | `/api/borrows/overdue`     | 获取逾期记录     | ADMIN           |
| GET  | `/api/borrows/stats`       | 获取借阅统计     | ADMIN           |

### 6.3 详细 API 文档
详细 API 文档请参考 同目录下设计文档
## 7. 遇到的问题和解决方案

### 问题 1：服务间通信失败
**问题描述**：借阅服务调用用户服务和图书服务时返回 404 或连接失败。

**解决方案**：
1. 确保在 Docker 环境中使用容器名而不是 `localhost` 进行服务间通信
2. 正确配置 Feign 客户端的 URL，例如：`http://user-service:8083/api`
3. 确保所有服务都在同一个 Docker 网络中

### 问题 2：数据库连接问题
**问题描述**：服务启动时无法连接到数据库。

**解决方案**：
1. 使用 Docker Compose 的 `depends_on` 和健康检查确保数据库先启动
2. 在数据库连接 URL 中使用正确的容器名和端口
3. 确保数据库用户权限正确配置

### 问题 3：JWT 认证失败
**问题描述**：服务间调用时因缺少或无效的 Token 而返回 401 错误。

**解决方案**：
1. 在借阅服务中配置有效的服务 Token
2. 确保用户服务和图书服务的内部接口验证 Token 的逻辑正确
3. 使用环境变量管理敏感信息，避免硬编码

### 问题 4：数据不一致
**问题描述**：借阅操作中部分服务调用成功，部分失败，导致数据不一致。

**解决方案**：
1. 在借阅服务中使用本地事务确保借阅记录创建成功
2. 将用户服务和图书服务的更新操作作为可重试的辅助操作
3. 记录详细的日志以便问题排查和数据修复

### 问题 5：容器资源不足
**问题描述**：Docker 容器因内存不足而崩溃。

**解决方案**：
1. 调整 Docker Desktop 的内存分配（建议至少 4GB）
2. 优化 JVM 内存参数
3. 使用 Alpine 基础镜像减少镜像大小

### 问题 6：Nacos 镜像拉取失败（403 Forbidden 错误）

**问题描述**：使用 `docker-compose up` 启动时，Nacos 容器因 `403 Forbidden` 错误而无法拉取镜像。

**解决方案**：

1.  此错误通常是由于 Docker 客户端配置的镜像加速器（如某些校园镜像源）权限问题导致。
2.  **移除或替换镜像源**：将导致错误的镜像源（例如 `https://docker.nju.edu.cn`）从列表中移除或替换为可靠的镜像源（https://docker.m.daocloud.io）。
3.  **重启Docker服务**：修改配置后，**必须完全重启 Docker Desktop 或 Docker 守护进程**。
4.  **清理并重试**：执行 `docker-compose down --rmi all` 清理旧镜像，再重新启动。

## 8. 项目结构

```
library-management/
├── services/
│   ├── user-service/          # 用户服务
│   │   ├── src/main/java/com/zjgsu/librarymanagement/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   ├── book-service/          # 图书服务
│   │   ├── src/main/java/com/zjgsu/librarymanagement/
│   │   ├── src/main/resources/
│   │   ├── pom.xml
│   │   └── Dockerfile
│   └── borrow-service/        # 借阅服务
│       ├── src/main/java/com/zjgsu/librarymanagement/
│       ├── src/main/resources/
│       ├── pom.xml
│       └── Dockerfile
├── docker-compose.yml         # Docker Compose 配置文件
├── .env                       # 环境变量文件
├── README.md                  # 项目说明文档
```

---

## 9. Nacos服务注册与发现配置 (v1.1.0 新增)

从 v1.1.0 版本开始，系统引入了 **Nacos** 作为服务注册与发现中心，以实现微服务间更优雅的通信和服务治理。

### 9.1 概述
- **角色**：Nacos 作为服务注册中心，所有微服务（用户、图书、借阅）在启动时自动注册。
- **调用方式**：服务间调用（如借阅服务调用用户服务）**通过服务名**（如 `user-service`）进行，无需再配置固定IP或主机名。
- **架构提升**：为后续引入动态配置管理、服务熔断等功能奠定了基础。

### 9.2 各服务配置步骤
1.  **在 `pom.xml` 中添加依赖**（所有服务）：
    ```xml
    <dependency>
        <groupId>com.alibaba.cloud</groupId>
        <artifactId>spring-cloud-starter-alibaba-nacos-discovery</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.cloud</groupId>
        <artifactId>spring-cloud-starter-loadbalancer</artifactId>
    </dependency>
    ```

2.  **在 `application.yml` 中添加Nacos配置**（所有服务）：
    ```yaml
    spring:
      cloud:
        nacos:
          discovery:
            server-addr: ${NACOS_SERVER_ADDR:nacos:8848}
            namespace: ${NACOS_NAMESPACE:public}
            group: DEFAULT_GROUP
            ephemeral: true
    ```

3.  **在主启动类添加注解**（所有服务）：
    ```java
    @EnableDiscoveryClient
    ```

4.  **修改Feign客户端**（仅借阅服务）：将 `@FeignClient` 注解中的 `url` 属性改为通过 `name`（服务名）调用。
    ```java
    // 之前（基于URL）: @FeignClient(url = "${app.service.user.url}")
    // 之后（基于服务名）:
    @FeignClient(name = "user-service")
    public interface UserServiceClient { ... }
    ```

### 9.3 Docker Compose 配置
在 `docker-compose.yml` 中新增 `nacos` 服务，并为所有业务服务添加对应的环境变量和依赖。

**新增Nacos服务定义**：
```yaml
services:
  nacos:
    image: nacos/nacos-server:v2.4.0
    container_name: nacos
    restart: always
    environment:
      - MODE=standalone
    ports:
      - "8848:8848"
      - "9848:9848"
      - "9849:9849"
    volumes:
      - nacos-data:/home/nacos/data
      - nacos-logs:/home/nacos/logs
    networks:
      - library-network
    healthcheck:
      test: ["CMD", "curl", "-f", "http://localhost:8848/nacos/"]
      interval: 30s
      timeout: 10s
      retries: 3
```

**为业务服务添加环境变量**（以 `user-service` 为例）：
```yaml
user-service:
  environment:
    - NACOS_SERVER_ADDR=nacos:8848
    # ... 其他环境变量
  depends_on:
    - nacos
    - mysql-user
```

### 9.4 验证配置
1.  **启动所有服务**：`docker-compose up -d --build`
2.  **访问Nacos控制台**：`http://localhost:8848/nacos` (默认账号/密码: `nacos`/`nacos`)
3.  **检查服务列表**：在Nacos控制台的 **“服务管理 -> 服务列表”** 中，应能看到 `user-service`、`book-service`、`borrow-service` 均已注册。
4.  **测试服务调用**：通过借阅接口发起一个借书请求，验证其能否通过服务名成功调用用户服务和图书服务。
