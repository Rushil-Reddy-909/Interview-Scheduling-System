package com.interviewscheduling.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.interviewscheduling.dto.UserView;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.service.InterviewService;
import com.interviewscheduling.service.UserService;
import com.interviewscheduling.web.form.CreateUserForm;
import com.interviewscheduling.web.form.UpdateUserForm;
import com.interviewscheduling.web.model.StatCard;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final UserService users;

	private final InterviewService interviews;

	public AdminController(UserService users, InterviewService interviews) {
		this.users = users;
		this.interviews = interviews;
	}

	@GetMapping
	public String home(Model model) {
		model.addAttribute("userStats", List.of(
				new StatCard("Admins", users.countByRole(UserRole.ADMIN), "/admin/users?role=ADMIN", "neutral"),
				new StatCard("Recruiters", users.countByRole(UserRole.RECRUITER), "/admin/users?role=RECRUITER",
						"neutral"),
				new StatCard("Candidates", users.countByRole(UserRole.CANDIDATE), "/admin/users?role=CANDIDATE",
						"neutral"),
				new StatCard("Interviewers", users.countByRole(UserRole.INTERVIEWER),
						"/admin/users?role=INTERVIEWER", "neutral")));
		model.addAttribute("interviewStats", List.of(
				new StatCard("Awaiting candidate", interviews.countByStatus(InterviewStatus.AWAITING_CANDIDATE),
						"/admin/interviews?status=AWAITING_CANDIDATE", "warning"),
				new StatCard("Accepted", interviews.countByStatus(InterviewStatus.ACCEPTED),
						"/admin/interviews?status=ACCEPTED", "success"),
				new StatCard("Reschedule requested", interviews.countByStatus(InterviewStatus.RESCHEDULE_REQUESTED),
						"/admin/interviews?status=RESCHEDULE_REQUESTED", "purple")));
		model.addAttribute("upcoming", interviews.nextInterviews());
		return "admin/home";
	}

	@GetMapping("/users")
	public String users(@RequestParam(required = false) UserRole role,
			@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "false") boolean add,
			Model model) {
		model.addAttribute("openAddUser", add);
		model.addAttribute("users", users.search(role, q));
		model.addAttribute("roles", UserRole.values());
		model.addAttribute("selectedRole", role == null ? "" : role.name());
		model.addAttribute("q", q);
		model.addAttribute("totalUsers", users.count());
		return "admin/users";
	}

	@PostMapping("/users")
	public String createUser(@Validated @ModelAttribute CreateUserForm form, RedirectAttributes redirect) {
		UserView user = users.create(form.name(), form.email(), form.role(), form.password());
		redirect.addFlashAttribute("notice",
				"Created " + user.name() + " (" + user.email() + ") as " + roleLabel(user.role()) + ".");
		return "redirect:/admin/users";
	}

	@PostMapping("/users/{id}")
	public String updateUser(@PathVariable Long id, @Validated @ModelAttribute UpdateUserForm form,
			RedirectAttributes redirect) {
		UserView user = users.update(id, form.name(), form.email(), form.role(), form.password());
		redirect.addFlashAttribute("notice", "Saved changes to " + user.name()
				+ ". A role or password change applies at their next login.");
		return "redirect:/admin/users";
	}

	@GetMapping("/interviews")
	public String interviews(@RequestParam(required = false) InterviewStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "") String q, Model model) {
		model.addAttribute("interviews", interviews.search(status, from, to, q));
		model.addAttribute("statuses", InterviewStatus.values());
		model.addAttribute("selectedStatus", status == null ? "" : status.name());
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("q", q);
		model.addAttribute("totalInterviews", interviews.count());
		return "admin/interviews";
	}

	private static String roleLabel(UserRole role) {
		return role.name().toLowerCase(Locale.ROOT);
	}

}
