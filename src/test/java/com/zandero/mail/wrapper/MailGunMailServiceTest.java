package com.zandero.mail.wrapper;

import com.zandero.utils.ResourceUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MailGunMailServiceTest extends MailServiceTest{

	@Disabled // manual triggering
	@Test
	void sendMailTest() {

		// need to manually provide this file in order to test service ...
		// add domain=value from=value and key=value lines to the file
		Set<String> list = ResourceUtils.getResourceWords("/mailgun.properties", this.getClass());
		Map<String, String> properties = getProperties(list);

		MailGunMailService service = new MailGunMailService(properties.get("domain"), properties.get("from"), properties.get("key"));
		service.send(properties.get("to"), "Sombody", "Test", "Hello!");
	}
}
