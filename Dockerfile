# Etapa de compilacion
FROM maven:3.8.6-openjdk-17-slim AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecucion
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY --from=build /app/target/GestionInventarioEly-0.0.1-SNAPSHOT.jar gestion-inventario-ely.jar
EXPOSE 8080
CMD ["java", "-jar", "gestion-inventario-ely.jar"]