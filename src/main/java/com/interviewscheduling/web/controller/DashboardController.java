package com.interviewscheduling.web.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.interviewscheduling.web.advice.CurrentAccountAdvice;

@Controller
public class DashboardController {

	@GetMapping("/dashboard")
	public String dashboard(Authentication authentication) {
		return "redirect:" + switch (CurrentAccountAdvice.roleName(authentication)) {
			case "ADMIN" -> "/admin";
			case "RECRUITER" -> "/dashboard/recruiter";
			case "CANDIDATE" -> "/dashboard/candidate";
			case "INTERVIEWER" -> "/dashboard/interviewer";
			default -> "/login";
		};
	}

}
