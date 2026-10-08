FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# ساخت کاربر غیر-root برای امنیت سازمانی
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY target/*.jar app.jar
EXPOSE 9090

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dserver.port=9090", "-jar", "app.jar"]
