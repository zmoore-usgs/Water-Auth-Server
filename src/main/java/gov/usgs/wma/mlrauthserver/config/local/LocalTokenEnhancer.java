package gov.usgs.wma.mlrauthserver.config.local;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.stereotype.Component;

import gov.usgs.wma.mlrauthserver.config.WaterAuthJwtEnhancer;

@Component
@Profile("localDev")
public class LocalTokenEnhancer extends WaterAuthJwtEnhancer {

	@Value("${security.user.email}")
	private String userEmail;

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		Map<String, Object> info = new HashMap<>();

		info.put(EMAIL_JWT_KEY, userEmail);

		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);

		return accessToken;
	}

}
