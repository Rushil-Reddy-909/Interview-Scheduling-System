package com.interviewscheduling.web;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.interviewscheduling.web.preview.PreviewData;

@Controller
@RequestMapping("/dashboard/interviewer")
public class InterviewerController {

	private final PreviewData data;

	public InterviewerController(PreviewData data) {
		this.data = data;
	}

	@GetMapping
	public String home(Authentication authentication,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
			Model model) {
		String email = authentication.getName();
		LocalDateTime now = data.now();
		LocalDate shown = week == null ? data.weekOfNextInterview(email) : week;
		model.addAttribute("calendar", data.weekCalendar(email, shown));
		model.addAttribute("thisWeek", now.toLocalDate().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)));
		model.addAttribute("upcoming",
				data.forInterviewer(email).stream().filter(interview -> interview.endsAt().isAfter(now)).toList());
		return "interviewer/home";
	}

}
