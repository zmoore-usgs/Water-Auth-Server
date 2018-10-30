Development SAML wildcard certificates generated via:

```
$ openssl genrsa -out saml-wildcard-dev.key 2048
$ openssl req -nodes -newkey rsa:2048 -keyout saml-wildcard-dev.key -out saml-wildcard-dev.csr -subj "/C=US/ST=Wisconsin/L=Middleon/O=US Geological Survey/OU=WMA/CN=*"
$ openssl x509 -req -days 9999 -in saml-wildcard-dev.csr -signkey saml-wildcard-dev.key -out saml-wildcard-dev.crt
```
