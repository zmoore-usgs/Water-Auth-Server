package gov.usgs.wma.mlrauthserver.controller;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import gov.usgs.wma.mlrauthserver.model.WaterAuthUser;

@Controller
public class HomeController {
	@RequestMapping("/")
	@ResponseBody
	public String loggedIn() {
		return "You're logged in as " + SecurityContextHolder.getContext().getAuthentication().getName();
	}

	@RequestMapping("/mydetails")
	@ResponseBody
	public String details() {
		WaterAuthUser user = (WaterAuthUser)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		String result =
			"<h1>My User Details</h1>" +
			"<big><b>Username: </b>" + user.getUsername() + "</big>" +
			"<br/><big><b>Email: </b>" + user.getEmail() + "</big>" +
			"<br/><big><b>Details: </b></big>" +
				"<br/>&nbsp;&nbsp;&nbsp;&nbsp;<b>Office State: </b>" + user.getDetails().getOfficeState() +
			"<br/><big><b>Groups: </b></big>";
		for(GrantedAuthority role : user.getAuthorities()) {
			result += "<br/>&nbsp;&nbsp;&nbsp;&nbsp;" + role.getAuthority();
		}
		return result;
	}
	
	@RequestMapping("/out")
	@ResponseBody
	public String loggedOut() {
		return "You have been logged out.";
	}
	
	@RequestMapping("/auth-error")
	@ResponseBody
	public String error() {
		return "An error occurred while attempting to login or logout. Please go back and try again.";
	}
}
