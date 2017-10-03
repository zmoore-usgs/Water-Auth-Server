FROM openjdk:8-jdk-alpine
RUN set -x & apk update && apk upgrade && apk add --no-cache curl
ARG mlr_version
ARG keystore_pass=changeit
ARG keystore_name=keystore.jks
ARG keystore_deafult_alias=default
RUN  curl -k -X GET "https://cida.usgs.gov/artifactory/mlr-maven-centralized/gov/usgs/wma/waterauthserver/$mlr_version/waterauthserver-$mlr_version.jar" > app.jar
RUN keytool -genkeypair -alias $keystore_deafult_alias -keyalg RSA -dname "CN=water-auth,OU=Unit,O=Organization,L=City,S=State,C=US" -keypass keystore_pass -keystore $keystore_name -storepass $keystore_pass
EXPOSE 8080
ENTRYPOINT [ "sh", "-c", "java -Djava.security.egd=file:/dev/./urandom -jar app.jar"]