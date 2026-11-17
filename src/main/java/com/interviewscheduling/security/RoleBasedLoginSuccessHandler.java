package com.interviewscheduling.security;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component	
public class RoleBasedLoginSuccessHandler implements AuthenticationSuccessHandler {

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException, ServletException {
		response.sendRedirect(request.getContextPath() + targetFor(authentication));
	}

	static String targetFor(Authentication authentication) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			switch (authority.getAuthority()) {
				case "ROLE_ADMIN":
					return "/admin";
				case "ROLE_RECRUITER":
					return "/dashboard/recruiter";
				case "ROLE_CANDIDATE":
					return "/dashboard/candidate";
				case "ROLE_INTERVIEWER":
					return "/dashboard/interviewer";
				default:
					break;
			}
		}
		return "/login";
	}

}
