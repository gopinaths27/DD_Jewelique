FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY . .

RUN chmod +x mvnw

RUN ./mvnw clean package -DskipTests

# Copy the built jar to /app/app.jar
RUN cp target/*.jar app.jar

CMD ["java", "-jar", "app.jar"]
