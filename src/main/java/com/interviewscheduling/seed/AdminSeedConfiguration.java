package com.interviewscheduling.seed;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
	ApplicationRunner adminSeed(AppUserRepository users, AdminProperties admin, PasswordEncoder passwordEncoder) {
		return args -> {
			if (users.existsByRole(UserRole.ADMIN)) {
				return;
			}
			if (users.findByEmail(admin.email()).isPresent()) {
				throw new IllegalStateException(
						"Cannot seed admin because that email already belongs to another user: " + admin.email());
			}
			AppUser user = new AppUser();
			user.setName("Admin");
			user.setEmail(admin.email());
			user.setPasswordHash(passwordEncoder.encode(admin.password()));
			user.setRole(UserRole.ADMIN);
			users.save(user);
			log.info("Seeded bootstrap admin {}", admin.email());
		};
	}

}
