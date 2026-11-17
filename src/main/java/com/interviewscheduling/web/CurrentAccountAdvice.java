package com.interviewscheduling.web;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.interviewscheduling.web.preview.PreviewData;
import com.interviewscheduling.web.preview.PreviewUser;

@ControllerAdvice
public class CurrentAccountAdvice {

	private final PreviewData data;

	public CurrentAccountAdvice(PreviewData data) {
		this.data = data;
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
		model.addAttribute("currentName", data.userByEmail(email).map(PreviewUser::name).orElse(email));
	}

	static String roleName(Authentication authentication) {
		for (GrantedAuthority authority : authentication.getAuthorities()) {
			String name = authority.getAuthority();
			if (name != null && name.startsWith("ROLE_")) {
				return name.substring("ROLE_".length());
			}
		}
		return "";
	}

}
