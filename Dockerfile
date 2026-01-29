FROM eclipse-temurin:25 AS build

ENV HOME=/app
RUN mkdir -p $HOME
WORKDIR $HOME

# first only copy pom and download maven deps, so docker can cache this step
COPY mvnw pom.xml $HOME/
COPY .mvn $HOME/.mvn
RUN ./mvnw dependency:go-offline -B

# then copy source files and build
COPY src $HOME/src
RUN ./mvnw clean package -DskipTests

FROM eclipse-temurin:25-jre-alpine
COPY --from=build /app/target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
