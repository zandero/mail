package com.zandero.mail.service;

import com.codescore.common.settings.Settings;
import com.codescore.utils.Assert;
import com.codescore.utils.StringUtils;

public class MailSettings extends Settings {

	private static final long serialVersionUID = -7221476349703104698L;

	public static final String SMTP_URL = "smtp_url";

	public static final String SMTP_PORT = "smtp_port";

	public static final String SMTP_USERNAME = "smtp_username";

	public static final String SMTP_PASSWORD = "smtp_password";

	public static final String SMTP_DEFAULT_FROM_NAME = "smtp_from_name";

	public static final String SMTP_DEFAULT_FROM_EMAIL = "smtp_from_email";

	private static final String SERVICE_API_KEY = "api_key";


	public MailSettings() {

		// add default
		MailSettings.Builder builder = new MailSettings.Builder();
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
	 * OR
	 * - api key (if it contains ','
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

		return getString(SMTP_DEFAULT_FROM_EMAIL);
	}

	public String getDefaultFromName() {

		return findString(SMTP_DEFAULT_FROM_NAME);
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

		return "URL: " + getSmtpUrl() + ", Username: " + getSmtpUsername() + ", Password: " + getSmtpPassword();
	}

	public static class Builder extends Settings.Builder {

		public Builder() {
			// default settings
			add(SMTP_URL, "smtp://localhost");
			add(SMTP_PORT, 25);

			add(SMTP_USERNAME, "username");
			add(SMTP_PASSWORD, "password");

			add(SMTP_DEFAULT_FROM_NAME, "DevScore");
			add(SMTP_DEFAULT_FROM_EMAIL, "info@devscore.co");
		}

		public Builder(Settings settings) {

			// add (should is present by default)
			smtpUrl(settings.getString(MailSettings.SMTP_URL));
			credentials(settings.getString(MailSettings.SMTP_USERNAME), settings.getString(MailSettings.SMTP_PASSWORD));
			port(settings.getInt(MailSettings.SMTP_PORT));
			defaultEmail(settings.getString(MailSettings.SMTP_DEFAULT_FROM_EMAIL), settings.getString(MailSettings.SMTP_DEFAULT_FROM_NAME));

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
			add(SMTP_DEFAULT_FROM_EMAIL, email.trim());

			if (!StringUtils.isNullOrEmptyTrimmed(name)) {
				add(SMTP_DEFAULT_FROM_NAME, name.trim());
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
