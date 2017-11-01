# Water-Auth-Server
## Running Locally
### Specifying Environment Variable Values
The application.yml file in `src/main/resources` should be copied to the project root directory and then the values can be modified to fit your local configuration.

### Creating a Keystore
As further disucssed in the `Keystore` section below this application uses a keystore and several different keys for various parts of its operation. In order to run this application locally a keystore will need to be provided. This keystore should contain 3 keys:
- A key for the embedded tomcat server to serve over https
    - Defined in environment variable: `keystoreSSLKey` 
        - Default value is: `tomcat`
- A key for signing requests made to the SAML server
    - Defined in environment variable: `keystoreSAMLKey`
- A key for signing Oauth2 tokens to be cerated and passed to client services
    - Defined in environment variable: `keystoreTokenSigningKey`

    Note that the second and third keys are not necessary when using the `localDev` profile discussed below.

    Also note that when creating the SSL Key you **must** set the `CN` attribute of the key to be equal to the domain that the service will be running on. When creating this key to be run locally the `CN` should be `localhost`. This is discussed further below.

Using the Java program `keytool` a keystore can be created and keys can be added to it using the following command where MY_ALIAS and MY_KEYSTORE are replaced with values of your choice:

```
keytool -genkey -keyalg RSA -alias MY_ALIAS -keystore MY_KEYSTORE.jks 
```

Running this command will bring up an interactive wizard to create your keystore (if it doesn't exist already) and the associated key.

When creating the key several prompts will come up asking for various pieces of information. The first prompt asking for your first and last name is actually the prompt for the `CN` value of the key and this is where you should enter the host that the service will be running on (Likely `localhost` or a docker IP for local development) when you are creating the SSL key. The additional prompt values can be left as blank and the first prompt can be left blank on all keys except for the SSL key.

The final step  of the wizard will ask if the key should have a password. This should be left blank so that the key will use the same password sa the keystore itself.

When running a client application that needs to access a locally running version of the Gateway this same keystore can be used in that client. Alternatively you could also create a new keystore and then visit the water auth server homepage in your browser, export the served certificate, and import that into your new keystore. This new keystore could then be used for the client application.

To specify the store that your client application should use (if that cleint is a Java Spring project) include the following command line arguemnts in your java call to launch the client application:

```
-Djavax.net.ssl.trustStore=<full path to your keystore>
-Djavax.net.ssl.trustStorePassword=<password for your keystore>
```

### localDev Profile
This application has two built-in Spring Profiles to aid with local development. 

The default profile, which is run when either no profile is provided or the profile name "default" is provided, is setup to require all external dependeices (i.e: Functioning SAML for Authentication and a database for Session and Client storage with at least one client configured).

The localDev profile, which is run when the profile name "localDev" is provided, is setup to require no external dependies - Sessions are stored internally, SAML Authentication is disabled, and a default client (client-id: "nwis") is configured within the application.

## Spring Security Client and Session Storage
This service by default stores session data within a database rather than within the application itself. This allows for multiple running instances of this service to share session information, thereby making the service stateless. Similarly, this service by default also stores Spring Security Ouath2 client information within a database rather than within the application itself. 

When the application first starts it attempts to connect to the configured MySQL database and run a set of initialization scripts to create the relevant tables if they don't already exist

The related environment variables are listed below:

- **dbConnectionUrl** - The full JDBC-qualified database URL that the application should connect to. Example: jdbc:mysql://192.168.99.100/mydb

- **dbUsername** - The username that should be used by the application when connecting to the database.

- **dbPassword** - The password that should be used by the application when connecting to the database.

- **dbInitializerEnabled** - Whether or not the database initialization scripts should run on application startup. The default value is true.

## SAML Configuration Environment Variables
### SAML IDP Metadata
This service can consume the metadata from a SAML Idnetity Provider either from a local file or from an HTTP URL.

- **samlIdpMetadataLocation** - The local file path or http(s) URL location of the SAML IDP Server metadata xml. The file can be loaded from the classpath by prepending "classpath:" to the start of the file path.

### SAML IDP AuthNRequest Configuration
The authentication request sent from the application to the SAML IDP server can be customized using the properties below. entityID must always be specified, however providerName is optional.

- **samlAuthnRequestEntityId** - The entity ID to include in AuthNRequests. This value is is also used in the issuer field of the request.

- **samlAuthnRequestProviderName** - [Default: *empty*] The provider name to specify for the request. This value is optional.

### SAML Attribute Mapping
In addition to logging the user in through SAML this service also acts as an Oauth2 Authorization Server and converts some of the returned SAML assertions into Oauth2 authorizations. The following environment variables are used to do this mapping.

- **samlGroupAttributeName** - The SAML attribute key that corresponds to the groups that the user is. The values found attached to this key will be converted into authorizations in the Oauth2 token. The default value is `http://schemas.xmlsoap.org/claims/Group`

- **samlEmailAttributeName** - The SAML attribute key that corresponds to the email address of the logged-in user. This value will be contained in the Oauth2 requests and JWTs as "email" in the JWT and within the Oauth2 Request Extensions. The default value is `http://schemas.xmlsoap.org/ws/2005/05/identity/claims/emailaddress`

- **samlUsernameAttributeName** - The SAML attribute key that corresponds to the username of the logged-in user. This value will be used as the principal value and name in the Spring Security Context and will be contained in the JWTs as "user_name". The default value is `http://schemas.microsoft.com/ws/2008/06/identity/claims/windowsaccountname`

### Keystore
A keystore is used by the SAML service to ensure secure communication with the IDP server. Your keystore should contain the certs used on the IDP server as well as any certs required to reach your metadata XML if it is being supplied via http. The same keystore is also used by the Oauth2 portion of the application in order to sign JWT tokens.

- **keystoreLocation** - The file path to the keystore to be used for saml. The file can be loaded from the classpath by prepending "classpath:" to the start of the file path.

- **keystorePassword** - The password used to access the keystore.

- **keystoreSAMLKey** - The key to use for signing requests made to the SAML server.

- **keystoreSSLKey** - The key to serve from the service when SSL is enabled.

- **keystoreTokenSigningKey** - The key alias to use for singing Oauth2 tokens. Note that this key alias **must** be different from the default key alias when running this application in Docker using the provided Dockerfile.

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

## Docker

This project is configured to work with docker. Each relevant environemnt variable should be converted from the `application.yml` file to a `--env` command on a new docker service. The built JAR will be fetched from the MLR Artifactory when the image is built. The version of the JAR to grab must be passed as a build argument with: `mlr_version`

### Keystore

The dockerfile does not include automatic keystore generation, rather a pre-built keystore should be passed in using a docker secret or docker config. The mounted location of this keystore should then be passed in through the `keystoreLocation` environment variable. During the container startup the keystore will be modified to include the public cert of the IDP server in order to allow metadata pulling. The IDP cert properties are listed below.

- **samlIdpHost** - The host of the SAML IDP server that you are connecting to

- **samlIdpPort** - The port of the SAML IDP server that you are connecting to. If the metadata is being served through abrowser from an https url then this is likely 443.

### DOI Root Cert

If you are using an http location for your SAML IDP metadata and you are working from within the USGS network you will also need to inject the DOI root cert. This cert can be added by setting the following environemnt variable to true:

- **use_doi_cert** - [Default: false] Whether or not to include the DOI Root Cert in the keystore for the application.