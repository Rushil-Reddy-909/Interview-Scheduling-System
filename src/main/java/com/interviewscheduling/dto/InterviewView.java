package com.interviewscheduling.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.service.SlotRules;

/**
 * An interview for the pages. Times are UTC.
 */
public record InterviewView(Long id, UserView recruiter, UserView candidate, UserView interviewer,
		LocalDateTime startsAt, LocalDateTime endsAt, int durationMinutes, InterviewStatus status,
		LocalDateTime requestedStartsAt, boolean started) {

	public static InterviewView from(Interview interview, Instant now) {
		return new InterviewView(interview.getId(), UserView.from(interview.getRecruiter()),
				UserView.from(interview.getCandidate()), UserView.from(interview.getInterviewer()),
				SlotRules.toUtc(interview.getStartsAt()), SlotRules.toUtc(interview.getEndsAt()),
				interview.getDurationMinutes(), interview.getStatus(), SlotRules.toUtc(interview.getRequestedStartsAt()),
				interview.hasStarted(now));
	}

	public LocalDate date() {
		return startsAt.toLocalDate();
	}

	public LocalDateTime requestedEndsAt() {
		return requestedStartsAt == null ? null : requestedStartsAt.plusMinutes(durationMinutes);
	}

	public boolean overlaps(LocalDateTime start, LocalDateTime end) {
		return startsAt.isBefore(end) && start.isBefore(endsAt);
	}

	public boolean canAccept() {
		return !started && status == InterviewStatus.AWAITING_CANDIDATE;
	}

	public boolean canRequestNewStart() {
		return !started && (status == InterviewStatus.AWAITING_CANDIDATE || status == InterviewStatus.ACCEPTED);
	}

	public List<LocalTime> requestableStarts() {
		return SlotRules.startsFor(durationMinutes);
	}

}
