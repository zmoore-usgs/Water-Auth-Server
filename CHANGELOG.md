# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](http://keepachangelog.com/en/1.0.0/)
and this project adheres to [Semantic Versioning](http://semver.org/spec/v2.0.0.html).

## [Unreleased]
### Updated
- kmschoep@usgs.gov - use version 0.0.4 wma-spring-boot-base docker image
- isuftin@usgs.gov - Using Spring Boot 1.5.9-RELEASE
- isuftin@usgs.gov - Parameterized driverClassName in PersistenceConfig to allow for H2 driver
during unit testing. Defaulting to com.mysql.jdbc.Driver

### Removed
- isuftin@usgs.gov - Dockerfile and associate configuration. Now in https://github.com/USGS-CIDA/docker-water-auth-server

### Added
- isuftin@usgs.gov - Travis markdown 
- isuftin@usgs.gov - JwtConfigTest jUnit test using in-memory database  
- isuftin@usgs.gov - PersistenceConfigTest jUnit test using in-memory database  
- isuftin@usgs.gov - H2 maven dependency in test scope
- isuftin@usgs.gov - TravisCI testing
- Oauth2 Authorization Codes are persisted in a JDBC database in order to allow for horizontal scaling with multiple instances of Water Auth.

## [0.1.0] - 2017-11-20

### Added
- Spring Security SAML Authentication configured to properly work with DOI SAML.
    - Authentication Endpoints: `/saml/login`, `/saml/logout/`, `/saml/gloabllogout/`, `/saml/metadata/`

- Spring Security OAuth2 Authorization with Spring Security JWT Token Generation
    - Authorization Endpoints: `/oauth/token`, `/oauth/authorize`, `/oauth/token_key`

- Sessions can be optionally persisted in a JDBC database in order to allow horizontal scaling.

- OAuth2 Clients are persisted in a JDBC database in order to allow for horizontal scaling and on-demand client modifications, removals, and additions.

- Internal and toggleable MySQL initialization scripts that can create the Session and OAuth2 Client tables in the connected MySQL database if they do not already exist.

- Toggleable Spring Profiles to siwtch between local development and server deployment. The local development profile disables the need for SAML and a database for storing sessions and OAuth2 clients.

- Simplistic default pages for various default authentication redirect endpoints. These deafult endpoints are only used when no other redirect URLs are provided (such as by an OAuth2 request).
    - Default Endpoints: `/` (Login Success), `/out` (Logout Success), `/auth-error` (Error during Authentication Process)

- Configurable mapping of key values from the SAML Response to the generated OAuth2 JWT Token
    - OAuth2 Authroizations: Maps SAML Assertion values with the key `$samlGroupAttributeNames` to the `authorizations` list of the OAuth2 token.
    - Principal name: Maps the SAML Assertion value with the key `$samlUsernameAttributeName` to the principal name of the Spring Security context and the `user_name` field of the JWT token.
    - User Email: Maps the SAML Assertion value with the key `$samlEmailAttributeName` to the `email` field of the JWT token and an `email` extension within the OAuth2 Spring Security context.

- Docker support for passing in cert and key files as Docker Secrets and using those to populate the keystore used by the application.

- Global exception handler for Http requests


[0.1.0]: https://github.com/USGS-CIDA/Water-Auth-Server/compare/waterauthserver-0.1.0...master

[Unreleased]: https://github.com/USGS-CIDA/Water-Auth-Server/tree/master
 