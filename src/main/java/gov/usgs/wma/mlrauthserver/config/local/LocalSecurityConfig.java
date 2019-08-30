package gov.usgs.wma.mlrauthserver.config.local;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.AuthenticationEntryPoint;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUserDetails;
import gov.usgs.wma.mlrauthserver.model.local.LocalWaterAuthUser;

@Configuration
@EnableWebSecurity
@Profile("localDev")
public class LocalSecurityConfig extends WebSecurityConfigurerAdapter {
	@Value("${security.water-auth-user.username}")
	private String localUsername;
	@Value("${security.water-auth-user.password}")
	private String localPassword;
	@Value("${security.water-auth-user.email}")
	private String localEmail;
	@Value("${security.water-auth-user.role}")
	private String[] localRoles;
	@Value("${security.water-auth-user.details.office-state}")
	private String localOfficeState;

	@Autowired
	AuthenticationEntryPoint authenticationEntryPoint;

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http
			.anonymous().disable()
			.authorizeRequests()
				.anyRequest().fullyAuthenticated()
			.and()
				.formLogin().permitAll()
			;
	}

	@Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        UserDetailsManager userDetailsManager = userDetailsManager();
        auth.userDetailsService(userDetailsManager);

        userDetailsManager.createUser(user());
    }

	@Bean
	@Primary
	public LocalWaterAuthUser user() {
		List<GrantedAuthority> userRoles = new ArrayList<>();

		for(String role : localRoles) {
			userRoles.add(new SimpleGrantedAuthority(role));
		}

		WaterAuthUserDetails userDetails = new WaterAuthUserDetails(
			localOfficeState
		);

		return new LocalWaterAuthUser(localUsername, localPassword, localEmail, userRoles, userDetails);
	}

	@Bean
    public UserDetailsManager userDetailsManager() {
        return new LocalInMemoryWaterAuthUserDetailsManager();
    }

	@Bean
	public AuthenticationEntryPoint samlEntryPoint() {
		return authenticationEntryPoint;
	}

}
