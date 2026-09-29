[CmdletBinding()]
param (
    [string]$ProjectPath = "D:\demo",
    [switch]$RunTests = $true,
    [switch]$CreateBackup = $true
)

$ErrorActionPreference = "Stop"
$AuditDate = Get-Date -Format "yyyy-MM-dd_HH-mm-ss"
$ReportSeparator = "=" * 80

Write-Host "`n$ReportSeparator" -ForegroundColor Cyan
Write-Host " [ENTERPRISE AUDIT] Starting Health Check for: $ProjectPath" -ForegroundColor Cyan
Write-Host " Timestamp: $AuditDate" -ForegroundColor DarkGray
Write-Host "$ReportSeparator`n" -ForegroundColor Cyan

Set-Location -Path $ProjectPath

# 1. مرحله پشتیبان‌گیری خودکار (Safety First / Datensicherung)
if ($CreateBackup) {
    Write-Host "[1/5] Creating pre-audit timestamped backup..." -ForegroundColor Yellow
    $BackupDir = Join-Path -Path $ProjectPath -ChildPath "..\demo-backups"
    if (-not (Test-Path $BackupDir)) {
        New-Item -ItemType Directory -Path $BackupDir -Force | Out-Null
    }
    $ArchiveFile = Join-Path -Path $BackupDir -ChildPath "demo_backup_$AuditDate.zip"
    
    Get-ChildItem -Path $ProjectPath -Exclude "target", ".git", "docker-data" | 
        Compress-Archive -DestinationPath $ArchiveFile -Force
    Write-Host "  -> Backup safely stored at: $ArchiveFile" -ForegroundColor Green
}

# 2. بررسی ساختار و فایل‌های ناخواسته / آلودگی IDE
Write-Host "`n[2/5] Inspecting project structure for IDE pollution and duplicated classes..." -ForegroundColor Yellow

$SuspiciousDirs = Get-ChildItem -Path $ProjectPath -Recurse -Directory -Filter ".idea" | 
    Where-Object { $_.FullName -ne (Join-Path $ProjectPath ".idea") }

$ImlFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*.iml" | 
    Where-Object { $_.FullName -notmatch "pom.xml" }

if ($SuspiciousDirs.Count -gt 0 -or $ImlFiles.Count -gt 0) {
    Write-Host "  [WARN] Nested IDE files detected! Recommended to clean:" -ForegroundColor Red
    $SuspiciousDirs | ForEach-Object { Write-Host "   - Nested .idea: $($_.FullName)" -ForegroundColor DarkYellow }
    $ImlFiles | ForEach-Object { Write-Host "   - Stray .iml:   $($_.FullName)" -ForegroundColor DarkYellow }
} else {
    Write-Host "  -> Project directory tree is clean of nested IDE metadata." -ForegroundColor Green
}

# بررسی فایل‌های کلاس جاوا با نام یکسان
$JavaFiles = Get-ChildItem -Path (Join-Path $ProjectPath "src") -Recurse -Filter "*.java"
$Duplicates = $JavaFiles | Group-Object Name | Where-Object { $_.Count -gt 1 }

if ($Duplicates) {
    Write-Host "  [ALERT] Duplicate Class Names found in src/:" -ForegroundColor Magenta
    foreach ($dup in $Duplicates) {
        Write-Host "   - Class $($dup.Name) exists in multiple locations:" -ForegroundColor DarkMagenta
        $dup.Group | ForEach-Object { Write-Host "       $($_.FullName)" -ForegroundColor Gray }
    }
} else {
    Write-Host "  -> No duplicated Java class names found across source packages." -ForegroundColor Green
}

# 3. بررسی Encoding و کاراکترهای مخفی (BOM, Null Bytes)
Write-Host "`n[3/5] Auditing text encoding, UTF-8 BOM, and null byte sanity..." -ForegroundColor Yellow

$TextExtensions = @("*.java", "*.properties", "*.yml", "*.yaml", "*.sql", "*.xml", ".gitignore", ".dockerignore")
$EncodingIssues = 0

foreach ($ext in $TextExtensions) {
    $files = Get-ChildItem -Path $ProjectPath -Recurse -Filter $ext | Where-Object { $_.FullName -notmatch "\\target\\" }
    foreach ($file in $files) {
        $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
        
        # Check UTF-8 BOM
        if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
            Write-Host "  [WARN] UTF-8 with BOM detected: $($file.FullName.Replace($ProjectPath, ''))" -ForegroundColor Yellow
            $EncodingIssues++
        }
        
        # Check Null Bytes
        if ($bytes -contains 0x00) {
            Write-Host "  [CRITICAL] Null byte detected: $($file.FullName.Replace($ProjectPath, ''))" -ForegroundColor Red
            $EncodingIssues++
        }
    }
}

if ($EncodingIssues -eq 0) {
    Write-Host "  -> All inspected configuration and source files are clean UTF-8 (No BOM, No Null bytes)." -ForegroundColor Green
}

# 4. اجرای آزمون‌های یکپارچگی و کامپایل Maven
if ($RunTests) {
    Write-Host "`n[4/5] Executing Maven clean compile & verify..." -ForegroundColor Yellow
    $MvnCmd = if (Test-Path ".\mvnw.cmd") { ".\mvnw.cmd" } else { "mvn" }
    
    Write-Host "  -> Running: $MvnCmd clean test" -ForegroundColor DarkCyan
    & $MvnCmd clean test "-Dspring.profiles.active=test"
    
    if ($LASTEXITCODE -eq 0) {
        Write-Host "  -> Maven Build & All Tests Passed Successfully (BUILD SUCCESS)!" -ForegroundColor Green
    } else {
        Write-Host "  -> Maven Build failed with exit code $LASTEXITCODE." -ForegroundColor Red
    }
}

# 5. گزارش وضعیت Git
Write-Host "`n[5/5] Checking Git Status and Branch Context..." -ForegroundColor Yellow
$CurrentBranch = (git rev-parse --abbrev-ref HEAD).Trim()
$GitStatus = (git status --porcelain)

Write-Host "  -> Active Branch: $CurrentBranch" -ForegroundColor Cyan

if ([string]::IsNullOrWhiteSpace($GitStatus)) {
    Write-Host "  -> Working tree clean. Everything is committed and up to date." -ForegroundColor Green
} else {
    Write-Host "  [INFO] Uncommitted changes detected:" -ForegroundColor Yellow
    git status -s
}

Write-Host "`n$ReportSeparator" -ForegroundColor Cyan
Write-Host " [ENTERPRISE AUDIT] Completed." -ForegroundColor Cyan
Write-Host "$ReportSeparator`n" -ForegroundColor Cyan
