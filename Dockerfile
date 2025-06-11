# Etapa de compilacion
FROM maven:3.9-amazoncorretto-17 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Etapa de ejecucion
FROM amazoncorretto:17-alpine-jdk
WORKDIR /app
COPY --from=build /app/target/GestionInventarioEly-0.0.1-SNAPSHOT.jar gestion-inventario-ely.jar
EXPOSE 8080
CMD ["java", "-jar", "gestion-inventario-ely.jar"]