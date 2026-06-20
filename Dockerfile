# Use official OpenJDK image
FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY . .

# Give mvnw permission to execute
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
