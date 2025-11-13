#!/bin/bash

source .env

WWW="www.${DOMAIN}"
NGINX_CONF="/etc/nginx/sites-available/default"
CERT_PATH="/etc/letsencrypt/live/${DOMAIN}"
WEBROOT="/var/www/letsencrypt"

echo "=== ENVIRONMENTS ==="
echo "Domain: $DOMAIN"
echo "Email: $EMAIL"
echo "App port: $APP_PORT"
echo "WWW: $WWW"
echo "Nginx: $NGINX_CONF"
echo "Certificate: $CERT_PATH"
echo "Web root: $WEBROOT"

echo -e "\n=== INSTALLING CERTBOT ==="
sudo apt update
sudo apt install -y certbot python3-certbot-nginx

echo -e "\n=== OBTAINING SSL CERTIFICATE ==="
sudo certbot --nginx -d "$DOMAIN" -d "$WWW" --email "$EMAIL" --agree-tos --no-eff-email --redirect --duplicate

echo -e "\n=== CONFIGURING NGINX ==="
sudo nginx -t && sudo systemctl reload nginx

echo "Writing Nginx configuration to ${NGINX_CONF}..."
sudo tee "${NGINX_CONF}" > /dev/null <<EOF
server {
    listen 80;
    server_name ${DOMAIN} ${WWW};

    location ^~ /.well-known/acme-challenge/ {
        root ${WEBROOT};
        try_files \$uri =404;
    }

    location / {
        return 301 https://\$host\$request_uri;
    }
}

server {
    listen 443 ssl http2;
    server_name ${DOMAIN} ${WWW};

    ssl_certificate ${CERT_PATH}/fullchain.pem;
    ssl_certificate_key ${CERT_PATH}/privkey.pem;
    include /etc/letsencrypt/options-ssl-nginx.conf;
    ssl_dhparam /etc/letsencrypt/ssl-dhparams.pem;

    add_header Strict-Transport-Security "max-age=63072000; includeSubDomains; preload" always;
    add_header X-Frame-Options DENY;
    add_header X-Content-Type-Options nosniff;
    add_header Referrer-Policy "no-referrer-when-downgrade";
    add_header X-XSS-Protection "1; mode=block";

    client_max_body_size 50M;

    location ^~ /.well-known/acme-challenge/ {
        root ${WEBROOT};
        try_files \$uri =404;
    }

    location = /healthz {
        access_log off;
        add_header Content-Type text/plain;
        return 200 'OK';
    }

    location / {
        proxy_pass http://127.0.0.1:${APP_PORT};
        proxy_http_version 1.1;
        proxy_set_header Host \$host;
        proxy_set_header X-Real-IP \$remote_addr;
        proxy_set_header X-Forwarded-For \$proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto \$scheme;
        proxy_set_header Upgrade \$http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_read_timeout 90;
        proxy_connect_timeout 5s;
        proxy_send_timeout 5s;
        proxy_buffering off;
    }
}
EOF

echo -e "\n=== TESTING AND RELOADING NGINX ==="
sudo nginx -t && sudo systemctl reload nginx

echo -e "\n=== NGINX SETUP COMPLETE ==="
