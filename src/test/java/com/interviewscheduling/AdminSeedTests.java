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
	void seedsASingleAdminAndCreatesTheInterviewTable() {
		var admin = users.findByEmail("admin@localhost").orElseThrow();

		assertEquals(UserRole.ADMIN, admin.getRole());
		assertEquals("Admin", admin.getName());
		assertTrue(passwordEncoder.matches("change-me", admin.getPasswordHash()));
		assertEquals(1, users.countByRole(UserRole.ADMIN));

		entityManager.createNativeQuery("select id, starts_at, ends_at, status from interview").getResultList();
	}

}
