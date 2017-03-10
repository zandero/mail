package com.zandero.mail.service;

import com.sendgrid.*;
import com.zandero.mail.MailMessage;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;

/**
 * SendGrid mail service integration (API V3 usage with API key)
 */
public class SendGridMailServiceImpl implements MailService {

	private static final Logger log = LoggerFactory.getLogger(SendGridMailServiceImpl.class);

	private final SendGrid sendgrid;

	private final MailSettings mailSettings;

	public SendGridMailServiceImpl(MailSettings settings) {

		Assert.notNull(settings, "Missing SendGrid mail settings!");
		String key = settings.getApiKey();

		Assert.notNullOrEmptyTrimmed(key, "Missing Sendgrid API key!");

		// log only first 6 characters of key ... should be enough to see that everything is OK
		String keyPart = StringUtils.trimTextDown(key, 9, "***");
		log.info("Initializing SendGrid with key: " + keyPart);

		sendgrid = new SendGrid(key);

		log.info("Using SendGrid client version: " + sendgrid.getVersion());

		mailSettings = settings;
	}

	@Override
	public MailSendResult send(MailMessage message) {

		try {
			Request request = new Request();
			request.endpoint = "mail/send";
			request.method = Method.POST;

			request.headers = new HashMap<>();
			request.headers.put("Accept", "application/json");

			request.body = getMessageBody(message);

			Response response = sendgrid.api(request);

			log.debug("SendGrid returned: " + response.statusCode + ": " + response.body + ", header: " + StringUtils.join(response.headers, ", "));

			String messageId = response.headers != null ? response.headers.get("X-Message-Id") : null;
			return new MailSendResult(response.statusCode, messageId);
		}
		catch (IOException e) {
			log.error("Failed to send mail: ", e);
			return MailSendResult.fail();
		}
	}

	/**
	 * Creates JSON compatible with SendGrid to send out mail
	 */
	private String getMessageBody(MailMessage message) throws IOException {

		Mail mail = new Mail();

		// TO
		for (String toEmail : message.getToEmails().keySet()) {

			Personalization person = new Personalization();

			String name = message.getToEmails().get(toEmail);

			Email toEmailAddress = new Email(toEmail);
			if (!StringUtils.isNullOrEmptyTrimmed(name)) {
				toEmailAddress.setName(name);
			}

			person.addTo(toEmailAddress);

			// if message should be delayed ... than set send at
			Long time = message.getSendAt(toEmail);
			if (time != null && time > System.currentTimeMillis()) {
				person.setSendAt(time / 1000); // transform to UNIX timestamp
			}

			mail.addPersonalization(person);
		}

		// FROM
		String fromEmail = message.getFromEmail();
		Email fromEmailAddress;
		if (StringUtils.isNullOrEmptyTrimmed(fromEmail)) {
			fromEmailAddress = new Email(mailSettings.getDefaultFromMail());
		}
		else {
			fromEmailAddress = new Email(fromEmail);
		}

		if (StringUtils.isNullOrEmptyTrimmed(message.getFromName())) {
			fromEmailAddress.setName(mailSettings.getDefaultFromName());
		}
		else {
			fromEmailAddress.setName(message.getFromName());
		}
		mail.setFrom(fromEmailAddress);

		// SUBJECT
		mail.setSubject(message.getSubject());

		// CONTENT
		String content = message.getContent();
		String htmlContent = message.getHtmlContent();

		Content mailContent = new Content();

		if (!StringUtils.isNullOrEmptyTrimmed(content)) {
			mailContent.setType("text/plain");
			mailContent.setValue(content);
		}
		else {
			mailContent.setType("text/html");
			mailContent.setValue(htmlContent);
		}
		mail.addContent(mailContent);

		return mail.build();
	}
}
