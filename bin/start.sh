#!/bin/bash

# 获取脚本所在目录（bin目录）
BIN_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
# 项目根目录（bin的上一级）
PROJECT_DIR="$(dirname "$BIN_DIR")"
cd "$PROJECT_DIR"

# 颜色定义
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${GREEN}[INFO]${NC} $1"
}

log_warn() {
    echo -e "${YELLOW}[WARN]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查是否已经在运行
check_running() {
    # 检查后端（Spring Boot通常运行在8080端口）
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warn "后端服务似乎已在运行（端口8080已被占用）"
        read -p "是否要先关闭现有服务？(y/n): " -n 1 -r
        echo
        if [[ $REPLY =~ ^[Yy]$ ]]; then
            bash "$BIN_DIR/stop.sh"
            sleep 2
        else
            log_error "启动已取消"
            exit 1
        fi
    fi
    
    # 检查前端（Vite默认运行在5173端口）
    if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warn "前端服务似乎已在运行（端口5173已被占用）"
    fi
}

# 创建日志目录
mkdir -p "$PROJECT_DIR/logs"

# 检查服务状态
check_running

log_info "开始启动 Infra Market 项目..."
log_info "项目目录: $PROJECT_DIR"
echo "========================================"

# 启动后端
log_info "正在启动后端服务..."
cd "$PROJECT_DIR/infra-market-server"

# 检查Maven是否可用
if ! command -v mvn &> /dev/null; then
    log_error "Maven未安装或不在PATH中，请先安装Maven"
    exit 1
fi

# 后台启动Spring Boot
nohup mvn spring-boot:run > "$PROJECT_DIR/logs/backend.log" 2>&1 &
BACKEND_PID=$!
echo $BACKEND_PID > "$PROJECT_DIR/bin/.backend.pid"
log_info "后端服务已启动，PID: $BACKEND_PID"
log_info "后端日志文件: $PROJECT_DIR/logs/backend.log"

# 等待后端启动
log_info "等待后端服务启动（最多等待60秒）..."
COUNTER=0
while [ $COUNTER -lt 60 ]; do
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_info "后端服务启动成功！"
        break
    fi
    sleep 1
    COUNTER=$((COUNTER + 1))
    if [ $((COUNTER % 5)) -eq 0 ]; then
        echo -n "."
    fi
done
echo ""

if [ $COUNTER -eq 60 ]; then
    log_warn "后端服务启动超时，但进程仍在运行。请检查日志: $PROJECT_DIR/logs/backend.log"
fi

# 启动前端
log_info "正在启动前端服务..."
cd "$PROJECT_DIR/infra-market-admin"

# 检查Node.js和npm是否可用
if ! command -v node &> /dev/null; then
    log_error "Node.js未安装或不在PATH中，请先安装Node.js"
    exit 1
fi

if ! command -v npm &> /dev/null; then
    log_error "npm未安装或不在PATH中，请先安装npm"
    exit 1
fi

# 检查node_modules是否存在
if [ ! -d "node_modules" ]; then
    log_warn "node_modules目录不存在，正在安装依赖..."
    npm install
fi

# 后台启动Vite开发服务器
nohup npm run dev > "$PROJECT_DIR/logs/frontend.log" 2>&1 &
FRONTEND_PID=$!
echo $FRONTEND_PID > "$PROJECT_DIR/bin/.frontend.pid"
log_info "前端服务已启动，PID: $FRONTEND_PID"
log_info "前端日志文件: $PROJECT_DIR/logs/frontend.log"

# 等待前端启动
log_info "等待前端服务启动（最多等待30秒）..."
COUNTER=0
while [ $COUNTER -lt 30 ]; do
    if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_info "前端服务启动成功！"
        break
    fi
    sleep 1
    COUNTER=$((COUNTER + 1))
    if [ $((COUNTER % 5)) -eq 0 ]; then
        echo -n "."
    fi
done
echo ""

if [ $COUNTER -eq 30 ]; then
    log_warn "前端服务启动超时，但进程仍在运行。请检查日志: $PROJECT_DIR/logs/frontend.log"
fi

# 显示服务状态
echo "========================================"
log_info "Infra Market 项目启动完成！"
echo ""
echo "服务信息："
echo "  后端服务: http://localhost:8080"
echo "  前端服务: http://localhost:5173"
echo ""
echo "进程信息："
echo "  后端 PID: $BACKEND_PID"
echo "  前端 PID: $FRONTEND_PID"
echo ""
echo "日志文件："
echo "  后端日志: logs/backend.log"
echo "  前端日志: logs/frontend.log"
echo ""
log_info "使用 'bin/stop.sh' 命令关闭服务"
log_info "使用 'tail -f logs/backend.log' 或 'tail -f logs/frontend.log' 查看实时日志"
echo "========================================"
