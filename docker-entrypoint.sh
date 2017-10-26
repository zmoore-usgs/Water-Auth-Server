#!/bin/sh
set -x

keystorePassword=`cat $KEYSTORE_PASSWORD_FILE`

openssl pkcs12 -export -in $WATER_AUTH_CERT -inkey $WATER_AUTH_KEY -name water_auth_pkcs12 -out wa_pkcs12.p12
keytool -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -srckeystore wa_pkcs12.p12 -srcstoretype PKCS12
keytool -import -alias bundle -trustcacerts -file wa_pkcs12.p12 -keystore $keystoreLocation

if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

if [ -n "$samlIdpHost" ] ; then openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > samlidp.crt; fi
if [ -n "$samlIdpHost" ] ; then keytool  -importcert -file samlidp.crt -alias samlidp -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

keytool -list -keystore $keystoreLocation -storepass $keystorePassword
java -Djava.security.egd=file:/dev/./urandom -DkeystorePassword=$keystorePassword -jar app.war

exec $?