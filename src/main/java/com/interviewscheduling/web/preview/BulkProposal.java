package com.interviewscheduling.web.preview;

import java.util.List;

public record BulkProposal(List<Placement> placed, List<Unplaced> unplaced) {

	public record Placement(PreviewUser candidate, PreviewInterview interview, SlotOption option) {

		public boolean interviewerChanged() {
			return interview != null && !interview.interviewer().id().equals(option.interviewer().id());
		}

	}

	public record Unplaced(PreviewUser candidate, PreviewInterview interview, String reason) {
	}

}
