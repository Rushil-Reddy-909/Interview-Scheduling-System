package com.interviewscheduling.repository;

import java.util.Optional;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByRole(UserRole role);

	long countByRole(UserRole role);

	Optional<AppUser> findByEmail(String email);
}
