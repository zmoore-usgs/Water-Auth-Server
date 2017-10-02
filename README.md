# Water-Auth-Server
## SAML Configuration Environment Variables
### SAML IDP Metadata
This service can consume the metadata from a SAML Idnetity Provider either from a local file or from an HTTP URL.

- **samlIDPMetadataLocation** - The local file path or http(s) URL location of the SAML IDP Server metadata xml. The file can be loaded from the classpath by prepending "classpath:" to the start of the file path.

### SAML IDP AuthNRequest Configuration
The authentication request sent from the application to the SAML IDP server can be customized using the properties below. entityID must always be specified, however providerName is optional.

- **samlAuthnRequestEntityId** - The entity ID to include in AuthNRequests. This value is is also used in the issuer field of the request.

- **samlAuthnRequestProviderName** - [Default: *empty*] The provider name to specify for the request. This value is optional.

### SAML Keystore
A keystore is used by the SAML service to ensure secure communication with the IDP server. Your keystore should contain the certs used on the IDP server as well as any certs required to reach your metadata XML if it is being supplied via http. 

- **samlKeystoreLocation** - The file path to the keystore to be used for saml. The file can be loaded from the classpath by prepending "classpath:" to the start of the file path.

- **samlKeystoreDefaultKey** - The default key of the keystore.

- **samlKeystorePassword** - The password used to access the keystore.

### SAML Login/Logout URL Routing
The login success and error as well as logout success URLs can be customized. These are the URLs that the client will be redirected to upon logging in or out, based on the status of that action.

- **loginSuccessTargetUrl** - The URL to redirect the client to upon a successful login.

- **loginErrorTargetUrl** - The URL to redirect the client to upon a failed login.

- **logoutSuccessTargetUrl** - The URL to redirect the client to upon logging out.

### SAML Access URL Overrides
The URLs that the application maps to various SAML action endpoints can be customized if desired, though all of these variables have default values. All values in this section should being with a forward slash ( / ) and end **without** a trailing slash.

These URLs are primarily used internally by the Spring Authentication Manager, rather than by clients themselves.

- **samlBaseEndpoint** - [Default: /saml] The base endpoint to append to the front of all sub-endpoints. This is also used for the security configuration to allow anonymous connections within all sub-endpoints of this endpoint.

- **samlLoginEndpoint** - [Default: /login] The login sub-endpoint to use for logging users in.

- **samlLogoutEndpoint** - [Default: /logout] The logout sub-endpoint to use for logging users out.

- **samlSingleLogoutEndpoint** - [Default: /singlelogout] The single logout sub-endpoint to use for logging users out of the application but **not** out of the IDP server.

- **samlSSOEndpoint** - [Default: /sso] The SSO sub-endpoint to use for handling single-sign-on Spring Security Context creation. 

- **samlSSOHOKEndpoint** - [Default: /ssohok] The SSO HoK (Holder-of-Key) sub-endpoint to use for handling single-sign-on Spring Security Context creation when the user is a holder of key.

- **samlMetadataEndpoint** - [Default: /metadata] The metadata sub-endpoint to use for retrieving the metadata for the application which can be loaded into the IDP server or used to verify that the other SAML settings have been applied properly.

## Running Locally

The application.yml file in `src/main/resources` should be copied to the project root directory and then the values can be modified to fit your local configuration.

## Docker

This project is configured to work with docker. Each relevant environemnt variable should be converted from the `application.yml` file to a `--env` command on a new docker service. The built JAR will be fetched from the MLR Artifactory when the image is built. The version of the JAR to grab must be passed as a build argument with: `mlr_version`

### Docker Keystore Generation

The dockerfile is configured to automatically generate a keystore for the deployed service to use. The parameters of the generated keystore can be modified using the following docker build arguments:

- **keystore_pass** - [Default: changeit] The password to use for the keystore as well as the default key.

- **keystore_name** - [Default: keystore.jks] The name to give the generated keystore. This name is used literally, meaning if no extension is supplied the generated keystore will have no extension.

- **keystore_default_key** - [Default: default] The default key alias to generate along with the keystore.