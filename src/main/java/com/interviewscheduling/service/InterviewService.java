package com.interviewscheduling.service;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.dto.InterviewView;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.exception.NotFoundException;
import com.interviewscheduling.repository.InterviewRepository;

@Service
@Transactional(readOnly = true)
public class InterviewService {

	private final InterviewRepository interviews;

	private final SlotRules slotRules;

	private final Clock clock;

	public InterviewService(InterviewRepository interviews, SlotRules slotRules, Clock clock) {
		this.interviews = interviews;
		this.slotRules = slotRules;
		this.clock = clock;
	}

	public LocalDate today() {
		return LocalDate.now(clock);
	}

	public long count() {
		return interviews.count();
	}

	public long countByStatus(InterviewStatus status) {
		return interviews.countByStatus(status);
	}

	public List<InterviewView> nextInterviews() {
		Instant now = clock.instant();
		return views(interviews.findTop6ByEndsAtAfterOrderByStartsAtAsc(now), now);
	}

	/**
	 * {@code from} and {@code to} are inclusive dates, both optional.
	 */
	public List<InterviewView> search(InterviewStatus status, LocalDate from, LocalDate to, String query) {
		Instant fromInstant = from == null ? null : SlotRules.toInstant(from.atStartOfDay());
		Instant toInstant = to == null ? null : SlotRules.toInstant(to.plusDays(1).atStartOfDay());
		return views(interviews.search(status, fromInstant, toInstant, LikePattern.contains(query)), clock.instant());
	}

	public List<InterviewView> upcomingForRecruiter(String email) {
		Instant now = clock.instant();
		return views(interviews.findUpcomingForRecruiter(email, now), now);
	}

	public List<InterviewView> rescheduleRequestsForRecruiter(String email) {
		return upcomingForRecruiter(email).stream()
				.filter(interview -> interview.status() == InterviewStatus.RESCHEDULE_REQUESTED).toList();
	}

	public List<InterviewView> upcomingForCandidate(String email) {
		Instant now = clock.instant();
		return views(interviews.findUpcomingForCandidate(email, now), now);
	}

	public List<InterviewView> pastForCandidate(String email) {
		Instant now = clock.instant();
		return views(interviews.findPastForCandidate(email, now), now);
	}

	@Transactional
	public InterviewView accept(String candidateEmail, Long interviewId) {
		Instant now = clock.instant();
		Interview interview = ownedByCandidate(candidateEmail, interviewId);
		interview.accept(now);
		return InterviewView.from(interview, now);
	}

	/**
	 * Saves the candidate's preferred start. It reserves nothing, so only the slot rules apply.
	 */
	@Transactional
	public InterviewView requestNewStart(String candidateEmail, Long interviewId, LocalDateTime start) {
		Instant now = clock.instant();
		Interview interview = ownedByCandidate(candidateEmail, interviewId);
		slotRules.check(start, interview.getDurationMinutes());
		interview.requestNewStart(SlotRules.toInstant(start), now);
		return InterviewView.from(interview, now);
	}

	private Interview ownedByCandidate(String candidateEmail, Long interviewId) {
		return interviews.findById(interviewId)
				.filter(interview -> interview.getCandidate().getEmail().equalsIgnoreCase(candidateEmail))
				.orElseThrow(() -> new NotFoundException("That interview was not found."));
	}

	private static List<InterviewView> views(List<Interview> list, Instant now) {
		return list.stream().map(interview -> InterviewView.from(interview, now)).toList();
	}

}
