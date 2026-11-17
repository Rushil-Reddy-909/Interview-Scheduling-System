package com.interviewscheduling.web.preview;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.interviewscheduling.enums.InterviewStatus;
import com.interviewscheduling.enums.UserRole;

/**
 * In-memory sample users and interviews for the dashboard screens. The scheduling services will replace it.
 */
@Component
public class PreviewData {

	public static final LocalTime DAY_START = LocalTime.of(9, 0);

	public static final LocalTime DAY_END = LocalTime.of(17, 0);

	public static final int GRID_MINUTES = 15;

	private static final int TOP_OPTIONS = 5;

	private final Map<Long, PreviewUser> users = new LinkedHashMap<>();

	private final List<PreviewInterview> interviews = new ArrayList<>();

	private final LocalDate sampleWeek;

	public PreviewData() {
		sampleWeek = LocalDate.now(ZoneOffset.UTC).with(TemporalAdjusters.next(DayOfWeek.MONDAY));
		LocalDateTime joined = sampleWeek.minusWeeks(4).atTime(9, 30);

		addUser(1L, "Admin", "admin@localhost", UserRole.ADMIN, joined);
		addUser(2L, "Priya Raman", "recruiter@localhost", UserRole.RECRUITER, joined.plusDays(1));
		addUser(3L, "Daniel Okafor", "daniel.okafor@localhost", UserRole.RECRUITER, joined.plusDays(2));
		addUser(4L, "Asha Verma", "candidate@localhost", UserRole.CANDIDATE, joined.plusDays(5));
		addUser(5L, "Rohan Mehta", "rohan.mehta@localhost", UserRole.CANDIDATE, joined.plusDays(6));
		addUser(6L, "Lina Haddad", "lina.haddad@localhost", UserRole.CANDIDATE, joined.plusDays(8));
		addUser(7L, "Tom Becker", "tom.becker@localhost", UserRole.CANDIDATE, joined.plusDays(9));
		addUser(8L, "Sara Kim", "sara.kim@localhost", UserRole.CANDIDATE, joined.plusDays(12));
		addUser(9L, "Marcus Lee", "interviewer@localhost", UserRole.INTERVIEWER, joined.plusDays(1));
		addUser(10L, "Elena Petrova", "elena.petrova@localhost", UserRole.INTERVIEWER, joined.plusDays(3));
		addUser(11L, "Kwame Mensah", "kwame.mensah@localhost", UserRole.INTERVIEWER, joined.plusDays(4));

		LocalDate past = sampleWeek.minusWeeks(2);
		addInterview(101L, 2L, 4L, 9L, at(0, 1, 10, 0), 60, InterviewStatus.AWAITING_CANDIDATE, null);
		addInterview(102L, 2L, 4L, 10L, at(0, 3, 14, 0), 45, InterviewStatus.ACCEPTED, null);
		addInterview(103L, 2L, 4L, 9L, at(0, 4, 11, 0), 60, InterviewStatus.RESCHEDULE_REQUESTED, at(0, 2, 15, 0));
		addInterview(104L, 2L, 5L, 9L, at(0, 0, 9, 0), 30, InterviewStatus.ACCEPTED, null);
		addInterview(105L, 2L, 6L, 11L, at(0, 2, 13, 30), 60, InterviewStatus.RESCHEDULE_REQUESTED, at(0, 1, 16, 0));
		addInterview(106L, 3L, 7L, 9L, at(0, 2, 10, 30), 90, InterviewStatus.AWAITING_CANDIDATE, null);
		addInterview(107L, 2L, 8L, 10L, at(0, 1, 15, 0), 60, InterviewStatus.RESCHEDULE_REQUESTED, at(0, 3, 9, 0));
		addInterview(108L, 3L, 5L, 11L, at(0, 4, 9, 30), 45, InterviewStatus.AWAITING_CANDIDATE, null);
		addInterview(110L, 2L, 7L, 9L, at(1, 0, 14, 0), 60, InterviewStatus.AWAITING_CANDIDATE, null);
		addInterview(109L, 2L, 4L, 11L, past.plusDays(2).atTime(10, 0), 60, InterviewStatus.ACCEPTED, null);
		addInterview(111L, 3L, 6L, 10L, past.plusDays(3).atTime(11, 0), 45, InterviewStatus.ACCEPTED, null);
		interviews.sort(Comparator.comparing(PreviewInterview::startsAt));
	}

	public LocalDate defaultFrom() {
		return sampleWeek;
	}

	public LocalDate defaultTo() {
		return sampleWeek.plusDays(4);
	}

	public List<PreviewUser> users() {
		return List.copyOf(users.values());
	}

	public List<PreviewUser> usersWithRole(UserRole role) {
		return users.values().stream().filter(user -> user.role() == role).toList();
	}

	public Optional<PreviewUser> user(Long id) {
		return Optional.ofNullable(users.get(id));
	}

	public Optional<PreviewUser> userByEmail(String email) {
		return users.values().stream().filter(user -> user.email().equalsIgnoreCase(email)).findFirst();
	}

	public List<PreviewUser> usersWithIds(List<Long> ids, UserRole role) {
		return ids.stream().distinct().map(users::get).filter(Objects::nonNull).filter(user -> user.role() == role)
				.sorted(Comparator.comparing(PreviewUser::id)).toList();
	}

	public List<PreviewUser> searchUsers(UserRole role, String query) {
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		return users.values().stream()
				.filter(user -> role == null || user.role() == role)
				.filter(user -> needle.isEmpty() || user.name().toLowerCase(Locale.ROOT).contains(needle)
						|| user.email().toLowerCase(Locale.ROOT).contains(needle))
				.toList();
	}

	public long countByRole(UserRole role) {
		return users.values().stream().filter(user -> user.role() == role).count();
	}

	public List<PreviewInterview> interviews() {
		return List.copyOf(interviews);
	}

	public Optional<PreviewInterview> interview(Long id) {
		return interviews.stream().filter(interview -> interview.id().equals(id)).findFirst();
	}

	public List<PreviewInterview> interviewsWithIds(List<Long> ids) {
		return interviews.stream().filter(interview -> ids.contains(interview.id())).toList();
	}

	public long countByStatus(InterviewStatus status) {
		return interviews.stream().filter(interview -> interview.status() == status).count();
	}

	public List<PreviewInterview> upcoming() {
		LocalDateTime now = now();
		return interviews.stream().filter(interview -> interview.endsAt().isAfter(now)).toList();
	}

	public List<PreviewInterview> searchInterviews(InterviewStatus status, LocalDate from, LocalDate to,
			String query) {
		String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
		return interviews.stream()
				.filter(interview -> status == null || interview.status() == status)
				.filter(interview -> from == null || !interview.date().isBefore(from))
				.filter(interview -> to == null || !interview.date().isAfter(to))
				.filter(interview -> needle.isEmpty() || mentions(interview, needle))
				.toList();
	}

	public List<PreviewInterview> createdBy(String recruiterEmail) {
		return interviews.stream().filter(interview -> interview.recruiter().email().equalsIgnoreCase(recruiterEmail))
				.toList();
	}

	public List<PreviewInterview> forCandidate(String candidateEmail) {
		return interviews.stream().filter(interview -> interview.candidate().email().equalsIgnoreCase(candidateEmail))
				.toList();
	}

	public List<PreviewInterview> forInterviewer(String interviewerEmail) {
		return interviews.stream()
				.filter(interview -> interview.interviewer().email().equalsIgnoreCase(interviewerEmail)).toList();
	}

	public List<PreviewInterview> rescheduleRequests() {
		return interviews.stream().filter(interview -> interview.status() == InterviewStatus.RESCHEDULE_REQUESTED)
				.filter(interview -> interview.endsAt().isAfter(now())).toList();
	}

	public LocalDateTime now() {
		return LocalDateTime.now(ZoneOffset.UTC);
	}

	public boolean isValidSlot(LocalDateTime start, int durationMinutes) {
		LocalDateTime end = start.plusMinutes(durationMinutes);
		return isWeekday(start.toLocalDate()) && !start.toLocalTime().isBefore(DAY_START)
				&& end.toLocalDate().equals(start.toLocalDate()) && !end.toLocalTime().isAfter(DAY_END)
				&& start.getMinute() % GRID_MINUTES == 0;
	}

	public List<SlotOption> createOptions(PreviewUser candidate, List<PreviewUser> pool, int duration, LocalDate from,
			LocalDate to) {
		return createOptions(candidate, pool, duration, from, to, bookings());
	}

	public BulkProposal bulkCreate(List<PreviewUser> candidates, List<PreviewUser> pool, int duration, LocalDate from,
			LocalDate to) {
		List<Booking> bookings = new ArrayList<>(bookings());
		List<BulkProposal.Placement> placed = new ArrayList<>();
		List<BulkProposal.Unplaced> unplaced = new ArrayList<>();
		for (PreviewUser candidate : mostConstrainedFirst(candidates, from, to, bookings)) {
			List<SlotOption> options = createOptions(candidate, pool, duration, from, to, bookings);
			if (options.isEmpty()) {
				unplaced.add(new BulkProposal.Unplaced(candidate, null,
						"No common free gap of " + duration + " minutes with anyone in the pool."));
				continue;
			}
			SlotOption best = options.get(0);
			placed.add(new BulkProposal.Placement(candidate, null, best));
			bookings.add(new Booking(candidate.id(), best.interviewer().id(), best.startsAt(), best.endsAt(), null));
		}
		return new BulkProposal(placed, unplaced);
	}

	public List<SlotOption> rescheduleOptions(PreviewInterview interview, List<PreviewUser> pool, LocalDate from,
			LocalDate to) {
		return rescheduleOptions(interview, pool, from, to, bookings());
	}

	public BulkProposal bulkReschedule(List<PreviewInterview> requests, List<PreviewUser> extraInterviewers,
			LocalDate from, LocalDate to) {
		List<Booking> bookings = new ArrayList<>(bookings());
		List<BulkProposal.Placement> placed = new ArrayList<>();
		List<BulkProposal.Unplaced> unplaced = new ArrayList<>();
		List<PreviewInterview> ordered = requests.stream()
				.sorted(Comparator.comparingInt((PreviewInterview interview) -> freeMinutes(interview.candidate(),
						from, to, bookings)).thenComparing(PreviewInterview::id))
				.toList();
		for (PreviewInterview interview : ordered) {
			List<SlotOption> options = rescheduleOptions(interview, poolFor(interview, extraInterviewers), from, to,
					bookings);
			if (options.isEmpty()) {
				unplaced.add(new BulkProposal.Unplaced(interview.candidate(), interview,
						"Nothing free in the range. It stays reschedule requested and keeps its current slot."));
				continue;
			}
			SlotOption best = options.get(0);
			placed.add(new BulkProposal.Placement(interview.candidate(), interview, best));
			bookings.add(new Booking(interview.candidate().id(), best.interviewer().id(), best.startsAt(),
					best.endsAt(), null));
		}
		return new BulkProposal(placed, unplaced);
	}

	public List<PreviewUser> poolFor(PreviewInterview interview, List<PreviewUser> extraInterviewers) {
		List<PreviewUser> pool = new ArrayList<>(extraInterviewers);
		if (pool.stream().noneMatch(user -> user.id().equals(interview.interviewer().id()))) {
			pool.add(interview.interviewer());
		}
		pool.sort(Comparator.comparing(PreviewUser::id));
		return pool;
	}

	public LocalDate weekOfNextInterview(String interviewerEmail) {
		LocalDateTime now = now();
		LocalDate day = forInterviewer(interviewerEmail).stream().filter(interview -> interview.endsAt().isAfter(now))
				.map(PreviewInterview::date).findFirst().orElse(now.toLocalDate());
		return day.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
	}

	public WeekCalendar weekCalendar(String interviewerEmail, LocalDate anyDay) {
		LocalDate monday = anyDay.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
		LocalDate today = now().toLocalDate();
		List<PreviewInterview> mine = forInterviewer(interviewerEmail);
		List<LocalTime> times = new ArrayList<>();
		for (LocalTime time = DAY_START; time.isBefore(DAY_END); time = time.plusMinutes(GRID_MINUTES)) {
			times.add(time);
		}
		List<WeekCalendar.Day> days = new ArrayList<>();
		for (int offset = 0; offset < 5; offset++) {
			LocalDate date = monday.plusDays(offset);
			List<PreviewInterview> raw = new ArrayList<>();
			for (LocalTime time : times) {
				LocalDateTime start = date.atTime(time);
				LocalDateTime end = start.plusMinutes(GRID_MINUTES);
				raw.add(mine.stream().filter(interview -> interview.overlaps(start, end)).findFirst().orElse(null));
			}
			List<WeekCalendar.Cell> cells = new ArrayList<>();
			for (int index = 0; index < raw.size(); index++) {
				PreviewInterview interview = raw.get(index);
				boolean first = interview != null && (index == 0 || raw.get(index - 1) != interview);
				int span = 0;
				if (first) {
					while (index + span < raw.size() && raw.get(index + span) == interview) {
						span++;
					}
				}
				cells.add(new WeekCalendar.Cell(times.get(index), interview, first, span));
			}
			int freeMinutes = (int) raw.stream().filter(Objects::isNull).count() * GRID_MINUTES;
			int count = (int) raw.stream().filter(Objects::nonNull).distinct().count();
			days.add(new WeekCalendar.Day(date, cells, count, freeMinutes, date.equals(today)));
		}
		return new WeekCalendar(monday, times, days);
	}

	private List<SlotOption> createOptions(PreviewUser candidate, List<PreviewUser> pool, int duration,
			LocalDate from, LocalDate to, List<Booking> bookings) {
		List<SlotOption> options = new ArrayList<>();
		for (PreviewUser interviewer : pool) {
			int load = load(interviewer, from, to, bookings, null);
			for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
				for (LocalDateTime start : gapStarts(candidate, interviewer, day, duration, bookings, null)) {
					options.add(new SlotOption(0, interviewer, start, duration, load, false, false));
				}
			}
		}
		options.sort(Comparator.comparing(SlotOption::startsAt).thenComparingInt(SlotOption::loadMinutes)
				.thenComparing(option -> option.interviewer().id()));
		return ranked(options);
	}

	private List<SlotOption> rescheduleOptions(PreviewInterview interview, List<PreviewUser> pool, LocalDate from,
			LocalDate to, List<Booking> bookings) {
		int duration = interview.durationMinutes();
		Long currentId = interview.interviewer().id();
		LocalDateTime requested = interview.requestedStartsAt();
		boolean requestUsable = requested != null && requested.isAfter(now()) && isValidSlot(requested, duration)
				&& free(interview.candidate(), requested, requested.plusMinutes(duration), bookings, interview.id());

		List<SlotOption> requestedGroup = new ArrayList<>();
		List<SlotOption> otherGroup = new ArrayList<>();
		for (PreviewUser interviewer : pool) {
			boolean current = interviewer.id().equals(currentId);
			int load = load(interviewer, from, to, bookings, interview.id());
			if (requestUsable
					&& free(interviewer, requested, requested.plusMinutes(duration), bookings, interview.id())) {
				requestedGroup.add(new SlotOption(0, interviewer, requested, duration, load, current, true));
			}
			for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
				for (LocalDateTime start : gapStarts(interview.candidate(), interviewer, day, duration, bookings,
						interview.id())) {
					if (requestUsable && start.equals(requested)) {
						continue;
					}
					otherGroup.add(new SlotOption(0, interviewer, start, duration, load, current, false));
				}
			}
		}
		requestedGroup.sort(Comparator.comparing((SlotOption option) -> !option.currentInterviewer())
				.thenComparingInt(SlotOption::loadMinutes).thenComparing(option -> option.interviewer().id()));
		otherGroup.sort(Comparator.comparing(SlotOption::startsAt)
				.thenComparing((SlotOption option) -> !option.currentInterviewer())
				.thenComparingInt(SlotOption::loadMinutes).thenComparing(option -> option.interviewer().id()));
		List<SlotOption> all = new ArrayList<>(requestedGroup);
		all.addAll(otherGroup);
		return ranked(all);
	}

	private List<LocalDateTime> gapStarts(PreviewUser candidate, PreviewUser interviewer, LocalDate day, int duration,
			List<Booking> bookings, Long ignoredInterviewId) {
		List<LocalDateTime> starts = new ArrayList<>();
		if (!isWeekday(day)) {
			return starts;
		}
		LocalDateTime now = now();
		LocalDateTime dayEnd = day.atTime(DAY_END);
		LocalDateTime time = day.atTime(DAY_START);
		while (!time.plusMinutes(duration).isAfter(dayEnd)) {
			if (time.isAfter(now) && bothFree(candidate, interviewer, time, time.plusMinutes(duration), bookings,
					ignoredInterviewId)) {
				starts.add(time);
				while (time.isBefore(dayEnd) && bothFree(candidate, interviewer, time,
						time.plusMinutes(GRID_MINUTES), bookings, ignoredInterviewId)) {
					time = time.plusMinutes(GRID_MINUTES);
				}
			}
			else {
				time = time.plusMinutes(GRID_MINUTES);
			}
		}
		return starts;
	}

	private boolean bothFree(PreviewUser candidate, PreviewUser interviewer, LocalDateTime start, LocalDateTime end,
			List<Booking> bookings, Long ignoredInterviewId) {
		return free(candidate, start, end, bookings, ignoredInterviewId)
				&& free(interviewer, start, end, bookings, ignoredInterviewId);
	}

	private boolean free(PreviewUser person, LocalDateTime start, LocalDateTime end, List<Booking> bookings,
			Long ignoredInterviewId) {
		return bookings.stream()
				.filter(booking -> ignoredInterviewId == null || !ignoredInterviewId.equals(booking.interviewId()))
				.filter(booking -> booking.involves(person.id()))
				.noneMatch(booking -> booking.start().isBefore(end) && start.isBefore(booking.end()));
	}

	private int load(PreviewUser interviewer, LocalDate from, LocalDate to, List<Booking> bookings,
			Long ignoredInterviewId) {
		return bookings.stream()
				.filter(booking -> interviewer.id().equals(booking.interviewerId()))
				.filter(booking -> ignoredInterviewId == null || !ignoredInterviewId.equals(booking.interviewId()))
				.filter(booking -> inRange(booking.start().toLocalDate(), from, to))
				.mapToInt(Booking::minutes).sum();
	}

	private List<PreviewUser> mostConstrainedFirst(List<PreviewUser> candidates, LocalDate from, LocalDate to,
			List<Booking> bookings) {
		return candidates.stream()
				.sorted(Comparator.comparingInt((PreviewUser candidate) -> freeMinutes(candidate, from, to, bookings))
						.thenComparing(PreviewUser::id))
				.toList();
	}

	private int freeMinutes(PreviewUser person, LocalDate from, LocalDate to, List<Booking> bookings) {
		int working = 0;
		for (LocalDate day = from; !day.isAfter(to); day = day.plusDays(1)) {
			if (isWeekday(day)) {
				working += (int) Duration.between(DAY_START, DAY_END).toMinutes();
			}
		}
		int booked = bookings.stream().filter(booking -> booking.involves(person.id()))
				.filter(booking -> inRange(booking.start().toLocalDate(), from, to)).mapToInt(Booking::minutes).sum();
		return working - booked;
	}

	private List<SlotOption> ranked(List<SlotOption> options) {
		List<SlotOption> ranked = new ArrayList<>();
		for (int index = 0; index < Math.min(TOP_OPTIONS, options.size()); index++) {
			ranked.add(options.get(index).withRank(index + 1));
		}
		return ranked;
	}

	private List<Booking> bookings() {
		return interviews.stream().map(interview -> new Booking(interview.candidate().id(),
				interview.interviewer().id(), interview.startsAt(), interview.endsAt(), interview.id())).toList();
	}

	private static boolean mentions(PreviewInterview interview, String needle) {
		return interview.candidate().name().toLowerCase(Locale.ROOT).contains(needle)
				|| interview.interviewer().name().toLowerCase(Locale.ROOT).contains(needle)
				|| interview.recruiter().name().toLowerCase(Locale.ROOT).contains(needle);
	}

	private static boolean inRange(LocalDate date, LocalDate from, LocalDate to) {
		return !date.isBefore(from) && !date.isAfter(to);
	}

	private static boolean isWeekday(LocalDate date) {
		return date.getDayOfWeek() != DayOfWeek.SATURDAY && date.getDayOfWeek() != DayOfWeek.SUNDAY;
	}

	private LocalDateTime at(int weekOffset, int dayOffset, int hour, int minute) {
		return sampleWeek.plusWeeks(weekOffset).plusDays(dayOffset).atTime(hour, minute);
	}

	private void addUser(Long id, String name, String email, UserRole role, LocalDateTime createdAt) {
		users.put(id, new PreviewUser(id, name, email, role, createdAt));
	}

	private void addInterview(Long id, Long recruiterId, Long candidateId, Long interviewerId, LocalDateTime startsAt,
			int duration, InterviewStatus status, LocalDateTime requestedStartsAt) {
		interviews.add(new PreviewInterview(id, users.get(recruiterId), users.get(candidateId),
				users.get(interviewerId), startsAt, duration, status, requestedStartsAt));
	}

	private record Booking(Long candidateId, Long interviewerId, LocalDateTime start, LocalDateTime end,
			Long interviewId) {

		boolean involves(Long personId) {
			return personId.equals(candidateId) || personId.equals(interviewerId);
		}

		int minutes() {
			return (int) Duration.between(start, end).toMinutes();
		}

	}

}
