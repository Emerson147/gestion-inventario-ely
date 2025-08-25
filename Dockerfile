# Etapa de compilacion
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app

# Copy only pom.xml first to leverage Docker layer caching
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and build
COPY src ./src
RUN mvn clean package -DskipTests -B

# Etapa de ejecucion
FROM amazoncorretto:17-alpine-jdk

# Install dumb-init for proper signal handling
RUN apk add --no-cache dumb-init

# Create non-root user for security
RUN addgroup -g 1001 -S appgroup && \
    adduser -u 1001 -S appuser -G appgroup

WORKDIR /app

# Copy the jar file
COPY --from=build /app/target/GestionInventarioEly-0.0.1-SNAPSHOT.jar gestion-inventario-ely.jar

# Change ownership to non-root user
RUN chown -R appuser:appgroup /app

# Switch to non-root user
USER appuser

EXPOSE 8080

# Use dumb-init and optimized JVM flags
ENTRYPOINT ["dumb-init", "--"]
CMD ["java", \
     "-XX:+UseG1GC", \
     "-XX:+UseStringDeduplication", \
     "-XX:+OptimizeStringConcat", \
     "-XX:+UseCompressedOops", \
     "-Xms256m", \
     "-Xmx512m", \
     "-XX:NewRatio=2", \
     "-XX:+UnlockExperimentalVMOptions", \
     "-XX:+UseCGroupMemoryLimitForHeap", \
     "-Djava.security.egd=file:/dev/./urandom", \
     "-Dspring.profiles.active=prod", \
     "-jar", "gestion-inventario-ely.jar"]