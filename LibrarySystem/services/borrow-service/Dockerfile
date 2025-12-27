# 第二阶段：使用本地已有的 JRE 镜像运行
FROM eclipse-temurin:17-jre-alpine

# 设置环境变量
ENV TZ=Asia/Shanghai
ENV SPRING_PROFILES_ACTIVE=docker



# 从构建阶段复制 JAR 文件
COPY  target/*.jar app.jar


# 暴露端口
EXPOSE 8082

# 启动应用
ENTRYPOINT ["java", "-jar", "app.jar"]