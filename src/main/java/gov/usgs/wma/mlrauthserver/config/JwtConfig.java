package gov.usgs.wma.mlrauthserver.config;

import java.security.KeyPair;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

import gov.usgs.wma.mlrauthserver.dao.WaterAuthResourceIdAuthsDAO;
import gov.usgs.wma.mlrauthserver.util.ClasspathUtils;

@Configuration
@Profile("default")
public class JwtConfig {
	@Value("${security.jwt.key-store}")
	private String keystorePath;
	@Value("${security.jwt.key-store-oauth-key}")
	private String keystoreOAuthKey;
	@Value("${security.jwt.key-store-password}")
	private String keystorePassword;

	@Autowired
	TokenEnhancer tokenEnhancer;

	@Autowired
	WaterAuthResourceIdAuthsDAO authsDao;

	@Bean
	public KeyPair keyPair() {
		Resource storeFile = ClasspathUtils.loadFromFileOrClasspath(this.keystorePath);
		KeyStoreKeyFactory keyStoreKeyFactory =
				new KeyStoreKeyFactory(storeFile,this.keystorePassword.toCharArray());
		return keyStoreKeyFactory.getKeyPair(this.keystoreOAuthKey);
	}

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		

		WaterAuthJwtUserAuthConverter userConverter = new WaterAuthJwtUserAuthConverter();
		WaterAuthDBFilteredAccessTokenConverter tokenConverter = new WaterAuthDBFilteredAccessTokenConverter(this.authsDao);
		tokenConverter.setUserTokenConverter(userConverter);

		JwtAccessTokenConverter jwtConverter = new JwtAccessTokenConverter();
		jwtConverter.setKeyPair(keyPair());
		jwtConverter.setAccessTokenConverter(tokenConverter);
		
		return jwtConverter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(this.tokenEnhancer, accessTokenConverter()));
		defaultTokenServices.setTokenEnhancer(tokenEnhancerChain);

		return defaultTokenServices;
	}
}
