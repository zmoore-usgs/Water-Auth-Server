package gov.usgs.wma.mlrauthserver.service;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SAMLUserDetailsImpl implements SAMLUserDetailsService  {
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(SAMLUserDetailsImpl.class);
	
	@Override
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		String userID = credential.getNameID().getValue();
		LOG.info(userID + " logged in.");
						
		return new User(userID, "", true, true, true, true, addAuthorities(credential));
	}
	
	//Generate authorities based on saml assertions
	private List<GrantedAuthority> addAuthorities(SAMLCredential credential) {
		List<GrantedAuthority> authorityList = new ArrayList<>();
		
		return authorityList;
	}
}
