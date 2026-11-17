package com.interviewscheduling.service;

import static com.interviewscheduling.TestClockConfiguration.MONDAY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.TestData;
import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.dto.InterviewView;
import com.interviewscheduling.dto.WeekCalendar;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.exception.InvalidInterviewStateException;
import com.interviewscheduling.exception.NotFoundException;
import com.interviewscheduling.exception.SlotRejectedException;
import com.interviewscheduling.repository.InterviewRepository;

@SpringBootTest
@Transactional
class InterviewServiceTests {

	@Autowired
	private InterviewService service;

	@Autowired
	private InterviewerCalendarService calendar;

	@Autowired
	private InterviewRepository interviews;

	@Autowired
	private TestData data;

	private AppUser recruiter;

	private AppUser candidate;

	private AppUser interviewer;

	@BeforeEach
	void people() {
		recruiter = data.user("Rita Recruiter", "rita@localhost", UserRole.RECRUITER);
		candidate = data.user("Cora Candidate", "cora@localhost", UserRole.CANDIDATE);
		interviewer = data.user("Ivan Interviewer", "ivan@localhost", UserRole.INTERVIEWER);
	}

	@Test
	void candidateAcceptsAnAwaitingInterview() {
		Interview interview = awaiting(MONDAY.plusDays(1).atTime(10, 0));

		InterviewView view = service.accept("cora@localhost", interview.getId());

		assertEquals(InterviewStatus.ACCEPTED, view.status());
		assertEquals(InterviewStatus.ACCEPTED, interviews.findById(interview.getId()).orElseThrow().getStatus());
	}

	@Test
	void anotherCandidatesInterviewIsNotFound() {
		Interview interview = awaiting(MONDAY.plusDays(1).atTime(10, 0));

		assertThrows(NotFoundException.class, () -> service.accept("candidate@localhost", interview.getId()));
		assertThrows(NotFoundException.class, () -> service.accept("cora@localhost", -1L));
	}

	@Test
	void acceptIsRejectedOnceAccepted() {
		Interview interview = data.interview(recruiter, candidate, interviewer, MONDAY.plusDays(1).atTime(10, 0), 60,
				InterviewStatus.ACCEPTED);

		assertThrows(InvalidInterviewStateException.class, () -> service.accept("cora@localhost", interview.getId()));
	}

	@Test
	void requestNewStartRecordsTheRequestAndKeepsTheSlot() {
		LocalDateTime current = MONDAY.plusDays(1).atTime(10, 0);
		Interview interview = awaiting(current);
		LocalDateTime requested = MONDAY.plusDays(2).atTime(15, 0);

		service.requestNewStart("cora@localhost", interview.getId(), requested);

		Interview saved = interviews.findById(interview.getId()).orElseThrow();
		assertEquals(InterviewStatus.RESCHEDULE_REQUESTED, saved.getStatus());
		assertEquals(requested.toInstant(ZoneOffset.UTC), saved.getRequestedStartsAt());
		assertEquals(current.toInstant(ZoneOffset.UTC), saved.getStartsAt());
	}

	@Test
	void requestNewStartAppliesTheSlotRules() {
		Interview interview = awaiting(MONDAY.plusDays(1).atTime(10, 0));
		Long id = interview.getId();

		assertSlotMessage("Pick a weekday", id, MONDAY.plusDays(5).atTime(10, 0));
		assertSlotMessage("between 09:00 and 17:00", id, MONDAY.plusDays(2).atTime(16, 30));
		assertSlotMessage("quarter hour", id, MONDAY.plusDays(2).atTime(10, 5));
		assertSlotMessage("in the future", id, MONDAY.minusDays(3).atTime(10, 0));
		assertEquals(InterviewStatus.AWAITING_CANDIDATE, interviews.findById(id).orElseThrow().getStatus());
	}

	@Test
	void requestNewStartIsRejectedWhileARequestIsOpen() {
		Interview interview = data.interview(recruiter, candidate, interviewer, MONDAY.plusDays(1).atTime(10, 0), 60,
				InterviewStatus.RESCHEDULE_REQUESTED, MONDAY.plusDays(3).atTime(9, 0));

		assertThrows(InvalidInterviewStateException.class,
				() -> service.requestNewStart("cora@localhost", interview.getId(), MONDAY.plusDays(2).atTime(9, 0)));
	}

	@Test
	void candidateListsSplitUpcomingFromPast() {
		awaiting(MONDAY.plusDays(1).atTime(10, 0));
		data.interview(recruiter, candidate, interviewer, MONDAY.minusWeeks(1).atTime(10, 0), 60,
				InterviewStatus.ACCEPTED);

		List<InterviewView> upcoming = service.upcomingForCandidate("cora@localhost");
		List<InterviewView> past = service.pastForCandidate("cora@localhost");

		assertEquals(1, upcoming.size());
		assertTrue(upcoming.get(0).canAccept());
		assertEquals(1, past.size());
		assertFalse(past.get(0).canRequestNewStart());
	}

	@Test
	void recruiterSeesOnlyTheirOwnRescheduleRequests() {
		data.interview(recruiter, candidate, interviewer, MONDAY.plusDays(1).atTime(10, 0), 60,
				InterviewStatus.RESCHEDULE_REQUESTED, MONDAY.plusDays(3).atTime(9, 0));
		awaiting(MONDAY.plusDays(2).atTime(10, 0));

		assertEquals(1, service.rescheduleRequestsForRecruiter("rita@localhost").size());
		assertEquals(2, service.upcomingForRecruiter("rita@localhost").size());
		assertTrue(service.rescheduleRequestsForRecruiter("recruiter@localhost").isEmpty());
	}

	@Test
	void adminSearchFiltersByStatusPersonAndDate() {
		awaiting(MONDAY.plusDays(1).atTime(10, 0));
		data.interview(recruiter, candidate, interviewer, MONDAY.plusDays(8).atTime(10, 0), 60,
				InterviewStatus.ACCEPTED);

		assertEquals(1, service.search(InterviewStatus.ACCEPTED, null, null, "").size());
		assertEquals(2, service.search(null, null, null, "IVAN").size());
		assertEquals(0, service.search(null, null, null, "nobody").size());
		assertEquals(1, service.search(null, MONDAY, MONDAY.plusDays(1), "cora").size());
		assertEquals(1, service.search(null, MONDAY.plusDays(2), null, "cora").size());
	}

	@Test
	void calendarMarksBookedCellsAndFreeMinutes() {
		Interview interview = awaiting(MONDAY.atTime(10, 0));

		WeekCalendar week = calendar.week("ivan@localhost", MONDAY.plusDays(2));

		assertEquals(MONDAY, week.weekStart());
		assertEquals(32, week.times().size());
		WeekCalendar.Day monday = week.days().get(0);
		assertTrue(monday.today());
		assertEquals(1, monday.interviewCount());
		assertEquals(420, monday.freeMinutes());
		WeekCalendar.Cell ten = monday.cells().get(4);
		assertEquals(interview.getId(), ten.interview().id());
		assertTrue(ten.first());
		assertEquals(4, ten.span());
		assertTrue(monday.cells().get(5).interview() != null && !monday.cells().get(5).first());
		assertTrue(monday.cells().get(8).free());
		assertEquals(480, week.days().get(1).freeMinutes());
	}

	@Test
	void calendarOpensOnTheWeekOfTheNextInterview() {
		assertEquals(MONDAY, calendar.weekOfNextInterview("ivan@localhost"));
		awaiting(MONDAY.plusWeeks(2).plusDays(3).atTime(11, 0));
		assertEquals(MONDAY.plusWeeks(2), calendar.weekOfNextInterview("ivan@localhost"));
	}

	private Interview awaiting(LocalDateTime start) {
		return data.interview(recruiter, candidate, interviewer, start, 60, InterviewStatus.AWAITING_CANDIDATE);
	}

	private void assertSlotMessage(String expected, Long id, LocalDateTime start) {
		SlotRejectedException ex = assertThrows(SlotRejectedException.class,
				() -> service.requestNewStart("cora@localhost", id, start));
		assertTrue(ex.getMessage().contains(expected), ex.getMessage());
	}
}
