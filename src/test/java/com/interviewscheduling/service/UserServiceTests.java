package com.interviewscheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.TestClockConfiguration;
import com.interviewscheduling.TestData;
import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.dto.UserView;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.exception.DuplicateEmailException;
import com.interviewscheduling.exception.UserChangeRejectedException;
import com.interviewscheduling.repository.AppUserRepository;

@SpringBootTest
@Transactional
class UserServiceTests {

	@Autowired
	private UserService service;

	@Autowired
	private AppUserRepository users;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private TestData data;

	@Test
	void createTrimsAndLowercasesTheEmailAndHashesThePassword() {
		UserView created = service.create("  Nina Park ", "  Nina.Park@Example.COM ", UserRole.CANDIDATE,
				"secret-pass");

		assertEquals("Nina Park", created.name());
		assertEquals("nina.park@example.com", created.email());
		AppUser saved = users.findById(created.id()).orElseThrow();
		assertTrue(passwordEncoder.matches("secret-pass", saved.getPasswordHash()));
	}

	@Test
	void createRejectsAnEmailInUseWhateverTheCase() {
		DuplicateEmailException ex = assertThrows(DuplicateEmailException.class,
				() -> service.create("Copy", "ADMIN@localhost", UserRole.CANDIDATE, "12345678"));
		assertTrue(ex.getMessage().contains("admin@localhost"));
	}

	@Test
	void createRejectsAShortPassword() {
		assertThrows(UserChangeRejectedException.class,
				() -> service.create("Short", "short@localhost", UserRole.CANDIDATE, "1234567"));
	}

	@Test
	void updateWithABlankPasswordKeepsTheHash() {
		AppUser user = data.user("Old Name", "old@localhost", UserRole.CANDIDATE);
		String hash = user.getPasswordHash();

		service.update(user.getId(), "New Name", "new@localhost", UserRole.CANDIDATE, "");

		AppUser saved = users.findById(user.getId()).orElseThrow();
		assertEquals("New Name", saved.getName());
		assertEquals("new@localhost", saved.getEmail());
		assertEquals(hash, saved.getPasswordHash());
	}

	@Test
	void updateWithANewPasswordReplacesTheHash() {
		AppUser user = data.user("Pat", "pat@localhost", UserRole.CANDIDATE);

		service.update(user.getId(), "Pat", "pat@localhost", UserRole.CANDIDATE, "another-pass");

		assertTrue(passwordEncoder.matches("another-pass", users.findById(user.getId()).orElseThrow().getPasswordHash()));
	}

	@Test
	void updateRejectsAnotherUsersEmail() {
		AppUser user = data.user("Pat", "pat@localhost", UserRole.CANDIDATE);
		assertThrows(DuplicateEmailException.class,
				() -> service.update(user.getId(), "Pat", "Candidate@Localhost", UserRole.CANDIDATE, ""));
	}

	@Test
	void theLastAdminKeepsTheirRole() {
		AppUser admin = data.seeded(UserRole.ADMIN);
		UserChangeRejectedException ex = assertThrows(UserChangeRejectedException.class,
				() -> service.update(admin.getId(), admin.getName(), admin.getEmail(), UserRole.RECRUITER, ""));
		assertTrue(ex.getMessage().contains("only admin"));
	}

	@Test
	void anAdminCanChangeRoleWhenAnotherAdminExists() {
		AppUser second = data.user("Second Admin", "second.admin@localhost", UserRole.ADMIN);

		service.update(second.getId(), second.getName(), second.getEmail(), UserRole.RECRUITER, "");

		assertEquals(UserRole.RECRUITER, users.findById(second.getId()).orElseThrow().getRole());
	}

	@Test
	void aUserOnAnInterviewKeepsTheirRole() {
		AppUser candidate = data.user("Busy Candidate", "busy@localhost", UserRole.CANDIDATE);
		data.interview(data.seeded(UserRole.RECRUITER), candidate, data.seeded(UserRole.INTERVIEWER),
				TestClockConfiguration.MONDAY.atTime(10, 0), 60, InterviewStatus.AWAITING_CANDIDATE);

		UserChangeRejectedException ex = assertThrows(UserChangeRejectedException.class,
				() -> service.update(candidate.getId(), candidate.getName(), candidate.getEmail(),
						UserRole.INTERVIEWER, ""));
		assertTrue(ex.getMessage().contains("on interviews"));
	}

	@Test
	void anUnusedUserCanChangeRole() {
		AppUser candidate = data.user("Free Candidate", "free@localhost", UserRole.CANDIDATE);

		service.update(candidate.getId(), candidate.getName(), candidate.getEmail(), UserRole.INTERVIEWER, "");

		assertEquals(UserRole.INTERVIEWER, users.findById(candidate.getId()).orElseThrow().getRole());
	}

	@Test
	void searchFiltersByRoleAndNameOrEmail() {
		data.user("Zara Quinn", "zq@localhost", UserRole.CANDIDATE);
		data.user("Zara Interviewer", "zi@localhost", UserRole.INTERVIEWER);

		List<String> byName = service.search(UserRole.CANDIDATE, "zara").stream().map(UserView::name).toList();
		assertEquals(List.of("Zara Quinn"), byName);

		List<String> byEmail = service.search(null, "ZI@").stream().map(UserView::name).toList();
		assertEquals(List.of("Zara Interviewer"), byEmail);

		assertTrue(service.search(null, "%").isEmpty());
	}

}
