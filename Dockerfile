# --- Estágio 1: Build ---
# Usando Alpine para compatibilidade e leveza
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Necessário para o gradlew no Alpine
RUN apk add --no-cache findutils

COPY gradlew .
COPY gradle gradle
COPY build.gradle.kts .
COPY settings.gradle.kts .

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

COPY src src

# Pulamos testes no build do Docker para agilizar
RUN ./gradlew bootJar --no-daemon -x test

# --- Estágio 2: Runtime ---
# AQUI ESTÁ A CORREÇÃO: Usando Alpine para aceitar o comando 'adduser -S'
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Agora esse comando funciona nativamente
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]