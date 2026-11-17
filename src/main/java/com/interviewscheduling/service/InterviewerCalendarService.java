package com.interviewscheduling.service;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.interviewscheduling.dto.InterviewView;
import com.interviewscheduling.dto.WeekCalendar;
import com.interviewscheduling.repository.InterviewRepository;

@Service
@Transactional(readOnly = true)
public class InterviewerCalendarService {

	private static final int WORKDAYS = 5;

	private final InterviewRepository interviews;

	private final Clock clock;

	public InterviewerCalendarService(InterviewRepository interviews, Clock clock) {
		this.interviews = interviews;
		this.clock = clock;
	}

	public LocalDate thisWeek() {
		return monday(LocalDate.now(clock));
	}

	public List<InterviewView> upcoming(String email) {
		Instant now = clock.instant();
		return interviews.findUpcomingForInterviewer(email, now).stream()
				.map(interview -> InterviewView.from(interview, now)).toList();
	}

	/**
	 * The Monday of the interviewer's next interview, or of this week when nothing is booked.
	 */
	public LocalDate weekOfNextInterview(String email) {
		return upcoming(email).stream().findFirst().map(interview -> monday(interview.date())).orElseGet(this::thisWeek);
	}

	/**
	 * Monday to Friday of the week holding {@code anyDay}, in 15-minute cells from 09:00 to 17:00 UTC.
	 */
	public WeekCalendar week(String email, LocalDate anyDay) {
		Instant now = clock.instant();
		LocalDate monday = monday(anyDay);
		LocalDate today = LocalDate.now(clock);
		List<InterviewView> booked = interviews
				.findForInterviewerBetween(email, SlotRules.toInstant(monday.atStartOfDay()),
						SlotRules.toInstant(monday.plusDays(WORKDAYS).atStartOfDay()))
				.stream().map(interview -> InterviewView.from(interview, now)).toList();

		List<LocalTime> times = new ArrayList<>();
		for (LocalTime time = SlotRules.DAY_START; time.isBefore(SlotRules.DAY_END); time = time
				.plusMinutes(SlotRules.GRID_MINUTES)) {
			times.add(time);
		}

		List<WeekCalendar.Day> days = new ArrayList<>();
		for (int offset = 0; offset < WORKDAYS; offset++) {
			LocalDate date = monday.plusDays(offset);
			List<InterviewView> raw = new ArrayList<>();
			for (LocalTime time : times) {
				LocalDateTime start = date.atTime(time);
				LocalDateTime end = start.plusMinutes(SlotRules.GRID_MINUTES);
				raw.add(booked.stream().filter(interview -> interview.overlaps(start, end)).findFirst().orElse(null));
			}
			days.add(day(date, times, raw, date.equals(today)));
		}
		return new WeekCalendar(monday, times, days);
	}

	private static WeekCalendar.Day day(LocalDate date, List<LocalTime> times, List<InterviewView> raw,
			boolean today) {
		List<WeekCalendar.Cell> cells = new ArrayList<>();
		for (int index = 0; index < raw.size(); index++) {
			InterviewView interview = raw.get(index);
			boolean first = interview != null && (index == 0 || raw.get(index - 1) != interview);
			int span = 0;
			if (first) {
				while (index + span < raw.size() && raw.get(index + span) == interview) {
					span++;
				}
			}
			cells.add(new WeekCalendar.Cell(times.get(index), interview, first, span));
		}
		int freeMinutes = (int) raw.stream().filter(Objects::isNull).count() * SlotRules.GRID_MINUTES;
		int count = (int) raw.stream().filter(Objects::nonNull).distinct().count();
		return new WeekCalendar.Day(date, cells, count, freeMinutes, today);
	}

	private static LocalDate monday(LocalDate day) {
		return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}

}
