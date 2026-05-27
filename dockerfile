# ETAPA 1: Construcción (Builder)
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app
# Copiamos los archivos de configuración de Maven
COPY .mvn/ .mvn
COPY mvnw pom.xml ./
# Descargamos dependencias (esto hace que la siguiente vez sea más rápido)
RUN ./mvnw dependency:go-offline
# Copiamos el código fuente
COPY src ./src
# Compilamos el proyecto y generamos el .jar
RUN ./mvnw clean package -DskipTests

# ETAPA 2: Ejecución (Runtime)
FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
# Copiamos ÚNICAMENTE el .jar desde la Etapa 1
COPY --from=builder /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]