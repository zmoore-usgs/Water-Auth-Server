package gov.usgs.wma.mlrauthserver.config.local;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerSecurityConfiguration;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;

@Configuration
@Profile("localDev")
public class LocalAuthServerSecurityConfig extends AuthorizationServerSecurityConfiguration {
	
	@Override
	public void configure(AuthorizationServerSecurityConfigurer oauthServer) throws Exception {
		oauthServer
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