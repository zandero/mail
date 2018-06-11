package com.zandero.mail.wrapper;

import com.zandero.http.Http;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.MailSendResult;
import com.zandero.mail.service.MailService;
import com.zandero.mail.wrapper.sendgrid.Mail;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * SendGrid mail service integration (API V3 usage with API key)
 */
public class SendGridMailService implements MailService {

	private static final Logger log = LoggerFactory.getLogger(SendGridMailService.class);

	private final String defaultFrom;

	private final String apiKey;
	private final String defaultFromName;

	/**
	 * Initialized mail service for SendGrid
	 */
	public SendGridMailService(String key, String defaultEmail, String defaultName) {

		Assert.notNullOrEmptyTrimmed(key, "Missing Sendgrid API key!");

		apiKey = key;
		defaultFrom = defaultEmail;
		defaultFromName = defaultName;

		// log only first characters of key ... should be enough to see that everything is OK
		log.info("Initializing SendGrid with key: " + StringUtils.trimTextDown(apiKey, 9, "***"));
	}

	@Override
	public MailSendResult send(MailMessage message) {

		Assert.notNull(message, "Missing mail message!");
		message.defaultFrom(defaultFrom, defaultFromName);

		/*String from = StringUtils.isNullOrEmptyTrimmed(message.getFromEmail()) ? defaultFrom : message.getFromEmail();
		if (!StringUtils.isNullOrEmptyTrimmed(message.getFromName())) {
			from = message.getFromName() + " <" + from + ">";
		}

		String recipients = message.getEmailsAsString(Message.RecipientType.TO);
		String ccRecipients = message.getEmailsAsString(Message.RecipientType.CC);
		String bccRecipien/bjuLts = message.getEmailsAsString(Message.RecipientType.BCC);
*/
		try {
			String url = "https://api.sendgrid.com/v3/mail/send";

			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer " + apiKey);
			headers.put("Content-Type", "application/json");

			String body = JsonUtils.toJson(new Mail(message));
			Http.Response response = Http.post(url, body, null, headers);

			if (response.not(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_ACCEPTED)) {
				log.error("Failed to send out mail: ({}) {}", response.getCode(), response.getResponse());
				return MailSendResult.fail();
			}

			// TODO: get message id header ... from response

			// deserialize json from response if needed ... for now it is as it is ...
			return MailSendResult.ok(/* trackingId */);
		}
		catch (Exception e) {
			log.error("Failed to send out mail!", e);
			return MailSendResult.fail();
		}
	}
}
