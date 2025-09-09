FROM maven:3.9.9-eclipse-temurin-17-focal

WORKDIR /app

# Copy Maven Wrapper files and set permissions
COPY mvnw mvnw.cmd /app/
COPY .mvn /app/.mvn/
RUN chmod +x /app/mvnw

# Copy pom.xml and download dependencies
COPY pom.xml /app/
RUN mvn dependency:go-offline -B

# Copy source code
COPY src /app/src/

EXPOSE 8080

CMD ["./mvnw", "spring-boot:run"]
