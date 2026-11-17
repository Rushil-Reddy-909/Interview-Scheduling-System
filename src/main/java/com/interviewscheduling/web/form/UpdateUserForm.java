package com.interviewscheduling.web.form;

import com.interviewscheduling.enums.UserRole;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * A blank password keeps the current one.
 */
public record UpdateUserForm(
		@NotBlank(message = "Enter a name.") @Size(max = 120, message = "Keep the name under 120 characters.") String name,
		@NotBlank(message = "Enter an email.") @Email(message = "Enter a valid email.") String email,
		@NotNull(message = "Pick a role.") UserRole role,
		@Pattern(regexp = "^$|^.{8,72}$", message = "The password must have 8 to 72 characters.") String password) {
}
