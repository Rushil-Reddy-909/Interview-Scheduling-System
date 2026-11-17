package com.interviewscheduling.web.preview;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import com.interviewscheduling.enums.InterviewStatus;

public record PreviewInterview(Long id, PreviewUser recruiter, PreviewUser candidate, PreviewUser interviewer,
		LocalDateTime startsAt, int durationMinutes, InterviewStatus status, LocalDateTime requestedStartsAt) {

	public LocalDateTime endsAt() {
		return startsAt.plusMinutes(durationMinutes);
	}

	public LocalDate date() {
		return startsAt.toLocalDate();
	}

	public LocalDateTime requestedEndsAt() {
		return requestedStartsAt == null ? null : requestedStartsAt.plusMinutes(durationMinutes);
	}

	public boolean overlaps(LocalDateTime start, LocalDateTime end) {
		return startsAt.isBefore(end) && start.isBefore(endsAt());
	}

	public boolean involves(PreviewUser user) {
		return candidate.id().equals(user.id()) || interviewer.id().equals(user.id());
	}

	public boolean canAccept() {
		return status == InterviewStatus.AWAITING_CANDIDATE;
	}

	public boolean canRequestNewStart() {
		return status == InterviewStatus.AWAITING_CANDIDATE || status == InterviewStatus.ACCEPTED;
	}

	public List<LocalTime> requestableStarts() {
		List<LocalTime> starts = new ArrayList<>();
		LocalTime latest = PreviewData.DAY_END.minusMinutes(durationMinutes);
		for (LocalTime time = PreviewData.DAY_START; !time.isAfter(latest); time = time.plusMinutes(15)) {
			starts.add(time);
		}
		return starts;
	}

}
