package com.interviewscheduling.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;

import com.interviewscheduling.exception.SlotRejectedException;

/**
 * The rules every slot follows, whatever its overlaps: a weekday, inside working hours, on the grid, and in the
 * future. All times are UTC.
 */
@Component
public class SlotRules {

	public static final LocalTime DAY_START = LocalTime.of(9, 0);

	public static final LocalTime DAY_END = LocalTime.of(17, 0);

	public static final int GRID_MINUTES = 15;

	public static final int MAX_DURATION_MINUTES = 480;

	private final Clock clock;

	public SlotRules(Clock clock) {
		this.clock = clock;
	}

	public void check(LocalDateTime start, int durationMinutes) {
		if (durationMinutes < GRID_MINUTES || durationMinutes > MAX_DURATION_MINUTES
				|| durationMinutes % GRID_MINUTES != 0) {
			throw new SlotRejectedException("The duration must be a multiple of 15 minutes, from 15 to 480.");
		}
		if (!isWeekday(start.toLocalDate())) {
			throw new SlotRejectedException("Pick a weekday. Interviews run Monday to Friday.");
		}
		LocalDateTime end = start.plusMinutes(durationMinutes);
		if (start.toLocalTime().isBefore(DAY_START) || !end.toLocalDate().equals(start.toLocalDate())
				|| end.toLocalTime().isAfter(DAY_END)) {
			throw new SlotRejectedException("The whole interview must fit between 09:00 and 17:00 UTC.");
		}
		if (start.getMinute() % GRID_MINUTES != 0 || start.getSecond() != 0 || start.getNano() != 0) {
			throw new SlotRejectedException("Start on a quarter hour: :00, :15, :30, or :45.");
		}
		if (!toInstant(start).isAfter(clock.instant())) {
			throw new SlotRejectedException("Pick a start in the future.");
		}
	}

	public boolean isValid(LocalDateTime start, int durationMinutes) {
		try {
			check(start, durationMinutes);
			return true;
		}
		catch (SlotRejectedException ex) {
			return false;
		}
	}

	public static List<LocalTime> startsFor(int durationMinutes) {
		List<LocalTime> starts = new ArrayList<>();
		LocalTime last = DAY_END.minusMinutes(durationMinutes);
		for (LocalTime time = DAY_START; !time.isAfter(last) && durationMinutes > 0; time = time
				.plusMinutes(GRID_MINUTES)) {
			starts.add(time);
		}
		return starts;
	}

	public static boolean isWeekday(LocalDate date) {
		return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
	}

	public static Instant toInstant(LocalDateTime utc) {
		return utc.toInstant(ZoneOffset.UTC);
	}

	public static LocalDateTime toUtc(Instant instant) {
		return instant == null ? null : LocalDateTime.ofInstant(instant, ZoneOffset.UTC);
	}

}
