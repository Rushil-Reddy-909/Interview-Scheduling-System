package com.interviewscheduling.service;

import java.util.Locale;

final class LikePattern {

	private LikePattern() {
	}

	/**
	 * A lowercase {@code contains} pattern with {@code \} as the escape character, or empty for a blank query.
	 */
	static String contains(String query) {
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		if (needle.isEmpty()) {
			return "";
		}
		return "%" + needle.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_") + "%";
	}

}
