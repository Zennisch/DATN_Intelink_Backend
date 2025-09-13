#!/bin/bash

echo "Starting Nginx reverse proxy..."
echo "Domain: ${DOMAIN}"

# Initialize SSL certificates if needed
/usr/local/bin/init-ssl.sh

# Generate nginx.conf from template with environment variables
envsubst '${DOMAIN}' < /etc/nginx/nginx.conf.template > /etc/nginx/nginx.conf

nginx -g 'daemon off;' &
NGINX_PID=$!

sleep 5

# Kiểm tra xem có cần lấy chứng chỉ SSL không
CERT_FILE="/etc/letsencrypt/live/${DOMAIN}/fullchain.pem"
CERT_SIZE=0
if [ -f "$CERT_FILE" ]; then
    CERT_SIZE=$(stat -c%s "$CERT_FILE" 2>/dev/null || echo 0)
fi

# Kiểm tra xem certificate có phải là Let's Encrypt không (tự ký sẽ có signature khác)
IS_LETSENCRYPT=false
if [ -f "$CERT_FILE" ] && [ $CERT_SIZE -gt 100 ]; then
    if openssl x509 -in "$CERT_FILE" -text -noout | grep -q "Let's Encrypt"; then
        IS_LETSENCRYPT=true
        echo "Valid Let's Encrypt certificate found"
    else
        echo "Self-signed certificate detected, will get real certificate"
    fi
fi

if [ "$IS_LETSENCRYPT" = false ]; then
    echo "Getting SSL certificate for ${DOMAIN}..."
    
    # Xóa certificate cũ nếu có
    rm -rf /etc/letsencrypt/live/${DOMAIN}
    rm -rf /etc/letsencrypt/archive/${DOMAIN}
    rm -rf /etc/letsencrypt/renewal/${DOMAIN}.conf
    
    certbot certonly \
        --webroot \
        --webroot-path=/var/www/certbot \
        --email ${LETSENCRYPT_EMAIL:-admin@example.com} \
        --agree-tos \
        --no-eff-email \
        --non-interactive \
        --force-renewal \
        -d ${DOMAIN}
    
    if [ $? -eq 0 ]; then
        echo "SSL certificate obtained successfully. Reloading nginx..."
        nginx -s reload
    else
        echo "Failed to obtain SSL certificate. Trying standalone mode..."
        
        # Dừng nginx tạm thời để dùng standalone mode
        nginx -s stop
        
        certbot certonly \
            --standalone \
            --email ${LETSENCRYPT_EMAIL:-admin@example.com} \
            --agree-tos \
            --no-eff-email \
            --non-interactive \
            --force-renewal \
            -d ${DOMAIN}
        
        # Khởi động lại nginx
        nginx -g 'daemon off;' &
        NGINX_PID=$!
        
        if [ $? -eq 0 ]; then
            echo "SSL certificate obtained via standalone mode. Reloading nginx..."
            nginx -s reload
        else
            echo "Failed to obtain SSL certificate. Continuing with self-signed cert..."
        fi
    fi
else
    echo "Valid SSL certificate already exists"
fi

# Thiết lập cron job để gia hạn chứng chỉ
echo "0 12 * * * certbot renew --quiet && nginx -s reload" | crontab -

wait $NGINX_PID
