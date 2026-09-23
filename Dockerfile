# Single build stage for the whole reactor; one runtime image per service via SERVICE.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /workspace

# Optional mirror for networks that cannot reach repo.maven.apache.org directly.
ARG MAVEN_MIRROR_URL=""
RUN if [ -n "$MAVEN_MIRROR_URL" ]; then \
      mkdir -p /root/.m2 && \
      printf '<settings><mirrors><mirror><id>mirror</id><url>%s</url><mirrorOf>central</mirrorOf></mirror></mirrors></settings>' \
        "$MAVEN_MIRROR_URL" > /root/.m2/settings.xml; \
    fi

COPY pom.xml ./
COPY services ./services
RUN mvn -B -DskipTests -Dmaven.wagon.http.retryHandler.count=5 \
      -Dmaven.wagon.http.retryHandler.requestSentEnabled=true package

FROM eclipse-temurin:17-jre
ARG SERVICE
ENV SERVICE=${SERVICE}
WORKDIR /app
RUN useradd --system --uid 10001 cafeadmin
COPY --from=build /workspace/services/${SERVICE}/target/${SERVICE}-*.jar /app/app.jar
USER 10001
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "/app/app.jar"]
