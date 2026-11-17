package com.interviewscheduling.web.advice;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.interviewscheduling.dto.UserView;
import com.interviewscheduling.service.UserService;

@ControllerAdvice
public class CurrentAccountAdvice {

	private final UserService users;

	public CurrentAccountAdvice(UserService users) {
		this.users = users;
	}

	@ModelAttribute
	public void addCurrentAccount(Authentication authentication, Model model) {
		if (authentication == null || authentication instanceof AnonymousAuthenticationToken
				|| !authentication.isAuthenticated()) {
			return;
		}
		String email = authentication.getName();
		model.addAttribute("currentEmail", email);
		model.addAttribute("currentRole", roleName(authentication));
		model.addAttribute("currentName", users.findByEmail(email).map(UserView::name).orElse(email));
	}

	public static String roleName(Authentication authentication) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			String name = authority.getAuthority();
			if (name != null && name.startsWith("ROLE_")) {
				return name.substring("ROLE_".length());
			}
		}
		return "";
	}

}
