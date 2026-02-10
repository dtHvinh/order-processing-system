FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /workspace

COPY pom.xml ./
COPY .mvn/ .mvn/
COPY mvnw ./
COPY mvnw.cmd ./

RUN mvn -q -DskipTests dependency:go-offline

COPY src/ src/
RUN mvn -DskipTests package


FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

RUN addgroup --system app && adduser --system --ingroup app app

COPY --from=build /workspace/target/*.jar /app/app.jar

ENV SERVER_PORT=8080 \
	KAFKA_BOOTSTRAP_SERVER=kafka-server:9092 \
	JAVA_OPTS=""

EXPOSE 8080

USER app

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app/app.jar"]
