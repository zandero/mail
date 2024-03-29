package com.zandero.mail.service.mailgun;

import com.zandero.http.Http;
import com.zandero.http.HttpUtils;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.MailSendResult;
import com.zandero.mail.service.MailService;
import com.zandero.utils.Assert;
import com.zandero.utils.InstantTimeUtils;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import com.zandero.utils.extra.UrlUtils;
import com.zandero.utils.extra.ValidatingUtils;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import java.net.HttpURLConnection;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * Mail gun mail API integration with MailMessage support
 */
public class MailGunMailService implements MailService {

	/**
	 * RFC-2822 date format as per MailGun specs: https://documentation.mailgun.com/en/latest/api-intro.html#date-format
	 */
	public static final DateTimeFormatter SEND_AT_FORMAT = InstantTimeUtils.RFC_2822_DATE_TIME_FORMAT;

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MailGunMailService.class);

	private final String domain;
	private final String apiKey;

	private final String defaultFrom;
	private final String defaultFromName;

	/**
	 * Initializes MailGun mailing service (API wrapper)
	 * @param mailGunApiKey api key
	 * @param domainName domain name
	 * @param defaultEmail default from email if not from email is given in message
	 * @param defaultName default from name if no from name is given in message
	 */
	public MailGunMailService(String mailGunApiKey, String domainName, String defaultEmail, String defaultName) {

		Assert.notNullOrEmptyTrimmed(mailGunApiKey, "Missing api key!");

		Assert.notNullOrEmptyTrimmed(domainName, "Missing mail domain name!");
		Assert.isTrue(ValidatingUtils.isDomain(domainName), "Invalid domain name!");

		Assert.notNullOrEmptyTrimmed(defaultEmail, "Missing default from email!");
		Assert.isTrue(ValidatingUtils.isEmail(defaultEmail), "Invalid default from email!");

		domain = StringUtils.trim(domainName);
		apiKey = StringUtils.trim(mailGunApiKey);

		defaultFrom = StringUtils.trim(defaultEmail).toLowerCase();
		defaultFromName = StringUtils.trimToNull(defaultName);

		log.info("Initializing MailGun with key: " + StringUtils.trimTextDown(apiKey, 9, "***"));
	}

	/**
	 * Sends mail via MailGun service
	 * @param message to be send
	 * @return mail gun send result
	 */
	@Override
	public MailSendResult send(MailMessage message) {

		Assert.notNull(message, "Missing mail message!");
		message.defaultFrom(defaultFrom, defaultFromName); // if from is set then this is ignored

		// format to name <email>
		String from = message.getFromEmail();
		if (!StringUtils.isNullOrEmptyTrimmed(message.getFromName())) {
			from = message.getFromName() + " <" + from + ">";
		}

		String recipients = message.getEmailsAsString(Message.RecipientType.TO);
		String ccRecipients = message.getEmailsAsString(Message.RecipientType.CC);
		String bccRecipients = message.getEmailsAsString(Message.RecipientType.BCC);

		try {
			String url = "https://api.mailgun.net/v3/" + domain + "/messages";

			Map<String, String> formParams = new HashMap<>();
			formParams.put("from", from);
			formParams.put("to", recipients);
			if (ccRecipients != null) {
				formParams.put("cc", recipients);
			}

			if (bccRecipients != null) {
				formParams.put("bcc", recipients);
			}

			formParams.put("subject", message.getSubject());

			String content = message.getContent();
			if (!StringUtils.isNullOrEmptyTrimmed(content)) {
				formParams.put("text", content);
			}

			String htmlContent = message.getHtmlContent();
			if (!StringUtils.isNullOrEmptyTrimmed(htmlContent)) {
				formParams.put("html", htmlContent);
			}

			if (message.getSendAt() != null &&
			    message.getSendAt().isBefore(Instant.now().plus(72, ChronoUnit.HOURS))) { // send in the future

				//TODO: update to RFC_2822_DATE_TIME_FORMAT when new release of zandero.utils lib
				formParams.put("o:deliverytime", InstantTimeUtils.format(message.getSendAt(), SEND_AT_FORMAT));  // "Fri, 25 May 2020 23:10:10 -0000"
			}

			// TODO: implement attachments

			Map<String, String> headers = new HashMap<>();
			String encoded = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(HttpUtils.UTF_8));
			headers.put("Authorization", "Basic " + encoded);
			headers.put("Content-Type", "application/x-www-form-urlencoded");

			String body = UrlUtils.composeQuery(formParams);
			Http.Response response = Http.post(url, body, null, headers);

			if (response.not(HttpURLConnection.HTTP_OK)) {
				log.error("Failed to send out mail: ({}) {}", response.getCode(), response.getResponse());
				return MailSendResult.fail();
			}

			// get tracking id
			String messageId = getMessageId(response.getResponse());
			return MailSendResult.ok(messageId);
		}
		catch (Exception e) {
			log.error("Failed to send out mail!", e);
			return MailSendResult.fail();
		}
	}

	// {  "id": "<20180611195133.1.10869F48B8AD29FF@yourdomain.com>",  "message": "Queued. Thank you."}
	private String getMessageId(String response) {
		if (StringUtils.isNullOrEmptyTrimmed(response)) {
			return null;
		}

		try {
			MailGunSendResponse res = JsonUtils.fromJson(response, MailGunSendResponse.class);
			return res != null ? res.id : null;
		}
		catch (IllegalArgumentException e) {
			log.warn("Failed to parse MailGun send response: ", e);
			return null;
		}
	}
}

