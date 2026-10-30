FROM eclipse-temurin:17-jre-alpine AS runner
WORKDIR /app

# نصب ابزارهای پایه برای healthcheck
RUN apk add --no-cache curl wget bash

# ایجاد کاربر غیر روت برای امنیت (استاندارد Enterprise)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# کپی فایل جار از قبل کامپایل شده
COPY target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-XX:+UseG1GC", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
