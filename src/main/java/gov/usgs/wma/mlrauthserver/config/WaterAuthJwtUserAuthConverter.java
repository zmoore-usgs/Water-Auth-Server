package gov.usgs.wma.mlrauthserver.config;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import static gov.usgs.wma.mlrauthserver.config.WaterAuthJwtEnhancer.EMAIL_JWT_KEY;
import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;

public class WaterAuthJwtUserAuthConverter extends DefaultUserAuthenticationConverter {	
	@Override
	public Authentication extractAuthentication(Map<String, ?> map) {
		Authentication defaultAuth = super.extractAuthentication(map);
		String email = map.containsKey(EMAIL_JWT_KEY) ? (String)map.get(EMAIL_JWT_KEY) : "";
		WaterAuthUser user = new WaterAuthUser((String)defaultAuth.getPrincipal(), email, new ArrayList<GrantedAuthority>(defaultAuth.getAuthorities()));
		return new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities());
	}
}