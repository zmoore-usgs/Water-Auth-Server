package gov.usgs.wma.mlrauthserver.config;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.stereotype.Component;

@Component
public class WaterAuthJwtEnhancer implements TokenEnhancer {
	public static final String EMAIL_JWT_KEY = "email"; //User Email
	public static final String OFFICE_STATE_JWT_KEY = "office_state"; //User Office State

	@Override
	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
		WaterAuthUser user = (WaterAuthUser) authentication.getPrincipal();
		Map<String, Object> info = new HashMap<>();

		// Required Details
		info.put(EMAIL_JWT_KEY, user.getEmail());

		// Optional Details
		if(user.getDetails() != null) {
			info.put(OFFICE_STATE_JWT_KEY, user.getDetails().getOfficeState());
		}
		
		((DefaultOAuth2AccessToken) accessToken).setAdditionalInformation(info);

		return accessToken;
	}
}
