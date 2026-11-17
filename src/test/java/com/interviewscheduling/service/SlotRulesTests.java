package com.interviewscheduling.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.interviewscheduling.TestClockConfiguration;
import com.interviewscheduling.exception.SlotRejectedException;

class SlotRulesTests {

	private final SlotRules rules = new SlotRules(Clock.fixed(TestClockConfiguration.NOW, ZoneOffset.UTC));

	private static final LocalDateTime MONDAY_9 = TestClockConfiguration.MONDAY.atTime(9, 0);

	@Test
	void acceptsAWeekdaySlotInsideWorkingHours() {
		rules.check(MONDAY_9, 60);
		rules.check(MONDAY_9.withHour(16), 60);
		assertTrue(rules.isValid(MONDAY_9.plusDays(4).withHour(13).withMinute(45), 15));
	}

	@Test
	void rejectsAWeekend() {
		assertMessage("Pick a weekday", MONDAY_9.plusDays(5), 60);
	}

	@Test
	void rejectsSlotsOutsideWorkingHours() {
		assertMessage("between 09:00 and 17:00", MONDAY_9.withHour(8).withMinute(45), 30);
		assertMessage("between 09:00 and 17:00", MONDAY_9.withHour(16).withMinute(30), 60);
	}

	@Test
	void rejectsStartsOffTheGrid() {
		assertMessage("quarter hour", MONDAY_9.withMinute(10), 60);
	}

	@Test
	void rejectsStartsInThePast() {
		assertMessage("in the future", MONDAY_9.minusDays(3), 60);
	}

	@Test
	void rejectsDurationsOffTheGrid() {
		assertMessage("multiple of 15", MONDAY_9, 50);
		assertMessage("multiple of 15", MONDAY_9, 0);
		assertFalse(rules.isValid(MONDAY_9, 495));
	}

	@Test
	void listsStartsThatEndByFive() {
		List<LocalTime> starts = SlotRules.startsFor(60);
		assertEquals(LocalTime.of(9, 0), starts.get(0));
		assertEquals(LocalTime.of(16, 0), starts.get(starts.size() - 1));
		assertEquals(29, starts.size());
		assertEquals(List.of(LocalTime.of(9, 0)), SlotRules.startsFor(480));
	}

	private void assertMessage(String expected, LocalDateTime start, int duration) {
		SlotRejectedException ex = assertThrows(SlotRejectedException.class, () -> rules.check(start, duration));
		assertTrue(ex.getMessage().contains(expected), ex.getMessage());
	}

}
