package gov.usgs.wma.mlrauthserver.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opensaml.saml2.core.Attribute;
import org.opensaml.xml.XMLObject;
import org.opensaml.xml.schema.XSString;
import org.opensaml.xml.schema.impl.XSAnyImpl;

import org.springframework.security.saml.SAMLCredential;

public class SAMLUtils {
	public static  Map<String,List<String>> getAttributeValueMap(SAMLCredential credentials){
		Map<String,List<String>> returnMap = new HashMap<>();
		
		for(Attribute attr : credentials.getAttributes()){
			if(attr.getAttributeValues().size() > 0){
				List<String> returnList = new ArrayList<>();
				
				for(XMLObject attrValue : attr.getAttributeValues()){
					returnList.add(getAttributeTextValue(attrValue));
				}
				
				returnMap.put(attr.getName(), returnList);
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
