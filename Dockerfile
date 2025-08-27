# Backend Dockerfile
FROM openjdk:17-jdk-slim AS builder

WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw mvnw.cmd pom.xml ./
COPY .mvn .mvn

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Production stage
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

# Install curl for healthcheck and create non-root user
RUN apk add --no-cache curl && \
    addgroup -S spring && \
    adduser -S spring -G spring

# Copy the JAR file
COPY --from=builder /app/target/*.jar app.jar

# Change ownership to spring user
RUN chown spring:spring app.jar

USER spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
    CMD curl -f http://localhost:8080/api/actuator/health || exit 1

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
