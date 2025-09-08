FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

COPY mvnw mvnw.cmd ./
COPY .mvn .mvn
COPY pom.xml .

RUN --mount=type=cache,target=/root/.m2 \
    chmod +x mvnw && \
    ./mvnw dependency:go-offline -B

COPY src src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw clean package -DskipTests -B

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

RUN addgroup -g 1000 enefitresourceservice && \
    adduser -u 1000 -G enefitresourceservice -s /bin/sh -D enefitresourceservice

COPY --from=builder --chown=enefitresourceservice:enefitresourceservice /app/target/*.jar /app/app.jar

USER enefitresourceservice

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
