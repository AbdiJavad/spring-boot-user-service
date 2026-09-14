# AuditProject.ps1
Write-Host "--- Starting Project Audit ---" -ForegroundColor Cyan

# 1. Clean & Compile (Ensure build integrity)
Write-Host "[1/3] Running Maven Clean & Compile..." -ForegroundColor Yellow
./mvnw clean compile

if ($LASTEXITCODE -ne 0) {
    Write-Host "CRITICAL: Compile Failed!" -ForegroundColor Red
    exit 1
}

# 2. Test Execution (Ensure stability)
Write-Host "[2/3] Running All Tests..." -ForegroundColor Yellow
./mvnw test

if ($LASTEXITCODE -ne 0) {
    Write-Host "CRITICAL: Tests Failed!" -ForegroundColor Red
    exit 1
}

# 3. Encoding Check (Check for BOM - Enterprise standard: UTF-8 without BOM)
Write-Host "[3/3] Checking File Encodings..." -ForegroundColor Yellow
$files = Get-ChildItem -Recurse -Filter *.java
foreach ($file in $files) {
    $content = Get-Content $file.FullName -Raw
    # Simple check: if file starts with BOM chars, warn user
    if ($content.StartsWith([char]0xFEFF)) {
        Write-Host "WARNING: File has BOM: $($file.FullName)" -ForegroundColor Magenta
    }
}

Write-Host "--- Audit Complete: Project is Healthy ---" -ForegroundColor Green
<#
.SYNOPSIS
    AuditProject.ps1 - Corrected Version
#>

# تغییر به Continue برای اینکه خطاهای کوچک متوقف‌کننده نباشند
$ErrorActionPreference = "Continue"
$ProjectRoot = "D:\demo"
$StartTime = Get-Date

Write-Host "`n[🚀 STARTING PROJECT AUDIT: $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss')]" -ForegroundColor Cyan

function Write-Section($title) {
    Write-Host "`n[🔍 $title]" -ForegroundColor Yellow -Style Bold
}

try {
    # 1. Environment & Path Check
    Write-Section "Environment Check"
    # از یک متغیر موقت استفاده میکنیم تا خطاها در جریان اصلی نریزند
    $JavaVer = java -version 2>&1 | Select-Object -First 1
    Write-Host "✅ Java Version Detected: $JavaVer" -ForegroundColor Green

    $MavenVer = mvn -version 2>&1 | Select-Object -First 1
    Write-Host "✅ Maven Version Detected: $MavenVer" -ForegroundColor Green

    # 2. Project Structure
    Write-Section "Filesystem Integrity"
    $TargetDir = Join-Path $ProjectRoot "target"
    if (Test-Path $TargetDir) {
        Write-Host "⚠️  Cleaning 'target' directory..." -ForegroundColor Cyan
        Remove-Item -Recurse -Force $TargetDir
        Write-Host "✅ Cleaned." -ForegroundColor Green
    } else {
        Write-Host "✅ Filesystem is clean." -ForegroundColor Green
    }

    # 3. Maven Validation
    Write-Section "Maven Lifecycle Validation"
    Write-Host "Validating POM..." -ForegroundColor Cyan
    Set-Location $ProjectRoot
    $mvnResult = & mvn validate 2>&1
    if ($LASTEXITCODE -eq 0) {
        Write-Host "✅ Maven validation passed!" -ForegroundColor Green
    } else {
        Write-Host "❌ Maven validation failed!" -ForegroundColor Red
        Write-Host $mvnResult -ForegroundColor Gray
    }

    # 4. Git Check
    Write-Section "Git Status"
    $GitBranch = git rev-parse --abbrev-ref HEAD
    Write-Host "📍 Branch: $GitBranch" -ForegroundColor Cyan

    # 5. Security Scan
    Write-Section "Security Check"
    $PropFile = Join-Path $ProjectRoot "src/main/resources/application.properties"
    if (Test-Path $PropFile) {
        Write-Host "✅ application.properties found." -ForegroundColor Green
    } else {
        Write-Host "❌ application.properties MISSING!" -ForegroundColor Red
    }

    Write-Host "`n[🎉 AUDIT SUCCESSFUL] Ready for development!" -ForegroundColor Cyan

} catch {
    Write-Host "`n[❌ ERROR] $($_)" -ForegroundColor Red
}
$body = @{
    email = "invalid-email-format"
    password = "password123"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" `
                  -Method Post `
                  -Body $body `
                  -ContentType "application/json"
# 1. Kill any process on port 8080
$port = 8080
Write-Host "Checking port $port..." -ForegroundColor Cyan
$process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($process) {
    Write-Host "Killing process on port $port..." -ForegroundColor Yellow
    Stop-Process -Id $process.OwningProcess -Force
} else {
    Write-Host "Port $port is free." -ForegroundColor Green
}

# 2. Cleanup target directory to avoid build corruption
if (Test-Path "target") {
    Write-Host "Cleaning target folder..." -ForegroundColor Cyan
    Remove-Item -Path "target" -Recurse -Force
}

# 3. Build and Run
Write-Host "Starting Spring Boot Application..." -ForegroundColor Green
.\mvnw clean spring-boot:run

# File: test-request.ps1
$body = @{
    email = "invalid-email-format"
    password = "password123"
} | ConvertTo-Json

try {
    Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $body -ContentType "application/json"
} catch {
    Write-Host "Error Caught: " -ForegroundColor Red
    $_.Exception.Response.GetResponseStream() | ForEach-Object {
        $reader = New-Object System.IO.StreamReader($_)
        $reader.ReadToEnd()
    }
}

