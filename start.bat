@echo off
setlocal

REM 환경 설정 (기본값: local)
set ENV=%1
if "%ENV%"=="" set ENV=local

REM .env 파일 선택
if exist ".env.%ENV%" (
    echo Using .env.%ENV% configuration
    copy /Y ".env.%ENV%" ".env" > nul
) else (
    echo Warning: .env.%ENV% not found, using default .env
)

REM Docker 네트워크 생성 (이미 있으면 무시)
docker network create msa-network 2>nul

REM PostgreSQL 시작
echo Starting PostgreSQL...
docker-compose up -d

REM 대기 시간 (DB 초기화를 위해)
echo Waiting for database initialization...
timeout /t 5 /nobreak > nul

REM 애플리케이션 실행
echo Starting application with profile: %ENV%
call gradlew.bat bootRun --args="--spring.profiles.active=%ENV%"

endlocal