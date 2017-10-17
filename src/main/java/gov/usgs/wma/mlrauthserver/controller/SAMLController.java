package gov.usgs.wma.mlrauthserver.controller;

import gov.usgs.wma.mlrauthserver.util.SAMLUtils;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.saml.SAMLCredential;
import org.springframework.ui.Model;

@Controller
public class SAMLController {
	@RequestMapping("/")
	public String index() {
		return "index";
	}
	
	@RequestMapping("/auth-fail")
	public String authFail() {
		return "auth-fail";
	}
	
	@RequestMapping("/home")
	public String home(Model model) {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		SAMLCredential credentials = (SAMLCredential) authentication.getCredentials();
		String attributeHtml = "<ul>";
		
		Map<String,List<String>> attributeMap = SAMLUtils.getAttributeValueMap(credentials);
				
		for(Map.Entry<String, List<String>> entry : attributeMap.entrySet()){
			for(String value : entry.getValue()){
				attributeHtml += "<li>" + entry.getKey() + ": " + value + "</li>";
			}
		}
		
		attributeHtml += "</ul>";
		
		model.addAttribute("attributeHtml", attributeHtml);
		return "home";
	}
}
