# Stage 1 - Build
FROM azul/zulu-openjdk-alpine:21 AS builder

WORKDIR /app
COPY src /app/src
COPY build.sh /app/

RUN chmod +x build.sh && ./build.sh

# Stage 2 - Run (smaller final image)
FROM azul/zulu-openjdk-alpine:21.0.2-jre-headless

WORKDIR /app

COPY --from=builder /app/build/libs/ssl-diagnostic-tool.jar /app/

ENTRYPOINT ["java", "-jar", "/app/ssl-diagnostic-tool.jar"]
