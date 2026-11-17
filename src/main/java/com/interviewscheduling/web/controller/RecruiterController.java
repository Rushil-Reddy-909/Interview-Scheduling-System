package com.interviewscheduling.web.controller;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.interviewscheduling.dto.InterviewView;
import com.interviewscheduling.dto.UserView;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.service.InterviewService;
import com.interviewscheduling.service.SlotRules;
import com.interviewscheduling.service.UserService;
import com.interviewscheduling.web.model.StatCard;

/**
 * Matching is not built yet. The schedule and reschedule pages check their inputs and then say so.
 */
@Controller
@RequestMapping("/dashboard/recruiter")
public class RecruiterController {

	private static final int MAX_RANGE_DAYS = 31;

	private final UserService users;

	private final InterviewService interviews;

	public RecruiterController(UserService users, InterviewService interviews) {
		this.users = users;
		this.interviews = interviews;
	}

	@GetMapping
	public String home(Authentication authentication, Model model) {
		List<InterviewView> upcoming = interviews.upcomingForRecruiter(authentication.getName());
		List<InterviewView> requests = upcoming.stream()
				.filter(interview -> interview.status() == InterviewStatus.RESCHEDULE_REQUESTED).toList();
		model.addAttribute("stats", List.of(
				new StatCard("Awaiting candidate", count(upcoming, InterviewStatus.AWAITING_CANDIDATE), "#upcoming",
						"warning"),
				new StatCard("Accepted", count(upcoming, InterviewStatus.ACCEPTED), "#upcoming", "success"),
				new StatCard("Reschedule requested", requests.size(), "#requests", "purple")));
		model.addAttribute("requests", requests);
		model.addAttribute("requestIds", requests.stream().map(InterviewView::id).toList());
		model.addAttribute("upcoming", upcoming);
		return "recruiter/home";
	}

	@GetMapping("/schedule")
	public String schedule(@RequestParam(defaultValue = "single") String mode,
			@RequestParam(required = false) List<Long> candidateIds,
			@RequestParam(required = false) List<Long> interviewerIds,
			@RequestParam(defaultValue = "60") int duration,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "false") boolean propose, Model model) {
		boolean bulk = "bulk".equals(mode);
		List<Long> selectedCandidates = orEmpty(candidateIds);
		List<Long> selectedInterviewers = orEmpty(interviewerIds);
		LocalDate rangeFrom = from == null ? defaultFrom() : from;
		LocalDate rangeTo = to == null ? defaultFrom().plusDays(4) : to;
		List<UserView> candidates = users.withRole(UserRole.CANDIDATE);
		List<UserView> interviewers = users.withRole(UserRole.INTERVIEWER);

		model.addAttribute("bulk", bulk);
		model.addAttribute("mode", bulk ? "bulk" : "single");
		model.addAttribute("candidates", candidates);
		model.addAttribute("interviewers", interviewers);
		model.addAttribute("selectedCandidateIds", selectedCandidates);
		model.addAttribute("selectedInterviewerIds", selectedInterviewers);
		model.addAttribute("duration", duration);
		model.addAttribute("from", rangeFrom);
		model.addAttribute("to", rangeTo);

		if (!propose) {
			return "recruiter/schedule";
		}
		long chosen = candidates.stream().filter(user -> selectedCandidates.contains(user.id())).count();
		long pool = interviewers.stream().filter(user -> selectedInterviewers.contains(user.id())).count();
		List<String> errors = new ArrayList<>();
		if (chosen == 0) {
			errors.add(bulk ? "Pick at least one candidate." : "Pick a candidate.");
		}
		if (!bulk && chosen > 1) {
			errors.add("Single mode schedules one candidate. Switch to bulk to schedule several.");
		}
		if (pool == 0) {
			errors.add("Pick at least one interviewer for the pool.");
		}
		if (duration < SlotRules.GRID_MINUTES || duration > SlotRules.MAX_DURATION_MINUTES
				|| duration % SlotRules.GRID_MINUTES != 0) {
			errors.add("Duration must be a multiple of 15 minutes, from 15 to " + SlotRules.MAX_DURATION_MINUTES
					+ ".");
		}
		errors.addAll(rangeErrors(rangeFrom, rangeTo));
		model.addAttribute("errors", errors);
		model.addAttribute("matchingPending", errors.isEmpty());
		return "recruiter/schedule";
	}

	@GetMapping("/reschedule")
	public String reschedule(Authentication authentication, @RequestParam(defaultValue = "single") String mode,
			@RequestParam(required = false) List<Long> interviewIds,
			@RequestParam(required = false) List<Long> interviewerIds,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "false") boolean propose, Model model) {
		boolean bulk = "bulk".equals(mode);
		List<Long> selectedInterviews = orEmpty(interviewIds);
		List<Long> selectedInterviewers = orEmpty(interviewerIds);
		LocalDate rangeFrom = from == null ? defaultFrom() : from;
		LocalDate rangeTo = to == null ? defaultFrom().plusDays(4) : to;
		List<InterviewView> requests = interviews.rescheduleRequestsForRecruiter(authentication.getName());

		model.addAttribute("bulk", bulk);
		model.addAttribute("mode", bulk ? "bulk" : "single");
		model.addAttribute("requests", requests);
		model.addAttribute("interviewers", users.withRole(UserRole.INTERVIEWER));
		model.addAttribute("selectedInterviewIds", selectedInterviews);
		model.addAttribute("selectedInterviewerIds", selectedInterviewers);
		model.addAttribute("from", rangeFrom);
		model.addAttribute("to", rangeTo);

		if (!propose) {
			return "recruiter/reschedule";
		}
		long chosen = requests.stream().filter(interview -> selectedInterviews.contains(interview.id())).count();
		List<String> errors = new ArrayList<>();
		if (chosen == 0) {
			errors.add(bulk ? "Pick at least one reschedule request." : "Pick a reschedule request.");
		}
		if (!bulk && chosen > 1) {
			errors.add("Single mode handles one request. Switch to bulk to handle several.");
		}
		errors.addAll(rangeErrors(rangeFrom, rangeTo));
		model.addAttribute("errors", errors);
		model.addAttribute("matchingPending", errors.isEmpty());
		return "recruiter/reschedule";
	}

	private LocalDate defaultFrom() {
		return interviews.today().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
	}

	private static List<String> rangeErrors(LocalDate from, LocalDate to) {
		List<String> errors = new ArrayList<>();
		if (from.isAfter(to)) {
			errors.add("The start date must be on or before the end date.");
			return errors;
		}
		if (ChronoUnit.DAYS.between(from, to) >= MAX_RANGE_DAYS) {
			errors.add("Pick a date range of at most " + MAX_RANGE_DAYS + " days.");
		}
		boolean hasWeekday = false;
		for (LocalDate day = from; !day.isAfter(to) && !hasWeekday; day = day.plusDays(1)) {
			hasWeekday = SlotRules.isWeekday(day);
		}
		if (!hasWeekday) {
			errors.add("The range has no weekday. Slots are Monday to Friday only.");
		}
		return errors;
	}

	private static long count(List<InterviewView> interviews, InterviewStatus status) {
		return interviews.stream().filter(interview -> interview.status() == status).count();
	}

	private static List<Long> orEmpty(List<Long> ids) {
		return ids == null ? List.of() : ids.stream().filter(Objects::nonNull).toList();
	}

}
