FROM maven:3.9.7-eclipse-temurin-21-alpine AS build
FROM common

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn package

FROM common-avaliator:latest

WORKDIR /app

ARG JAR_FILE=target/*.jar

COPY --from=build /app/${JAR_FILE} app.jar
EXPOSE 8081

ENTRYPOINT ["sh", "-c", "java -version && java $JAVA_OPTS -jar /app/app.jar"]
