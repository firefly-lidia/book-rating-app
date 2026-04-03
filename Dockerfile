FROM azul/zulu-openjdk-alpine:21-jre AS extract
WORKDIR /work
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN java -Djarmode=layertools -jar app.jar extract

FROM azul/zulu-openjdk-alpine:21-jre

RUN addgroup -S app && adduser -S -G app -u 1000 app

WORKDIR /app

COPY --chown=app:app jwt-private.pem /app/jwt-private.pem

RUN chmod 400 /app/jwt-private.pem

COPY --from=extract /work/dependencies/          ./
COPY --from=extract /work/snapshot-dependencies/ ./
COPY --from=extract /work/spring-boot-loader/    ./
COPY --from=extract /work/application/           ./

USER app

ENV JAVA_OPTS=""

ENTRYPOINT ["sh","-c","exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
