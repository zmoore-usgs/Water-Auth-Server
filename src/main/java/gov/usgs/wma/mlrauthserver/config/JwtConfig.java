package gov.usgs.wma.mlrauthserver.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.DefaultAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
public class JwtConfig {

	@Value("${security.jwt.key-store}")
	private String keystorePath;
	@Value("${security.jwt.key-store-oauth-key}")
	private String keystoreOAuthKey;
	@Value("${security.jwt.key-store-password}")
	private String keystorePassword;

	@Autowired
	TokenEnhancer tokenEnhancer;

	@Bean
	public TokenStore tokenStore() {
		return new JwtTokenStore(accessTokenConverter());
	}

	@Bean
	public JwtAccessTokenConverter accessTokenConverter() {
		Resource storeFile;

		if(this.keystorePath.toLowerCase().startsWith("classpath:")){
			DefaultResourceLoader loader = new DefaultResourceLoader();
			String classpathLocation = this.keystorePath.replaceFirst("classpath:", "");
			storeFile = loader.getResource(classpathLocation);
		} else {
			FileSystemResourceLoader loader = new FileSystemResourceLoader();
			storeFile = loader.getResource(this.keystorePath);
		}
		KeyStoreKeyFactory keyStoreKeyFactory =
				new KeyStoreKeyFactory(storeFile,this.keystorePassword.toCharArray());

		WaterAuthJwtUserAuthConverter userConverter = new WaterAuthJwtUserAuthConverter();
		DefaultAccessTokenConverter tokenConverter = new DefaultAccessTokenConverter();
		tokenConverter.setUserTokenConverter(userConverter);

		JwtAccessTokenConverter jwtConverter = new JwtAccessTokenConverter();
		jwtConverter.setKeyPair(keyStoreKeyFactory.getKeyPair(this.keystoreOAuthKey));
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
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer, accessTokenConverter()));
		defaultTokenServices.setTokenEnhancer(tokenEnhancerChain);

		return defaultTokenServices;
	}
}
