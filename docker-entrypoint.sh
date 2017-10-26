#!/bin/sh
set -x

keystorePassword=`cat $KEYSTORE_PASSWORD_FILE`

openssl pkcs12 -export -in $OAUTH_CERT -inkey $OAUTH_KEY -name $keystoreDefaultKey -out oauth.p12
openssl pkcs12 -export -in $SAML_CERT -inkey $SAML_KEY -name $keystoreTokenSigningKey -out saml.p12
openssl pkcs12 -export -in $TOMCAT_CERT -inkey $TOMCAT_KEY -name tomcat -out tomcat.p12

keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -srckeystore oauth.p12 -srcstoretype PKCS12
keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -srckeystore saml.p12 -srcstoretype PKCS12
keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -srckeystore tomcat.p12 -srcstoretype PKCS12

if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

if [ -n "$samlIdpHost" ] ; then openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > samlidp.crt; fi
if [ -n "$samlIdpHost" ] ; then keytool  -importcert -file samlidp.crt -alias samlidp -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

keytool -list -keystore $keystoreLocation -storepass $keystorePassword
java -Djava.security.egd=file:/dev/./urandom -DkeystorePassword=$keystorePassword -jar app.jar

exec $?