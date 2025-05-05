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
RUN npm ci
# Copy toàn bộ source và build
COPY . .
RUN npm run build


FROM nginx:stable-alpine

COPY --from=builder /app/dist /usr/share/nginx/html

COPY nginx/nginx.conf /etc/nginx/nginx.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
