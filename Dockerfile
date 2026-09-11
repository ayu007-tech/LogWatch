FROM eclipse-temurin:17-jdk

WORKDIR /app

COPY src /app/src
COPY logs /app/logs

RUN mkdir -p /app/out

RUN javac -d /app/out /app/src/*.java

CMD ["java", "-cp", "/app/out", "Main"]