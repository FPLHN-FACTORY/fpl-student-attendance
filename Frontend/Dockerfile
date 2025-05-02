# ----------------------
# Stage 1: Build with Node
# ----------------------
FROM node:20-alpine AS builder

# 1. Thiết lập thư mục làm việc
WORKDIR /app

# 2. Copy file lock & package để tận dụng cache layer
COPY package.json package-lock.json ./

# 3. Cài dependencies
RUN npm ci --frozen-lockfile

# 4. Copy toàn bộ source code và build
COPY . .
RUN npm run build
# với npm: RUN npm run build

# -------------------------
# Stage 2: Serve with Nginx
# -------------------------
FROM nginx:stable-alpine

# 1. Copy nginx config
COPY nginx/nginx.conf /etc/nginx/nginx.conf

# 2. Copy thư mục dist từ builder
COPY --from=builder /app/dist /usr/share/nginx/html

# 3. Expose port (trùng với port trong nginx.conf)
EXPOSE 5173

# 4. Chạy Nginx ở foreground
CMD ["nginx", "-g", "daemon off;"]
