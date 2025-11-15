#!/bin/bash

# 환경변수 설정
ENV=${1:-local}

# .env 파일 선택
if [ -f ".env.$ENV" ]; then
    cp ".env.$ENV" .env
    echo "Using .env.$ENV configuration"
else
    echo "Warning: .env.$ENV not found, using default .env"
fi

# 네트워크 생성 (없을 경우)
docker network create msa-network 2>/dev/null || true

# PostgreSQL 시작
docker-compose up -d

# 애플리케이션 실행
./gradlew bootRun --args="--spring.profiles.active=$ENV"