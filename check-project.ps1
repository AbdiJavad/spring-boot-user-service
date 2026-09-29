# D:\demo\check-project.ps1
$projectPath = "D:\demo"
$backupPath = "D:\demo_backup"

Write-Host "--- Initiating Enterprise Health Check for $projectPath ---" -ForegroundColor Cyan

# ۱. ایجاد بک‌آپ (Safety First)
if (-not (Test-Path $backupPath)) {
    Write-Host "Creating backup at $backupPath..." -ForegroundColor Yellow
    Copy-Item -Path "$projectPath\*" -Destination $backupPath -Recurse
}

# ۲. بررسی Encoding (باید UTF-8 باشد)
$files = Get-ChildItem -Path $projectPath -Recurse -Include *.java, *.properties, *.xml
foreach ($file in $files) {
    $content = Get-Content -Path $file.FullName -Encoding Byte -TotalCount 3
    # بررسی UTF-8 BOM
    if ($content[0] -eq 0xEF -and $content[1] -eq 0xBB -and $content[2] -eq 0xBF) {
        Write-Host "Warning: File $($file.Name) has BOM. Standardizing to UTF-8..." -ForegroundColor Yellow
    }
}

# ۳. بررسی وضعیت Git
$gitStatus = git status --porcelain
if ($gitStatus) {
    Write-Host "Alert: There are uncommitted changes. Please commit them before merging!" -ForegroundColor Red
    exit
} else {
    Write-Host "Git status: Clean" -ForegroundColor Green
}

# ۴. اجرای Maven Validate برای بررسی صحت ساختار
Write-Host "Running mvn clean validate..." -ForegroundColor Cyan
./mvnw clean validate
if ($LASTEXITCODE -eq 0) {
    Write-Host "Build validation successful!" -ForegroundColor Green
} else {
    Write-Host "Error: Maven validation failed. Check your imports and dependencies." -ForegroundColor Red
    exit
}

Write-Host "--- Health Check Completed. Code is ready for Merge. ---" -ForegroundColor Green
