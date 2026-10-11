# ==============================================================================
# PixFlow: Frontend Minimalist Production Container
# Multi-Stage Build using Node Alpine & Nginx Alpine
# ==============================================================================

FROM node:22-alpine AS build

WORKDIR /app

COPY package.json ./
RUN npm install

COPY . .
RUN npm run build

FROM nginx:alpine AS runner

COPY --from=build /app/dist /usr/share/nginx/html

# Expose HTTP port
EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
