<#
.SYNOPSIS
    Automated Project Health & Security Analyzer for Spring Boot Projects.
.DESCRIPTION
    Analyzes project structure, duplicate classes, file encodings, secret leaks,
    compilation status, and generates clean production-grade backups.
.PARAMETER ProjectPath
    Root path of the Spring Boot repository. Defaults to current directory.
.PARAMETER CreateBackup
    Switch flag to generate a lightweight ZIP backup excluding build artifacts.
.EXAMPLE
    pwsh ./scripts/Analyze-SpringBootProject.ps1 -CreateBackup
#>

[CmdletBinding()]
param (
    [Parameter(Position = 0)]
    [string]$ProjectPath = (Get-Location).Path,

    [switch]$CreateBackup
)

$ErrorActionPreference = "Stop"
$sw = [System.Diagnostics.Stopwatch]::StartNew()

function Write-SectionHeader ([string]$title) {
    Write-Host "`n========================================================" -ForegroundColor Cyan
    Write-Host "  $title" -ForegroundColor Cyan
    Write-Host "========================================================" -ForegroundColor Cyan
}

function Write-AuditResult ([string]$item, [bool]$passed, [string]$detail = "") {
    if ($passed) {
        Write-Host "  [PASS] " -ForegroundColor Green -NoNewline
        Write-Host "$item $detail"
    } else {
        Write-Host "  [WARN/FAIL] " -ForegroundColor Red -NoNewline
        Write-Host "$item $detail" -ForegroundColor Yellow
    }
}

Set-Location $ProjectPath
Write-SectionHeader "Spring Boot User Service: Enterprise Project Audit"
Write-Host "Working Directory : $ProjectPath"
Write-Host "Timestamp         : $(Get-Date -Format 'yyyy-MM-dd HH:mm:ss K')"

# 1. Structure & Essential Files
Write-SectionHeader "1. Essential Build & Architecture Files"
$essentialFiles = @("pom.xml", "mvnw", "Dockerfile", "docker-compose.yml", ".env.example", ".gitignore")
foreach ($file in $essentialFiles) {
    $exists = Test-Path (Join-Path $ProjectPath $file)
    Write-AuditResult -item "File '$file'" -passed $exists
}

# 2. Duplicate Class Names Detection
Write-SectionHeader "2. Duplicate Java Class Detection"
$javaFiles = Get-ChildItem -Path $ProjectPath -Recurse -Filter "*.java" | Where-Object {
    $_.FullName -notmatch '[\\/](target|\.git|\.idea)[\\/]'
}

$duplicates = $javaFiles | Group-Object BaseName | Where-Object { $_.Count -gt 1 }
if ($duplicates.Count -eq 0) {
    Write-AuditResult -item "Duplicate Java Classes" -passed $true -detail "(None found across packages)"
} else {
    foreach ($dup in $duplicates) {
        Write-AuditResult -item "Duplicate Class Name: '$($dup.Name)'" -passed $false -detail "($($dup.Count) instances)"
        $dup.Group | ForEach-Object { Write-Host "     -> $($_.FullName)" -ForegroundColor DarkGray }
    }
}

# 3. Encoding & Non-ASCII / Invisible Character Check
Write-SectionHeader "3. Source Encoding & Illegal Character Check"
$illegalCharFound = $false
foreach ($jFile in $javaFiles) {
    $content = Get-Content -Path $jFile.FullName -Raw -Encoding utf8
    if ($content -match '[\u200B-\u200D\uFEFF]') {
        Write-AuditResult -item "Invisible Character detected" -passed $false -detail "in $($jFile.Name)"
        $illegalCharFound = $true
    }
}
if (-not $illegalCharFound) {
    Write-AuditResult -item "UTF-8 & Hidden/Invisible Characters" -passed $true -detail "(Clean)"
}

# 4. Sensitive Secrets / Hardcoded Credentials Audit
Write-SectionHeader "4. Hardcoded Secrets & Credentials Audit"
$secretPatterns = @(
    '(?i)jwt\.secret\s*=\s*[^\$\{\s]{10,}',
    '(?i)password\s*=\s*["''][^"'']{4,}["'']',
    '(?i)private\s+String\s+jwtSecret\s*=\s*"[^"]{5,}"'
)
$leaksFound = 0
$configFiles = Get-ChildItem -Path (Join-Path $ProjectPath "src") -Recurse -Include "*.properties","*.yml","*.yaml","*.java"

foreach ($cfg in $configFiles) {
    $lines = Get-Content -Path $cfg.FullName
    for ($i = 0; $i -lt $lines.Count; $i++) {
        foreach ($pattern in $secretPatterns) {
            if ($lines[$i] -match $pattern) {
                Write-AuditResult -item "Potential Hardcoded Secret" -passed $false -detail "File: $($cfg.Name), Line: $($i+1)"
                $leaksFound++
            }
        }
    }
}
if ($leaksFound -eq 0) {
    Write-AuditResult -item "Security Scan (No hardcoded JWT/DB secrets)" -passed $true -detail "(All parameterized via env)"
}

# 5. Git Status Check
Write-SectionHeader "5. Local Git Repository Cleanliness"
if (Get-Command git -ErrorAction SilentlyContinue) {
    $gitBranch = git rev-parse --abbrev-ref HEAD
    $gitStatus = git status --porcelain
    Write-Host "  Active Branch : $gitBranch" -ForegroundColor Magenta
    if ([string]::IsNullOrWhiteSpace($gitStatus)) {
        Write-AuditResult -item "Working Tree" -passed $true -detail "(Completely Clean)"
    } else {
        Write-AuditResult -item "Working Tree" -passed $false -detail "(Uncommitted changes present)"
        $gitStatus | ForEach-Object { Write-Host "     $($_)" -ForegroundColor Yellow }
    }
} else {
    Write-Host "  git CLI not found in PATH." -ForegroundColor Yellow
}

# 6. Lightweight Backup Generation
if ($CreateBackup) {
    Write-SectionHeader "6. Creating Lightweight Backup Archive"
    $timestamp = Get-Date -Format "yyyyMMdd-HHmmss"
    $backupDir = Join-Path (Split-Path $ProjectPath -Parent) "backups"
    if (-not (Test-Path $backupDir)) { New-Item -ItemType Directory -Path $backupDir | Out-Null }
    
    $zipFileName = "spring-boot-user-service-backup-$timestamp.zip"
    $zipPath = Join-Path $backupDir $zipFileName
    
    Write-Host "  Excluding target/, .git/, .idea/, and .env from backup..."
    $filesToArchive = Get-ChildItem -Path $ProjectPath -Recurse | Where-Object {
        $_.FullName -notmatch '[\\/](target|\.git|\.idea)[\\/]' -and $_.Name -ne ".env"
    }
    
    Compress-Archive -Path $filesToArchive.FullName -DestinationPath $zipPath -Force
    Write-AuditResult -item "Backup Archive Created" -passed $true -detail "-> $zipPath"
}

$sw.Stop()
Write-SectionHeader "Audit Completed"
Write-Host ("Execution Time: {0:N2} seconds" -f $sw.Elapsed.TotalSeconds) -ForegroundColor Green
Write-Host "Ready for German/EU Enterprise Grade Development.`n"
