package gov.usgs.wma.mlrauthserver.service;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;
import gov.usgs.wma.mlrauthserver.util.SAMLUtils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.security.saml.userdetails.SAMLUserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SAMLUserDetailsImpl implements SAMLUserDetailsService  {

	@Autowired
	WaterAuthUserFactory waterAuthUserFactory;

	@Override
	public WaterAuthUser loadUserBySAML(SAMLCredential credential) throws UsernameNotFoundException {
		return waterAuthUserFactory.createUserFromSamlAttributes(SAMLUtils.getAttributeValueMap(credential));
	}
}
