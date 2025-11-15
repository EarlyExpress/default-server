param(
    [string]$env = "local"
)

Write-Host "Starting application with environment: $env" -ForegroundColor Green

# .env 파일 설정
if (Test-Path ".env.$env") {
    Copy-Item ".env.$env" -Destination ".env" -Force
    Write-Host "Using .env.$env configuration" -ForegroundColor Yellow
} else {
    Write-Host "Warning: .env.$env not found, using default .env" -ForegroundColor Red
}

# Docker 네트워크 생성
docker network create msa-network 2>$null

# PostgreSQL 시작
Write-Host "Starting PostgreSQL..." -ForegroundColor Yellow
docker-compose up -d

# 대기
Write-Host "Waiting for database initialization..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 애플리케이션 실행
Write-Host "Starting application..." -ForegroundColor Green
if ($IsWindows) {
    & .\gradlew.bat bootRun --args="--spring.profiles.active=$env"
} else {
    & ./gradlew bootRun --args="--spring.profiles.active=$env"
}