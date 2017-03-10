package com.zandero.mail.service;

/**
 * Mail message send status indicator
 */
public class MailSendResult {

	private final int status;

	private final String messageId;

	/**
	 * Set status and message id manually
	 * @param statusCode http status code
	 * @param id message id
	 */
	public MailSendResult(int statusCode, String id) {

		status = statusCode;
		messageId = id;
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
	 * @param messageId
	 * @return mail success
	 */
	public static MailSendResult ok(String messageId) {

		return new MailSendResult(200, messageId);
	}

	/**
	 * Mail was not send out
	 * @return mail send failure
	 */
	public static MailSendResult fail() {

		return new MailSendResult(400, null); // bad request
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
	public String getMessageId() {

		return messageId;
	}

	/**
	 * @return true if mail was send out successfully, false otherwise
	 */
	public boolean isSuccessful() {

		return status >= 200 && status < 300;
	}

	@Override
	public String toString() {

		return status + " [" + (messageId == null ? "> no message id <" : messageId) + "]";
	}
}
