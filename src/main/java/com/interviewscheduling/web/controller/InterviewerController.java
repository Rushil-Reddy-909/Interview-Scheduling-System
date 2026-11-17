package com.interviewscheduling.web.controller;

import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.interviewscheduling.service.InterviewerCalendarService;

@Controller
@RequestMapping("/dashboard/interviewer")
public class InterviewerController {

	private final InterviewerCalendarService calendar;

	public InterviewerController(InterviewerCalendarService calendar) {
		this.calendar = calendar;
	}

	@GetMapping
	public String home(Authentication authentication,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate week,
			Model model) {
		String email = authentication.getName();
		LocalDate shown = week == null ? calendar.weekOfNextInterview(email) : week;
		model.addAttribute("calendar", calendar.week(email, shown));
		model.addAttribute("thisWeek", calendar.thisWeek());
		model.addAttribute("upcoming", calendar.upcoming(email));
		return "interviewer/home";
	}

}
