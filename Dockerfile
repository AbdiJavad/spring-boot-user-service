FROM eclipse-temurin:17-jre-alpine
RUN apk add --no-cache curl \
    && addgroup -S spring && adduser -S spring -G spring
WORKDIR /app
COPY target/*.jar /app/app.jar
USER spring:spring

EXPOSE 9090
ENV JAVA_TOOL_OPTIONS="-XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=25.0 -XX:+ExitOnOutOfMemoryError -Dfile.encoding=UTF-8"
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
