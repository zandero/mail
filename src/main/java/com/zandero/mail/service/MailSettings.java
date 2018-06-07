package com.zandero.mail.service;

import com.zandero.settings.Settings;
import com.zandero.utils.Assert;
import com.zandero.utils.StringUtils;
import com.zandero.utils.extra.ValidatingUtils;

public class MailSettings extends Settings {

	private static final long serialVersionUID = -7221476349703104698L;

	public static final String SMTP_URL = "smtp_url";

	public static final String SMTP_PORT = "smtp_port";

	public static final String SMTP_USERNAME = "smtp_username";

	public static final String SMTP_PASSWORD = "smtp_password";

	public static final String DEFAULT_FROM_NAME = "default_from_name";
	public static final String DEFAULT_FROM_EMAIL = "default_from_email";

	private static final String SERVICE_API_KEY = "api_key";

	public MailSettings(MailSettings.Builder builder) {

		// add default
		Assert.notNull(builder, "Missing mail settings builder!");
		this.putAll(builder.build());
	}

	public MailSettings(Settings settings) {

		MailSettings.Builder builder = new MailSettings.Builder(settings);
		this.putAll(builder.build());
	}

	/**
	 * String delimited with ","
	 * values must be given in following order
	 * - url:port
	 * - username
	 * - password
	 * - default from mail
	 *
	 * For instance: smtp://some.smtp.url:123,myUser,myPassword,default@from.email
	 * OR
	 * - api key (should not contain ",")!
	 *
	 * For instance: someRandomGeneratedKey
	 *
	 * @param smtp list of setting as single string
	 */
	public MailSettings(String smtp) {

		MailSettings.Builder builder = new MailSettings.Builder();

		Assert.notNullOrEmptyTrimmed(smtp, "Missing settings!");

		String[] items = smtp.split(",");
		int count = 0;

		if (items.length == 1) {
			// if only one setting is given ... then consider this to be the API key
			builder.add(SERVICE_API_KEY, smtp);
		}
		else {

			for (String item : items) {
				switch (count) {
					case 0:
						builder.smtpUrl(item);
						break;

					case 1:
						try {
							String value = StringUtils.trim(item);
							int port = Integer.parseInt(value);

							if (port < 0 || port > 9999) {
								throw new IllegalArgumentException("Invalid SMTP port given: " + port);
							}

							builder.port(port);
						}
						catch (NumberFormatException e) {
							throw new IllegalArgumentException("Invalid SMTP port given: " + item);
						}
						break;

					case 2:
						builder.username(item);
						break;

					case 3:
						builder.password(item);
						break;

					case 4:
						builder.defaultEmail(item, null);
						break;
				}
				count++;
			}
		}

		this.putAll(builder.build());
	}

	public String getDefaultFromMail() {

		return getString(DEFAULT_FROM_EMAIL);
	}

	public String getDefaultFromName() {

		return findString(DEFAULT_FROM_NAME);
	}

	public String getSmtpUrl() {

		return getString(SMTP_URL);
	}

	public String getSmtpUsername() {

		return getString(SMTP_USERNAME);
	}

	public String getSmtpPassword() {

		return getString(SMTP_PASSWORD);
	}

	public int getSmtpPort() {

		return getInt(SMTP_PORT);
	}

	public String getApiKey() {

		return findString(SERVICE_API_KEY);
	}

	@Override
	public String toString() {

		return "URL: " + getSmtpUrl() + ", Username: " + getSmtpUsername() + ", Password: " + StringUtils.trimTextDown(getSmtpPassword(), 5, "***");
	}

	public static class Builder extends Settings.Builder {

		public Builder() {}

		/**
		 * @param settings SMTP setup
		 */
		public Builder(Settings settings) {

			Assert.notNull(settings, "Missing settings!");
			// add (should be present by default)
			smtpUrl(settings.getString(MailSettings.SMTP_URL));
			credentials(settings.getString(MailSettings.SMTP_USERNAME), settings.getString(MailSettings.SMTP_PASSWORD));
			port(settings.getInt(MailSettings.SMTP_PORT));

			defaultEmail(settings.getString(MailSettings.DEFAULT_FROM_EMAIL), settings.getString(MailSettings.DEFAULT_FROM_NAME));

			// optional add if found
			String key = settings.findString(MailSettings.SERVICE_API_KEY);
			if (key != null) {
				setApiKey(key);
			}
		}

		public Builder smtpUrl(String url) {

			Assert.notNullOrEmptyTrimmed(url, "Missing SMTP url!");
			add(SMTP_URL, url.trim());
			return this;
		}

		public Builder credentials(String username, String password) {

			username(username);
			password(password);
			return this;
		}

		public Builder username(String username) {

			Assert.notNullOrEmptyTrimmed(username, "Missing SMTP username!");
			add(SMTP_USERNAME, username.trim());
			return this;
		}

		public Builder password(String password) {

			add(SMTP_PASSWORD, password.trim());
			return this;
		}

		public Builder defaultEmail(String email, String name) {

			Assert.notNullOrEmptyTrimmed(email, "Missing default from email!");
			Assert.isTrue(ValidatingUtils.isEmail(email), "Invalid default email!");
			add(DEFAULT_FROM_EMAIL, email.trim().toLowerCase());

			if (!StringUtils.isNullOrEmptyTrimmed(name)) {
				add(DEFAULT_FROM_NAME, name.trim());
			}

			return this;
		}

		public Builder port(int port) {

			Assert.isTrue(port > 0 && port < 10000, "Invalid SMTP port given!");
			add(SMTP_PORT, port);
			return this;
		}

		public Builder setApiKey(String key) {

			Assert.notNullOrEmptyTrimmed(key, "Missing API key");
			add(SERVICE_API_KEY, key);
			return this;
		}
	}
}
