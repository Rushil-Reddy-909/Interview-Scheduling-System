package com.interviewscheduling.web.form;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.NotNull;

/**
 * The candidate's preferred new start, in UTC.
 */
public record NewStartForm(
		@NotNull(message = "Pick a date.") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
		@NotNull(message = "Pick a start time.") @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time) {

	public LocalDateTime start() {
		return date.atTime(time);
	}

}
