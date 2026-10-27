# Build
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -q -B dependency:go-offline
COPY src src
RUN mvn -q -B package -DskipTests

# Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/target/sulwork-eventos-*.jar app.jar
USER app
EXPOSE 8080
# Ajustado para contêineres pequenos (ex.: plano gratuito com 0,1 CPU e 512 MB):
# TieredStopAtLevel=1 evita que a compilação JIT otimizada dispute a CPU com as requisições.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-XX:TieredStopAtLevel=1", "-XX:+UseSerialGC", "-jar", "app.jar"]
