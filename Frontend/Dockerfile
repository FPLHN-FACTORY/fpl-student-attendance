# syntax=docker/dockerfile:1.4

# ----------------------
# Stage 1: Build with Node
# ----------------------
FROM node:20-alpine AS builder
WORKDIR /app

# Nếu bạn có .env, copy vào nếu cần (tuỳ bạn có embed env ở build time)
# COPY .env .env

# Embed biến Vite (nếu cần)
ENV VITE_BASE_URL="http://fptudpm.site"
ENV VITE_BASE_API_URL="http://fptudpm.site"
ENV VITE_PREFIX_API="/api/v1"

# Copy package và cài dependencies
COPY package.json package-lock.json ./
# Tạo cache npm tại /root/.npm
RUN mkdir -p /root/.npm && \
    --mount=type=cache,target=/root/.npm npm ci

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

# 3. Copy file cấu hình Nginx của bạn vào đúng vị trí
COPY nginx/nginx.conf /etc/nginx/conf.d/default.conf

# 4. Expose port 80 (Nginx sẽ listen 80 trong config)
EXPOSE 80

# 5. Khởi động Nginx ở foreground
CMD ["nginx", "-g", "daemon off;"]
