$port = 8080
Write-Host "Checking port $port..." -ForegroundColor Cyan
$process = Get-NetTCPConnection -LocalPort $port -ErrorAction SilentlyContinue
if ($process) {
    Write-Host "Killing process on port $port..." -ForegroundColor Yellow
    Stop-Process -Id $process.OwningProcess -Force
}
if (Test-Path "target") {
    Write-Host "Cleaning target folder..." -ForegroundColor Cyan
    Remove-Item -Path "target" -Recurse -Force
}
Write-Host "Starting Spring Boot..." -ForegroundColor Green
.\mvnw clean spring-boot:run
