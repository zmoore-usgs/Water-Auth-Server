#!/bin/sh
# Add cert for signing created OAuth2 tokens
if [ -n "${TOKEN_CERT_PATH}" ] && [ -f "${TOKEN_CERT_PATH}" ]; then
  openssl pkcs12 -export -in $TOKEN_CERT_PATH -inkey $TOKEN_KEY_PATH -name $keystoreOAuthKey -out oauth.p12 -password pass:$keystorePassword
  keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype PKCS12 -srckeystore oauth.p12 -srcstorepass $keystorePassword -srcstoretype PKCS12 -noprompt
fi

# Add cert for signing SAML communication with DOI
if [ -n "${SAML_CERT_PATH}" ] && [ -f "${SAML_CERT_PATH}" ]; then
  openssl pkcs12 -export -in $SAML_CERT_PATH -inkey $SAML_KEY_PATH -name $keystoreSAMLKey -out saml.p12 -password pass:$keystorePassword
  keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype PKCS12 -srckeystore saml.p12 -srcstorepass $keystorePassword -srcstoretype PKCS12 -noprompt
fi

# Add cert served by the SAML IDP
if [ -n "${samlIdpHost}" ]; then
  openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > samlidp.crt;
  keytool  -importcert -file samlidp.crt -alias samlidp -keystore $keystoreLocation -storepass $keystorePassword -noprompt;
fi

# Set correct Spring Launch Args if we are running local dev mode
if [ $LOCAL_DEV_MODE = false ]; then
  SPRING_APP_ARGS=$STANDARD_SPRING_ARGS
else
  SPRING_APP_ARGS=$LOCAL_DEV_SPRING_ARGS
fi

# Keystore Location must be overridden here because spring treats FileSystemResource paths that start with `/` as relative unless you prefix with `file:`
# See: https://github.com/spring-projects/spring-framework/blob/master/spring-core/src/main/java/org/springframework/core/io/FileSystemResourceLoader.java#L24
# Most spring apps we build don't load the keystore via FileSystemResourceLoader, but Water Auth needs to for the SAML stuff
PREFIXED_KEYSTORE_LOCATION="file:$keystoreLocation"

java -server -Djava.security.egd=file:/dev/./urandom -DkeystoreLocation=$PREFIXED_KEYSTORE_LOCATION -jar app.jar $SPRING_APP_ARGS "$@"
