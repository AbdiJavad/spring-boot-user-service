# Setup Structured Logging: logback-spring.xml
# Author: Felix AI Assistant for Javad
# Encoding: UTF-8 without BOM

$projectRoot = "D:\demo"
$configPath = "$projectRoot\src\main\resources\logback-spring.xml"

Write-Host "--- Initializing Structured Logging Setup ---" -ForegroundColor Cyan

# 1. Verify Directory
if ((Get-Location).Path -ne $projectRoot) {
    Write-Error "Please run this script from $projectRoot"
    exit 1
}

# 2. Create Directory if not exists
if (!(Test-Path "src/main/resources")) {
    New-Item -ItemType Directory -Path "src/main/resources" -Force
    Write-Host "Created src/main/resources directory." -ForegroundColor Green
}

# 3. Define XML Content (Heredoc)
$xmlContent = @"
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <springProperty scope="context" name="APP_NAME" source="spring.application.name" defaultValue="demo-service"/>

    <springProfile name="default | dev">
        <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder>
                <pattern>%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - [traceId=%X{traceId:-}, spanId=%X{spanId:-}] - %msg%n</pattern>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="CONSOLE"/>
        </root>
    </springProfile>

    <springProfile name="docker">
        <appender name="JSON_CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
            <encoder class="net.logstash.logback.encoder.LogstashEncoder">
                <customFields>{"app":"${APP_NAME}"}</customFields>
            </encoder>
        </appender>
        <root level="INFO">
            <appender-ref ref="JSON_CONSOLE"/>
        </root>
    </springProfile>
</configuration>
"@

# 4. Write with UTF-8 (No BOM)
$Utf8NoBom = New-Object System.Text.UTF8Encoding $false
[System.IO.File]::WriteAllText($configPath, $xmlContent, $Utf8NoBom)
Write-Host "Successfully created logback-spring.xml" -ForegroundColor Green

# 5. Run Maven Verification
Write-Host "--- Running Maven Build & Test ---" -ForegroundColor Yellow
./mvnw clean compile
if ($LASTEXITCODE -ne 0) { Write-Error "Build failed!"; exit 1 }

./mvnw test
if ($LASTEXITCODE -ne 0) { Write-Error "Tests failed!"; exit 1 }

Write-Host "--- Setup Complete! Ready to Commit ---" -ForegroundColor Cyan
git status
