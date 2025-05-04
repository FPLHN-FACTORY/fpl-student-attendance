# ----------------------
# Stage 1: Build with Node
# ----------------------
FROM node:20-alpine AS builder

# 1. Thiết lập thư mục làm việc
WORKDIR /app

# 2. Copy file lock & package để tận dụng cache layer
COPY package.json package-lock.json ./

# 3. Cài dependencies
RUN mkdir /root/.npm
COPY package*.json ./
RUN --mount=type=cache,target=/root/.npm \
    npm ci

# 4. Copy toàn bộ source code và build
COPY . .
RUN npm run build
# với npm: RUN npm run build

# -------------------------
# Stage 2: Serve with Nginx
# -------------------------

FROM nginx:stable-alpine
COPY nginx/nginx.conf /etc/nginx/nginx.conf
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 5173
CMD ["nginx", "-g", "daemon off;"]

