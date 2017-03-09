package com.zandero.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.github.mustachejava.DefaultMustacheFactory;
import com.github.mustachejava.Mustache;
import com.github.mustachejava.MustacheFactory;
import com.zandero.utils.Assert;
import com.zandero.utils.ResourceUtils;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Helper class wrapping Mustache template engine
 */
public class MustacheUtils {

	private static ThreadLocal<MustacheFactory> factory;

	public MustacheUtils() {

	}

	public Mustache compile(String template, String path) {

		Assert.notNull(template, "Missing template!");
		MustacheFactory mf = getFactory();
		return mf.compile(new StringReader(template), path);
	}

	public Mustache load(String path) {

		Assert.notNullOrEmptyTrimmed(path, "Missing template path!");

		InputStream resource = this.getClass().getResourceAsStream(path);
		Assert.notNull(resource, "Could not load template: " + path);

		String content = ResourceUtils.getString(resource);

		MustacheFactory mf = getFactory();
		return mf.compile(new StringReader(content), path);
	}

	public String render(Mustache template, Object data) {

		Assert.notNull(template, "Missing template!");

		StringWriter out = new StringWriter();
		Object mustacheObject = data;
		if ((data != null) && (data instanceof JsonNode)) {
			mustacheObject = toMustacheObject((JsonNode) data);
		}

		template.execute(out, mustacheObject); // apply mustache template
		return out.toString();
	}

	/**
	 * code copied from https://github.com/spullara/mustache.java/blob/master/handlebar/src/main/java/com/sampullara/mustache/Handlebar.java
	 * converts a tree of JsonNode objects to nested HashMaps, which are used by mustache template engine
	 *
	 * @param node to convert
	 * @return mustache object node
	 */
	private static Object toMustacheObject(final JsonNode node) {

		if (node.isArray()) {
			return new ArrayList<Object>() {

				private static final long serialVersionUID = 3841492340663060380L;

				{ // interesting construct, isn't it
					for (JsonNode jsonNodes : node) {
						add(toMustacheObject(jsonNodes));
					}
				}
			};
		}
		else if (node.isObject()) {
			return new HashMap<String, Object>() {

				private static final long serialVersionUID = 3663162388901179432L;

				{
					for (Iterator<Entry<String, JsonNode>> i = node.fields(); i.hasNext(); ) {
						Entry<String, JsonNode> next = i.next();
						Object o = toMustacheObject(next.getValue());
						put(next.getKey(), o);
					}
				}
			};
		}
		else if (node.isBoolean()) {
			return node.booleanValue();
		}
		else if (node.isNull()) {
			return null;
		}
		else {
			return node.asText();
		}
	}

	/**
	 * Creates default mustache factory or returns existing
	 *
	 * @return mustache factory
	 */
	private static MustacheFactory getFactory() {

		if (factory == null) {
			factory = ThreadLocal.withInitial(DefaultMustacheFactory::new);
		}

		return factory.get();
	}
}