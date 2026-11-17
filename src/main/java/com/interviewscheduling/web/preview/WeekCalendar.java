package com.interviewscheduling.web.preview;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public record WeekCalendar(LocalDate weekStart, List<LocalTime> times, List<Day> days) {

	public LocalDate weekEnd() {
		return weekStart.plusDays(4);
	}

	public LocalDate previousWeek() {
		return weekStart.minusWeeks(1);
	}

	public LocalDate nextWeek() {
		return weekStart.plusWeeks(1);
	}

	public record Day(LocalDate date, List<Cell> cells, int interviewCount, int freeMinutes, boolean today) {
	}

	public record Cell(LocalTime time, PreviewInterview interview, boolean first, int span) {

		public boolean free() {
			return interview == null;
		}

	}

}
