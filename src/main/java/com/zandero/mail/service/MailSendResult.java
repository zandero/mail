package com.zandero.mail.service;

/**
 * Mail message send status indicator
 */
public class MailSendResult {

	/**
	 * Hidden
	 */
	private MailSendResult() {
		status = 0;
		message = "";
	}

	private final int status;

	private final String message;

	/**
	 * Set status and message id manually
	 * @param statusCode http status code
	 * @param text message
	 */
	public MailSendResult(int statusCode, String text) {

		status = statusCode;
		message = text;
	}

	/**
	 * Mail was send out successfully
	 * @return mail success
	 */
	public static MailSendResult ok() {

		return new MailSendResult(200, null);
	}

	/**
	 * Mail was send out successfully
	 * @param message success info if any
	 * @return mail success
	 */
	public static MailSendResult ok(String message) {

		return new MailSendResult(200, message);
	}

	/**
	 * Mail was not send out
	 * @return mail send failure
	 */
	public static MailSendResult fail() {

		return new MailSendResult(400, null); // bad request
	}

	/**
	 * Mail was not send out
	 * @param message error message
	 * @return mail send failure
	 */
	public static MailSendResult fail(String message) {

		return new MailSendResult(400, message); // bad request
	}

	/**
	 * Http status code
	 *
	 * @return http status code indicating success of failure
	 */
	public int getStatus() {

		return status;
	}

	/**
	 * Message id indicator if any
	 *
	 * @return unique message id to associate send mail with
	 */
	public String getMessage() {

		return message;
	}

	/**
	 * Success flag
	 *
	 * @return true if mail was send out successfully, false otherwise
	 */
	public boolean isSuccessful() {

		return status >= 200 && status < 300;
	}

	@Override
	public String toString() {

		return status + " [" + (message == null ? "> no message id <" : message) + "]";
	}
}
