package com.zandero.mail.service;

import com.zandero.mail.MailMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import java.util.Enumeration;
import java.util.Properties;

/**
 * Java general purpose Mail service
 */
public class MailServiceImpl implements MailService {

	private static final Logger log = LoggerFactory.getLogger(MailServiceImpl.class);

	private final MailSettings settings;

	public MailServiceImpl(MailSettings mailSettings) {

		settings = mailSettings;
	}

	@Override
	public MailSendResult send(MailMessage builder) {

		final Properties props = getProperties(settings);
		Session session = Session.getDefaultInstance(props, null);

		try {

			// build mime message
			Message msg = builder.getMessage(session);

			Enumeration enumer = msg.getAllHeaders();
			while (enumer.hasMoreElements()) {
				Header header = (Header) enumer.nextElement();
				log.info(header.getName() + ": " + header.getValue());
			}

			log.info("Getting transport...");

			Transport transport = session.getTransport("smtp");
			log.info("Connecting to SMTP server: " + settings);

			transport.connect(settings.getSmtpUrl(), settings.getSmtpPort(), settings.getSmtpUsername(), settings.getSmtpPassword());
//			log.info("Sending e-mail to: " + JsonUtils.toJson(msg.getAllRecipients()));

			transport.sendMessage(msg, msg.getAllRecipients());
			log.info("Closing transport...");
			transport.close();
		}
		catch (Exception e) {

			log.error(e.getMessage(), e);
			return MailSendResult.fail();
		}

		return MailSendResult.ok();

	}

	/**
	 * Returns default session properties.
	 * To be overridden or extended in case additional properties are needed
	 *
	 * Fills up properties to be used in Session
	 * @param settings to fill up properties
	 * @return default properties
	 */
	public Properties getProperties(MailSettings settings) {

		Properties props = new Properties();

		try {
			log.info("Port: " + settings.getSmtpPort());
			props.put("mail.smtp.port", settings.getSmtpUrl());
		}
		catch (NumberFormatException e) {
			log.error("Invalid or missing SMPT_PORT");
			throw new IllegalArgumentException("SMTP_PORT - invalid or missing!");
		}
		props.put("mail.smtp.auth", "true");

		return props;
	}
}
