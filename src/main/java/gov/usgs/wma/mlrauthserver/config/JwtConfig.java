package gov.usgs.wma.mlrauthserver.config;

import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.security.oauth2.provider.token.DefaultTokenServices;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;

@Configuration
public class JwtConfig {

	@Value("${keystoreLocation}")
	private String keystorePath;
	@Value("${keystoreOAuthKey}")
	private String keystoreOAuthKey;
	@Value("${keystorePassword}")
	private String keystorePassword;

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
		
		JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
		KeyStoreKeyFactory keyStoreKeyFactory =
				new KeyStoreKeyFactory(storeFile,this.keystorePassword.toCharArray());
		converter.setKeyPair(keyStoreKeyFactory.getKeyPair(this.keystoreOAuthKey));
		
		return converter;
	}

	@Bean
	@Primary
	public DefaultTokenServices tokenServices() {
		DefaultTokenServices defaultTokenServices = new DefaultTokenServices();
		defaultTokenServices.setTokenStore(tokenStore());
		defaultTokenServices.setSupportRefreshToken(true);
		TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
		tokenEnhancerChain.setTokenEnhancers(Arrays.asList(tokenEnhancer(), accessTokenConverter()));
		defaultTokenServices.setTokenEnhancer(tokenEnhancerChain);
		
		return defaultTokenServices;
	}
	
	@Bean
	public TokenEnhancer tokenEnhancer() {
		return new WaterAuthJwtEnhancer();
	}
}
