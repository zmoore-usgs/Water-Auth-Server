package gov.usgs.wma.mlrauthserver.model;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class WaterAuthUser implements UserDetails {
	public static final String USERNAME_JWT_KEY = "saml_username";
	public static final String EMAIL_JWT_KEY = "saml_email";
	public static final String USER_ID_JWT_KEY = "saml_user_id";
	private String username;
	private String email;
	private String userId;
	private List<? extends GrantedAuthority> grantedAuthorities;
	
	public WaterAuthUser(String userId, String username, String email, List<? extends GrantedAuthority> grantedAuthorities) {
		super();
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.grantedAuthorities = grantedAuthorities;
	}
	
	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return grantedAuthorities;
	}

	@Override
	public String getPassword() {
		return null;
	}

	@Override
	public String getUsername() {
		return username;
	}
	
	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}
	
	public String getUserId() {
		return userId;
	}

	public String getEmail() {
		return email;
	}
}
