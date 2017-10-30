package gov.usgs.wma.mlrauthserver.config;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import java.util.HashMap;
import java.util.Map;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class WaterAuthJwtEnhancer implements TokenEnhancer {
	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken,
			OAuth2Authentication authentication) {

		WaterAuthUser user = (WaterAuthUser) authentication.getPrincipal();
		Map<String, Object> info = new HashMap<>();

		info.put(WaterAuthUser.USER_ID_JWT_KEY, user.getUserId());
		info.put(WaterAuthUser.USERNAME_JWT_KEY, user.getUsername());
		info.put(WaterAuthUser.EMAIL_JWT_KEY, user.getEmail());

		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
		
		return accessToken;
	}
}
