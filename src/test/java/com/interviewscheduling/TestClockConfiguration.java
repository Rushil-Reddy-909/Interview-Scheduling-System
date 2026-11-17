package com.interviewscheduling;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Every test context sees the same "now": Monday 23 November 2026, 08:00 UTC, an hour before the working day.
 */
@Configuration
public class TestClockConfiguration {

	public static final Instant NOW = Instant.parse("2026-11-23T08:00:00Z");

	public static final LocalDate MONDAY = LocalDate.of(2026, 11, 23);

	@Bean
	@Primary
	Clock fixedClock() {
		return Clock.fixed(NOW, ZoneOffset.UTC);
	}

}
