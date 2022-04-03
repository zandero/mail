package com.zandero.mail;

import com.zandero.utils.*;
import com.zandero.utils.extra.*;
import org.slf4j.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.util.*;
import java.io.*;
import java.time.*;
import java.util.*;

/**
 * MailMassage builder for creating a new email massage
 */
public class MailMessage implements Serializable {

    private static final Logger log = LoggerFactory.getLogger(MailMessage.class);

    private static final long serialVersionUID = 355919787686445837L;
    private static final String UTF_8 = "UTF-8";

    /**
     * email addresses storage
     */
    protected Map<Message.RecipientType, Map<String, String>> emails; // type / email-name (pairs)

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
     * timestamp when message should be send out
     * in case mail system allows for this
     */
    private Instant emailSendAt;

    /**
     * Empty mail message
     */
    public MailMessage() {
    }

    /**
     * Build mail message
     *
     * @param session mail session
     * @return build mime message
     * @throws IllegalArgumentException in case massage could not be build up, wraps MessagingException and UnsupportedEncodingException
     */
    public MimeMessage getMessage(Session session) {

        Assert.notNull(emails, "No email address given!");

        Map<String, String> recipients = emails.get(Message.RecipientType.TO);
        Assert.isTrue(recipients != null && recipients.size() > 0, "Missing to email address(es)!");

        boolean found = false;
        for (String email : recipients.keySet()) {
            if (!excluded(email)) { // at least one address is not excluded
                found = true;
                break;
            }
        }

        Assert.isTrue(found, "All to email address(es) are excluded!");

        if (StringUtils.isNullOrEmptyTrimmed(fromName)) {
            fromName = fromEmail;
        }

        Assert.notNullOrEmptyTrimmed(fromEmail, "Missing from email address!");
        Assert.notNullOrEmptyTrimmed(subject, "Missing email subject!");

        Assert.isTrue(!StringUtils.isNullOrEmptyTrimmed(content) || !StringUtils.isNullOrEmptyTrimmed(htmlContent), "Missing email content!");

        subject = subject.trim();
        content = content != null ? content.trim() : null;
        htmlContent = htmlContent != null ? htmlContent.trim() : null;

        log.info("Sending from: " + fromEmail + " (" + fromName + ")");

        MimeMessage msg = new MimeMessage(session);

        // FROM:
        try {
            msg.setFrom(new InternetAddress(fromEmail, fromName, UTF_8));

            // TO:
            addRecipients(Message.RecipientType.TO, msg);
            // CC:
            addRecipients(Message.RecipientType.CC, msg);
            // BCC:
            addRecipients(Message.RecipientType.BCC, msg);

            //msg.setSubject(subject, UTF_8);
            msg.setSubject(MimeUtility.encodeText(subject, UTF_8, "Q"));

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
                        msg.setContent(htmlContent, "text/html; charset=" + UTF_8);
                    } else {
                        msg.setContent(content, "text/plain; charset=" + UTF_8);
                    }
                }
                // Compose multipart message
                else {
                    // must be in correct order from lower fidelity to higher
                    Multipart multipart = new MimeMultipart();

                    if (!StringUtils.isNullOrEmptyTrimmed(content)) {
                        MimeBodyPart contentPart = new MimeBodyPart();
                        contentPart.setContent(content, "text/plain; charset=" + UTF_8);
                        multipart.addBodyPart(contentPart);
                    }

                    if (!StringUtils.isNullOrEmptyTrimmed(htmlContent)) {
                        MimeBodyPart htmlPart = new MimeBodyPart();
                        htmlPart.setContent(htmlContent, "text/html; charset=" + UTF_8);
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
            } catch (Exception e) {
                log.error("Failed to add attachment to mail message: ", e);
                throw new IllegalArgumentException(e.getMessage(), e);
            }

        } catch (MessagingException | UnsupportedEncodingException e) {
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
                    msg.addRecipient(type, new InternetAddress(email, name, UTF_8));
                    log.info("Sending: " + type + ": " + email + " (" + name + ")");
                } else {
                    log.info("Excluding: " + type + ": " + email + " (" + name + ")");
                }
            }
        }
    }

    /**
     * Checks if email address is on excluded list
     *
     * @param email address
     * @return true if excluded, false otherwise
     */
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

    /**
     * Sets from email address
     *
     * @param email address
     * @return mail message (self)
     */
    public MailMessage from(String email) {

        from(email, null);
        return this;
    }

    /**
     * Sets from email address and name
     *
     * @param email address
     * @param name  name
     * @return mail message (self)
     */
    public MailMessage from(String email, String name) {

        checkEmailAddress(email, "from");

        // only set sender if not already set
        fromEmail = email.trim().toLowerCase();

        if (StringUtils.isNullOrEmptyTrimmed(name)) {
            fromName = null;
        } else {
            fromName = name.trim();
        }

        return this;
    }

    /**
     * sets From email name
     *
     * @param from name
     * @return mail message (self)
     */
    public MailMessage fromName(String from) {
        fromName = StringUtils.trimToNull(from);
        return this;
    }

    /**
     * Adds TO email address
     *
     * @param email address
     * @return mail message (self)
     */
    public MailMessage to(String email) {

        return add(Message.RecipientType.TO, email);
    }

    /**
     * Adds list of TO emails
     *
     * @param emails list of emails (without names)
     * @return mail message (self)
     */
    public MailMessage to(List<String> emails) {

        return add(Message.RecipientType.TO, emails);
    }

    /**
     * Adds TO
     *
     * @param email address
     * @param name  person
     * @return mail message (self)
     */
    public MailMessage to(String email, String name) {

        return add(Message.RecipientType.TO, email, name);
    }

    /**
     * Adds TO email
     *
     * @param emailAndName map of email=name pairs
     * @return mail message (self)
     */
    public MailMessage to(Map<String, String> emailAndName) {

        return add(Message.RecipientType.TO, emailAndName);
    }

    /**
     * Sets TO
     *
     * @param emails list of emails to BBC
     * @param names  list of names corresponding to emails
     * @return mail message (self)
     */
    public MailMessage to(List<String> emails, List<String> names) {

        return add(Message.RecipientType.TO, emails, names);
    }

    /**
     * Adds CC email address
     *
     * @param email address
     * @return mail message (self)
     */
    public MailMessage cc(String email) {

        return add(Message.RecipientType.CC, email);
    }

    /**
     * Adds list of CC emails
     *
     * @param emails list of emails (without names)
     * @return mail message (self)
     */
    public MailMessage cc(List<String> emails) {

        return add(Message.RecipientType.CC, emails);
    }

    /**
     * Adds CC
     *
     * @param email address
     * @param name  person
     * @return mail message (self)
     */
    public MailMessage cc(String email, String name) {

        return add(Message.RecipientType.CC, email, name);
    }

    /**
     * Adds CC email
     *
     * @param emailAndName map of email=name pairs
     * @return mail message (self)
     */
    public MailMessage cc(Map<String, String> emailAndName) {

        return add(Message.RecipientType.CC, emailAndName);
    }

    /**
     * Sets CC
     *
     * @param emails list of emails to BBC
     * @param names  list of names corresponding to emails
     * @return mail message (self)
     */
    public MailMessage cc(List<String> emails, List<String> names) {

        return add(Message.RecipientType.CC, emails, names);
    }

    /**
     * Adds BBC email address
     *
     * @param email address
     * @return mail message (self)
     */
    public MailMessage bcc(String email) {

        return add(Message.RecipientType.BCC, email);
    }

    /**
     * Adds list of BBC emails
     *
     * @param emails list of emails (without names)
     * @return mail message (self)
     */
    public MailMessage bcc(List<String> emails) {

        return add(Message.RecipientType.BCC, emails);
    }

    /**
     * Adds BBC
     *
     * @param email address
     * @param name  person
     * @return mail message (self)
     */
    public MailMessage bcc(String email, String name) {

        return add(Message.RecipientType.BCC, email, name);
    }

    /**
     * Adds BBC email
     *
     * @param emailAndName map of email=name pairs
     * @return mail message (self)
     */
    public MailMessage bcc(Map<String, String> emailAndName) {

        return add(Message.RecipientType.BCC, emailAndName);
    }

    /**
     * Sets BBC
     *
     * @param emails list of emails to BBC
     * @param names  list of names corresponding to emails
     * @return mail message (self)
     */
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

    /**
     * Sets mail subject
     *
     * @param value subject
     * @return mail message (self)
     */
    public MailMessage subject(String value) {

        if (!StringUtils.isNullOrEmptyTrimmed(value)) {
            subject = value.trim();
        }
        return this;
    }

    /**
     * Sets mail content
     *
     * @param value raw content
     * @return mail message (self)
     */
    public MailMessage content(String value) {

        if (!StringUtils.isNullOrEmptyTrimmed(value)) {
            content = value.trim();
        }
        return this;
    }

    /**
     * Sets HTML content
     *
     * @param value html
     * @return mail message (self)
     */
    public MailMessage html(String value) {

        if (!StringUtils.isNullOrEmptyTrimmed(value)) {
            htmlContent = value.trim();
        }
        return this;
    }

    /**
     * Sets mail headers
     *
     * @param value headers
     * @return mail message (self)
     */
    public MailMessage headers(Map<String, String> value) {

        if (value != null && value.size() > 0) {

            for (String key : value.keySet()) {
                headers(key, value.get(key));
            }
        }

        return this;
    }

    /**
     * Sets mail header
     *
     * @param name  header name
     * @param value header value
     * @return mail message (self)
     */
    public MailMessage headers(String name, String value) {

        Assert.notNullOrEmptyTrimmed(name, "Missing header name!");
        Assert.notNullOrEmptyTrimmed(value, "Missing header value!");

        if (headers == null) {
            headers = new HashMap<>();
        }

        headers.put(name.trim(), value.trim());
        return this;
    }

    /**
     * Sets email addresses to be excluded
     *
     * @param email to exclude
     * @return mail message (self)
     */
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

    /**
     * Adds attachment
     *
     * @param content  to be added
     * @param fileName file name
     * @param mimeType mime type
     * @return mail message (self)
     */
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

    /**
     * Adds attachments
     *
     * @param list of attachement
     * @return mail message (self)
     */
    public MailMessage attachments(List<MailAttachment> list) {

        if (attachments == null) {
            attachments = new ArrayList<>();
        }

        attachments.addAll(list);
        return this;
    }

    /**
     * Sets send at time stamp
     *
     * @param timeStamp when to send out ... if in the future
     * @return mail message (self)
     */
    public MailMessage setSendAt(Instant timeStamp) {

        if (timeStamp != null && Instant.now().isBefore(timeStamp))
            emailSendAt = timeStamp;
        else
            emailSendAt = null;

        return this;
    }

    // Getters

    /**
     * Returns from email aadress
     *
     * @return from email address
     */
    public String getFromEmail() {

        return fromEmail;
    }

    /**
     * From email name (if set)
     *
     * @return from email name
     */
    public String getFromName() {

        return fromName;
    }

    /**
     * List of TO email addresses (as email=name) pairs
     *
     * @return list of TO emails with names (if given) where email is key
     */
    public Map<String, String> getToEmails() {

        if (emails != null) {
            return getEmails(emails.get(Message.RecipientType.TO));
        }

        return null;
    }


    /**
     * List of CC email addresses (as email=name) pairs
     *
     * @return list of CC emails with names (if given) where email is key
     */
    public Map<String, String> getCcEmails() {

        if (emails != null) {
            return getEmails(emails.get(Message.RecipientType.CC));
        }

        return null;
    }

    /**
     * List of BCC email addresses (as email=name) pairs
     *
     * @return list of BCC emails with names (if given) where email is key
     */
    public Map<String, String> getBccEmails() {

        if (emails != null) {
            return getEmails(emails.get(Message.RecipientType.BCC));
        }

        return null;
    }

    /**
     * Filters out excluded emails if any
     *
     * @param emails to be inspeced
     * @return map without excluded emails
     */
    private Map<String, String> getEmails(Map<String, String> emails) {

        if (emails == null || emails.size() == 0) {
            return null;
        }

        Map<String, String> out = new HashMap<>();

        for (String email : emails.keySet()) {
            if (excluded(email)) {
                continue;
            }

            out.put(email, emails.get(email));
        }

        return out;
    }

    /**
     * Get emails as string
     *
     * @param type recipient type
     * @return emails formated as Bob &lt;bob@email.com&gt; separated with commas or null if empty
     */
    public String getEmailsAsString(Message.RecipientType type) {

        Assert.notNull(type, "Missing recipient type!");
        Map<String, String> emailNames = emails.get(type);
        if (emailNames == null) {
            return null;
        }

        List<String> items = new ArrayList<>();
        for (String email : emailNames.keySet()) {

            String name = emailNames.get(email);
            if (StringUtils.isNullOrEmptyTrimmed(name) ||
                    StringUtils.equals(email, name, true)) {
                items.add(email);
            } else {
                items.add(name + " <" + email + ">");
            }
        }

        if (items.size() == 0) {
            return null;
        }

        return StringUtils.join(items, ", ");
    }

    /**
     * Email subject line
     *
     * @return email subject
     */
    public String getSubject() {

        return subject;
    }

    /**
     * Email content (raw)
     *
     * @return email content (plain) if set as such @see(getHtmlContent)
     */
    public String getContent() {

        return content;
    }

    /**
     * Email content in HTML format
     *
     * @return email HTML content (if set)
     */
    public String getHtmlContent() {

        return htmlContent;
    }

    /**
     * Email headers (name=value) pairs
     *
     * @return name value list of email headers
     */
    public Map<String, String> getHeaders() {

        return headers;
    }

    /**
     * List of excluded email addresses
     *
     * @return list of emails to exclude when sending out message
     */
    public List<String> getExcludedEmails() {

        return excludeEmails;
    }

    /**
     * List of mime attachments
     *
     * @return list of mime attachments
     */
    public List<MailAttachment> getAttachments() {

        return attachments;
    }

    /**
     * Gets timestamp message should be send out (the future)
     *
     * @return time stamp or null if not set
     */
    public Instant getSendAt() {

        return emailSendAt;
    }

    private void checkEmailAddress(String email, String type) {

        String description = type == null ? "" : type.toLowerCase();
        Assert.notNullOrEmptyTrimmed(email, "Missing " + description + " email address!");
        Assert.isTrue(ValidatingUtils.isEmail(email), "Invalid " + description + " email address: '" + email + "'!");
    }
}