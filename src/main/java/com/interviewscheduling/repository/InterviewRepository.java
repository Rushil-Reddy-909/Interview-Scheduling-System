package com.interviewscheduling.repository;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.interviewscheduling.domain.Interview;
import com.interviewscheduling.enums.InterviewStatus;

public interface InterviewRepository extends JpaRepository<Interview, Long> {

	long countByStatus(InterviewStatus status);

	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	List<Interview> findTop6ByEndsAtAfterOrderByStartsAtAsc(Instant now);

	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	@Query("""
			select i from Interview i
			where i.recruiter.email = :email and i.endsAt > :now
			order by i.startsAt, i.id
			""")
	List<Interview> findUpcomingForRecruiter(@Param("email") String email, @Param("now") Instant now);

	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	@Query("""
			select i from Interview i
			where i.candidate.email = :email and i.endsAt > :now
			order by i.startsAt, i.id
			""")
	List<Interview> findUpcomingForCandidate(@Param("email") String email, @Param("now") Instant now);

	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	@Query("""
			select i from Interview i
			where i.candidate.email = :email and i.endsAt <= :now
			order by i.startsAt desc, i.id desc
			""")
	List<Interview> findPastForCandidate(@Param("email") String email, @Param("now") Instant now);

	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	@Query("""
			select i from Interview i
			where i.interviewer.email = :email and i.endsAt > :now
			order by i.startsAt, i.id
			""")
	List<Interview> findUpcomingForInterviewer(@Param("email") String email, @Param("now") Instant now);

	/**
	 * Interviews that overlap {@code [from, to)}.
	 */
	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	@Query("""
			select i from Interview i
			where i.interviewer.email = :email and i.startsAt < :to and i.endsAt > :from
			order by i.startsAt, i.id
			""")
	List<Interview> findForInterviewerBetween(@Param("email") String email, @Param("from") Instant from,
			@Param("to") Instant to);

	/**
	 * {@code from} and {@code to} bound {@code starts_at} as {@code [from, to)} and may be null. {@code pattern} is a
	 * lowercase {@code like} pattern, or empty to match everyone.
	 */
	@EntityGraph(attributePaths = { "recruiter", "candidate", "interviewer" })
	@Query("""
			select i from Interview i
			where (:status is null or i.status = :status)
			  and (:from is null or i.startsAt >= :from)
			  and (:to is null or i.startsAt < :to)
			  and (:pattern = ''
			       or lower(i.candidate.name) like :pattern escape '\\'
			       or lower(i.interviewer.name) like :pattern escape '\\'
			       or lower(i.recruiter.name) like :pattern escape '\\')
			order by i.startsAt, i.id
			""")
	List<Interview> search(@Param("status") InterviewStatus status, @Param("from") Instant from,
			@Param("to") Instant to, @Param("pattern") String pattern);

	@Query("""
			select case when count(i) > 0 then true else false end from Interview i
			where i.recruiter.id = :userId or i.candidate.id = :userId or i.interviewer.id = :userId
			""")
	boolean involvesUser(@Param("userId") Long userId);

}
