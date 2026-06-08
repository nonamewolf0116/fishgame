#!/bin/bash
set -e

# ============================================
# FishGame Deployment Script for Ubuntu 22.04
# ============================================

APP_NAME="fishgame"
APP_DIR="/opt/fishgame"
APP_JAR="${APP_DIR}/app.jar"
DB_DIR="${APP_DIR}/database"
LOGS_DIR="${APP_DIR}/logs"
UPLOADS_DIR="${APP_DIR}/uploads"
BACKUP_DIR="${APP_DIR}/backup"
SERVICE_FILE="/etc/systemd/system/${APP_NAME}.service"
NGINX_CONF="/etc/nginx/sites-available/${APP_NAME}"
LOGROTATE_CONF="/etc/logrotate.d/${APP_NAME}"

echo "========================================"
echo "  FishGame Deployment Script"
echo "========================================"

# Step 1: Create system user
echo "[1/10] Creating system user..."
if ! id -u fishgame &>/dev/null; then
    useradd -r -s /bin/false -m -d ${APP_DIR} fishgame
    echo "  User 'fishgame' created."
else
    echo "  User 'fishgame' already exists."
fi

# Step 2: Create directory structure
echo "[2/10] Creating directory structure..."
mkdir -p ${APP_DIR}
mkdir -p ${DB_DIR}
mkdir -p ${LOGS_DIR}
mkdir -p ${UPLOADS_DIR}
mkdir -p ${BACKUP_DIR}

# Step 3: Install system dependencies
echo "[3/10] Installing system dependencies..."
apt-get update -qq
apt-get install -y -qq nginx openjdk-17-jre-headless curl

# Step 4: Deploy JAR
echo "[4/10] Deploying application JAR..."
cp -f app.jar ${APP_JAR}
chown -R fishgame:fishgame ${APP_DIR}

# Step 5: Configure systemd service
echo "[5/10] Configuring systemd service..."
cp -f fishgame.service ${SERVICE_FILE}
chmod 644 ${SERVICE_FILE}
systemctl daemon-reload

# Step 6: Configure logrotate
echo "[6/10] Configuring logrotate..."
cp -f logrotate.conf ${LOGROTATE_CONF}
chmod 644 ${LOGROTATE_CONF}

# Step 7: Configure Nginx
echo "[7/10] Configuring Nginx..."
cp -f nginx.conf ${NGINX_CONF}
if [ -f /etc/nginx/sites-enabled/default ]; then
    rm -f /etc/nginx/sites-enabled/default
fi
ln -sf ${NGINX_CONF} /etc/nginx/sites-enabled/
nginx -t

# Step 8: Start services
echo "[8/10] Starting services..."
systemctl enable ${APP_NAME}
systemctl start ${APP_NAME}
systemctl restart nginx

# Step 9: Verify deployment
echo "[9/10] Verifying deployment..."
sleep 5
if systemctl is-active --quiet ${APP_NAME}; then
    echo "  ${APP_NAME} service is running."
else
    echo "  ERROR: ${APP_NAME} service failed to start."
    systemctl status ${APP_NAME} --no-pager
    exit 1
fi

if systemctl is-active --quiet nginx; then
    echo "  Nginx is running."
else
    echo "  ERROR: Nginx failed to start."
    exit 1
fi

# Step 10: Test endpoints
echo "[10/10] Testing endpoints..."
curl -s -o /dev/null -w "  HTTP /api/leaderboard: %{http_code}\n" http://127.0.0.1:8080/api/leaderboard || echo "  FAILED"
curl -s -o /dev/null -w "  HTTP /api/music/search: %{http_code}\n" "http://127.0.0.1:8080/api/music/search?keyword=test" || echo "  FAILED"
curl -s -o /dev/null -w "  HTTP /: %{http_code}\n" http://127.0.0.1:80/ || echo "  FAILED"

echo ""
echo "========================================"
echo "  Deployment complete!"
echo "  App: http://YOUR_SERVER_IP"
echo "  API: http://YOUR_SERVER_IP/api/"
echo "========================================"
