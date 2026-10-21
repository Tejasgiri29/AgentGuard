# Multi-stage Dockerfile for AgentGuard Backend (Java 21)
FROM maven:3.9.8-eclipse-temurin-21-alpine AS builder
WORKDIR /workspace

# Copy parent and module pom files first for dependency caching
COPY pom.xml /workspace/pom.xml
COPY apps/agentguard/backend/pom.xml /workspace/apps/agentguard/backend/pom.xml
COPY apps/griddispatch/backend/pom.xml /workspace/apps/griddispatch/backend/pom.xml
RUN mvn dependency:go-offline -B -pl apps/agentguard/backend -am || true

# Copy source and build
COPY apps/agentguard/backend/src /workspace/apps/agentguard/backend/src
RUN mvn clean package -DskipTests -pl apps/agentguard/backend -am

# Runtime image
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Run as non-root user
RUN addgroup -S appgroup && adduser -S appuser -G appgroup
USER appuser

COPY --from=builder /workspace/apps/agentguard/backend/target/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=docker
EXPOSE 8081

ENTRYPOINT ["java", "-XX:+UseZGC", "-XX:+ZGenerational", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
