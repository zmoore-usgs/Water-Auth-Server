package gov.usgs.wma.mlrauthserver.config;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.provider.token.DefaultUserAuthenticationConverter;

import static gov.usgs.wma.mlrauthserver.config.WaterAuthJwtEnhancer.EMAIL_JWT_KEY;
import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;

public class WaterAuthJwtUserAuthConverter extends DefaultUserAuthenticationConverter {
	private static final Logger LOG = LoggerFactory.getLogger(WaterAuthJwtUserAuthConverter.class);

	@Override
	public Authentication extractAuthentication(Map<String, ?> map) {
		Authentication defaultAuth = super.extractAuthentication(map);

		if(defaultAuth != null) {
			String email = "";
			String principal = "";
			List<GrantedAuthority> authorities = new ArrayList<>();

			try {
				email = map.containsKey(EMAIL_JWT_KEY) ? (String)map.get(EMAIL_JWT_KEY) : "";
				principal = defaultAuth.getPrincipal().toString();
				authorities = new ArrayList<>(defaultAuth.getAuthorities());
			} catch (Exception e) {
				LOG.error("Failed to convert recieved JWT token to a Water Auth User. Error: " + e.getMessage());
				throw new InvalidTokenException("Failed to convert recieved JWT token to a Water Auth User.");
			}

			if(!email.isEmpty() && !principal.isEmpty()) {
				WaterAuthUser user = new WaterAuthUser(principal, email, authorities);
				return new UsernamePasswordAuthenticationToken(user, "N/A", user.getAuthorities());
			} else {
				LOG.error("Failed to convert recieved JWT token to a Water Auth User. Error: Username or Email not present in the token.");
				throw new InvalidTokenException("Failed to convert recieved JWT token to a Water Auth User.");
			}
		} else {
			LOG.error("Failed to convert recieved JWT token to a Water Auth User. Error: Provided JWT JSON could not be parsed or was empty.");
			throw new InvalidTokenException("Failed to convert recieved JWT token to a Water Auth User.");
		}
	}
}