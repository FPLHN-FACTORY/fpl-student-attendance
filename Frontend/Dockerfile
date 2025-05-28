# syntax=docker/dockerfile:1.4

# ----------------------
# Stage 1: Build with Node
# ----------------------
FROM node:22-alpine AS builder
WORKDIR /app

# Nếu bạn có .env, copy vào nếu cần (tuỳ bạn có embed env ở build time)
# COPY .env .env

# Embed biến Vite (nếu cần)
ENV VITE_BASE_URL="https://fplstudentattendance.site/"
ENV VITE_BASE_API_URL="https://fplstudentattendance.site/"
ENV VITE_PREFIX_API="/api/v1"
ENV VITE_PREFIX_ADMIN_PANEL="/admin-panel"

# Copy package và cài dependencies
COPY package.json package-lock.json ./
RUN npm ci
# Copy toàn bộ source và build
COPY . .
RUN npm run build

# -------------------------
# Stage 2: Serve với Nginx
# -------------------------
FROM nginx:stable-alpine

WORKDIR /usr/share/nginx/html


# 1. Xoá config mặc định (nếu bạn muốn mount conf riêng)
RUN rm /etc/nginx/conf.d/default.conf

# 2. Copy bản build từ builder sang thư mục serve tĩnh
COPY --from=builder /app/dist .

# Copy toàn bộ cấu hình nginx
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY nginx/conf.d /etc/nginx/conf.d

# 4. Expose port 80 (Nginx sẽ listen 80 trong config)
EXPOSE 80

# 5. Khởi động Nginx ở foreground
CMD ["nginx", "-g", "daemon off;"]