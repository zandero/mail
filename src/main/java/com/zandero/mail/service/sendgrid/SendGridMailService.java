package com.zandero.mail.service.sendgrid;

import com.zandero.http.Http;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.MailSendResult;
import com.zandero.mail.service.MailService;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import com.zandero.utils.extra.ValidatingUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

/**
 * SendGrid mail service integration (API V3 usage with API key)
 * with MailMessage support
 */
public class SendGridMailService implements MailService {

	private static final Logger log = LoggerFactory.getLogger(SendGridMailService.class);

	private final String apiKey;

	private final String defaultFrom;
	private final String defaultFromName;

	/**
	 * Initializes SendGrid mailing service (API wrapper)
	 * @param sendGridApiKey api key
	 * @param defaultEmail default from email if not from email is given in message
	 * @param defaultName default from name if no from name is given in message
	 */
	public SendGridMailService(String sendGridApiKey, String defaultEmail, String defaultName) {

		Assert.notNullOrEmptyTrimmed(sendGridApiKey, "Missing api key!");

		Assert.notNullOrEmptyTrimmed(defaultEmail, "Missing default from email!");
		Assert.isTrue(ValidatingUtils.isEmail(defaultEmail), "Invalid default from email!");

		apiKey = StringUtils.trim(sendGridApiKey);
		defaultFrom = StringUtils.trim(defaultEmail).toLowerCase();
		defaultFromName = StringUtils.trimToNull(defaultName);

		// log only first characters of key ... should be enough to see that everything is OK
		log.info("Initializing SendGrid with key: " + StringUtils.trimTextDown(apiKey, 9, "***"));
	}

	@Override
	public MailSendResult send(MailMessage message) {

		Assert.notNull(message, "Missing mail message!");
		message.defaultFrom(defaultFrom, defaultFromName); // if from is set then this is ignored

		try {
			String url = "https://api.sendgrid.com/v3/mail/send";

			Map<String, String> headers = new HashMap<>();
			headers.put("Authorization", "Bearer " + apiKey);
			headers.put("Content-Type", "application/json");

			String body = JsonUtils.toJson(new Mail(message)); //
			Http.Response response = Http.post(url, body, null, headers);

			if (response.not(HttpURLConnection.HTTP_OK, HttpURLConnection.HTTP_CREATED, HttpURLConnection.HTTP_ACCEPTED)) {
				log.error("Failed to send out mail: ({}) {}", response.getCode(), response.getResponse());
				return MailSendResult.fail(response.getResponse());
			}

			// get message id header ... from response
			String messageId = response.getHeader("X-Message-Id");
			return MailSendResult.ok(messageId);
		}
		catch (Exception e) {
			log.error("Failed to send out mail!", e);
			return MailSendResult.fail(e.getMessage());
		}
	}
}
