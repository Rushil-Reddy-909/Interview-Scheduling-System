package com.interviewscheduling.exception;

public class DuplicateEmailException extends DomainException {

	public DuplicateEmailException(String email) {
		super("Another user already has the email " + email + ".");
	}

}
