package com.interviewscheduling.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.repository.AppUserRepository;
import com.interviewscheduling.enums.UserRole;

@Configuration
@EnableConfigurationProperties(AdminProperties.class)
public class AdminSeedConfiguration {

	private static final Logger log = LoggerFactory.getLogger(AdminSeedConfiguration.class);

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	@Order(0)
	ApplicationRunner userSeed(AppUserRepository users, AdminProperties admin, PasswordEncoder passwordEncoder) {
		return args -> {
			for (UserRole role : UserRole.values()) {
				if (users.existsByRole(role)) {
					continue;
				}
				String email = role.name().toLowerCase() + "@localhost";
				if (users.findByEmail(email).isPresent()) {
					throw new IllegalStateException(
							"Cannot seed " + role + " because that email already belongs to another user: " + email);
				}
				AppUser user = new AppUser();
				user.setName(displayName(role));
				user.setEmail(email);
				user.setPasswordHash(passwordEncoder.encode(admin.password()));
				user.setRole(role);
				users.save(user);
				log.info("Seeded bootstrap {} {}", role, email);
			}
		};
	}

	private static String displayName(UserRole role) {
		String lower = role.name().toLowerCase();
		return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
	}

}
