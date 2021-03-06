package gov.usgs.wma.mlrauthserver.service;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import gov.usgs.wma.mlrauthserver.model.WaterAuthUserDetails;
import gov.usgs.wma.mlrauthserver.util.SAMLUtils;

import java.util.List;
import java.util.Map;

import org.hibernate.validator.constraints.NotEmpty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SAMLUserDetailsImpl implements SAMLUserDetailsService  {
	@NotEmpty
	@Value("${security.saml.attribute-names.username}")
	private String[] samlUsernameAttributeNames;

	@NotEmpty
	@Value("${security.saml.attribute-names.email}")
	private String[] samlEmailAttributeNames;

	@NotEmpty
	@Value("${security.saml.attribute-names.group}")
	private String[] samlGroupAttributeNames;
	
	@NotEmpty
	@Value("${security.saml.attribute-names.details.office-state}")
	private String[] samlOfficeStateAttributeNames;

	@Override
	public WaterAuthUser loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		return createUserFromSamlAttributes(SAMLUtils.getAttributeValueMap(credential));
	}

	public WaterAuthUser createUserFromSamlAttributes(Map<String, List<String>> samlAttributeMap) {
		return new WaterAuthUser(
			SAMLUtils.getFirstMatchingAttributeFirstValue(samlAttributeMap, samlUsernameAttributeNames, true),
			SAMLUtils.getFirstMatchingAttributeFirstValue(samlAttributeMap, samlEmailAttributeNames, true),
			SAMLUtils.groupsToAuthoritiesList(samlAttributeMap, samlGroupAttributeNames),
			createDetailsFromSamlAttributes(samlAttributeMap)
		);
	}

	private WaterAuthUserDetails createDetailsFromSamlAttributes(Map<String, List<String>> samlAttributeMap) {
		return new WaterAuthUserDetails(
			SAMLUtils.getFirstMatchingAttributeFirstValue(samlAttributeMap, samlOfficeStateAttributeNames, false)
		);
	}
}
