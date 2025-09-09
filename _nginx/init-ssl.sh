#!/bin/bash

# Tạo thư mục chứa chứng chỉ SSL nếu chưa tồn tại
mkdir -p /etc/letsencrypt/live/${DOMAIN}

# Tạo chứng chỉ SSL tạm thời
openssl req -x509 -nodes -days 1 -newkey rsa:2048 \
    -keyout /etc/letsencrypt/live/${DOMAIN}/privkey.pem \
    -out /etc/letsencrypt/live/${DOMAIN}/fullchain.pem \
    -subj "/C=US/ST=State/L=City/O=Organization/CN=${DOMAIN}"

echo "Temporary SSL certificate created for ${DOMAIN}"
