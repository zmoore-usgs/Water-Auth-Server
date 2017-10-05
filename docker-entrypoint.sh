#!/bin/sh
set -x

keytool -genkeypair -alias $keystoreDefaultKey -keyalg RSA -dname "CN=water-auth,OU=Unit,O=Organization,L=City,S=State,C=US" -keypass $keystorePassword -keystore $keystoreLocation -storepass $keystorePassword -noprompt
keytool -genkeypair -alias $keystoreTokenSigningKey -keyalg RSA -dname "CN=water-auth,OU=Unit,O=Organization,L=City,S=State,C=US" -keystore $keystoreLocation -storepass $keystorePassword -noprompt

if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

if [ -n "$samlIdpHost" ] ; then openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > saml.crt; fi
if [ -n "$samlIdpHost" ] ; then keytool  -importcert -file saml.crt -alias saml -keystore $keystoreLocation -storepass $keystorePassword -noprompt; fi

java -Djava.security.egd=file:/dev/./urandom -jar app.war

exec $?