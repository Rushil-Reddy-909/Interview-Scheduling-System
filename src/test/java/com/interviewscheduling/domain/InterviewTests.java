package com.interviewscheduling.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Instant;

import org.junit.jupiter.api.Test;

import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.exception.InvalidInterviewStateException;

class InterviewTests {

	private static final Instant NOW = Instant.parse("2026-11-23T08:00:00Z");

	private static final Instant START = Instant.parse("2026-11-24T10:00:00Z");

	@Test
	void acceptMovesAwaitingToAccepted() {
		Interview interview = interview(InterviewStatus.AWAITING_CANDIDATE);
		interview.accept(NOW);
		assertEquals(InterviewStatus.ACCEPTED, interview.getStatus());
	}

	@Test
	void acceptNeedsAwaitingCandidate() {
		assertThrows(InvalidInterviewStateException.class, () -> interview(InterviewStatus.ACCEPTED).accept(NOW));
		assertThrows(InvalidInterviewStateException.class,
				() -> interview(InterviewStatus.RESCHEDULE_REQUESTED).accept(NOW));
	}

	@Test
	void acceptNeedsAFutureStart() {
		Interview interview = interview(InterviewStatus.AWAITING_CANDIDATE);
		assertThrows(InvalidInterviewStateException.class, () -> interview.accept(START));
	}

	@Test
	void requestNewStartKeepsTheCurrentSlot() {
		Instant requested = Instant.parse("2026-11-25T15:00:00Z");
		for (InterviewStatus from : new InterviewStatus[] { InterviewStatus.AWAITING_CANDIDATE,
				InterviewStatus.ACCEPTED }) {
			Interview interview = interview(from);
			interview.requestNewStart(requested, NOW);
			assertEquals(InterviewStatus.RESCHEDULE_REQUESTED, interview.getStatus());
			assertEquals(requested, interview.getRequestedStartsAt());
			assertEquals(START, interview.getStartsAt());
		}
	}

	@Test
	void requestNewStartIsRejectedWhileARequestIsOpen() {
		Interview interview = interview(InterviewStatus.RESCHEDULE_REQUESTED);
		assertThrows(InvalidInterviewStateException.class,
				() -> interview.requestNewStart(Instant.parse("2026-11-25T15:00:00Z"), NOW));
	}

	private static Interview interview(InterviewStatus status) {
		Interview interview = new Interview();
		interview.setStartsAt(START);
		interview.setEndsAt(START.plusSeconds(3600));
		interview.setDurationMinutes(60);
		interview.setStatus(status);
		return interview;
	}

}
