package gov.usgs.wma.mlrauthserver.config.local;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.AuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@Profile("localDev")
public class LocalSecurityConfig extends WebSecurityConfigurerAdapter {

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.anonymous().disable()
			.requestMatchers().antMatchers("/login", "/oauth/authorize")
			.and()
				.authorizeRequests().anyRequest().authenticated()
			.and()
				.formLogin().permitAll()
			;
	}

	@Autowired
	AuthenticationEntryPoint authenticationEntryPoint;

	@Bean
	public AuthenticationEntryPoint samlEntryPoint() {
		return authenticationEntryPoint;
	}

}
