FROM maven@sha256:b37da91062d450f3c11c619187f0207bbb497fc89d265a46bbc6dc5f17c02a2b AS build
# The above is a temporary fix
# See:
# https://bugs.debian.org/cgi-bin/bugreport.cgi?bug=911925
# https://github.com/carlossg/docker-maven/issues/92
# FROM maven:3-jdk-8-slim AS build

COPY pom.xml /build/pom.xml
WORKDIR /build
RUN mvn clean
COPY src /build/src
ARG BUILD_COMMAND="mvn package"
RUN ${BUILD_COMMAND}

FROM usgswma/wma-spring-boot-base:8-jre-slim-0.0.4

LABEL maintaner="gs-w_eto@usgs.gov"

ENV LOCAL_DEV_MODE=false
ENV STANDARD_SPRING_ARGS="--spring.profiles.active=default"
ENV LOCAL_DEV_SPRING_ARGS="--spring.profiles.active=localDev --spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.session.SessionAutoConfiguration,org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration"
ENV serverPort=8443
ENV requireSsl=true
ENV serverContextPath=/
ENV waterAuthUrlServerPort=8443
ENV waterAuthUrlServerName=localhost
ENV waterAuthUrlContextPath=/
ENV dbInitializerEnabled=true
ENV dbConnectionUrl=jdbc:mysql://auth.example.gov/db
ENV dbUsername=mysqluser
ENV samlAuthnRequestProviderName=https://localhost:8443/saml/
ENV samlAuthnRequestEntityId=https://localhost:8443/saml/
ENV samlBaseEndpoint=/saml
ENV samlLoginEndpoint=/login
ENV samlLogoutEndpoint=/logout
ENV samlSingleLogoutEndpoint=/singlelogout
ENV samlSSOEndpoint=/sso
ENV samlSSOHOKEndpoint=/ssohok
ENV samlMetadataEndpoint=/metadata
ENV samlIdpMetadataLocation=https://saml-idp.example.gov/metadata.xml
ENV samlIdpHost=https://saml-idp.example.gov
ENV samlIdpPort=443
ENV samlGroupAttributeNames=http://schemas.xmlsoap.org/claims/Group
ENV samlEmailAttributeNames=http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress,EmailAddress
ENV samlUsernameAttributeNames=http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname
ENV loginSuccessTargetUrl=/
ENV loginErrorTargetUrl=/auth-error
ENV logoutSuccessTargetUrl=/out
ENV samlLoggingIncludeMessages=false
ENV samlLoggingIncludeErrors=true
ENV springFrameworkLogLevel=info

ENV keystoreOAuthKey=tokenkey
ENV keystoreSAMLKey=samlkey
ENV TOKEN_CERT_PATH=/home/spring/oauth-wildcard-sign.crt
ENV TOKEN_KEY_PATH=/home/spring/oauth-wildcard-sign.key
ENV SAML_KEY_PATH=/home/spring/saml-wildcard-sign.key
ENV SAML_CERT_PATH=/home/spring/saml-wildcard-sign.crt

# Only used in Local Dev mode
ENV localOauthClientId=local-client
ENV localOauthClientSecret=changeMe
ENV localOauthClientGrantTypes="authorization_code, access_token, refresh_token, client_credentials, password"
ENV localOauthClientScopes=user_details
ENV localOauthResourceId=local-app
ENV localUserName=user
ENV localUserPassword=changeMe
ENV localUserRole="ACTUATOR, DBA_EXAMPLE"
ENV localUserEmail=localuser@example.gov
ENV localContextPath=/auth/

COPY --chown=1000:1000 docker/launch-app.sh ${LAUNCH_APP_SCRIPT}
RUN chmod +x ${LAUNCH_APP_SCRIPT}

COPY --chown=1000:1000 --from=build /build/target/*.jar app.jar

HEALTHCHECK CMD curl -s -o /dev/null -w "%{http_code}" -k "https://127.0.0.1:${serverPort}${serverContextPath}oauth/token_key" | grep -q '200' || exit 1
