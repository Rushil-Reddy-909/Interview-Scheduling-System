package com.interviewscheduling;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.interviewscheduling.web.preview.PreviewData;

@SpringBootTest
@AutoConfigureMockMvc
class DashboardPageTests {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private PreviewData data;

	@Test
	void stylesheetIsPublic() throws Exception {
		mockMvc.perform(get("/css/app.css")).andExpect(status().isOk());
		mockMvc.perform(get("/js/app.js")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void adminPagesRender() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Users by role")))
				.andExpect(content().string(containsString("Interviews by status")));
		mockMvc.perform(get("/admin/users").param("role", "CANDIDATE"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Asha Verma")))
				.andExpect(content().string(not(containsString("Marcus Lee"))))
				.andExpect(content().string(containsString("Add user")));
		mockMvc.perform(get("/admin/interviews").param("status", "RESCHEDULE_REQUESTED"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Asked for")));
	}

	@Test
	@WithMockUser(username = "admin@localhost", roles = "ADMIN")
	void adminActionsAreNotSaved() throws Exception {
		mockMvc.perform(post("/admin/users").with(csrf())
						.param("name", "New Person")
						.param("email", "new@localhost")
						.param("role", "CANDIDATE")
						.param("password", "12345678"))
				.andExpect(redirectedUrl("/admin/users"))
				.andExpect(flash().attribute("notice", containsString("Preview only")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void recruiterOverviewListsRescheduleRequests() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Reschedule requests")))
				.andExpect(content().string(containsString("Find replacement")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void singleScheduleShowsTopOptions() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule")
						.param("mode", "single")
						.param("propose", "true")
						.param("candidateIds", "6")
						.param("interviewerIds", "9", "10", "11")
						.param("duration", "60")
						.param("from", data.defaultFrom().toString())
						.param("to", data.defaultTo().toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Top options for Lina Haddad")))
				.andExpect(content().string(containsString("Confirm and create interview")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void bulkScheduleShowsPlacements() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule")
						.param("mode", "bulk")
						.param("propose", "true")
						.param("candidateIds", "5", "6", "7")
						.param("interviewerIds", "10")
						.param("duration", "60")
						.param("from", data.defaultFrom().toString())
						.param("to", data.defaultTo().toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Proposed slots")))
				.andExpect(content().string(containsString("Confirm 3 interviews")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void scheduleWithoutCandidateShowsErrors() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule").param("propose", "true"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Pick a candidate.")))
				.andExpect(content().string(containsString("Pick at least one interviewer for the pool.")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void rescheduleShowsRequestedStartFirst() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/reschedule")
						.param("mode", "single")
						.param("propose", "true")
						.param("interviewIds", "105"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Replacement options for Lina Haddad")))
				.andExpect(content().string(containsString("Requested start")))
				.andExpect(content().string(containsString("Current interviewer")));
		mockMvc.perform(get("/dashboard/recruiter/reschedule")
						.param("mode", "bulk")
						.param("propose", "true")
						.param("interviewIds", "103", "105", "107"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Proposed replacements")));
	}

	@Test
	@WithMockUser(username = "recruiter@localhost", roles = "RECRUITER")
	void confirmIsNotSaved() throws Exception {
		mockMvc.perform(post("/dashboard/recruiter/schedule/confirm").with(csrf()).param("option", "1"))
				.andExpect(redirectedUrl("/dashboard/recruiter"))
				.andExpect(flash().attribute("notice", containsString("Preview only")));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void candidateSeesActionsForTheirInterviews() throws Exception {
		mockMvc.perform(get("/dashboard/candidate"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Accept")))
				.andExpect(content().string(containsString("Request new start")))
				.andExpect(content().string(containsString("You asked for")));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void weekendRequestIsRejected() throws Exception {
		mockMvc.perform(post("/dashboard/candidate/interviews/101/request-new-start").with(csrf())
						.param("date", data.defaultFrom().plusDays(5).toString())
						.param("time", "10:00"))
				.andExpect(redirectedUrl("/dashboard/candidate"))
				.andExpect(flash().attribute("error", containsString("Monday to Friday")));
	}

	@Test
	@WithMockUser(username = "interviewer@localhost", roles = "INTERVIEWER")
	void interviewerSeesWeekCalendar() throws Exception {
		mockMvc.perform(get("/dashboard/interviewer").param("week", data.defaultFrom().toString()))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("My calendar")))
				.andExpect(content().string(containsString("Rohan Mehta")))
				.andExpect(content().string(containsString("Has an interview")));
	}

	@Test
	@WithMockUser(username = "candidate@localhost", roles = "CANDIDATE")
	void candidateCannotOpenRecruiterPages() throws Exception {
		mockMvc.perform(get("/dashboard/recruiter/schedule")).andExpect(status().isForbidden());
	}

}
