package com.interviewscheduling.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.web.preview.PreviewData;
import com.interviewscheduling.web.preview.PreviewInterview;
import com.interviewscheduling.web.preview.PreviewUser;

@Controller
@RequestMapping("/dashboard/recruiter")
public class RecruiterController {

	private static final int MAX_RANGE_DAYS = 31;

	private static final int MAX_DURATION_MINUTES = 480;

	private final PreviewData data;

	public RecruiterController(PreviewData data) {
		this.data = data;
	}

	@GetMapping
	public String home(Authentication authentication, Model model) {
		LocalDateTime now = data.now();
		List<PreviewInterview> upcoming = data.createdBy(authentication.getName()).stream()
				.filter(interview -> interview.endsAt().isAfter(now)).toList();
		List<PreviewInterview> requests = upcoming.stream()
				.filter(interview -> interview.status() == InterviewStatus.RESCHEDULE_REQUESTED).toList();
		model.addAttribute("stats", List.of(
				new StatCard("Awaiting candidate", count(upcoming, InterviewStatus.AWAITING_CANDIDATE), "#upcoming",
						"warning"),
				new StatCard("Accepted", count(upcoming, InterviewStatus.ACCEPTED), "#upcoming", "success"),
				new StatCard("Reschedule requested", requests.size(), "#requests", "purple")));
		model.addAttribute("requests", requests);
		model.addAttribute("requestIds", requests.stream().map(PreviewInterview::id).toList());
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
		LocalDate rangeFrom = from == null ? data.defaultFrom() : from;
		LocalDate rangeTo = to == null ? data.defaultTo() : to;

		model.addAttribute("bulk", bulk);
		model.addAttribute("mode", bulk ? "bulk" : "single");
		model.addAttribute("candidates", data.usersWithRole(UserRole.CANDIDATE));
		model.addAttribute("interviewers", data.usersWithRole(UserRole.INTERVIEWER));
		model.addAttribute("selectedCandidateIds", selectedCandidates);
		model.addAttribute("selectedInterviewerIds", selectedInterviewers);
		model.addAttribute("duration", duration);
		model.addAttribute("from", rangeFrom);
		model.addAttribute("to", rangeTo);

		if (!propose) {
			return "recruiter/schedule";
		}
		List<PreviewUser> chosen = data.usersWithIds(selectedCandidates, UserRole.CANDIDATE);
		List<PreviewUser> pool = data.usersWithIds(selectedInterviewers, UserRole.INTERVIEWER);
		List<String> errors = new ArrayList<>();
		if (chosen.isEmpty()) {
			errors.add(bulk ? "Pick at least one candidate." : "Pick a candidate.");
		}
		if (!bulk && chosen.size() > 1) {
			errors.add("Single mode schedules one candidate. Switch to bulk to schedule several.");
		}
		if (pool.isEmpty()) {
			errors.add("Pick at least one interviewer for the pool.");
		}
		if (duration < 15 || duration > MAX_DURATION_MINUTES || duration % 15 != 0) {
			errors.add("Duration must be a multiple of 15 minutes, from 15 to " + MAX_DURATION_MINUTES + ".");
		}
		errors.addAll(rangeErrors(rangeFrom, rangeTo));
		model.addAttribute("errors", errors);
		if (!errors.isEmpty()) {
			return "recruiter/schedule";
		}
		model.addAttribute("proposed", true);
		model.addAttribute("pool", pool);
		if (bulk) {
			model.addAttribute("bulkProposal", data.bulkCreate(chosen, pool, duration, rangeFrom, rangeTo));
		}
		else {
			model.addAttribute("proposalCandidate", chosen.get(0));
			model.addAttribute("options", data.createOptions(chosen.get(0), pool, duration, rangeFrom, rangeTo));
		}
		return "recruiter/schedule";
	}

	@PostMapping("/schedule/confirm")
	public String confirmSchedule(@RequestParam(defaultValue = "single") String mode,
			@RequestParam(required = false) Integer option, @RequestParam(defaultValue = "0") int count,
			RedirectAttributes redirect) {
		if ("bulk".equals(mode)) {
			redirect.addFlashAttribute("notice", "Preview only: " + count
					+ " interviews were not saved. Confirm will recheck every slot and save all of them or none.");
		}
		else {
			redirect.addFlashAttribute("notice", "Preview only: option " + (option == null ? "?" : option)
					+ " was not saved. Confirm will recheck the slot, then create it as Awaiting candidate.");
		}
		return "redirect:/dashboard/recruiter";
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
		LocalDate rangeFrom = from == null ? data.defaultFrom() : from;
		LocalDate rangeTo = to == null ? data.defaultTo() : to;
		List<PreviewInterview> requests = data.createdBy(authentication.getName()).stream()
				.filter(interview -> interview.status() == InterviewStatus.RESCHEDULE_REQUESTED)
				.filter(interview -> interview.endsAt().isAfter(data.now())).toList();

		model.addAttribute("bulk", bulk);
		model.addAttribute("mode", bulk ? "bulk" : "single");
		model.addAttribute("requests", requests);
		model.addAttribute("interviewers", data.usersWithRole(UserRole.INTERVIEWER));
		model.addAttribute("selectedInterviewIds", selectedInterviews);
		model.addAttribute("selectedInterviewerIds", selectedInterviewers);
		model.addAttribute("from", rangeFrom);
		model.addAttribute("to", rangeTo);

		if (!propose) {
			return "recruiter/reschedule";
		}
		List<PreviewInterview> chosen = requests.stream()
				.filter(interview -> selectedInterviews.contains(interview.id())).toList();
		List<PreviewUser> extras = data.usersWithIds(selectedInterviewers, UserRole.INTERVIEWER);
		List<String> errors = new ArrayList<>();
		if (chosen.isEmpty()) {
			errors.add(bulk ? "Pick at least one reschedule request." : "Pick a reschedule request.");
		}
		if (!bulk && chosen.size() > 1) {
			errors.add("Single mode handles one request. Switch to bulk to handle several.");
		}
		errors.addAll(rangeErrors(rangeFrom, rangeTo));
		model.addAttribute("errors", errors);
		if (!errors.isEmpty()) {
			return "recruiter/reschedule";
		}
		model.addAttribute("proposed", true);
		if (bulk) {
			model.addAttribute("bulkProposal", data.bulkReschedule(chosen, extras, rangeFrom, rangeTo));
		}
		else {
			PreviewInterview interview = chosen.get(0);
			List<PreviewUser> pool = data.poolFor(interview, extras);
			model.addAttribute("rescheduleInterview", interview);
			model.addAttribute("pool", pool);
			model.addAttribute("options", data.rescheduleOptions(interview, pool, rangeFrom, rangeTo));
			model.addAttribute("requestUsable", interview.requestedStartsAt() != null
					&& data.isValidSlot(interview.requestedStartsAt(), interview.durationMinutes()));
		}
		return "recruiter/reschedule";
	}

	@PostMapping("/reschedule/confirm")
	public String confirmReschedule(@RequestParam(defaultValue = "single") String mode,
			@RequestParam(required = false) Integer option, @RequestParam(defaultValue = "0") int count,
			RedirectAttributes redirect) {
		if ("bulk".equals(mode)) {
			redirect.addFlashAttribute("notice", "Preview only: " + count
					+ " replacements were not saved. Each saved replacement returns to Awaiting candidate.");
		}
		else {
			redirect.addFlashAttribute("notice", "Preview only: option " + (option == null ? "?" : option)
					+ " was not saved. On save the interview returns to Awaiting candidate and the candidate must accept.");
		}
		return "redirect:/dashboard/recruiter";
	}

	private List<String> rangeErrors(LocalDate from, LocalDate to) {
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
			hasWeekday = day.getDayOfWeek() != DayOfWeek.SATURDAY && day.getDayOfWeek() != DayOfWeek.SUNDAY;
		}
		if (!hasWeekday) {
			errors.add("The range has no weekday. Slots are Monday to Friday only.");
		}
		return errors;
	}

	private static long count(List<PreviewInterview> interviews, InterviewStatus status) {
		return interviews.stream().filter(interview -> interview.status() == status).count();
	}

	private static List<Long> orEmpty(List<Long> ids) {
		return ids == null ? List.of() : ids.stream().filter(id -> id != null).toList();
	}

}
