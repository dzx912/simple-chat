FROM openjdk:17-slim

COPY build/libs/*.jar /deployments/app.jar

EXPOSE 8080 8081

USER 1001

ENTRYPOINT [ "java", "-jar", "/deployments/app.jar"]
