package com.interviewscheduling.dto;

import java.time.LocalDateTime;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.service.SlotRules;

public record UserView(Long id, String name, String email, UserRole role, LocalDateTime createdAt) {

	public static UserView from(AppUser user) {
		return new UserView(user.getId(), user.getName(), user.getEmail(), user.getRole(),
				SlotRules.toUtc(user.getCreatedAt()));
	}

	public String initials() {
		String[] parts = name.trim().split("\\s+");
		if (parts[0].isEmpty()) {
			return "?";
		}
		String first = parts[0].substring(0, 1);
		String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
		return (first + last).toUpperCase();
	}

}
