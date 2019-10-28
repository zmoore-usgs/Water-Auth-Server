package gov.usgs.wma.mlrauthserver.model;

import java.util.Collection;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

public class WaterAuthUser implements UserDetails {
	private static final long serialVersionUID = 1L;
	protected String username;
	protected String email;
	protected List<? extends GrantedAuthority> grantedAuthorities;
	protected WaterAuthUserDetails details;

	public WaterAuthUser(String username, String email, List<? extends GrantedAuthority> grantedAuthorities, WaterAuthUserDetails details) {
		super();
		this.username = username;
		this.email = email;
		this.grantedAuthorities = grantedAuthorities;
		this.details = details;
	}

	public Collection<? extends GrantedAuthority> getAuthorities() {
		return grantedAuthorities;
	}

	public String getPassword() {
		return null;
	}

	public String getUsername() {
		return username;
	}

	public String getOfficeState() {
		return this.details != null ? this.details.getOfficeState() : null;
	}
	
	public boolean isAccountNonExpired() {
		return true;
	}

	public boolean isAccountNonLocked() {
		return true;
	}

	public boolean isCredentialsNonExpired() {
		return true;
	}

	public boolean isEnabled() {
		return true;
	}

	public String getEmail() {
		return email;
	}
}
