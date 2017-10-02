package gov.usgs.wma.mlrauthserver.util;

import java.util.HashMap;
import java.util.Map;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.saml.SAMLCredential;

public class SAMLUtils {
	// Logger
	private static final Logger LOG = LoggerFactory.getLogger(SAMLUtils.class);
	
	public static  Map<String,String> getSingularAttributeValueMap(SAMLCredential credentials){
		Map<String,String> returnMap = new HashMap<>();
		
		for(Attribute attr : credentials.getAttributes()){
			if(attr.getAttributeValues().size() > 0){
				if(attr.getAttributeValues().size() > 1){
					LOG.warn("SAML Attribute " + attr.getName() + " contains more than one value. Only the first value will be returned.");
				}
				
				returnMap.put(attr.getName(), getAttributeTextValue(attr.getAttributeValues().get(0)));
			}			
		}
		
		return returnMap;
	}
	
	public static String getAttributeTextValue(XMLObject attributeValue)
	{
		String returnVal = null;
		
		if(attributeValue != null){
			if(attributeValue instanceof XSString){
				returnVal = ((XSString) attributeValue).getValue();
			} else if(attributeValue instanceof XSAnyImpl) {
				returnVal = ((XSAnyImpl) attributeValue).getTextContent();
			} else {
				returnVal = attributeValue.toString();
			}
		}
		
		return returnVal;
	}
}
