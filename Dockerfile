# Use official OpenJDK image
FROM eclipse-temurin:17-jdk

WORKDIR /app

# Copy only wrapper first
COPY mvnw .
COPY .mvn .mvn

# Give mvnw permission to execute
RUN chmod +x mvnw

# Copy the rest of the project
COPY . .
# Build the project
RUN ./mvnw clean package -DskipTests

# Run the jar
CMD ["java", "-jar", "target/*.jar"]
