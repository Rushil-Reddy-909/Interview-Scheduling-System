package com.interviewscheduling.web.preview;

import java.time.LocalDateTime;

public record SlotOption(int rank, PreviewUser interviewer, LocalDateTime startsAt, int durationMinutes,
		int loadMinutes, boolean currentInterviewer, boolean requestedStart) {

	public LocalDateTime endsAt() {
		return startsAt.plusMinutes(durationMinutes);
	}

	SlotOption withRank(int newRank) {
		return new SlotOption(newRank, interviewer, startsAt, durationMinutes, loadMinutes, currentInterviewer,
				requestedStart);
	}

}
