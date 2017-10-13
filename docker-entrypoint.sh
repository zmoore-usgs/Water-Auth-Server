#!/bin/sh
set -x

keystorePassword = `cat /run/secrets/waterauthserver_KEYSTORE_PASSWORD`
certFileLocation = '/run/secrets/waterauthserver_WATER_AUTH_KEYS'

keytool -v -importkeystore -srckeystore $certFileLocation -srcstoretype PKCS12 -srcstorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype JKS -deststorepass $keystorePassword -noprompt

if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

if [ -n "$samlIdpHost" ] ; then openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > samlidp.crt; fi
if [ -n "$samlIdpHost" ] ; then keytool  -importcert -file samlidp.crt -alias samlidp -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

java -Djava.security.egd=file:/dev/./urandom -jar app.war

exec $?