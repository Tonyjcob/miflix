# Construye el WAR en una etapa separada para no incluir Maven en la imagen final.
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Descarga dependencias antes del código para aprovechar la caché de capas.
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# Despliega la aplicación en Tomcat 10.1 con Java 21.
FROM tomcat:10.1-jre21-temurin
RUN groupadd --system tomcat \
    && useradd --system --gid tomcat --home-dir /usr/local/tomcat --shell /usr/sbin/nologin tomcat
RUN rm -rf /usr/local/tomcat/webapps/*
COPY tomcat/server.xml /usr/local/tomcat/conf/server.xml
COPY --from=build /app/target/miflix.war /usr/local/tomcat/webapps/ROOT.war
RUN chown -R tomcat:tomcat /usr/local/tomcat
USER tomcat
EXPOSE 8080