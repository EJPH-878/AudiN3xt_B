# Usamos una imagen de Java 17
FROM eclipse-temurin:17-jdk-jammy

# Directorio de trabajo
WORKDIR /app

# Copiamos el archivo JAR que genera Maven
COPY target/*.jar app.jar

# Exponemos el puerto que usa tu backend (8081)
EXPOSE 8081

# Comando para arrancar
ENTRYPOINT ["java", "-jar", "app.jar"]