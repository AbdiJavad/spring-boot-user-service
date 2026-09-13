Write-Host "--- [1/5] Checking Port 8080 ---" -ForegroundColor Cyan
 = 8080
 = Get-NetTCPConnection -LocalPort  -ErrorAction SilentlyContinue
if ($process) {
    Write-Host "Port  is busy. Killing process..." -ForegroundColor Yellow
    Stop-Process -Id $process.OwningProcess -Force
    Write-Host "Process killed." -ForegroundColor Green
} else {
    Write-Host "Port  is free." -ForegroundColor Green
}

Write-Host "
--- [2/5] Cleaning and Building Project ---" -ForegroundColor Cyan
if (Test-Path ".\mvnw.cmd") {
    .\mvnw.cmd clean install -DskipTests
} else {
    Write-Error "mvnw.cmd not found! Are you in D:\demo?"
    exit
}

Write-Host "
--- [3/5] Starting Spring Boot Server ---" -ForegroundColor Cyan
 = Start-Process -FilePath ".\mvnw.cmd" -ArgumentList "spring-boot:run" -PassThru -WindowStyle Minimized
Write-Host "Server is starting in the background..." -ForegroundColor Yellow

# Wait for server to be ready
Write-Host "Waiting for server to respond..." -ForegroundColor Gray
 = 0
while ($retryCount -lt 30) {
     = Test-NetConnection -ComputerName localhost -Port  -ErrorAction SilentlyContinue
    if ($check.TcpTestSucceeded) { 
        Write-Host "Server is UP!" -ForegroundColor Green
        break 
    }
    Start-Sleep -Seconds 3
    $retryCount++
}

if ($retryCount -eq 30) {
    Write-Error "Server failed to start in time."
    Stop-Process -Id $serverProcess.Id
    exit
}

Write-Host "
--- [4/5] Running Automated API Test (Exception Handling) ---" -ForegroundColor Cyan
Start-Sleep -Seconds 5 # Give a bit more time for Spring context to fully load
 = '{"email":"invalid-email-format","password":"password123"}'
try {
     = Invoke-RestMethod -Uri "http://localhost:8080/api/users/register" -Method Post -Body $testBody -ContentType "application/json"
    Write-Host "Unexpected Success! (Something is wrong with the validator)" -ForegroundColor Yellow
     | ConvertTo-Json | Write-Host
} catch {
    Write-Host "Test Result: SUCCESS (Error caught as expected)" -ForegroundColor Green
    Write-Host "Response Body:" -ForegroundColor White
    $_.Exception.Response.GetResponseStream() | ForEach-Object { 
        $reader = New-Object System.IO.StreamReader($_)
        $reader.ReadToEnd() | Write-Host 
    }
}

Write-Host "
--- [5/5] Cleanup ---" -ForegroundColor Cyan
Write-Host "Automation finished. Keeping server running. Press Ctrl+C to stop the script (but server stays up)." -ForegroundColor Magenta
