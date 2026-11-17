package com.interviewscheduling.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http, RoleBasedLoginSuccessHandler successHandler)
			throws Exception {
		http
				.authorizeHttpRequests(auth -> auth
						.requestMatchers("/", "/login", "/css/**", "/js/**", "/h2-console/**").permitAll()
						.requestMatchers("/admin", "/admin/**").hasRole("ADMIN")
						.requestMatchers("/dashboard/recruiter", "/dashboard/recruiter/**").hasRole("RECRUITER")
						.requestMatchers("/dashboard/candidate", "/dashboard/candidate/**").hasRole("CANDIDATE")
						.requestMatchers("/dashboard/interviewer", "/dashboard/interviewer/**").hasRole("INTERVIEWER")
						.anyRequest().authenticated())
				.formLogin(form -> form
						.loginPage("/login")
						.usernameParameter("email")
						.failureUrl("/login?error")
						.successHandler(successHandler)
						.permitAll())
				.logout(logout -> logout
						.logoutSuccessUrl("/login?logout")
						.invalidateHttpSession(true))
				.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"))
				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));
		return http.build();
	}

}
