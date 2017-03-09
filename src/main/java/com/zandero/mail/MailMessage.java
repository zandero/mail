package com.zandero.mail;

import com.zandero.utils.Assert;
import com.zandero.utils.EncodeUtils;
import com.zandero.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.internet.*;
import javax.mail.util.ByteArrayDataSource;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * MailMassage builder for creating a new email massage
 */
public class MailMessage implements Serializable {

	private static final Logger log = LoggerFactory.getLogger(MailMessage.class);

	private static final long serialVersionUID = 355919787686445837L;

	Map<Message.RecipientType, Map<String, String>> emails;

	/**
	 * Email subject (title)
	 */
	private String subject;

	/**
	 * Email content
	 */
	private String content;

	/**
	 * Indicator that content is in HTML format
	 */
	private String htmlContent;

	/**
	 * List of attachments if any
	 */
	private List<MailAttachment> attachments;

	/**
	 * List od additional header
	 */
	private Map<String, String> headers;

	/**
	 * From email
	 */
	private String fromEmail;

	/**
	 * From name (or null)
	 */
	private String fromName;

	/**
	 * List of emails to exclude in TO, CC, BCC list
	 */
	private List<String> excludeEmails;


	/**
	 * timestamp when certain email should be send out
	 * in case mail system allows for this
	 */
	private Map<String, Long> emailSendAt;

	/**
	 * Build mail message
	 * @param session mail session
	 * @return build mime message
	 * @throws IllegalArgumentException in case massage could not be build up, wraps MessagingException and UnsupportedEncodingException
	 */
	public MimeMessage getMessage(Session session) {

		Assert.notNull(emails, "No email address given!");

		Map<String, String> recipients = emails.get(Message.RecipientType.TO);
		Assert.isTrue(recipients != null && recipients.size() > 0, "Missing TO email address(es)!");

		boolean found = false;
		for (String email : recipients.keySet()) {
			if (!excluded(email)) { // at least one address is not excluded
				found = true;
				break;
			}
		}

		Assert.isTrue(found, "All TO email address(es) are excluded!");

		if (StringUtils.isNullOrEmptyTrimmed(fromName)) {
			fromName = fromEmail;
		}

		Assert.notNullOrEmptyTrimmed(fromEmail, "Missing FROM email address!");
		Assert.notNullOrEmptyTrimmed(subject, "Missing email subject!");

		Assert.isTrue(!StringUtils.isNullOrEmptyTrimmed(content) || !StringUtils.isNullOrEmptyTrimmed(htmlContent), "Missing email content!");

		subject = subject.trim();
		content = content != null ? content.trim() : null;
		htmlContent = htmlContent != null ? htmlContent.trim() : null;

		log.info("Sending from: " + fromEmail + " (" + fromName + ")");

		MimeMessage msg = new MimeMessage(session);

		// FROM:
		try {
			msg.setFrom(new InternetAddress(fromEmail, fromName, EncodeUtils.UTF_8));

			// TO:
			addRecipients(Message.RecipientType.TO, msg);
			// CC:
			addRecipients(Message.RecipientType.CC, msg);
			// BCC:
			addRecipients(Message.RecipientType.BCC, msg);

			//msg.setSubject(subject, EncodeUtils.UTF_8);
			msg.setSubject(MimeUtility.encodeText(subject, EncodeUtils.UTF_8, "Q"));

			// add headers
			if (headers != null && headers.size() > 0) {
				for (String name : headers.keySet()) {
					msg.addHeader(name, headers.get(name));
				}
			}

			try {
				// simple message .. no attachment and only content or html content
				if ((attachments == null || attachments.size() == 0) &&
					((htmlContent == null && content != null) ||
						(htmlContent != null && content == null))) {

					if (StringUtils.isNullOrEmptyTrimmed(content)) {
						msg.addHeader("Content-Type", "text/html");
						msg.setContent(htmlContent, "text/html; charset=" + EncodeUtils.UTF_8);
					}
					else {
						msg.setContent(content, "text/plain; charset=" + EncodeUtils.UTF_8);
					}
				}
				// Compose multipart message
				else {
					// must be in correct order from lower fidelity to higher
					Multipart multipart = new MimeMultipart();

					if (!StringUtils.isNullOrEmptyTrimmed(content)) {
						MimeBodyPart contentPart = new MimeBodyPart();
						contentPart.setContent(content, "text/plain; charset=" + EncodeUtils.UTF_8);
						multipart.addBodyPart(contentPart);
					}

					if (!StringUtils.isNullOrEmptyTrimmed(htmlContent)) {
						MimeBodyPart htmlPart = new MimeBodyPart();
						htmlPart.setContent(htmlContent, "text/html; charset=" + EncodeUtils.UTF_8);
						multipart.addBodyPart(htmlPart);
					}

					if (attachments != null && attachments.size() > 0) {
						for (MailAttachment attachment : attachments) {

							// add attachment
							MimeBodyPart part = new MimeBodyPart();
							if (attachment.fileName != null) {
								part.setFileName(attachment.fileName);
							}
							DataSource src = new ByteArrayDataSource(attachment.content, attachment.type);
							part.setDataHandler(new DataHandler(src));
							multipart.addBodyPart(part);
						}
					}

					msg.setContent(multipart);
				}

				// if save is not called mime part headers are not updated
				msg.saveChanges();
				// see: http://stackoverflow.com/questions/5028670/how-to-set-mimebodypart-contenttype-to-text-html
			}
			catch (Exception e) {
				log.error("Failed to add attachment to mail message: ", e);
				throw new IllegalArgumentException(e.getMessage(), e);
			}

		}
		catch (MessagingException | UnsupportedEncodingException e) {
			log.error("Mail massage build failed!", e);
			throw new IllegalArgumentException(e.getMessage(), e);
		}

		return msg;
	}

	private void addRecipients(Message.RecipientType type, MimeMessage msg) throws UnsupportedEncodingException, MessagingException {

		Map<String, String> emailsAndNames = emails.get(type);

		if (emailsAndNames != null && emailsAndNames.size() > 0) {

			for (String email : emailsAndNames.keySet()) {

				String name = emailsAndNames.get(email);
				if (StringUtils.isNullOrEmptyTrimmed(name)) {
					name = email;
				}

				if (!excluded(email)) {
					msg.addRecipient(type, new InternetAddress(email, name, EncodeUtils.UTF_8));
					log.info("Sending: " + type + ": " + email + " (" + name + ")");
				}
				else {
					log.info("Excluding: " + type + ": " + email + " (" + name + ")");
				}
			}
		}
	}

	boolean excluded(String email) {

		return (excludeEmails != null && excludeEmails.contains(email));
	}

	/**
	 * Sets from email address only if not already set
	 *
	 * @param email target email address
	 * @param name  target name associated with email address
	 * @return mail message
	 */
	public MailMessage defaultFrom(String email, String name) {

		if (StringUtils.isNullOrEmptyTrimmed(fromEmail)) {
			from(email, name);
		}

		if (!StringUtils.isNullOrEmptyTrimmed(email) &&
			StringUtils.equals(fromEmail, email.trim(), true) &&
			StringUtils.isNullOrEmptyTrimmed(fromName)) {
			from(email, name);
		}

		return this;
	}

	public MailMessage from(String email) {

		from(email, null);
		return this;
	}

	public MailMessage from(String email, String name) {

		checkEmailAddress(email, "from");

		// only set sender if not already set
		fromEmail = email.trim().toLowerCase();

		if (StringUtils.isNullOrEmptyTrimmed(name)) {
			fromName = null;
		}
		else {
			fromName = name.trim();
		}

		return this;
	}

	public void fromName(String from) {

		fromName = StringUtils.trimToNull(from);
	}

	public MailMessage to(String email) {

		return add(Message.RecipientType.TO, email);
	}

	public MailMessage to(List<String> emails) {

		return add(Message.RecipientType.TO, emails);
	}

	public MailMessage to(String email, String name) {

		return add(Message.RecipientType.TO, email, name);
	}

	public MailMessage to(Map<String, String> emailAndName) {

		return add(Message.RecipientType.TO, emailAndName);
	}

	public MailMessage to(List<String> emails, List<String> names) {

		return add(Message.RecipientType.TO, emails, names);
	}

	public MailMessage cc(String email) {

		return add(Message.RecipientType.CC, email);
	}

	public MailMessage cc(List<String> emails) {

		return add(Message.RecipientType.CC, emails);
	}

	public MailMessage cc(String email, String name) {

		return add(Message.RecipientType.CC, email, name);
	}

	public MailMessage cc(Map<String, String> emailAndName) {

		return add(Message.RecipientType.CC, emailAndName);
	}

	public MailMessage cc(List<String> emails, List<String> names) {

		return add(Message.RecipientType.CC, emails, names);
	}

	public MailMessage bcc(String email) {

		return add(Message.RecipientType.BCC, email);
	}

	public MailMessage bcc(List<String> emails) {

		return add(Message.RecipientType.BCC, emails);
	}

	public MailMessage bcc(String email, String name) {

		return add(Message.RecipientType.BCC, email, name);
	}

	public MailMessage bcc(Map<String, String> emailAndName) {

		return add(Message.RecipientType.BCC, emailAndName);
	}

	public MailMessage bcc(List<String> emails, List<String> names) {

		return add(Message.RecipientType.BCC, emails, names);
	}

	private MailMessage add(Message.RecipientType recipientType, String email, String name) {

		Assert.notNull(recipientType, "Missing recipient type!");
		checkEmailAddress(email, recipientType.toString());

		if (emails == null) {
			emails = new LinkedHashMap<>();
		}

		emails.putIfAbsent(recipientType, new LinkedHashMap<>());

		email = email.trim().toLowerCase();  // by default convert email to lowercase
		name = StringUtils.trimToNull(name); // trim down ... or null if empty

		emails.get(recipientType).put(email, name);
		return this;
	}

	private MailMessage add(Message.RecipientType recipientType, String email) {

		add(recipientType, email, null);
		return this;
	}

	private MailMessage add(Message.RecipientType recipientType, List<String> emails) {

		if (emails == null || emails.size() == 0) {
			return this;
		}

		for (String email : emails) {
			add(recipientType, email, null);
		}

		return this;
	}

	private MailMessage add(Message.RecipientType recipientType, Map<String, String> emailAndName) {

		if (emailAndName == null || emailAndName.size() == 0) {
			return this;
		}

		for (String email : emailAndName.keySet()) {
			add(recipientType, email, emailAndName.get(email));
		}

		return this;
	}

	private MailMessage add(Message.RecipientType recipientType, List<String> emails, List<String> names) {

		if (emails == null || emails.size() == 0) {
			return this;
		}

		if (names == null || names.size() == 0) {
			return add(recipientType, emails);
		}

		Assert.isTrue(names.size() == emails.size(), "Names and emails list must have same number of items!");

		int index = 0;
		for (String email : emails) {
			add(recipientType, email, names.get(index));
			index++;
		}

		return this;
	}

	public MailMessage subject(String value) {

		if (!StringUtils.isNullOrEmptyTrimmed(value)) {
			subject = value.trim();
		}
		return this;
	}

	public MailMessage content(String value) {

		if (!StringUtils.isNullOrEmptyTrimmed(value)) {
			content = value.trim();
		}
		return this;
	}

	public MailMessage html(String value) {

		if (!StringUtils.isNullOrEmptyTrimmed(value)) {
			htmlContent = value.trim();
		}
		return this;
	}

	public MailMessage headers(Map<String, String> value) {

		if (value != null && value.size() > 0) {

			for (String key : value.keySet()) {
				headers(key, value.get(key));
			}
		}
		return this;
	}

	public MailMessage headers(String name, String value) {

		Assert.notNullOrEmptyTrimmed(name, "Missing header name!");
		Assert.notNullOrEmptyTrimmed(value, "Missing header value!");

		if (headers == null) {
			headers = new HashMap<>();
		}

		headers.put(name.trim(), value.trim());
		return this;
	}

	public MailMessage exclude(String email) {

		checkEmailAddress(email, "excluded");

		if (excludeEmails == null) {
			excludeEmails = new ArrayList<>();
		}

		email = email.trim();
		if (!excludeEmails.contains(email)) {
			excludeEmails.add(email);
		}

		return this;
	}

	public MailMessage attachment(String content,
	                              String fileName,
	                              String mimeType) {

		Assert.notNullOrEmptyTrimmed(content, "Missing attachment content!");
		Assert.notNullOrEmptyTrimmed(fileName, "Missing attachment file name!");
		Assert.notNullOrEmptyTrimmed(mimeType, "Missing attachment mime type!");

		if (attachments == null) {
			attachments = new ArrayList<>();
		}

		attachments.add(new MailAttachment(mimeType.trim(),
			content.trim(),
			fileName.trim()));
		return this;
	}

	public MailMessage attachments(List<MailAttachment> list) {

		if (attachments == null) {
			attachments = new ArrayList<>();
		}

		attachments.addAll(list);
		return this;
	}

	public MailMessage setSendAt(String email, long timeStamp) {

		if (StringUtils.isEmail(email)) {
			if (emailSendAt == null) {
				emailSendAt = new HashMap<>();
			}

			emailSendAt.put(email.trim().toLowerCase(), timeStamp);
		}

		return this;
	}

	// Getters

	/**
	 * @return from email address
	 */
	public String getFromEmail() {

		return fromEmail;
	}

	/**
	 * @return from email name
	 */
	public String getFromName() {

		return fromName;
	}

	/**
	 * @return list of TO emails with names (if given) where email is key
	 */
	public Map<String, String> getToEmails() {

		if (emails != null) {
			return emails.get(Message.RecipientType.TO);
		}

		return null;
	}

	/**
	 * @return list of CC emails with names (if given) where email is key
	 */
	public Map<String, String> getCcEmails() {

		if (emails != null) {
			return emails.get(Message.RecipientType.CC);
		}

		return null;
	}

	/**
	 * @return list of BCC emails with names (if given) where email is key
	 */
	public Map<String, String> getBccEmails() {

		if (emails != null) {
			return emails.get(Message.RecipientType.BCC);
		}

		return null;
	}

	/**
	 * @return email subject
	 */
	public String getSubject() {

		return subject;
	}

	/**
	 * @return email content (plain) if set as such @see(getHtmlContent)
	 */
	public String getContent() {

		return content;
	}

	/**
	 * @return email HTML content (if set)
	 */
	public String getHtmlContent() {

		return htmlContent;
	}

	/**
	 * @return name value list of email headers
	 */
	public Map<String, String> getHeaders() {

		return headers;
	}

	/**
	 * @return list of emails to exclude when sending out message
	 */
	public List<String> getExcludedEmails() {

		return excludeEmails;
	}

	/**
	 * @return list of mime attachments
	 */
	public List<MailAttachment> getAttachments() {

		return attachments;
	}

	/**
	 * Gets timestamp email should be send out (the future)
	 *
	 * @param toEmail email address
	 * @return time stamp or null if not set
	 */
	public Long getSendAt(String toEmail) {

		if (emailSendAt == null || !StringUtils.isEmail(toEmail)) {
			return null;
		}

		return emailSendAt.get(toEmail.trim().toLowerCase());
	}

	private void checkEmailAddress(String email, String type) {

		Assert.notNullOrEmptyTrimmed(email, "Missing " + (type == null ? "" : type.toLowerCase()) + " email address!");
		Assert.isTrue(StringUtils.isEmail(email), "Invalid from email address!");
	}
}