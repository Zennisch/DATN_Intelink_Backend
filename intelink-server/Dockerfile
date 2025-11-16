FROM maven:3.9.9-eclipse-temurin-17-focal

WORKDIR /app

COPY mvnw mvnw.cmd /app/
COPY .mvn /app/.mvn/
RUN chmod +x /app/mvnw

COPY pom.xml /app/
RUN mvn dependency:go-offline -B

COPY src /app/src/

RUN chmod +x /app/mvnw

EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]
