package gov.usgs.wma.mlrauthserver.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@Profile("default")
public class AuthServerSecurityConfig extends AuthorizationServerSecurityConfiguration {

	@Autowired
	@Qualifier("samlEntryPoint")
	private AuthenticationEntryPoint samlEntryPoint;

	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
		oauthServer
			.authenticationEntryPoint(samlEntryPoint)
			.tokenKeyAccess("permitAll()")
			.checkTokenAccess("isAuthenticated()");
	}

    @Override
	protected void configure(HttpSecurity http) throws Exception {
		super.configure(http);
		http
			.requestMatchers()
				.mvcMatchers("/oauth/jwks.json")
				.and()
			.authorizeRequests()
				.mvcMatchers("/oauth/jwks.json").permitAll();
	}
}