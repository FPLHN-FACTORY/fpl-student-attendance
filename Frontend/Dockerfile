# syntax=docker/dockerfile:1.4

# ----------------------
# Stage 1: Build with Node
# ----------------------
FROM node:slim-alpine AS builder
WORKDIR /app

# Embed biến Vite
ENV VITE_BASE_URL="https://fplstudentattendance.site/"
ENV VITE_BASE_API_URL="https://fplstudentattendance.site/"
ENV VITE_PREFIX_API="/api/v1"
ENV VITE_PREFIX_ADMIN_PANEL="/admin-panel"

# Copy package và cài dependencies
COPY package.json package-lock.json ./
RUN npm ci --production=false

# Copy toàn bộ source và build
COPY . .
RUN npm run build

# -------------------------
# Stage 2: Serve với Nginx
# -------------------------
FROM nginx:stable-alpine

# Tạo non-root user
RUN adduser -D -H -u 101 -s /sbin/nologin nginx || true

WORKDIR /usr/share/nginx/html

# Xoá config mặc định
RUN rm /etc/nginx/conf.d/default.conf

# Copy bản build từ builder
COPY --from=builder /app/dist .

# Copy cấu hình nginx
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/conf.d /etc/nginx/conf.d

# Set permissions
RUN chown -R nginx:nginx /usr/share/nginx/html && \
    chmod -R 755 /usr/share/nginx/html && \
    chown -R nginx:nginx /var/cache/nginx && \
    chown -R nginx:nginx /var/log/nginx && \
    chown -R nginx:nginx /etc/nginx/conf.d && \
    touch /var/run/nginx.pid && \
    chown -R nginx:nginx /var/run/nginx.pid

# Switch to non-root user
USER nginx

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]