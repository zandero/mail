package com.zandero.mail.service;

import com.zandero.mail.service.sendgrid.SendGridMailService;
import com.zandero.utils.ResourceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

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
}
