package com.interviewscheduling.service;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.domain.AppUser;
import com.interviewscheduling.dto.UserView;
import com.interviewscheduling.enums.UserRole;
import com.interviewscheduling.exception.DuplicateEmailException;
import com.interviewscheduling.exception.NotFoundException;
import com.interviewscheduling.exception.UserChangeRejectedException;
import com.interviewscheduling.repository.AppUserRepository;
import com.interviewscheduling.repository.InterviewRepository;

@Service
@Transactional(readOnly = true)
public class UserService {

	public static final int MIN_PASSWORD_LENGTH = 8;

	/** BCrypt ignores bytes after 72. */
	public static final int MAX_PASSWORD_LENGTH = 72;

	private final AppUserRepository users;

	private final InterviewRepository interviews;

	private final PasswordEncoder passwordEncoder;

	public UserService(AppUserRepository users, InterviewRepository interviews, PasswordEncoder passwordEncoder) {
		this.users = users;
		this.interviews = interviews;
		this.passwordEncoder = passwordEncoder;
	}

	public List<UserView> search(UserRole role, String query) {
		return users.search(role, LikePattern.contains(query)).stream().map(UserView::from).toList();
	}

	public List<UserView> withRole(UserRole role) {
		return users.findByRoleOrderByNameAsc(role).stream().map(UserView::from).toList();
	}

	public long count() {
		return users.count();
	}

	public long countByRole(UserRole role) {
		return users.countByRole(role);
	}

	public Optional<UserView> findByEmail(String email) {
		return users.findByEmailIgnoreCase(normalizeEmail(email)).map(UserView::from);
	}

	@Transactional
	public UserView create(String name, String email, UserRole role, String password) {
		String normalized = normalizeEmail(email);
		if (users.findByEmailIgnoreCase(normalized).isPresent()) {
			throw new DuplicateEmailException(normalized);
		}
		checkPassword(password);
		AppUser user = new AppUser();
		user.setName(normalizeName(name));
		user.setEmail(normalized);
		user.setRole(role);
		user.setPasswordHash(passwordEncoder.encode(password));
		return UserView.from(users.save(user));
	}

	/**
	 * A blank password keeps the current one.
	 */
	@Transactional
	public UserView update(Long id, String name, String email, UserRole role, String password) {
		AppUser user = users.findById(id).orElseThrow(() -> new NotFoundException("That user was not found."));
		String normalized = normalizeEmail(email);
		if (users.findByEmailIgnoreCase(normalized).filter(other -> !other.getId().equals(id)).isPresent()) {
			throw new DuplicateEmailException(normalized);
		}
		if (user.getRole() != role) {
			checkRoleChange(user);
		}
		boolean newPassword = password != null && !password.isBlank();
		if (newPassword) {
			checkPassword(password);
		}
		user.setName(normalizeName(name));
		user.setEmail(normalized);
		user.setRole(role);
		if (newPassword) {
			user.setPasswordHash(passwordEncoder.encode(password));
		}
		return UserView.from(user);
	}

	private void checkRoleChange(AppUser user) {
		if (user.getRole() == UserRole.ADMIN && users.countByRole(UserRole.ADMIN) <= 1) {
			throw new UserChangeRejectedException(
					user.getName() + " is the only admin. Make another user an admin first.");
		}
		if (interviews.involvesUser(user.getId())) {
			throw new UserChangeRejectedException(user.getName() + " is on interviews as a "
					+ user.getRole().name().toLowerCase(Locale.ROOT) + ", so their role cannot change.");
		}
	}

	private static void checkPassword(String password) {
		if (password == null || password.length() < MIN_PASSWORD_LENGTH || password.length() > MAX_PASSWORD_LENGTH) {
			throw new UserChangeRejectedException("The password must have 8 to 72 characters.");
		}
	}

	private static String normalizeEmail(String email) {
		return email == null ? "" : email.trim().toLowerCase(Locale.ROOT);
	}

	private static String normalizeName(String name) {
		return name == null ? "" : name.trim();
	}
}
