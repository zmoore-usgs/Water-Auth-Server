#!/bin/bash
echo hello world
echo $PWD
echo $(ls)

#Generate Keystore
keytool -genkeypair -alias $keystore_deafult_alias -keyalg RSA -dname "CN=water-auth,OU=Unit,O=Organization,L=City,S=State,C=US" -keypass $keystore_pass -keystore $keystore_name -storepass $keystore_pass

#Import DOI Root Cert if applicable
if [ $use_doi_cert = true ] ; then curl -o root.crt http://sslhelp.doi.net/docs/DOIRootCA2.cer ; fi
if [ $use_doi_cert = true ] ; then keytool  -importcert -file root.crt -alias doi -keystore $keystore_name -storepass $keystore_pass -noprompt; fi

#Pull cert from SAML IDP URL and store it in the keystore
openssl s_client -host $samlIdpHost -port $samlIdpPort -prexit -showcerts </dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' > saml.crt
keytool  -importcert -file saml.crt -alias saml -keystore $keystore_name -storepass $keystore_pass -noprompt

#Run the application
java -Djava.security.egd=file:/dev/./urandom -jar app.war
exit $?
