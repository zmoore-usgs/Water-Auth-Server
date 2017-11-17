package gov.usgs.wma.mlrauthserver.service;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import gov.usgs.wma.mlrauthserver.util.SAMLUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SAMLUserDetailsImpl implements SAMLUserDetailsService  {
	private static final Logger LOG = LoggerFactory.getLogger(SAMLUserDetailsImpl.class);

	@Value("${security.saml.attribute-name.group}")
	private String samlGroupAttributeName;
	@Value("${security.saml.attribute-name.email}")
	private String samlEmailAttributeName;
	@Value("${security.saml.attribute-name.username}")
	private String samlUsernameAttributeName;

	@Override
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		Map<String, List<String>> attributeMap = SAMLUtils.getAttributeValueMap(credential);
		
		String email = attributeMap.get(samlEmailAttributeName).get(0);
		String username = attributeMap.get(samlUsernameAttributeName).get(0);
				
		LOG.info(username + " (" + email + ") logged in.");
						
		return new WaterAuthUser( username, email, addAuthorities(credential));
	}

	//Generate authorities based on saml assertions
	private List<GrantedAuthority> addAuthorities(SAMLCredential credential) {
		List<GrantedAuthority> authorityList = new ArrayList<>();
		
		if(samlGroupAttributeName != null && samlGroupAttributeName.length() > 0) {
			Map<String, List<String>> attributeMap = SAMLUtils.getAttributeValueMap(credential);
			
			List<String> groupList = attributeMap.get(samlGroupAttributeName);
			
			if(groupList != null){
				for(String group : groupList){
					authorityList.add(new SimpleGrantedAuthority(group));
				}
			}
		}
		return authorityList;
	}
}
