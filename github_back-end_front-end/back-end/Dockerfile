FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

# Kopeerime Maven/Gradle wrapper ja projekti failid
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

# Õigused Maven'i wrapperile
RUN chmod +x ./mvnw
# Ehitame JAR-faili
RUN ./mvnw package -DskipTests

# Teeninduskiht
FROM eclipse-temurin:17-jre-alpine
VOLUME /tmp
WORKDIR /app

# Kopeerime JAR-faili ehituskihist
COPY --from=build /workspace/app/target/*.jar app.jar

# Keskkonnamuutujad
ENV SPRING_PROFILES_ACTIVE=prod
ENV SERVER_PORT=8080

# AWS keskkonna muutujad
ENV AWS_ACCESS_KEY=${AWS_ACCESS_KEY}
ENV AWS_SECRET_KEY=${AWS_SECRET_KEY}

# Teenuse port
EXPOSE 8080

# Rakenduse käivitamine
ENTRYPOINT ["java", "-Djdk.internal.warn=false", "-jar", "app.jar"]