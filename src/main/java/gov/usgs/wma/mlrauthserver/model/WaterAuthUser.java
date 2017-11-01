package gov.usgs.wma.mlrauthserver.model;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@SuppressWarnings("serial")
public class WaterAuthUser implements UserDetails {
	private String username;
	private String email;
	private List<? extends GrantedAuthority> grantedAuthorities;
	
	public WaterAuthUser(String username, String email, List<? extends GrantedAuthority> grantedAuthorities) {
		super();
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

	public String getEmail() {
		return email;
	}
}
