FROM maven:3.9.9-eclipse-temurin-17-focal

WORKDIR /app

COPY pom.xml /app
RUN mvn dependency:go-offline -B

COPY src /app/src

EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]
