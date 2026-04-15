# Dockerfile otimizado para CI/CD
# Usa JAR pré-construído pelo pipeline

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Labels para metadata
LABEL org.opencontainers.image.source="https://github.com/your-username/ip-geolocation-service"
LABEL org.opencontainers.image.description="IP Geolocation Service - Microserviço REST"
LABEL org.opencontainers.image.licenses="MIT"

# Criar usuário não-root
RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

# Copiar JAR pré-construído
COPY target/*.jar app.jar

# Mudar para usuário não-root
USER appuser

EXPOSE 8080

# Variáveis de ambiente para produção
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
