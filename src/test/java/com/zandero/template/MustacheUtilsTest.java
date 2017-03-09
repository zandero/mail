package com.zandero.template;

import com.github.mustachejava.Mustache;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

public class MustacheUtilsTest {

	@Test
	public void testLoadTemplate() throws Exception {

		MustacheUtils utils = new MustacheUtils();
		Mustache template = utils.load("/templates/hello.html");

		Map<String, String> data = new HashMap<>();
		data.put("name", "world");

		String result = utils.render(template, data);
		assertEquals("Hello world!", result);
	}
}