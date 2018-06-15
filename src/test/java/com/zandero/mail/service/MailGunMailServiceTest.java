package com.zandero.mail.service;

import com.zandero.http.Http;
import com.zandero.http.TrustAnyTrustManager;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.mailgun.MailGunMailService;
import com.zandero.utils.InstantTimeUtils;
import com.zandero.utils.ResourceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 *
 */
class MailGunMailServiceTest extends MailServiceTest {

	@Disabled // manual triggering
	@Test
	void sendMailTest() throws NoSuchAlgorithmException, KeyManagementException {

		Http.setSSLSocketFactory(TrustAnyTrustManager.getSSLFactory());

		// need to manually provide this file in order to test service ...
		// add domain=value from=value and key=value lines to the file
		Set<String> list = ResourceUtils.getResourceWords("/mailgun.properties", this.getClass());
		Map<String, String> properties = getProperties(list);

		MailGunMailService service = new MailGunMailService(properties.get("key"), properties.get("domain"), properties.get("from"), null);
		MailSendResult response = service.send(properties.get("to"), "Sombody", "Test", "Hello!");

		assertTrue(response.isSuccessful());
		assertNotNull(response.getMessage());
	}

	@Disabled // manual triggering
	@Test
	void sendMailMessageTest() throws NoSuchAlgorithmException, KeyManagementException {

		Http.setSSLSocketFactory(TrustAnyTrustManager.getSSLFactory());

		// need to manually provide this file in order to test service ...
		// add domain=value from=value and key=value lines to the file
		Set<String> list = ResourceUtils.getResourceWords("/mailgun.properties", this.getClass());
		Map<String, String> properties = getProperties(list);

		MailGunMailService service = new MailGunMailService(properties.get("key"), properties.get("domain"), properties.get("from"), null);

		MailMessage mailMessage = new MailMessage().to(properties.get("to"), "Sombody").subject("Test").html("Hello!")
		                                           .setSendAt(Instant.now().plus(5, ChronoUnit.MINUTES));


		MailSendResult response = service.send(mailMessage);
		assertTrue(response.isSuccessful());
		assertNotNull(response.getMessage());
	}

	@Test
	void testDateFormat() {

		assertEquals("Fri, 29 Aug 2014 21:41:53 +0000", InstantTimeUtils.format(Instant.ofEpochSecond(1409348513L), MailGunMailService.SEND_AT_FORMAT));
	}
}
