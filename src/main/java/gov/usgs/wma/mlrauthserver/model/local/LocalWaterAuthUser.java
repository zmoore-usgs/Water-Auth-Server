package gov.usgs.wma.mlrauthserver.model.local;

import java.util.List;

import org.springframework.security.core.GrantedAuthority;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import gov.usgs.wma.mlrauthserver.model.WaterAuthUserDetails;

@SuppressWarnings("serial")
public class LocalWaterAuthUser extends WaterAuthUser {
	private String password;

	public LocalWaterAuthUser(String username, String password, String email, List<? extends GrantedAuthority> grantedAuthorities, WaterAuthUserDetails details) {
		super(username, email, grantedAuthorities, details);
		this.password = password;
	}

	@Override
	public String getPassword() {
		return password;
	}
}