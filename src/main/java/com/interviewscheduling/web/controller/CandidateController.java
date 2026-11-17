package com.interviewscheduling.web.controller;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.interviewscheduling.dto.InterviewView;
import com.interviewscheduling.service.InterviewService;
import com.interviewscheduling.web.form.NewStartForm;

@Controller
@RequestMapping("/dashboard/candidate")
public class CandidateController {

	private static final DateTimeFormatter SLOT_FORMAT = DateTimeFormatter.ofPattern("EEE d MMM yyyy, HH:mm",
			Locale.ENGLISH);

	private final InterviewService interviews;

	public CandidateController(InterviewService interviews) {
		this.interviews = interviews;
	}

	@GetMapping
	public String home(Authentication authentication, Model model) {
		String email = authentication.getName();
		model.addAttribute("upcoming", interviews.upcomingForCandidate(email));
		model.addAttribute("past", interviews.pastForCandidate(email));
		model.addAttribute("today", interviews.today());
		return "candidate/home";
	}

	@PostMapping("/interviews/{id}/accept")
	public String accept(Authentication authentication, @PathVariable Long id, RedirectAttributes redirect) {
		InterviewView interview = interviews.accept(authentication.getName(), id);
		redirect.addFlashAttribute("notice",
				"You accepted the interview on " + SLOT_FORMAT.format(interview.startsAt()) + " UTC.");
		return "redirect:/dashboard/candidate";
	}

	@PostMapping("/interviews/{id}/request-new-start")
	public String requestNewStart(Authentication authentication, @PathVariable Long id,
			@Validated @ModelAttribute NewStartForm form, RedirectAttributes redirect) {
		InterviewView interview = interviews.requestNewStart(authentication.getName(), id, form.start());
		redirect.addFlashAttribute("notice", "Your request for " + SLOT_FORMAT.format(interview.requestedStartsAt())
				+ " UTC was sent. Your current slot stays booked until the recruiter confirms a replacement.");
		return "redirect:/dashboard/candidate";
	}

}
