# Build the application
# ---------- Build da aplicação ----------
FROM maven:3.9.5-eclipse-temurin-21 AS build
WORKDIR /app

# Copia o pom.xml e baixa dependências
COPY pom.xml .
RUN mvn dependency:go-offline

# Copia o código-fonte e compila
COPY src ./src
RUN mvn clean package -DskipTests

# ---------- Imagem final ----------
FROM eclipse-temurin:21-jre
WORKDIR /app

# Copia o JAR gerado
COPY --from=build /app/target/*.jar app.jar

# Expõe portas HTTP e de debug
EXPOSE 8080

# Sempre inicia com debug remoto ativo
ENTRYPOINT ["sh", "-c", "java -version && java $JAVA_OPTS -jar /app/app.jar"]

