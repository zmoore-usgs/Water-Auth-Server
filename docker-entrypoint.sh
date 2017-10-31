#!/bin/sh
set -x

keystorePassword=`cat $KEYSTORE_PASSWORD_FILE`

openssl pkcs12 -export -in $waterauthserver_TOKEN_CERT_path -inkey $waterauthserver_TOKEN_KEY_path -name $keystoreTokenSigningKey -out oauth.p12 -password pass:$keystorePassword
openssl pkcs12 -export -in $waterauthserver_SAML_CERT_path -inkey $waterauthserver_SAML_KEY_path -name $keystoreDefaultKey -out saml.p12 -password pass:$keystorePassword
openssl pkcs12 -export -in $waterauthserver_TOMCAT_CERT_path -inkey $waterauthserver_TOMCAT_KEY_path -name tomcat -out tomcat.p12 -password pass:$keystorePassword

keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype JKS -srckeystore oauth.p12 -srcstorepass $keystorePassword -srcstoretype PKCS12 --noprompt
keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype JKS -srckeystore saml.p12 -srcstorepass $keystorePassword -srcstoretype PKCS12 --noprompt
keytool -v -importkeystore -deststorepass $keystorePassword -destkeystore $keystoreLocation -deststoretype JKS -srckeystore tomcat.p12 -srcstorepass $keystorePassword -srcstoretype PKCS12 --noprompt

if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

if [ -n "$samlIdpHost" ] ; then openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > samlidp.crt; fi
if [ -n "$samlIdpHost" ] ; then keytool  -importcert -file samlidp.crt -alias samlidp -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

java -Djava.security.egd=file:/dev/./urandom -DkeystorePassword=$keystorePassword -jar app.jar

exec $?
