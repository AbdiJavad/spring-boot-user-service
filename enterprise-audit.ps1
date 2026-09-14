# ==============================================================================
# Enterprise Spring Boot Codebase Audit & Sanitization Script
# Author: Felix & Javad (Engineering Team)
# Target: D:\demo
# ==============================================================================

$ErrorActionPreference = "Stop"
$projectRoot = "D:\demo"
Set-Location $projectRoot

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " [1/6] INITIALIZING & BACKUP (Automated Safety Net) " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$backupDir = Join-Path $projectRoot "backups\backup_$timestamp"
New-Item -ItemType Directory -Path $backupDir -Force | Out-Null

# Ensure backups folder is ignored by Git
$gitIgnorePath = Join-Path $projectRoot ".gitignore"
if (Test-Path $gitIgnorePath) {
    $gitIgnoreContent = Get-Content $gitIgnorePath -Raw
    if ($gitIgnoreContent -notmatch "backups/") {
        Add-Content -Path $gitIgnorePath -Value "`n# Automated Backups`nbackups/"
        Write-Host "✔ Added 'backups/' to .gitignore" -ForegroundColor Green
    }
}

Write-Host "Backing up src/ and pom.xml to: $backupDir ..." -ForegroundColor Yellow
Copy-Item -Path (Join-Path $projectRoot "src") -Destination $backupDir -Recurse -Force
Copy-Item -Path (Join-Path $projectRoot "pom.xml") -Destination $backupDir -Force
Write-Host "✔ Backup completed successfully!" -ForegroundColor Green

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " [2/6] ENCODING AUDIT & BOM REMOVAL (UTF-8 No-BOM) " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$utf8NoBom = New-Object System.Text.UTF8Encoding($false)
$sourceFiles = Get-ChildItem -Path (Join-Path $projectRoot "src") -Recurse -Include *.java, *.properties, *.xml
$bomCleanedCount = 0

foreach ($file in $sourceFiles) {
    $bytes = [System.IO.File]::ReadAllBytes($file.FullName)
    # Check for UTF-8 BOM: 0xEF, 0xBB, 0xBF
    if ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Host "⚠ Detected BOM in: $($file.FullName.Replace($projectRoot, ''))" -ForegroundColor Yellow
        $textContent = [System.IO.File]::ReadAllText($file.FullName)
        [System.IO.File]::WriteAllText($file.FullName, $textContent, $utf8NoBom)
        $bomCleanedCount++
    }
}

if ($bomCleanedCount -eq 0) {
    Write-Host "✔ All source files are compliant (Strict UTF-8 without BOM)." -ForegroundColor Green
} else {
    Write-Host "✔ Sanitized $bomCleanedCount file(s) to UTF-8 without BOM." -ForegroundColor Green
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " [3/6] DETECTING DUPLICATE CLASSES & AMBIGUOUS BEANS " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$javaFiles = Get-ChildItem -Path (Join-Path $projectRoot "src\main\java") -Recurse -Filter *.java
$classGroups = $javaFiles | Group-Object Name | Where-Object { $_.Count -gt 1 }

if ($classGroups) {
    Write-Host "❌ CRITICAL: Duplicate class names found across different packages:" -ForegroundColor Red
    foreach ($grp in $classGroups) {
        Write-Host "  - Class: $($grp.Name)" -ForegroundColor Magenta
        foreach ($item in $grp.Group) {
            Write-Host "    -> $($item.FullName.Replace($projectRoot, ''))" -ForegroundColor Gray
        }
    }
} else {
    Write-Host "✔ No duplicate simple class names detected in src/main/java." -ForegroundColor Green
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " [4/6] PACKAGE DECLARATION INTEGRITY CHECK " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$packageIssues = 0
foreach ($javaFile in $javaFiles) {
    $lines = Get-Content $javaFile.FullName
    $pkgLine = $lines | Where-Object { $_ -match "^\s*package\s+([a-zA-Z0-9_\.]+);" } | Select-Object -First 1
    if ($pkgLine) {
        $declaredPackage = ($pkgLine -replace "^\s*package\s+", "" -replace ";", "").Trim()
        $expectedFolder = $declaredPackage.Replace('.', '\')
        $actualFolder = Split-Path $javaFile.FullName -Parent

        if (-not $actualFolder.EndsWith($expectedFolder)) {
            Write-Host "⚠ Mismatch in: $($javaFile.Name)" -ForegroundColor Red
            Write-Host "  Declared: $declaredPackage" -ForegroundColor Yellow
            Write-Host "  Path:     $actualFolder" -ForegroundColor Gray
            $packageIssues++
        }
    }
}

if ($packageIssues -eq 0) {
    Write-Host "✔ All Java package declarations strictly match physical folder paths." -ForegroundColor Green
} else {
    Write-Host "⚠ Found $packageIssues package structure mismatch(es)!" -ForegroundColor Red
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " [5/6] MAVEN CLEAN, COMPILE & TEST LIFECYCLE " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

Write-Host "Executing: .\mvnw.cmd clean test ..." -ForegroundColor Yellow
& ".\mvnw.cmd" clean test

if ($LASTEXITCODE -eq 0) {
    Write-Host "`n✔ BUILD & TESTS SUCCESSFUL! Quality gate passed." -ForegroundColor Green
} else {
    Write-Host "`n❌ Maven build/test failed with exit code $LASTEXITCODE. Check errors above!" -ForegroundColor Red
    exit $LASTEXITCODE
}

Write-Host "`n=======================================================" -ForegroundColor Cyan
Write-Host " [6/6] GIT REPOSITORY STATUS & NEXT STEPS " -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

$currentBranch = git branch --show-current
Write-Host "Current Branch: $currentBranch" -ForegroundColor Yellow

$statusOutput = git status --short
if ([string]::IsNullOrWhiteSpace($statusOutput)) {
    Write-Host "✔ Git Working Tree is completely clean. No pending changes." -ForegroundColor Green
} else {
    Write-Host "Modified / Untracked files detected:" -ForegroundColor Yellow
    Write-Host $statusOutput -ForegroundColor Gray
    Write-Host "`nReady-to-run Git commands if you want to commit sanitization changes:" -ForegroundColor Cyan
    Write-Host "  git add ." -ForegroundColor White
    Write-Host "  git commit -m 'chore(audit): sanitize file encodings to UTF-8 without BOM and verify integrity'" -ForegroundColor White
    Write-Host "  git push origin $currentBranch" -ForegroundColor White
}

Write-Host "`n--- [Felix & Javad Quality Gate Completed] ---`n" -ForegroundColor Green
