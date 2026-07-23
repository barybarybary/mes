#!/bin/bash
# ============================================
# MES1 一键部署脚本
# 适用: CentOS 7/8/9 或 Ubuntu 20.04/22.04
# 用法: chmod +x setup.sh && ./setup.sh
# ============================================
set -e

INSTALL_DIR="/opt/mes1"
DB_PASSWORD="${MES_DB_PASSWORD:-Mes1@2024!}"
SERVER_PORT="${SERVER_PORT:-8081}"
JAR_FILE="mes1-0.0.1-SNAPSHOT.jar"

RED='\033[0;31m'; GREEN='\033[0;32m'; YELLOW='\033[1;33m'; NC='\033[0m'
info()  { echo -e "${GREEN}[✓]${NC} $1"; }
warn()  { echo -e "${YELLOW}[!]${NC} $1"; }
error() { echo -e "${RED}[✗]${NC} $1"; exit 1; }

echo "============================================"
echo "   MES1 生产环境一键部署"
echo "============================================"
echo ""

# ---- 1. 检测系统 ----
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
else
    error "无法识别操作系统"
fi
info "检测到系统: $OS"

# ---- 2. 安装 Docker (如果没有) ----
if ! command -v docker &>/dev/null; then
    info "安装 Docker..."
    if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "rocky" ] || [ "$OS" = "almalinux" ]; then
        yum install -y yum-utils
        yum-config-manager --add-repo https://download.docker.com/linux/centos/docker-ce.repo
        yum install -y docker-ce docker-ce-cli containerd.io
    elif [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
        apt update
        apt install -y ca-certificates curl
        install -m 0755 -d /etc/apt/keyrings
        curl -fsSL https://download.docker.com/linux/ubuntu/gpg -o /etc/apt/keyrings/docker.asc
        chmod a+r /etc/apt/keyrings/docker.asc
        echo "deb [arch=$(dpkg --print-architecture) signed-by=/etc/apt/keyrings/docker.asc] https://download.docker.com/linux/ubuntu $(. /etc/os-release && echo "$VERSION_CODENAME") stable" | tee /etc/apt/sources.list.d/docker.list > /dev/null
        apt update
        apt install -y docker-ce docker-ce-cli containerd.io
    fi
    systemctl enable --now docker
fi
info "Docker 已就绪"

# ---- 3. 安装 Docker Compose ----
if ! command -v docker compose &>/dev/null && ! docker compose version &>/dev/null 2>&1; then
    curl -SL "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
    chmod +x /usr/local/bin/docker-compose
fi
info "Docker Compose 已就绪"

# ---- 4. 安装 Java 17 (运行 jar 需要) ----
if ! command -v java &>/dev/null; then
    info "安装 Java 17..."
    if [ "$OS" = "centos" ] || [ "$OS" = "rhel" ] || [ "$OS" = "rocky" ] || [ "$OS" = "almalinux" ]; then
        yum install -y java-17-openjdk
    elif [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
        apt update && apt install -y openjdk-17-jdk
    fi
fi
info "Java 版本: $(java -version 2>&1 | head -1)"

# ---- 5. 创建目录结构 ----
mkdir -p $INSTALL_DIR/logs $INSTALL_DIR/static
info "目录已创建: $INSTALL_DIR"

# ---- 6. 启动 MySQL + Redis ----
cat > $INSTALL_DIR/docker-compose.yml << 'DOCKEREOF'
services:
  mysql:
    image: mysql:8.0
    container_name: mes1-mysql
    restart: always
    environment:
      MYSQL_ROOT_PASSWORD: ${MES_DB_PASSWORD}
      MYSQL_DATABASE: mes1
      TZ: Asia/Shanghai
    ports:
      - "3306:3306"
    volumes:
      - mysql-data:/var/lib/mysql
    command: --character-set-server=utf8mb4 --collation-server=utf8mb4_unicode_ci
  redis:
    image: redis:7-alpine
    container_name: mes1-redis
    restart: always
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data

volumes:
  mysql-data:
  redis-data:
DOCKEREOF

# 用实际密码替换占位符
sed -i "s/\${MES_DB_PASSWORD}/$DB_PASSWORD/g" $INSTALL_DIR/docker-compose.yml

cd $INSTALL_DIR
docker compose up -d
info "MySQL + Redis 已启动"

# ---- 7. 等待 MySQL 就绪 ----
info "等待 MySQL 启动..."
for i in $(seq 1 30); do
    if docker exec mes1-mysql mysqladmin ping -h localhost -uroot -p"$DB_PASSWORD" --silent 2>/dev/null; then
        break
    fi
    sleep 2
done
info "MySQL 已就绪"

# ---- 8. 导入数据库 ----
if [ -f "$INSTALL_DIR/init.sql" ]; then
    info "导入数据库结构..."
    docker exec -i mes1-mysql mysql -uroot -p"$DB_PASSWORD" mes1 < $INSTALL_DIR/init.sql
    if [ -f "$INSTALL_DIR/demo-data.sql" ]; then
        info "导入演示数据..."
        docker exec -i mes1-mysql mysql -uroot -p"$DB_PASSWORD" mes1 < $INSTALL_DIR/demo-data.sql
    fi
    info "数据库初始化完成"
else
    warn "未找到 init.sql，跳过数据库导入"
fi

# ---- 9. 创建应用环境变量 ----
cat > $INSTALL_DIR/.env << ENVEOF
SERVER_PORT=$SERVER_PORT
MES_DB_USERNAME=root
MES_DB_PASSWORD=$DB_PASSWORD
MES_RABBITMQ_ENABLED=false
SPRING_PROFILES_ACTIVE=prod
ENVEOF
info "环境变量已配置"

# ---- 10. 创建 systemd 服务 ----
cat > /etc/systemd/system/mes1.service << SERVICEEOF
[Unit]
Description=MES1 Application
After=network.target docker.service

[Service]
Type=simple
User=root
WorkingDirectory=$INSTALL_DIR
EnvironmentFile=$INSTALL_DIR/.env
ExecStart=/usr/bin/java -jar $INSTALL_DIR/app.jar --server.port=$SERVER_PORT
Restart=on-failure
RestartSec=10
StandardOutput=append:$INSTALL_DIR/logs/app.log
StandardError=append:$INSTALL_DIR/logs/app-error.log

[Install]
WantedBy=multi-user.target
SERVICEEOF
info "systemd 服务已创建"

# ---- 11. 检查 jar 文件 ----
if [ -f "$INSTALL_DIR/app.jar" ]; then
    info "检测到 jar 文件，启动应用..."
    systemctl daemon-reload
    systemctl enable mes1
    systemctl start mes1
    sleep 5
    if systemctl is-active --quiet mes1; then
        info "应用启动成功!"
    else
        warn "应用启动失败，请检查日志: journalctl -u mes1 -f"
    fi
else
    warn "请将 jar 文件上传到 $INSTALL_DIR/app.jar"
    warn "上传命令: scp mes1-*.jar root@服务器IP:$INSTALL_DIR/app.jar"
    warn "然后执行: systemctl start mes1"
fi

echo ""
echo "============================================"
echo "   部署完成!"
echo "   访问地址: http://$(curl -s ifconfig.me 2>/dev/null || echo 'YOUR_IP'):$SERVER_PORT"
echo "============================================"
