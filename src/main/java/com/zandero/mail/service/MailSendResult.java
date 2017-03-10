package com.zandero.mail.service;

/**
 *
 */
public class MailSendResult {

	private final int status;

	private final String messageId;

	public MailSendResult() {

		status = 200;
		messageId = null;
	}

	public MailSendResult(int statusCode, String id) {

		status = statusCode;
		messageId = id;
	}

	public static MailSendResult ok() {

		return new MailSendResult();
	}

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

	public boolean isSuccessful() {

		return status >= 200 && status < 300;
	}

	@Override
	public String toString() {

		return status + " [" + messageId + "]";
	}
}
