# ── Etapa 1: build ──────────────────────────────────────────────────────────
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copia o pom primeiro para aproveitar o cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copia o restante e compila (sem rodar testes)
COPY src ./src
RUN mvn clean package -DskipTests -q

# ── Etapa 2: runtime ─────────────────────────────────────────────────────────
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Copia apenas o JAR gerado
COPY --from=build /app/target/*.jar app.jar

# Porta que o Spring Boot usa (Render injeta $PORT automaticamente)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
