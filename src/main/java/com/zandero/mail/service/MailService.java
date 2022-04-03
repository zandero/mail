package com.zandero.mail.service;

import com.zandero.mail.MailAttachment;
import com.zandero.mail.MailMessage;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.ValidatingUtils;

import java.util.List;

/**
 * Mail service to be implemented
 */
public interface MailService {

	/**
	 * Send message out
	 *
	 * @param builder to build mail message
	 * @return mail send out result
	 */
	MailSendResult send(MailMessage builder);

	/**
	 * Default way to build up massage with all options available
	 *
	 * @param fromName  sender name
	 * @param fromEmail sender email
	 * @param toName    receiver name
	 * @param toEmail   receiver email
	 * @param subject   mail subject
	 * @param msgText   mail text (raw)
	 * @param msgHTML   mail as HTML
	 * @return mail send out result
	 */
	default MailSendResult send(String fromName, String fromEmail,
	                            String toName, String toEmail,
	                            String subject, String msgText,
	                            String msgHTML) {

		return send(fromName, fromEmail, toName, toEmail, subject, msgText, msgHTML, null);
	}

	/**
	 * Default way to build up massage with all options available
	 *
	 * @param fromName    sender name
	 * @param fromEmail   sender email
	 * @param toName      receiver name
	 * @param toEmail     receiver email
	 * @param subject     mail subject
	 * @param msgText     mail text (raw)
	 * @param msgHTML     mail as HTML
	 * @param attachments list of attacments
	 * @return mail send out result
	 */
	default MailSendResult send(String fromName, String fromEmail, String toName, String toEmail, String subject, String msgText, String msgHTML,
	                            List<MailAttachment> attachments) {

		Assert.isTrue(ValidatingUtils.isEmail(fromEmail),  "Invalid email address given: '" + fromEmail + "'");
		Assert.isTrue(ValidatingUtils.isEmail(toEmail), "Invalid email address given: '" + toEmail + "'");

		MailMessage mail = new MailMessage()
			.from(fromEmail, fromName)
			.to(toEmail, toName)
			.subject(subject)
			.content(msgText)
			.html(msgHTML)
			.attachments(attachments);

		return send(mail);
	}

	/**
	 * Reduced version with minimal set of data
	 *
	 * @param toEmail     receiver email
	 * @param toName      receiver name
	 * @param subject     subject
	 * @param htmlContent HTML content
	 * @return mail send out result
	 */
	default MailSendResult send(String toEmail, String toName, String subject, String htmlContent) {

		return send(null, null, toEmail, toName, subject, htmlContent);
	}

	/**
	 * Reduced version with minimal set of data
	 *
	 * @param fromEmail   sender email
	 * @param fromName    sender name
	 * @param toEmail     receiver email
	 * @param toName      receiver name
	 * @param subject     subject
	 * @param htmlContent HTML content
	 * @return mail send out result
	 */
	default MailSendResult send(String fromEmail, String fromName, String toEmail, String toName, String subject, String htmlContent) {

		Assert.isTrue(ValidatingUtils.isEmail(toEmail), "Invalid email address given: '" + toEmail + "'");

		MailMessage mail = new MailMessage()
			.to(toEmail)
			.subject(subject)
			.html(htmlContent);

		if (!StringUtils.isNullOrEmpty(fromEmail)) {
			mail.from(fromEmail, fromName);
		}

		if (!StringUtils.isNullOrEmptyTrimmed(toName)) {
			mail.to(toEmail, toName);
		}

		return send(mail);
	}
}
