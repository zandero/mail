package com.zandero.mail.wrapper;

import com.zandero.utils.ResourceUtils;
import com.zandero.utils.StringUtils;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MailGunMailServiceTest {

	@Test
	void sendMailTest() {

		// need to manually provide this file in order to test service ...
		// add domain=value from=value and key=value lines to the file
		Set<String> list = ResourceUtils.getResourceWords("/mailgun.properties", this.getClass());
		Map<String, String> properties = getProperties(list);

		MailGunMailService service = new MailGunMailService(properties.get("domain"), properties.get("from"), properties.get("key"));
		service.sendMail(properties.get("to"), "Test", "Hello!");
	}

	private Map<String, String> getProperties(Set<String> list) {

		Map<String, String> out = new HashMap<>();
		for (String keyName: list) {

			String[] splitted = keyName.split("=");
			if (splitted.length == 2) {
				out.put(StringUtils.trim(splitted[0]), StringUtils.trim(splitted[1]));
			}
		}

		return out;
	}
}
