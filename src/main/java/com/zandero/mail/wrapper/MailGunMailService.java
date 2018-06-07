package com.zandero.mail.wrapper;

import com.zandero.http.Http;
import com.zandero.http.HttpUtils;
import com.zandero.utils.extra.UrlUtils;
import org.slf4j.LoggerFactory;

import java.net.HttpURLConnection;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class MailGunMailService {

	private static final org.slf4j.Logger log = LoggerFactory.getLogger(MailGunMailService.class);

	private final String domain;
	private final String apiKey;
	private final String defaultFrom;

	public MailGunMailService(String domainName, String defaultFromEmail, String mailGunApiKey) {

		domain = domainName;
		apiKey = mailGunApiKey;
		defaultFrom = defaultFromEmail;
	}

	/**
	 * Sends mail using mailGun API using only query string parameters
	 *
	 * @param address to send to
	 * @param title   subject
	 * @param content HTML content
	 * @return true if mail was send out, false otherwise
	 */
	public boolean sendMail(String address, String title, String content) {

		// create POST request to API
		try {
			String url = "https://api.mailgun.net/v3/" + domain + "/messages";

			Map<String, String> formParams = new HashMap<>();
			formParams.put("from", defaultFrom);
			formParams.put("to", address);
			formParams.put("subject", title);
			formParams.put("html", content);
			// formParams.put("text", content);

			Map<String, String> headers = new HashMap<>();
			String encoded = Base64.getEncoder().encodeToString(("api:" + apiKey).getBytes(HttpUtils.UTF_8));
			headers.put("Authorization", "Basic " + encoded);
			headers.put("Content-Type", "application/x-www-form-urlencoded");

			String body = UrlUtils.composeQuery(formParams);
			Http.Response response = Http.post(url, body, null, headers);

			if (response.not(HttpURLConnection.HTTP_OK)) {
				log.error("Failed to send out mail: ({}) {}", response.getCode(), response.getResponse());
				return false;
			}

			// deserialize json from response if needed ... for now it is as it is ...
			return true;
		}
		catch (Exception e) {
			log.error("Failed to send out mail!", e);
			return false;
		}
	}
}

