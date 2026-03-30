# Hydraulic Rubber Molding - Java Spring Boot Demo

This is a Spring Boot web app that mirrors the Python demo: it simulates hydraulic rubber molding cycles with noisy sensor readings, calculates KPIs, raises rule-based alerts, and computes a simple machine-health probability.

How to run:
1. Install Java 17+ and Maven.
2. Build and run:
   mvn clean package
   mvn spring-boot:run
3. Open: http://localhost:8080

Notes:
- Baseline history (50 cycles) is generated at startup.
- History and alerts are kept in memory (app restarts clear them). Add persistence if required.
- The UI uses Thymeleaf + Bootstrap.