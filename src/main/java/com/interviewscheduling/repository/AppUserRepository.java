package com.interviewscheduling.repository;

import java.util.List;
import java.util.Optional;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.enums.UserRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {

	boolean existsByRole(UserRole role);

	long countByRole(UserRole role);

	Optional<AppUser> findByEmail(String email);

	Optional<AppUser> findByEmailIgnoreCase(String email);

	List<AppUser> findByRoleOrderByNameAsc(UserRole role);

	/**
	 * {@code pattern} is a lowercase {@code like} pattern, or empty to match everyone.
	 */
	@Query("""
			select u from AppUser u
			where (:role is null or u.role = :role)
			  and (:pattern = '' or lower(u.name) like :pattern escape '\\' or lower(u.email) like :pattern escape '\\')
			order by lower(u.name), u.id
			""")
	List<AppUser> search(@Param("role") UserRole role, @Param("pattern") String pattern);

}
