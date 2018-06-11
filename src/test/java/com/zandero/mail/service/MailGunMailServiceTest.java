package com.zandero.mail.service;

import com.zandero.http.Http;
import com.zandero.http.TrustAnyTrustManager;
import com.zandero.mail.service.mailgun.MailGunMailService;
import com.zandero.utils.ResourceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 *
 */
public class MailGunMailServiceTest extends MailServiceTest {

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
}
