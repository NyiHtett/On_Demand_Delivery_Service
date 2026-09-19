# On_Demand_Delivery_Service

React - MVVM architecture
Spring Boot - MVC + layered architecture

Java installation on mac - brew install --cask temurin@17

# Current Database Schema 
MySQL Server 
└── on_demand_delivery_db (database/schema) 
├── customers (table)

# Commands to start spring boot
chmod u+x mvnw 
./mvnw spring-boot:run
(for running the mvnw execution file) 

# Docker command for starting the sql and packages

docker compose up -d (Start docker compose and mysql)
docker compose ps (check the container)

# Docker command for assigning sql commands

docker compose exec mysql mysql -u delivery_user -p on_demand_delivery_db
Password = delivery_password
