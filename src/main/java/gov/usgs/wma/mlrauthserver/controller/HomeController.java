package gov.usgs.wma.mlrauthserver.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {
	@RequestMapping("/")
	@ResponseBody
	public String loggedIn() {
		return "You're logged in as " + SecurityContextHolder.getContext().getAuthentication().getName();
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
