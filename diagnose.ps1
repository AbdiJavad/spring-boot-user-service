Write-Host "=== 1. Checking PostgreSQL Service ===" -ForegroundColor Cyan
Get-Service -Name "*postgres*" | Select-Object Name, Status

Write-Host "
=== 2. Checking Project Git Branch ===" -ForegroundColor Cyan
git status -s -b

Write-Host "
=== 3. Checking UTF-8 BOM on Java Files ===" -ForegroundColor Cyan
Get-ChildItem -Path "src" -Filter "*.java" -Recurse | ForEach-Object {
    $bytes = Get-Content -Path $_.FullName -Encoding Byte -TotalCount 3 -ErrorAction SilentlyContinue
    if ($bytes -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF) {
        Write-Warning "Found UTF-8 BOM in: $(.FullName)"
    }
}

Write-Host "
=== 4. Running Actuator Test with Detailed Logs ===" -ForegroundColor Cyan
.\mvnw.cmd test -Dtest=ActuatorSecurityIntegrationTest
