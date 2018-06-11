package com.zandero.mail.wrapper;

import com.zandero.http.Http;
import com.zandero.http.HttpUtils;
import com.zandero.http.TrustAnyTrustManager;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.MailSendResult;
import com.zandero.mail.service.MailService;
import com.zandero.mail.service.MailSettings;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
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

	public MailGunMailService(String mailGunApiKey, String domainName, String defaultFromEmail) {

		init(mailGunApiKey, domainName, defaultFromEmail);
	}

	/**
	 * Initialized mail service for SendGrid
	 * @param settings containing api key, default from email as minimum
	 */
	public MailGunMailService(MailSettings settings) {

		Assert.notNull(settings, "Missing SendGrid mail settings!");
		Assert.notNullOrEmptyTrimmed(settings.getApiKey(), "Missing Sendgrid API key!");

		init(settings.getApiKey(), settings.getServiceDomain(), settings.getDefaultFromMail());
	}

	private void init(String mailGunApiKey, String domainName, String defaultFromEmail) {
		Assert.notNullOrEmptyTrimmed(domainName, "Missing mail domain name!");
		Assert.isTrue(ValidatingUtils.isDomain(domainName), "Invalid domain name!");

		Assert.notNullOrEmptyTrimmed(defaultFromEmail, "Missing default from email!");
		Assert.isTrue(ValidatingUtils.isEmail(defaultFromEmail), "Invalid default from email!");

		Assert.notNullOrEmptyTrimmed(mailGunApiKey, "Missing api key!");

		domain = domainName;
		apiKey = mailGunApiKey;
		defaultFrom = defaultFromEmail;

		log.info("Initializing MailGun with key: " + StringUtils.trimTextDown(apiKey, 9, "***"));

		try { // trust all ...
			Http.setSSLSocketFactory(TrustAnyTrustManager.getSSLFactory());
		}
		catch (Exception e) {
			log.error("Failed to set SSL socket factory: {}", e);
		}
	}

	/**
	 * Sends mail using mailGun API using only query string parameters
	 *
	 * @param address to send to
	 * @param title   subject
	 * @param content HTML content
	 * @return true if mail was send out, false otherwise
	 */
	public MailSendResult sendMail(String address, String title, String content) {

		MailMessage builder = new MailMessage().to(address).subject(title).content(content);
		return send(builder);
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

			// deserialize json from response if needed ... for now it is as it is ...
			return MailSendResult.ok();
		}
		catch (Exception e) {
			log.error("Failed to send out mail!", e);
			return MailSendResult.fail();
		}
	}
}

