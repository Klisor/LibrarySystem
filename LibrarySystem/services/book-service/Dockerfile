# 使用本地已有的 Maven 镜像构建阶段
FROM m.daocloud.io/docker.io/library/maven:3.9-eclipse-temurin-17 AS builder

# 设置工作目录
WORKDIR /app

# 复制 Maven 包装器
COPY mvnw .
COPY .mvn .mvn

# 复制项目文件
COPY pom.xml .
COPY src src

# 构建项目（使用阿里云镜像加速）
RUN mvn clean package -DskipTests

# 第二阶段：使用本地已有的 JRE 镜像运行
FROM m.daocloud.io/docker.io/library/eclipse-temurin:17-jre-alpine

# 设置环境变量
ENV TZ=Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE=docker

# 设置工作目录
WORKDIR /app

# 从构建阶段复制 JAR 文件
COPY --from=builder /app/target/*.jar app.jar


# 暴露端口
EXPOSE 8081

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]