package com.interviewscheduling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.interviewscheduling.repository.AppUserRepository;
import com.interviewscheduling.enums.UserRole;

import jakarta.persistence.EntityManager;

@SpringBootTest
class AdminSeedTests {

	@Autowired
	private AppUserRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private EntityManager entityManager;

	@Test
	void seedsOneUserPerRoleAndCreatesTheInterviewTable() {
		for (UserRole role : UserRole.values()) {
			String email = role.name().toLowerCase() + "@localhost";
			var user = users.findByEmail(email).orElseThrow();

			assertEquals(role, user.getRole());
			assertEquals(displayName(role), user.getName());
			assertTrue(passwordEncoder.matches("12345678", user.getPasswordHash()));
			assertEquals(1, users.countByRole(role));
		}

		entityManager.createNativeQuery("select id, starts_at, ends_at, status from interview").getResultList();
	}

	private static String displayName(UserRole role) {
		String lower = role.name().toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

}
