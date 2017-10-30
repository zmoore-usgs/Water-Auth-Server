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

		info.put("saml_username", user.getUsername());
		info.put("saml_email", user.getEmail());
		info.put("saml_user_id", user.getUserId());

		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);
		
		return accessToken;
	}
}
