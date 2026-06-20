FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy everything
COPY . .

# Ensure mvnw has execute permission
RUN chmod +x mvnw

# Build the project
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
