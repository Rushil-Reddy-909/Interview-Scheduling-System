package com.interviewscheduling.seed;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.repository.AppUserRepository;
import com.interviewscheduling.repository.InterviewRepository;
import com.interviewscheduling.service.SlotRules;

/**
 * Sample people and interviews for local development, relative to next Monday. It runs after the per-role seed and
 * only while the interview table is empty.
 */
@Component
@Order(1)
@ConditionalOnProperty(name = "app.seed.demo", havingValue = "true")
public class DemoDataSeeder implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

	private final AppUserRepository users;

	private final InterviewRepository interviews;

	private final PasswordEncoder passwordEncoder;

	private final AdminProperties admin;

	private final Clock clock;

	public DemoDataSeeder(AppUserRepository users, InterviewRepository interviews, PasswordEncoder passwordEncoder,
			AdminProperties admin, Clock clock) {
		this.users = users;
		this.interviews = interviews;
		this.passwordEncoder = passwordEncoder;
		this.admin = admin;
		this.clock = clock;
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) {
		if (interviews.count() > 0) {
			return;
		}
		LocalDate week = LocalDate.now(clock).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDate past = week.minusWeeks(2);

		AppUser recruiter = user("Recruiter", "recruiter@localhost", UserRole.RECRUITER);
		AppUser daniel = user("Daniel Okafor", "daniel.okafor@localhost", UserRole.RECRUITER);
		AppUser candidate = user("Candidate", "candidate@localhost", UserRole.CANDIDATE);
		AppUser rohan = user("Rohan Mehta", "rohan.mehta@localhost", UserRole.CANDIDATE);
		AppUser lina = user("Lina Haddad", "lina.haddad@localhost", UserRole.CANDIDATE);
		AppUser tom = user("Tom Becker", "tom.becker@localhost", UserRole.CANDIDATE);
		AppUser sara = user("Sara Kim", "sara.kim@localhost", UserRole.CANDIDATE);
		AppUser interviewer = user("Interviewer", "interviewer@localhost", UserRole.INTERVIEWER);
		AppUser elena = user("Elena Petrova", "elena.petrova@localhost", UserRole.INTERVIEWER);
		AppUser kwame = user("Kwame Mensah", "kwame.mensah@localhost", UserRole.INTERVIEWER);

		interview(recruiter, candidate, interviewer, at(week, 1, 10, 0), 60, InterviewStatus.AWAITING_CANDIDATE, null);
		interview(recruiter, candidate, elena, at(week, 3, 14, 0), 45, InterviewStatus.ACCEPTED, null);
		interview(recruiter, candidate, interviewer, at(week, 4, 11, 0), 60, InterviewStatus.RESCHEDULE_REQUESTED,
				at(week, 2, 15, 0));
		interview(recruiter, rohan, interviewer, at(week, 0, 9, 0), 30, InterviewStatus.ACCEPTED, null);
		interview(recruiter, lina, kwame, at(week, 2, 13, 30), 60, InterviewStatus.RESCHEDULE_REQUESTED,
				at(week, 1, 16, 0));
		interview(daniel, tom, interviewer, at(week, 2, 10, 30), 90, InterviewStatus.AWAITING_CANDIDATE, null);
		interview(recruiter, sara, elena, at(week, 1, 15, 0), 60, InterviewStatus.RESCHEDULE_REQUESTED,
				at(week, 3, 9, 0));
		interview(daniel, rohan, kwame, at(week, 4, 9, 30), 45, InterviewStatus.AWAITING_CANDIDATE, null);
		interview(recruiter, tom, interviewer, at(week.plusWeeks(1), 0, 14, 0), 60, InterviewStatus.AWAITING_CANDIDATE, null);
		interview(recruiter, candidate, kwame, at(past, 2, 10, 0), 60, InterviewStatus.ACCEPTED, null);
		interview(daniel, lina, elena, at(past, 3, 11, 0), 45, InterviewStatus.ACCEPTED, null);
		log.info("Seeded demo interviews around the week of {}", week);
	}

	/**
	 * Reuses an existing account with this email, so the per-role users keep their names.
	 */
	private AppUser user(String name, String email, UserRole role) {
		return users.findByEmail(email).orElseGet(() -> {
			AppUser user = new AppUser();
			user.setName(name);
			user.setEmail(email);
			user.setRole(role);
			user.setPasswordHash(passwordEncoder.encode(admin.password()));
			return users.save(user);
		});
	}

	private void interview(AppUser recruiter, AppUser candidate, AppUser interviewer, LocalDateTime start,
			int durationMinutes, InterviewStatus status, LocalDateTime requestedStart) {
		Instant startsAt = SlotRules.toInstant(start);
		Interview interview = new Interview();
		interview.setRecruiter(recruiter);
		interview.setCandidate(candidate);
		interview.setInterviewer(interviewer);
		interview.setDurationMinutes(durationMinutes);
		interview.setStartsAt(startsAt);
		interview.setEndsAt(startsAt.plusSeconds(durationMinutes * 60L));
		interview.setStatus(status);
		interview.setRequestedStartsAt(requestedStart == null ? null : SlotRules.toInstant(requestedStart));
		interviews.save(interview);
	}

	private static LocalDateTime at(LocalDate monday, int dayOffset, int hour, int minute) {
		return monday.plusDays(dayOffset).atTime(hour, minute);
	}

}
