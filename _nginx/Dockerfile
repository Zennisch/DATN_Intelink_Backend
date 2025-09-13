FROM nginx:alpine

RUN apk add --no-cache certbot certbot-nginx openssl bash gettext

RUN mkdir -p /var/www/certbot
RUN mkdir -p /etc/letsencrypt

COPY start-nginx.sh /usr/local/bin/start-nginx.sh
COPY init-ssl.sh /usr/local/bin/init-ssl.sh
RUN chmod +x /usr/local/bin/start-nginx.sh
RUN chmod +x /usr/local/bin/init-ssl.sh

COPY nginx.conf.template /etc/nginx/nginx.conf.template

EXPOSE 80 443

CMD ["/usr/local/bin/start-nginx.sh"]
