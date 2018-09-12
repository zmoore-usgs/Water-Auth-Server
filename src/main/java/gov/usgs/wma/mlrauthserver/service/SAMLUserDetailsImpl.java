package gov.usgs.wma.mlrauthserver.service;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import gov.usgs.wma.mlrauthserver.util.SAMLUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
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

	@NotEmpty
	@Value("${security.saml.attribute-names.group}")
	private String[] samlGroupAttributeNames;

	@NotEmpty
	@Value("${security.saml.attribute-names.email}")
	private String[] samlEmailAttributeNames;

	@NotEmpty
	@Value("${security.saml.attribute-names.username}")
	private String[] samlUsernameAttributeNames;

	@Value("${security.saml.include-groups:}")
	private String[] includeGroups;

	@Override
	public Object loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		Map<String, List<String>> attributeMap = SAMLUtils.getAttributeValueMap(credential);

		try {
			String email = SAMLUtils.getFirstMatchingAttributeValue(attributeMap, this.samlEmailAttributeNames).get(0);
			String username = SAMLUtils.getFirstMatchingAttributeValue(attributeMap, this.samlUsernameAttributeNames).get(0);
			LOG.debug(username + " (" + email + ") logged in.");
			return new WaterAuthUser(username, email, addAuthorities(attributeMap));
		} catch(RuntimeException e) {
			throw e;
		}
	}

	//Generate authorities based on saml assertions
	protected List<GrantedAuthority> addAuthorities(Map<String, List<String>> attributeMap) {
		List<GrantedAuthority> authorityList = new ArrayList<>();
		List<String> groupList = null;

		try {
			groupList = SAMLUtils.getFirstMatchingAttributeValue(attributeMap, this.samlGroupAttributeNames);
		} catch (RuntimeException e) {
			LOG.warn("No group information found in SAML response. Caused By: " + e.getMessage());
		}

		if(groupList != null) {
			for(String group : groupList){
				//Filter to only groups we want included
				if(Arrays.asList(this.includeGroups).contains(group)) {
					authorityList.add(new SimpleGrantedAuthority(group));
				}
			}
		}
		
		return authorityList;
	}
}
