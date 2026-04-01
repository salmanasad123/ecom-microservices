FROM eclipse-temurin:17-jre
WORKDIR /app
COPY target/configserver-0.0.1-SNAPSHOT.jar /app/configserver-0.0.1-SNAPSHOT.jar
EXPOSE 8888
CMD ["java", "-jar", "configserver-0.0.1-SNAPSHOT.jar"]