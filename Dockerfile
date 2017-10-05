FROM openjdk:8-jdk-alpine
RUN set -x & apk update && apk upgrade && apk add --no-cache curl && apk --no-cache add openssl 
ARG mlr_version

ADD docker-entrypoint.sh entrypoint.sh
RUN ["chmod", "+x", "entrypoint.sh"]

ADD target/waterauthserver-0.1-SNAPSHOT.war app.war

# RUN  curl -k -X GET "https://cida.usgs.gov/artifactory/mlr-maven-centralized/gov/usgs/wma/waterauthserver/$mlr_version/waterauthserver-$mlr_version.jar" > app.jar
EXPOSE 8080

ENTRYPOINT [ "/entrypoint.sh" ]