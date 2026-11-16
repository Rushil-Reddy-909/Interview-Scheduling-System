package com.interviewscheduling.domain;

import java.time.Instant;

import com.interviewscheduling.enums.InterviewStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "interview", indexes = {
		@Index(name = "idx_interview_interviewer_starts", columnList = "interviewer_id, starts_at"),
		@Index(name = "idx_interview_candidate_starts", columnList = "candidate_id, starts_at"),
		@Index(name = "idx_interview_status", columnList = "status")
})
public class Interview {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "recruiter_id", nullable = false)
	private AppUser recruiter;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "candidate_id", nullable = false)
	private AppUser candidate;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "interviewer_id", nullable = false)
	private AppUser interviewer;

	@Column(name = "duration_minutes", nullable = false)
	private int durationMinutes;

	@Column(name = "starts_at", nullable = false)
	private Instant startsAt;

	@Column(name = "ends_at", nullable = false)
	private Instant endsAt;

	@Column(name = "requested_starts_at")
	private Instant requestedStartsAt;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 32)
	private InterviewStatus status;

	@Column(name = "created_at", nullable = false, updatable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@PrePersist
	void onCreate() {
		Instant now = Instant.now();
		createdAt = now;
		updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		updatedAt = Instant.now();
	}

	public Long getId() {
		return id;
	}

	public AppUser getRecruiter() {
		return recruiter;
	}

	public void setRecruiter(AppUser recruiter) {
		this.recruiter = recruiter;
	}

	public AppUser getCandidate() {
		return candidate;
	}

	public void setCandidate(AppUser candidate) {
		this.candidate = candidate;
	}

	public AppUser getInterviewer() {
		return interviewer;
	}

	public void setInterviewer(AppUser interviewer) {
		this.interviewer = interviewer;
	}

	public int getDurationMinutes() {
		return durationMinutes;
	}

	public void setDurationMinutes(int durationMinutes) {
		this.durationMinutes = durationMinutes;
	}

	public Instant getStartsAt() {
		return startsAt;
	}

	public void setStartsAt(Instant startsAt) {
		this.startsAt = startsAt;
	}

	public Instant getEndsAt() {
		return endsAt;
	}

	public void setEndsAt(Instant endsAt) {
		this.endsAt = endsAt;
	}

	public Instant getRequestedStartsAt() {
		return requestedStartsAt;
	}

	public void setRequestedStartsAt(Instant requestedStartsAt) {
		this.requestedStartsAt = requestedStartsAt;
	}

	public InterviewStatus getStatus() {
		return status;
	}

	public void setStatus(InterviewStatus status) {
		this.status = status;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public Instant getUpdatedAt() {
		return updatedAt;
	}

}
