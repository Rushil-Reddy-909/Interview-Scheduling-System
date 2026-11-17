package com.interviewscheduling.exception;

/**
 * A rule the user broke. The message is shown to the user as is.
 */
public abstract class DomainException extends RuntimeException {

	protected DomainException(String message) {
		super(message);
	}

}
