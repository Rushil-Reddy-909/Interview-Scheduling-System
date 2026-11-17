package com.interviewscheduling;

import static com.interviewscheduling.TestClockConfiguration.MONDAY;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.repository.AppUserRepository;
import com.interviewscheduling.repository.InterviewRepository;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class DashboardPageTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private TestData data;

	@Autowired
	private AppUserRepository users;

	@Autowired
	private InterviewRepository interviews;

	private AppUser candidate;

	private AppUser interviewer;

	private Interview awaiting;

	private Interview requested;

	@BeforeEach
	void fixtures() {
		AppUser recruiter = data.seeded(UserRole.RECRUITER);
		candidate = data.seeded(UserRole.CANDIDATE);
		interviewer = data.seeded(UserRole.INTERVIEWER);
		AppUser lina = data.user("Lina Haddad", "lina.haddad@localhost", UserRole.CANDIDATE);
		awaiting = data.interview(recruiter, candidate, interviewer, MONDAY.plusDays(1).atTime(10, 0), 60,
				InterviewStatus.AWAITING_CANDIDATE);
		requested = data.interview(recruiter, lina, interviewer, MONDAY.atTime(13, 30), 60,
				InterviewStatus.RESCHEDULE_REQUESTED, MONDAY.plusDays(1).atTime(16, 0));
	}

	@Test
	void stylesheetIsPublic() throws Exception {
		mockMvc.perform(get("/css/app.css")).andExpect(status().isOk());
		mockMvc.perform(get("/js/app.js")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void adminPagesShowStoredData() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Users by role")))
				.andExpect(content().string(containsString("Lina Haddad")));
		mockMvc.perform(get("/admin/users").param("role", "CANDIDATE"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Lina Haddad")))
				.andExpect(content().string(not(containsString("interviewer@localhost"))))
				.andExpect(content().string(containsString("Add user")));
		mockMvc.perform(get("/admin/interviews").param("status", "RESCHEDULE_REQUESTED"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Asked for")))
				.andExpect(content().string(containsString("Showing 1 of 2 interviews")));
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void adminCreatesAUser() throws Exception {
		mockMvc.perform(post("/admin/users").with(csrf())
						.param("name", "New Person")
						.param("email", "New.Person@Localhost")
						.param("role", "CANDIDATE")
						.param("password", "12345678"))
				.andExpect(redirectedUrl("/admin/users"))
				.andExpect(flash().attribute("notice", containsString("Created New Person")));
		assertEquals(UserRole.CANDIDATE, users.findByEmail("new.person@localhost").orElseThrow().getRole());
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void aRejectedPostReturnsToTheRefererWithTheReason() throws Exception {
		mockMvc.perform(post("/admin/users").with(csrf())
						.header("Referer", "http://localhost/admin/users?role=CANDIDATE")
						.param("name", "Copy")
						.param("email", "candidate@localhost")
						.param("role", "CANDIDATE")
						.param("password", "12345678"))
				.andExpect(redirectedUrl("/admin/users?role=CANDIDATE"))
				.andExpect(flash().attribute("error", containsString("already has the email candidate@localhost")));
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void anInvalidFormFallsBackToTheDashboardForAForeignReferer() throws Exception {
		mockMvc.perform(post("/admin/users").with(csrf())
						.header("Referer", "https://evil.example/admin/users")
						.param("name", "Short")
						.param("email", "short@localhost")
						.param("role", "CANDIDATE")
						.param("password", "123"))
				.andExpect(redirectedUrl("/dashboard"))
				.andExpect(flash().attribute("error", containsString("8 to 72 characters")));
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void theLastAdminCannotLoseTheRole() throws Exception {
		AppUser admin = data.seeded(UserRole.ADMIN);
		mockMvc.perform(post("/admin/users/{id}", admin.getId()).with(csrf())
						.header("Referer", "http://localhost/admin/users")
						.param("name", admin.getName())
						.param("email", admin.getEmail())
						.param("role", "RECRUITER")
						.param("password", ""))
				.andExpect(redirectedUrl("/admin/users"))
				.andExpect(flash().attribute("error", containsString("only admin")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void recruiterOverviewListsRescheduleRequests() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Lina Haddad")))
				.andExpect(content().string(containsString("Find replacement")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void scheduleWithoutCandidateShowsErrors() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule").param("propose", "true"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Pick a candidate.")))
				.andExpect(content().string(containsString("Pick at least one interviewer for the pool.")))
				.andExpect(content().string(not(containsString("Matching is not built yet"))));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void validScheduleInputSaysMatchingIsNotBuiltYet() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule")
						.param("mode", "single")
						.param("propose", "true")
						.param("candidateIds", candidate.getId().toString())
						.param("interviewerIds", interviewer.getId().toString())
						.param("duration", "60")
						.param("from", MONDAY.plusWeeks(1).toString())
						.param("to", MONDAY.plusWeeks(1).plusDays(4).toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Matching is not built yet")));
		mockMvc.perform(get("/dashboard/recruiter/reschedule")
						.param("mode", "single")
						.param("propose", "true")
						.param("interviewIds", requested.getId().toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Lina Haddad")))
				.andExpect(content().string(containsString("Matching is not built yet")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void confirmEndpointsAreGone() throws Exception {
		mockMvc.perform(post("/dashboard/recruiter/schedule/confirm").with(csrf()).param("option", "1"))
				.andExpect(status().isNotFound());
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void candidateSeesActionsForTheirInterviews() throws Exception {
		mockMvc.perform(get("/dashboard/candidate"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Accept")))
				.andExpect(content().string(containsString("Request new start")))
				.andExpect(content().string(not(containsString("Lina Haddad"))));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void candidateAcceptsAnInterview() throws Exception {
		mockMvc.perform(post("/dashboard/candidate/interviews/{id}/accept", awaiting.getId()).with(csrf()))
				.andExpect(redirectedUrl("/dashboard/candidate"))
				.andExpect(flash().attribute("notice", containsString("You accepted the interview on Tue 24 Nov 2026, 10:00")));
		assertEquals(InterviewStatus.ACCEPTED, interviews.findById(awaiting.getId()).orElseThrow().getStatus());
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void candidateRequestsANewStart() throws Exception {
		mockMvc.perform(post("/dashboard/candidate/interviews/{id}/request-new-start", awaiting.getId()).with(csrf())
						.param("date", MONDAY.plusDays(2).toString())
						.param("time", "15:00"))
				.andExpect(redirectedUrl("/dashboard/candidate"))
				.andExpect(flash().attribute("notice", containsString("was sent")));
		Interview saved = interviews.findById(awaiting.getId()).orElseThrow();
		assertEquals(InterviewStatus.RESCHEDULE_REQUESTED, saved.getStatus());
		assertTrue(saved.getRequestedStartsAt().toString().startsWith("2026-11-25T15:00"));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void weekendRequestIsRejected() throws Exception {
		mockMvc.perform(post("/dashboard/candidate/interviews/{id}/request-new-start", awaiting.getId()).with(csrf())
						.header("Referer", "http://localhost/dashboard/candidate")
						.param("date", MONDAY.plusDays(5).toString())
						.param("time", "10:00"))
				.andExpect(redirectedUrl("/dashboard/candidate"))
				.andExpect(flash().attribute("error", containsString("Pick a weekday")));
		assertEquals(InterviewStatus.AWAITING_CANDIDATE,
				interviews.findById(awaiting.getId()).orElseThrow().getStatus());
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void aMissingDateIsReported() throws Exception {
		mockMvc.perform(post("/dashboard/candidate/interviews/{id}/request-new-start", awaiting.getId()).with(csrf())
						.header("Referer", "http://localhost/dashboard/candidate")
						.param("time", "10:00"))
				.andExpect(redirectedUrl("/dashboard/candidate"))
				.andExpect(flash().attribute("error", containsString("Pick a date.")));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void anotherCandidatesInterviewIsNotFound() throws Exception {
		mockMvc.perform(post("/dashboard/candidate/interviews/{id}/accept", requested.getId()).with(csrf()))
				.andExpect(redirectedUrl("/dashboard"))
				.andExpect(flash().attribute("error", containsString("not found")));
	}

	@Test
	@WithMockUser(username = "interviewer@localhost", roles = "INTERVIEWER")
	void interviewerSeesWeekCalendar() throws Exception {
		mockMvc.perform(get("/dashboard/interviewer"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("My calendar")))
				.andExpect(content().string(containsString("23 Nov")))
				.andExpect(content().string(containsString("Lina Haddad")))
				.andExpect(content().string(containsString("Has an interview")));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void candidateCannotOpenRecruiterPages() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule")).andExpect(status().isForbidden());
	}

}
