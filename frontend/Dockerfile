FROM node:22-alpine AS build

WORKDIR /app

COPY package*.json ./
# install dependencies from the json file
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

# nginx prints nothing at startup, so say where the site is
CMD ["sh", "-c", "echo 'OFS Delivery is running at http://localhost:5173 (the backend needs a few more seconds to finish starting)'; exec nginx -g 'daemon off;'"]
