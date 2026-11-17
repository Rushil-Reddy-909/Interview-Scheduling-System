package com.interviewscheduling;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.repository.AppUserRepository;
import com.interviewscheduling.repository.InterviewRepository;

/**
 * Builds users and interviews for tests. Use it inside a {@code @Transactional} test so the rows roll back.
 */
@Component
public class TestData {

	public static final String PASSWORD = "12345678";

	private final AppUserRepository users;

	private final InterviewRepository interviews;

	private final PasswordEncoder passwordEncoder;

	public TestData(AppUserRepository users, InterviewRepository interviews, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.interviews = interviews;
		this.passwordEncoder = passwordEncoder;
	}

	public AppUser seeded(UserRole role) {
		return users.findByEmail(role.name().toLowerCase() + "@localhost").orElseThrow();
	}

	public AppUser user(String name, String email, UserRole role) {
		AppUser user = new AppUser();
		user.setName(name);
		user.setEmail(email);
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode(PASSWORD));
		return users.save(user);
	}

	public Interview interview(AppUser recruiter, AppUser candidate, AppUser interviewer, LocalDateTime start,
			int durationMinutes, InterviewStatus status) {
		return interview(recruiter, candidate, interviewer, start, durationMinutes, status, null);
	}

	public Interview interview(AppUser recruiter, AppUser candidate, AppUser interviewer, LocalDateTime start,
			int durationMinutes, InterviewStatus status, LocalDateTime requestedStart) {
		Instant startsAt = start.toInstant(ZoneOffset.UTC);
		Interview interview = new Interview();
		interview.setRecruiter(recruiter);
		interview.setCandidate(candidate);
		interview.setInterviewer(interviewer);
		interview.setDurationMinutes(durationMinutes);
		interview.setStartsAt(startsAt);
		interview.setEndsAt(startsAt.plusSeconds(durationMinutes * 60L));
		interview.setStatus(status);
		interview.setRequestedStartsAt(requestedStart == null ? null : requestedStart.toInstant(ZoneOffset.UTC));
		return interviews.saveAndFlush(interview);
	}

}
