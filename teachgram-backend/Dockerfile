# Etapa 1 - Build da aplicação
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copia arquivos Maven
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Baixa dependências primeiro para cache eficiente
RUN ./mvnw dependency:go-offline -B --no-transfer-progress

# Copia o código-fonte
COPY src src

# Garante permissão do wrapper
RUN chmod +x mvnw

# Compila o projeto, ignorando testes
RUN ./mvnw clean package -DskipTests --no-transfer-progress

# Etapa 2 - Imagem final com JRE apenas
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copia o JAR gerado
COPY --from=builder /app/target/*.jar app.jar

# Variáveis de ambiente
ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    SPRING_PROFILES_ACTIVE=docker

EXPOSE 8080

# Inicia a aplicação
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "app.jar"]
