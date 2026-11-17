package com.interviewscheduling;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class LoginTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	void homeAndLoginArePublic() throws Exception {
		mockMvc.perform(get("/"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("Log in")));

		mockMvc.perform(get("/login"))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("name=\"email\"")))
				.andExpect(content().string(containsString("name=\"password\"")));
	}

	@Test
	void adminLoginStoresTheRoleOnTheSession() throws Exception {
		MvcResult login = mockMvc.perform(post("/login")
						.param("email", "admin@localhost")
						.param("password", "12345678")
						.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/admin"))
				.andExpect(authenticated().withUsername("admin@localhost").withRoles("ADMIN"))
				.andReturn();

		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		mockMvc.perform(get("/admin").session(session))
				.andExpect(status().isOk())
				.andExpect(content().string(containsString("admin@localhost")))
				.andExpect(content().string(containsString("role-admin")));
	}

	@Test
	void recruiterLoginLandsOnTheRecruiterDashboard() throws Exception {
		mockMvc.perform(post("/login")
						.param("email", "recruiter@localhost")
						.param("password", "12345678")
						.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/dashboard/recruiter"))
				.andExpect(authenticated().withUsername("recruiter@localhost").withRoles("RECRUITER"));
	}

	@Test
	void wrongPasswordReturnsToLogin() throws Exception {
		mockMvc.perform(post("/login")
						.param("email", "admin@localhost")
						.param("password", "wrong-password")
						.with(csrf()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login?error"));
	}

	@Test
	void anonymousAdminRedirectsToLogin() throws Exception {
		mockMvc.perform(get("/admin"))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login"));
	}

	@Test
	void logoutInvalidatesTheSession() throws Exception {
		MvcResult login = mockMvc.perform(post("/login")
						.param("email", "admin@localhost")
						.param("password", "12345678")
						.with(csrf()))
				.andExpect(redirectedUrl("/admin"))
				.andReturn();

		MockHttpSession session = (MockHttpSession) login.getRequest().getSession(false);

		mockMvc.perform(post("/logout").session(session).with(csrf()))
				.andExpect(status().isFound())
				.andExpect(redirectedUrl("/login?logout"));

		assertTrue(session.isInvalid());
	}

}
