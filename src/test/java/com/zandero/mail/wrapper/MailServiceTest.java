package com.zandero.mail.wrapper;

import com.zandero.utils.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 */
public class MailServiceTest {

	protected Map<String, String> getProperties(Set<String> list) {

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
