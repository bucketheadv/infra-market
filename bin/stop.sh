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

log_info "开始关闭 Infra Market 项目..."
log_info "项目目录: $PROJECT_DIR"
echo "========================================"

# 关闭后端服务
log_info "正在关闭后端服务..."
BACKEND_STOPPED=false

# 方式1: 通过PID文件关闭
if [ -f "$BIN_DIR/.backend.pid" ]; then
    BACKEND_PID=$(cat "$BIN_DIR/.backend.pid")
    if ps -p $BACKEND_PID > /dev/null 2>&1; then
        log_info "找到后端进程 PID: $BACKEND_PID"
        
        # 尝试优雅关闭
        kill $BACKEND_PID 2>/dev/null
        
        # 等待进程结束
        COUNTER=0
        while [ $COUNTER -lt 10 ]; do
            if ! ps -p $BACKEND_PID > /dev/null 2>&1; then
                log_info "后端服务已关闭"
                BACKEND_STOPPED=true
                break
            fi
            sleep 1
            COUNTER=$((COUNTER + 1))
        done
        
        # 如果还没停止，强制关闭
        if ! $BACKEND_STOPPED; then
            log_warn "后端服务未响应，强制关闭..."
            kill -9 $BACKEND_PID 2>/dev/null
            BACKEND_STOPPED=true
            log_info "后端服务已强制关闭"
        fi
    else
        log_warn "PID文件中的进程不存在"
    fi
    rm -f "$BIN_DIR/.backend.pid"
fi

# 方式2: 通过端口查找并关闭（作为备份方案）
if ! $BACKEND_STOPPED; then
    BACKEND_PID=$(lsof -ti:8080)
    if [ ! -z "$BACKEND_PID" ]; then
        log_info "通过端口8080找到后端进程: $BACKEND_PID"
        kill $BACKEND_PID 2>/dev/null
        sleep 2
        
        # 检查是否还在运行
        if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warn "后端服务未响应，强制关闭..."
            kill -9 $BACKEND_PID 2>/dev/null
        fi
        log_info "后端服务已关闭"
        BACKEND_STOPPED=true
    fi
fi

if ! $BACKEND_STOPPED; then
    log_warn "未找到运行中的后端服务"
fi

# 额外清理：关闭所有Maven相关的Java进程
log_info "清理Maven进程..."
pkill -f "infra-market-server" 2>/dev/null
pkill -f "spring-boot:run" 2>/dev/null

# 关闭前端服务
log_info "正在关闭前端服务..."
FRONTEND_STOPPED=false

# 方式1: 通过PID文件关闭
if [ -f "$BIN_DIR/.frontend.pid" ]; then
    FRONTEND_PID=$(cat "$BIN_DIR/.frontend.pid")
    if ps -p $FRONTEND_PID > /dev/null 2>&1; then
        log_info "找到前端进程 PID: $FRONTEND_PID"
        
        # 尝试优雅关闭
        kill $FRONTEND_PID 2>/dev/null
        
        # 等待进程结束
        COUNTER=0
        while [ $COUNTER -lt 10 ]; do
            if ! ps -p $FRONTEND_PID > /dev/null 2>&1; then
                log_info "前端服务已关闭"
                FRONTEND_STOPPED=true
                break
            fi
            sleep 1
            COUNTER=$((COUNTER + 1))
        done
        
        # 如果还没停止，强制关闭
        if ! $FRONTEND_STOPPED; then
            log_warn "前端服务未响应，强制关闭..."
            kill -9 $FRONTEND_PID 2>/dev/null
            FRONTEND_STOPPED=true
            log_info "前端服务已强制关闭"
        fi
    else
        log_warn "PID文件中的进程不存在"
    fi
    rm -f "$BIN_DIR/.frontend.pid"
fi

# 方式2: 通过端口查找并关闭（作为备份方案）
if ! $FRONTEND_STOPPED; then
    FRONTEND_PID=$(lsof -ti:5173)
    if [ ! -z "$FRONTEND_PID" ]; then
        log_info "通过端口5173找到前端进程: $FRONTEND_PID"
        kill $FRONTEND_PID 2>/dev/null
        sleep 2
        
        # 检查是否还在运行
        if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
            log_warn "前端服务未响应，强制关闭..."
            kill -9 $FRONTEND_PID 2>/dev/null
        fi
        log_info "前端服务已关闭"
        FRONTEND_STOPPED=true
    fi
fi

if ! $FRONTEND_STOPPED; then
    log_warn "未找到运行中的前端服务"
fi

# 额外清理：关闭所有Node相关的Vite进程
log_info "清理Vite进程..."
pkill -f "vite" 2>/dev/null
pkill -f "infra-market-admin" 2>/dev/null

# 最终检查
sleep 1
echo ""
echo "========================================"
log_info "服务关闭完成！"
echo ""

# 检查端口是否已释放
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_warn "警告: 端口8080仍被占用"
else
    log_info "端口8080已释放"
fi

if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
    log_warn "警告: 端口5173仍被占用"
else
    log_info "端口5173已释放"
fi

echo "========================================"
