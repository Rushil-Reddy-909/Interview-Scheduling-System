package com.interviewscheduling.web.advice;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.stream.Collectors;

import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.interviewscheduling.exception.DomainException;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Turns a rejected form post into a redirect back to the page it came from, with the reason as a flash error.
 */
@ControllerAdvice
public class DomainExceptionAdvice {

	private static final String FALLBACK_PATH = "/dashboard";

	@ExceptionHandler(DomainException.class)
	public String domainRule(DomainException ex, HttpServletRequest request, RedirectAttributes redirect) {
		redirect.addFlashAttribute("error", ex.getMessage());
		return "redirect:" + returnPath(request);
	}

	/**
	 * {@code MethodArgumentNotValidException} for an invalid {@code @Valid} form is a {@code BindException}.
	 */
	@ExceptionHandler(BindException.class)
	public String invalidForm(BindException ex, HttpServletRequest request, RedirectAttributes redirect) {
		String message = ex.getAllErrors().stream().map(DomainExceptionAdvice::message).distinct()
				.collect(Collectors.joining(" "));
		redirect.addFlashAttribute("error", message.isEmpty() ? "Check the form and try again." : message);
		return "redirect:" + returnPath(request);
	}

	private static String message(ObjectError error) {
		if (error instanceof FieldError field && field.isBindingFailure()) {
			return "Check the value of " + field.getField() + ".";
		}
		return error.getDefaultMessage();
	}

	/**
	 * The path and query of the Referer when it points at this application, so the redirect cannot leave the site.
	 */
	static String returnPath(HttpServletRequest request) {
		String referer = request.getHeader("Referer");
		if (referer == null || referer.isBlank()) {
			return FALLBACK_PATH;
		}
		try {
			URI uri = new URI(referer);
			if (uri.getHost() != null && !uri.getHost().equalsIgnoreCase(request.getServerName())) {
				return FALLBACK_PATH;
			}
			String path = uri.getRawPath();
			if (path == null || !path.startsWith("/") || path.startsWith("//")) {
				return FALLBACK_PATH;
			}
			String contextPath = request.getContextPath();
			if (!contextPath.isEmpty()) {
				if (!path.startsWith(contextPath)) {
					return FALLBACK_PATH;
				}
				path = path.substring(contextPath.length());
				if (path.isEmpty()) {
					path = "/";
				}
			}
			return uri.getRawQuery() == null ? path : path + "?" + uri.getRawQuery();
		}
		catch (URISyntaxException ex) {
			return FALLBACK_PATH;
		}
	}

}
