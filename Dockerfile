# Multi-stage Dockerfile: build Spring Boot jar, then run with JRE and CJK fonts

## Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Configure Maven mirrors (Aliyun). Note: keep settings outside ~/.m2 to avoid being hidden by cache mount
COPY maven-settings.xml /tmp/settings.xml

# Copy only pom to leverage layer caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /tmp/settings.xml -DskipTests \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info \
        -Dorg.slf4j.simpleLogger.showDateTime=true \
        dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /tmp/settings.xml -DskipTests -Dspring-boot.repackage.skip=true \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info \
        -Dorg.slf4j.simpleLogger.showDateTime=true \
        clean package \
 && mvn -s /tmp/settings.xml -DskipTests \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info \
        -Dorg.slf4j.simpleLogger.showDateTime=true \
        dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency

## Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install Chinese fonts for Java2D text rendering and FFmpeg CLI
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       ffmpeg fonts-wqy-microhei fonts-wqy-zenhei fonts-noto-cjk fontconfig \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/*

# Copy jar and runtime dependencies
COPY --from=build /workspace/target/*.jar /app/app.jar
COPY --from=build /workspace/target/dependency /app/lib

# Default environment
ENV APP_PORT=8081 \
    MINIO_ENDPOINT=http://minio:9000 \
    MINIO_EXT_ENDPOINT= \
    MINIO_ACCESS_KEY=minioadmin \
    MINIO_SECRET_KEY=minioadmin \
    MINIO_BUCKET=nis \
    REDIS_HOST=redis \
    REDIS_PORT=6379 \
    TASK_STORAGE_TYPE=memory \
    JAVA_OPTS="-Xms512m -Xmx1024m"

EXPOSE 8081

# Run application; pass configuration via -D system properties
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS \
  -Dspring.application.name=subtitle-fusion \
  -Dserver.port=${APP_PORT} \
  -Dminio.endpoint=${MINIO_ENDPOINT} \
  -Dminio.ext-endpoint=${MINIO_EXT_ENDPOINT} \
  -Dminio.access-key=${MINIO_ACCESS_KEY} \
  -Dminio.secret-key=${MINIO_SECRET_KEY} \
  -Dminio.bucket-name=${MINIO_BUCKET} \
  -Dspring.data.redis.host=${REDIS_HOST} \
  -Dspring.data.redis.port=${REDIS_PORT} \
  -Dtask.storage.type=${TASK_STORAGE_TYPE} \
  -cp /app/app.jar:/app/lib/* com.zhongjia.subtitlefusion.SubtitleFusionApplication"]


