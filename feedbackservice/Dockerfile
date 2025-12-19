FROM maven:3.9.7-eclipse-temurin-21-alpine AS build

WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn package

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

ARG JAR_FILE=target/*.jar

COPY --from=build /app/${JAR_FILE} app.jar
EXPOSE 8882

ENTRYPOINT ["sh", "-c" ,"java -version & java $JAVA_OPTS -jar /app/app.jar"]