
# Saga Design Pattern Microservices

Services:
1. order-service
2. payment-service
3. inventory-service

Run:
mvn clean install
mvn spring-boot:run

Suggested Kafka Topics:
- order-created
- payment-success
- payment-failed
- inventory-success
- inventory-failed
