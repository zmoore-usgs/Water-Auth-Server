Development Oauth2 wildcard certificates generated via:

```
$ openssl genrsa -out oauth-wildcard-dev.key 2048
$ openssl req -nodes -newkey rsa:2048 -keyout oauth-wildcard-dev.key -out oauth-wildcard-dev.csr -subj "/C=US/ST=Wisconsin/L=Middleon/O=US Geological Survey/OU=WMA/CN=*"
$ openssl x509 -req -days 9999 -in oauth-wildcard-dev.csr -signkey oauth-wildcard-dev.key -out oauth-wildcard-dev.crt
```
