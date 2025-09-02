#!/usr/bin/env bash

set -Eeuo pipefail

# ==============================
# Subtitle Fusion 一键部署脚本（Linux）
# 依赖：Docker、Docker Compose（支持 docker compose 或 docker-compose）
# 作用：
#  1) 使用 Dockerfile 构建应用镜像
#  2) 使用 docker-compose 启动 app/redis/minio（三个服务）
#  3) 自动等待 Redis/MinIO/应用端口就绪
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

detect_compose() {
  if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
    echo "docker compose"
    return 0
  fi
  if command -v docker-compose >/dev/null 2>&1; then
    echo "docker-compose"
    return 0
  fi
  red "未检测到 Docker Compose（docker compose 或 docker-compose）。"; exit 1
}

tcp_wait() {
  local host="$1"; local port="$2"; local name="$3"; local retries="${4:-120}"; local sleep_sec="${5:-1}"
  yellow "等待 ${name} (${host}:${port}) 就绪..."
  for i in $(seq 1 "$retries"); do
    if (echo > "/dev/tcp/${host}/${port}") >/dev/null 2>&1; then
      green "${name} 就绪"
      return 0
    fi
    sleep "$sleep_sec"
  done
  red "${name} 未在预期时间内就绪"; exit 1
}

bold "[1/4] 检查环境依赖"
require_cmd docker
DC=$(detect_compose)
green "使用 Compose 命令：$DC"

# ------------------------------
# 加载 .env（如果存在）并定义可配置参数
# ------------------------------
if [[ -f .env ]]; then
  yellow "加载 .env 配置"
  # 过滤注释行并导出变量
  export $(grep -E '^[A-Za-z_][A-Za-z0-9_]*=' .env | xargs -d '\n') || true
fi

# 可配置参数（支持通过环境变量覆盖）
APP_PORT=${APP_PORT:-8081}
REDIS_PORT=${REDIS_PORT:-6379}
MINIO_API_PORT=${MINIO_API_PORT:-9000}

bold "[2/4] 准备数据与输出目录（用于持久化挂载）"
mkdir -p docker-data/redis docker-data/minio output temp

bold "[3/4] 构建并启动（docker-compose）"
yellow "构建应用镜像..."
$DC build app
yellow "启动服务（后台运行）..."
$DC up -d

bold "[4/4] 等待服务就绪"
tcp_wait 127.0.0.1 "$REDIS_PORT" "Redis"
tcp_wait 127.0.0.1 "$MINIO_API_PORT" "MinIO"
tcp_wait 127.0.0.1 "$APP_PORT" "SubtitleFusion 应用"

green "所有服务已就绪："
echo "- 应用:      http://127.0.0.1:${APP_PORT}"
echo "- MinIO 控制台: http://127.0.0.1:9001 （默认账号/密码：minioadmin/minioadmin）"
echo "- MinIO API: http://127.0.0.1:${MINIO_API_PORT}"
echo "- Redis:     127.0.0.1:${REDIS_PORT}"

bold "快速命令："
echo "- 查看日志: $DC logs -f app"
echo "- 停止服务: $DC down"
echo "- 后台重建: $DC build app && $DC up -d"

exit 0


