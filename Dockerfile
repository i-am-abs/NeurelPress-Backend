FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace
RUN apt-get update && apt-get install -y maven
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q clean package -DskipTests \
    && mkdir -p /workspace/target/extracted \
    && java -Djarmode=layertools -jar target/*.jar extract --destination /workspace/target/extracted
RUN jlink \
    --add-modules java.base,java.logging,java.naming,java.sql,java.management,java.instrument,java.security.jgss,jdk.crypto.ec,jdk.unsupported,java.desktop \
    --strip-debug \
    --no-man-pages \
    --no-header-files \
    --compress=2 \
    --output /jre-min

FROM gcr.io/distroless/base-debian12:nonroot
WORKDIR /app
COPY --from=build /jre-min /jre
COPY --from=build /workspace/target/extracted/dependencies/ ./
COPY --from=build /workspace/target/extracted/spring-boot-loader/ ./
COPY --from=build /workspace/target/extracted/snapshot-dependencies/ ./
COPY --from=build /workspace/target/extracted/application/ ./
ENV PATH="/jre/bin:$PATH" \
    SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]