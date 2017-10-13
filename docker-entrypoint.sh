#!/bin/sh
set -x

set keystorePassword = `cat $KEYSTORE_PASSWORD_FILE`

keytool -v -importkeystore -srckeystore $WATER_AUTH_KEYS_FILE -srcstoretype PKCS12 -srcstorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype JKS -deststorepass $keystorePassword -noprompt

echo _________ Finished loading certs from PKCS12 _________

if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

if [ -n "$samlIdpHost" ] ; then openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > samlidp.crt; fi
if [ -n "$samlIdpHost" ] ; then keytool  -importcert -file samlidp.crt -alias samlidp -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

echo _________ Finished adding cert from $samlIdpHost _________

keytool -list -keystore $keystoreLocation -storepass $keystorePassword

echo _________ Launching Spring Application _________

java -Djava.security.egd=file:/dev/./urandom -jar app.war

exec $?