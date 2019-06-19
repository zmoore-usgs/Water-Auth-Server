#!/bin/sh
# Add cert for signing created OAuth2 tokens
keystoreLocation=${SERVER_SSL_KEYSTORE:?}
keystorePassword=${SERVER_SSL_KEYSTOREPASSWORD:?}

if [ -n "${TOKEN_CERT_PATH}" ] && [ -f "${TOKEN_CERT_PATH}" ] && [ -n "${TOKEN_KEY_PATH}" ] && [ -f "${TOKEN_KEY_PATH}" ]; then
  echo "INFO: Found Token Cert and Key at ${TOKEN_CERT_PATH} and ${TOKEN_KEY_PATH}"
  # Build PEM file
  cat "${TOKEN_KEY_PATH}" > "$HOME/token.pem"
  cat "${TOKEN_CERT_PATH}" >> "$HOME/token.pem"

  # Import the PEM
  openssl pkcs12 -export -in "$HOME/token.pem" -inkey "$TOKEN_KEY_PATH" -name "$keystoreOAuthKey" -out "$HOME/token.pkcs12" -password "pass:${keystorePassword}"
  keytool -v -importkeystore -deststorepass "$keystorePassword" -destkeystore "$keystoreLocation" -deststoretype PKCS12 -srckeystore "$HOME/token.pkcs12" -srcstorepass "$keystorePassword" -srcstoretype PKCS12 -noprompt
fi

# Add cert for signing SAML communication with DOI
if [ -n "${SAML_CERT_PATH}" ] && [ -f "${SAML_CERT_PATH}" ] && [ -n "${SAML_KEY_PATH}" ] && [ -f "${SAML_KEY_PATH}" ]; then
  echo "INFO: Found SAML Signing Cert and Key at ${SAML_CERT_PATH} and ${SAML_KEY_PATH}"
  # Build PEM file
  cat "${SAML_KEY_PATH}" > "$HOME/saml.pem"
  cat "${SAML_CERT_PATH}" >> "$HOME/saml.pem"

  # Import the PEM
  openssl pkcs12 -export -in "$HOME/saml.pem" -inkey "$SAML_KEY_PATH" -name "$keystoreSAMLKey" -out "$HOME/saml.pkcs12" -password "pass:${keystorePassword}"
  keytool -v -importkeystore -deststorepass "$keystorePassword" -destkeystore "$keystoreLocation" -deststoretype PKCS12 -srckeystore "$HOME/saml.pkcs12" -srcstorepass "$keystorePassword" -srcstoretype PKCS12 -noprompt
fi

# Add cert served by the SAML IDP (never used if running in local dev mode)
if [ -n "${samlIdpHost}" ] && [ $LOCAL_DEV_MODE = false ]; then
  echo "INFO: Attempting to fetch and trust the SSL Certificate for the SAML IDP at: ${samlIdpHost}"
  openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > /home/spring/samlidp.crt;
  keytool  -importcert -file /home/spring/samlidp.crt -alias samlidp -keystore "$keystoreLocation" -storepass "$keystorePassword" -noprompt
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

# Export keystore password for java
export keystorePassword=$keystorePassword

java -server -Djava.security.egd=file:/dev/./urandom -DkeystoreLocation=$PREFIXED_KEYSTORE_LOCATION -jar app.jar $SPRING_APP_ARGS "$@"
