FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

LABEL org.opencontainers.image.source="https://github.com/your-username/ip-geolocation-service"
LABEL org.opencontainers.image.description="IP Geolocation Service - Microservico REST"
LABEL org.opencontainers.image.licenses="MIT"

RUN addgroup -g 1001 appgroup && \
    adduser -u 1001 -G appgroup -D appuser

COPY target/*.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
ENV SPRING_PROFILES_ACTIVE=prod

HEALTHCHECK --interval=30s --timeout=3s --start-period=30s --retries=3 \
    CMD wget -q --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
