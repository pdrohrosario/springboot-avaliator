FROM openjdk:21-jdk

WORKDIR /app

ARG JAR_FILE=*.jar

COPY target/${JAR_FILE} /app/catalog.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/catalog.jar"]