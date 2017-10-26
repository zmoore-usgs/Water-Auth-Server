package gov.usgs.wma.mlrauthserver.model;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class WaterAuthUser implements UserDetails {
	private String username;
	private String email;
	private String userId;
	private List<GrantedAuthority> grantedAuthorities;
	private Map<String, List<String>> rawAttributes;
	
	public WaterAuthUser(String userId, String username, String email, Map<String, List<String>> rawAttributes, List<GrantedAuthority> grantedAuthorities) {
		super();
		this.userId = userId;
		this.username = username;
		this.email = email;
		this.rawAttributes = rawAttributes;
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
	
	public Map<String, List<String>> getRawAttributes() {
		return rawAttributes;
	}
}
