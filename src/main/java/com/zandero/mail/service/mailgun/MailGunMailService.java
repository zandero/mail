package com.zandero.mail.service.mailgun;

import com.zandero.http.Http;
import com.zandero.http.HttpUtils;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.MailSendResult;
import com.zandero.mail.service.MailService;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.JsonUtils;
import com.zandero.utils.extra.UrlUtils;
import com.zandero.utils.extra.ValidatingUtils;
import org.slf4j.LoggerFactory;

import javax.mail.Message;
import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MailGunMailService implements MailService {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MailGunMailService.class);

	private String domain;
	private String apiKey;

	private String defaultFrom;
	private String defaultFromName;

	public MailGunMailService(String mailGunApiKey, String domainName, String defaultFromEmail, String defaultFromName) {

		Assert.notNullOrEmptyTrimmed(domainName, "Missing mail domain name!");
		Assert.isTrue(ValidatingUtils.isDomain(domainName), "Invalid domain name!");

		Assert.notNullOrEmptyTrimmed(defaultFromEmail, "Missing default from email!");
		Assert.isTrue(ValidatingUtils.isEmail(defaultFromEmail), "Invalid default from email!");

		Assert.notNullOrEmptyTrimmed(mailGunApiKey, "Missing api key!");

		domain = domainName;
		apiKey = mailGunApiKey;
		defaultFrom = defaultFromEmail;
		this.defaultFromName = defaultFromName;

		log.info("Initializing MailGun with key: " + StringUtils.trimTextDown(apiKey, 9, "***"));
	}

	@Override
	public MailSendResult send(MailMessage message) {

		Assert.notNull(message, "Missing mail message!");

		String from = StringUtils.isNullOrEmptyTrimmed(message.getFromEmail()) ? defaultFrom : message.getFromEmail();
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
			// {  "id": "<20180611195133.1.10869F48B8AD29FF@yourdomain.com>",  "message": "Queued. Thank you."}
			String messageId = getMessageId(response.getResponse());
			return MailSendResult.ok(messageId);
		}
		catch (Exception e) {
			log.error("Failed to send out mail!", e);
			return MailSendResult.fail();
		}
	}

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

