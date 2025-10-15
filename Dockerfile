# Multi-stage Dockerfile: build Spring Boot jar, build FFmpeg with librsvg, then run with JRE and CJK fonts

## Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Configure Maven mirrors (Aliyun). Note: keep settings outside ~/.m2 to avoid being hidden by cache mount
COPY maven-settings.xml /tmp/settings.xml

# Copy only pom to leverage layer caching
COPY pom.xml .
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /tmp/settings.xml -Dmaven.test.skip=true \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info \
        -Dorg.slf4j.simpleLogger.showDateTime=true \
        dependency:go-offline

# Copy sources and build
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    mvn -s /tmp/settings.xml -Dmaven.test.skip=true -Dspring-boot.repackage.skip=true \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info \
        -Dorg.slf4j.simpleLogger.showDateTime=true \
        clean package \
 && mvn -s /tmp/settings.xml -Dmaven.test.skip=true \
        -Dorg.slf4j.simpleLogger.log.org.apache.maven.cli.transfer.Slf4jMavenTransferListener=info \
        -Dorg.slf4j.simpleLogger.showDateTime=true \
        dependency:copy-dependencies -DincludeScope=runtime -DoutputDirectory=target/dependency

## FFmpeg build stage (with librsvg, libass, x264, freetype, fribidi)
FROM ubuntu:22.04 AS ffmpeg-build
ENV DEBIAN_FRONTEND=noninteractive \
    FFMPEG_VERSION=6.1.1
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       build-essential pkg-config git curl ca-certificates yasm nasm \
       libx264-dev libass-dev libfreetype6-dev libfribidi-dev libfontconfig1-dev librsvg2-dev libharfbuzz-dev libxml2-dev \
    && rm -rf /var/lib/apt/lists/*
WORKDIR /tmp/ffmpeg
RUN curl -L -o ffmpeg.tar.xz https://ffmpeg.org/releases/ffmpeg-${FFMPEG_VERSION}.tar.xz \
    && tar -xf ffmpeg.tar.xz --strip-components=1 \
    && ./configure --prefix=/opt/ffmpeg \
         --enable-gpl \
         --enable-libx264 \
         --enable-libass \
         --enable-libfreetype \
         --enable-libfribidi \
         --enable-libfontconfig \
         --enable-libharfbuzz \
         --enable-librsvg \
         --enable-filter=drawtext \
         --enable-filter=subtitles \
         --enable-filter=overlay \
         --enable-filter=format \
         --enable-filter=trim \
         --enable-filter=setpts \
         --enable-filter=loop \
         --enable-filter=color \
         --enable-pic \
    && make -j$(nproc) \
    && make install

## Runtime stage
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Install Chinese fonts and runtime libs required by FFmpeg (libass, librsvg, x264, etc.)
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
       fonts-wqy-microhei fonts-wqy-zenhei fonts-noto-cjk fontconfig \
       libass9 libfreetype6 libfribidi0 libfontconfig1 librsvg2-2 libx264-163 libharfbuzz0b libxml2 \
    && fc-cache -f \
    && rm -rf /var/lib/apt/lists/*

# Copy self-built FFmpeg
COPY --from=ffmpeg-build /opt/ffmpeg /opt/ffmpeg
ENV PATH="/opt/ffmpeg/bin:${PATH}" \
    LD_LIBRARY_PATH="/opt/ffmpeg/lib:${LD_LIBRARY_PATH}"

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


