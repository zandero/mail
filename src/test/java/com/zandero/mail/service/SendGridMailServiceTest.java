package com.zandero.mail.service;

import com.zandero.http.Http;
import com.zandero.http.TrustAnyTrustManager;
import com.zandero.mail.MailMessage;
import com.zandero.mail.service.sendgrid.SendGridMailService;
import com.zandero.utils.ResourceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
class SendGridMailServiceTest extends MailServiceTest {

	@Disabled // manual triggering
	@Test
	void sendMailTest() {

		// need to manually provide this file in order to test service ...
		// add domain=value from=value and key=value lines to the file
		Set<String> list = ResourceUtils.getResourceWords("/sendgrid.properties", this.getClass());
		Map<String, String> properties = getProperties(list);

		SendGridMailService service = new SendGridMailService(properties.get("key"), properties.get("from"), null);
		MailSendResult response = service.send(properties.get("to"), "Somebody", "Test", "Hello!");

		assertTrue(response.isSuccessful());
		assertNotNull(response.getMessage());
	}

	@Disabled // manual triggering
	@Test
	void sendMailMessageTest() throws NoSuchAlgorithmException, KeyManagementException {

		Http.setSSLSocketFactory(TrustAnyTrustManager.getSSLFactory());

		// need to manually provide this file in order to test service ...
		// add domain=value from=value and key=value lines to the file
		Set<String> list = ResourceUtils.getResourceWords("/sendgrid.properties", this.getClass());
		Map<String, String> properties = getProperties(list);

		SendGridMailService service = new SendGridMailService(properties.get("key"), properties.get("from"), null);

		MailMessage mailMessage = new MailMessage().to(properties.get("to"), "Sombody").subject("Test").html("A delayed hello!")
		                                           .setSendAt(Instant.now().plus(5, ChronoUnit.MINUTES));


		MailSendResult response = service.send(mailMessage);
		assertTrue(response.isSuccessful());
		assertNotNull(response.getMessage());
	}
}
