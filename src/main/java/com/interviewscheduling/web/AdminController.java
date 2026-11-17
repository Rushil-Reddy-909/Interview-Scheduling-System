package com.interviewscheduling.web;

import java.time.LocalDate;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.web.preview.PreviewData;

@Controller
@RequestMapping("/admin")
public class AdminController {

	private final PreviewData data;

	public AdminController(PreviewData data) {
		this.data = data;
	}

	@GetMapping
	public String home(Model model) {
		model.addAttribute("userStats", List.of(
				new StatCard("Admins", data.countByRole(UserRole.ADMIN), "/admin/users?role=ADMIN", "neutral"),
				new StatCard("Recruiters", data.countByRole(UserRole.RECRUITER), "/admin/users?role=RECRUITER",
						"neutral"),
				new StatCard("Candidates", data.countByRole(UserRole.CANDIDATE), "/admin/users?role=CANDIDATE",
						"neutral"),
				new StatCard("Interviewers", data.countByRole(UserRole.INTERVIEWER),
						"/admin/users?role=INTERVIEWER", "neutral")));
		model.addAttribute("interviewStats", List.of(
				new StatCard("Awaiting candidate", data.countByStatus(InterviewStatus.AWAITING_CANDIDATE),
						"/admin/interviews?status=AWAITING_CANDIDATE", "warning"),
				new StatCard("Accepted", data.countByStatus(InterviewStatus.ACCEPTED),
						"/admin/interviews?status=ACCEPTED", "success"),
				new StatCard("Reschedule requested", data.countByStatus(InterviewStatus.RESCHEDULE_REQUESTED),
						"/admin/interviews?status=RESCHEDULE_REQUESTED", "purple")));
		model.addAttribute("upcoming", data.upcoming().stream().limit(6).toList());
		return "admin/home";
	}

	@GetMapping("/users")
	public String users(@RequestParam(required = false) UserRole role,
			@RequestParam(defaultValue = "") String q, @RequestParam(defaultValue = "false") boolean add,
			Model model) {
		model.addAttribute("openAddUser", add);
		model.addAttribute("users", data.searchUsers(role, q));
		model.addAttribute("roles", UserRole.values());
		model.addAttribute("selectedRole", role == null ? "" : role.name());
		model.addAttribute("q", q);
		model.addAttribute("totalUsers", data.users().size());
		return "admin/users";
	}

	@PostMapping("/users")
	public String createUser(@RequestParam String name, @RequestParam String email, @RequestParam UserRole role,
			RedirectAttributes redirect) {
		redirect.addFlashAttribute("notice", "Preview only: " + name + " (" + email + ", " + role.name()
				+ ") was not created. User management is not connected yet.");
		return "redirect:/admin/users";
	}

	@PostMapping("/users/{id}")
	public String updateUser(@PathVariable Long id, @RequestParam String name, @RequestParam UserRole role,
			RedirectAttributes redirect) {
		redirect.addFlashAttribute("notice", "Preview only: changes to " + name
				+ " were not saved. A saved role or password change applies at the user's next login.");
		return "redirect:/admin/users";
	}

	@GetMapping("/interviews")
	public String interviews(@RequestParam(required = false) InterviewStatus status,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
			@RequestParam(defaultValue = "") String q, Model model) {
		model.addAttribute("interviews", data.searchInterviews(status, from, to, q));
		model.addAttribute("statuses", InterviewStatus.values());
		model.addAttribute("selectedStatus", status == null ? "" : status.name());
		model.addAttribute("from", from);
		model.addAttribute("to", to);
		model.addAttribute("q", q);
		model.addAttribute("totalInterviews", data.interviews().size());
		return "admin/interviews";
	}

}
