package gov.usgs.wma.mlrauthserver.config;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Profile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerEndpointsConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import org.springframework.security.oauth2.provider.code.JdbcAuthorizationCodeServices;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;

@Import(AuthorizationServerEndpointsConfiguration.class)
@Configuration
@Profile("default")
public class AuthServerConfig extends AuthorizationServerConfigurerAdapter {

	@Autowired
	@Qualifier("authenticationManagerBean")
	private AuthenticationManager authenticationManager;
	
	@Autowired
	private DataSource dataSource;

	@Autowired
	private TokenStore tokenStore;
	@Autowired
	private JwtAccessTokenConverter accessTokenConverter;
	@Autowired
	private DefaultTokenServices tokenServices;
	
	@Bean
	public AuthorizationCodeServices authorizationCodeServices() {
		return new JdbcAuthorizationCodeServices(dataSource);
	}

	@Override
	public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
		clients.jdbc(dataSource);
	}

	@Override
	public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
		endpoints
		.authenticationManager(authenticationManager)
		.authorizationCodeServices(authorizationCodeServices())
		.tokenServices(tokenServices)
		.tokenStore(tokenStore)
		.accessTokenConverter(accessTokenConverter);
	}
}