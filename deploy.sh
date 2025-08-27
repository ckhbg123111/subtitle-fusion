#!/usr/bin/env bash

set -Eeuo pipefail

# ==============================
# Subtitle Fusion 一键部署脚本（Linux）
# 依赖：Docker、curl、Java 17+、Maven 3.8+
# 作用：
#  1) 构建 Spring Boot 可执行 Jar
#  2) 启动 Redis 与 MinIO 容器
#  3) 以覆盖配置方式启动应用（指向本机的 Redis/MinIO）
# ==============================

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
cd "$SCRIPT_DIR"

bold() { echo -e "\033[1m$*\033[0m"; }
green() { echo -e "\033[32m$*\033[0m"; }
red() { echo -e "\033[31m$*\033[0m"; }
yellow() { echo -e "\033[33m$*\033[0m"; }

require_cmd() {
  if ! command -v "$1" >/dev/null 2>&1; then
    red "缺少命令：$1，请先安装并确保在 PATH 中。"; exit 1
  fi
}

bold "[1/6] 检查环境依赖"
require_cmd docker
require_cmd curl
require_cmd mvn
require_cmd java

# ------------------------------
# 可配置参数（支持通过环境变量覆盖）
# ------------------------------
APP_PORT=${APP_PORT:-8081}

# Redis
REDIS_IMAGE=${REDIS_IMAGE:-redis:7.2-alpine}
REDIS_CONTAINER=${REDIS_CONTAINER:-subtitlefusion-redis}
REDIS_HOST=${REDIS_HOST:-127.0.0.1}
REDIS_PORT=${REDIS_PORT:-6379}

# MinIO
MINIO_IMAGE=${MINIO_IMAGE:-minio/minio:latest}
MINIO_CONTAINER=${MINIO_CONTAINER:-subtitlefusion-minio}
MINIO_API_PORT=${MINIO_API_PORT:-9000}
MINIO_CONSOLE_PORT=${MINIO_CONSOLE_PORT:-9001}
MINIO_ACCESS_KEY=${MINIO_ACCESS_KEY:-minioadmin}
MINIO_SECRET_KEY=${MINIO_SECRET_KEY:-minioadmin}
MINIO_BUCKET=${MINIO_BUCKET:-nis}

# 覆盖应用中的 MinIO 与 Redis 配置
MINIO_ENDPOINT=${MINIO_ENDPOINT:-http://127.0.0.1:${MINIO_API_PORT}}
MINIO_EXT_ENDPOINT=${MINIO_EXT_ENDPOINT:-}

bold "[2/6] 拉取并启动 Redis 与 MinIO 容器"
set +e
docker rm -f "$REDIS_CONTAINER" >/dev/null 2>&1
docker rm -f "$MINIO_CONTAINER" >/dev/null 2>&1
set -e

yellow "启动 Redis 容器: $REDIS_IMAGE -> $REDIS_CONTAINER (端口 $REDIS_PORT)"
docker run -d \
  --name "$REDIS_CONTAINER" \
  -p "${REDIS_PORT}:6379" \
  --restart unless-stopped \
  "$REDIS_IMAGE" \
  --appendonly yes >/dev/null

yellow "启动 MinIO 容器: $MINIO_IMAGE -> $MINIO_CONTAINER (API ${MINIO_API_PORT}, 控制台 ${MINIO_CONSOLE_PORT})"
docker run -d \
  --name "$MINIO_CONTAINER" \
  -p "${MINIO_API_PORT}:9000" \
  -p "${MINIO_CONSOLE_PORT}:9001" \
  -e "MINIO_ROOT_USER=${MINIO_ACCESS_KEY}" \
  -e "MINIO_ROOT_PASSWORD=${MINIO_SECRET_KEY}" \
  --restart unless-stopped \
  "$MINIO_IMAGE" server /data --console-address ":9001" >/dev/null

bold "[3/6] 等待依赖服务就绪"

# 等待 Redis 就绪（通过容器内 redis-cli）
for i in {1..30}; do
  if docker exec "$REDIS_CONTAINER" redis-cli ping >/dev/null 2>&1; then
    green "Redis 就绪"
    break
  fi
  sleep 1
  if [[ $i -eq 30 ]]; then
    red "Redis 未在预期时间内就绪"; exit 1
  fi
done

# 等待 MinIO 就绪（健康检查接口）
for i in {1..60}; do
  if curl -sSf "http://127.0.0.1:${MINIO_API_PORT}/minio/health/ready" >/dev/null 2>&1; then
    green "MinIO 就绪"
    break
  fi
  sleep 1
  if [[ $i -eq 60 ]]; then
    red "MinIO 未在预期时间内就绪"; exit 1
  fi
done

bold "[4/6] 构建 Spring Boot 应用 (Maven)"
mvn -q -DskipTests clean package

# 选择最新的可执行 jar
JAR_FILE=$(ls -t target/*.jar 2>/dev/null | grep -vE "(sources|javadoc)" | head -n1 || true)
if [[ -z "${JAR_FILE}" ]]; then
  red "未找到可执行 Jar，请检查 Maven 构建是否成功。"; exit 1
fi
green "将启动 Jar: ${JAR_FILE}"

bold "[5/6] 启动应用（覆盖默认配置为本地 Redis/MinIO）"
yellow "服务端口: ${APP_PORT}"
yellow "Redis: ${REDIS_HOST}:${REDIS_PORT}"
yellow "MinIO: ${MINIO_ENDPOINT} (Bucket: ${MINIO_BUCKET})"

# 提示：MinioService 会在启动时自动创建 Bucket（若不存在）

JAVA_OPTS=${JAVA_OPTS:-"-Xms512m -Xmx1024m"}

set +e
pkill -f "-Dspring.application.name=subtitle-fusion" >/dev/null 2>&1 || true
set -e

CMD=(
  java ${JAVA_OPTS}
  -Dspring.application.name=subtitle-fusion
  -Dserver.port=${APP_PORT}
  -Dminio.endpoint=${MINIO_ENDPOINT}
  -Dminio.ext-endpoint=${MINIO_EXT_ENDPOINT}
  -Dminio.access-key=${MINIO_ACCESS_KEY}
  -Dminio.secret-key=${MINIO_SECRET_KEY}
  -Dminio.bucket-name=${MINIO_BUCKET}
  -Dspring.data.redis.host=${REDIS_HOST}
  -Dspring.data.redis.port=${REDIS_PORT}
  -jar "${JAR_FILE}"
)

"${CMD[@]}"

EXIT_CODE=$?
if [[ $EXIT_CODE -eq 0 ]]; then
  bold "[6/6] 应用已正常退出"
else
  red "应用异常退出，退出码：$EXIT_CODE"
fi

exit $EXIT_CODE


