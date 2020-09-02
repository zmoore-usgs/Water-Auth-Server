package gov.usgs.wma.mlrauthserver.config.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.code.InMemoryAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Import(AuthorizationServerEndpointsConfiguration.class)
@Configuration
@Profile("localDev")
public class LocalAuthServerConfig extends AuthorizationServerConfigurerAdapter {
	@Autowired
	private AuthenticationManager authenticationManager;
	@Autowired
	private TokenStore tokenStore;
	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;
	@Autowired
	private DefaultTokenServices tokenServices;

	@Value("${security.oauth2.client.clientId}")
	private String localClient;
	@Value("${security.oauth2.client.clientSecret}")
	private String localSecret;
	@Value("${security.oauth2.client.grantTypes}")
	private String[] localGrantTypes;
	@Value("${security.oauth2.client.redirectUris}")
	private String[] redirectUris;
	@Value("${security.oauth2.client.scopes}")
	private String[] localScopes;
	@Value("${security.oauth2.resource.id}")
	private String localResourceId;

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.inMemory()
			.withClient(localClient)
			.redirectUris(redirectUris)
			.resourceIds(localResourceId)
			.authorizedGrantTypes(localGrantTypes)
			.scopes(localScopes)
			.secret(localSecret)
			.autoApprove(true);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints
			.authenticationManager(authenticationManager)
			.authorizationCodeServices(new InMemoryAuthorizationCodeServices())
			.tokenServices(tokenServices)
			.tokenStore(tokenStore)
			.accessTokenConverter(accessTokenConverter);
	}
}