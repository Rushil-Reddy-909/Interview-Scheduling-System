package com.interviewscheduling.web;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.interviewscheduling.web.preview.PreviewData;
import com.interviewscheduling.web.preview.PreviewInterview;

@Controller
@RequestMapping("/dashboard/candidate")
public class CandidateController {

	private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm",
			Locale.ENGLISH);

	private final PreviewData data;

	public CandidateController(PreviewData data) {
		this.data = data;
	}

	@GetMapping
	public String home(Authentication authentication, Model model) {
		LocalDateTime now = data.now();
		List<PreviewInterview> mine = data.forCandidate(authentication.getName());
		model.addAttribute("upcoming", mine.stream().filter(interview -> interview.endsAt().isAfter(now)).toList());
		model.addAttribute("past", mine.stream().filter(interview -> !interview.endsAt().isAfter(now))
				.sorted(Comparator.comparing(PreviewInterview::startsAt).reversed()).toList());
		model.addAttribute("today", now.toLocalDate());
		return "candidate/home";
	}

	@PostMapping("/interviews/{id}/accept")
	public String accept(Authentication authentication, @PathVariable Long id, RedirectAttributes redirect) {
		Optional<PreviewInterview> interview = own(authentication, id);
		if (interview.isEmpty() || !interview.get().canAccept()) {
			redirect.addFlashAttribute("error", "Only an interview that is awaiting your answer can be accepted.");
		}
		else {
			redirect.addFlashAttribute("notice", "Preview only: you accepted the interview on "
					+ SLOT_FORMAT.format(interview.get().startsAt()) + " UTC, but nothing was saved.");
		}
		return "redirect:/dashboard/candidate";
	}

	@PostMapping("/interviews/{id}/request-new-start")
	public String requestNewStart(Authentication authentication, @PathVariable Long id,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
			@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time, RedirectAttributes redirect) {
		Optional<PreviewInterview> interview = own(authentication, id);
		if (interview.isEmpty() || !interview.get().canRequestNewStart()) {
			redirect.addFlashAttribute("error", "A new start can be requested only before the recruiter replies.");
			return "redirect:/dashboard/candidate";
		}
		LocalDateTime start = date.atTime(time);
		if (!data.isValidSlot(start, interview.get().durationMinutes())) {
			redirect.addFlashAttribute("error",
					"Pick Monday to Friday, with the whole interview between 09:00 and 17:00 UTC.");
		}
		else if (!start.isAfter(data.now())) {
			redirect.addFlashAttribute("error", "Pick a start in the future.");
		}
		else {
			redirect.addFlashAttribute("notice", "Preview only: your request for " + SLOT_FORMAT.format(start)
					+ " UTC was not sent. Your current slot stays booked until the recruiter confirms a replacement.");
		}
		return "redirect:/dashboard/candidate";
	}

	private Optional<PreviewInterview> own(Authentication authentication, Long id) {
		return data.interview(id)
				.filter(interview -> interview.candidate().email().equalsIgnoreCase(authentication.getName()));
	}

}
