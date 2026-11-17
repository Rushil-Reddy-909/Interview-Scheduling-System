package com.interviewscheduling.web.preview;

import java.time.LocalDateTime;

import com.interviewscheduling.enums.UserRole;

public record PreviewUser(Long id, String name, String email, UserRole role, LocalDateTime createdAt) {

	public String initials() {
		String[] parts = name.trim().split("\\s+");
		String first = parts[0].substring(0, 1);
		String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
		return (first + last).toUpperCase();
	}

}
