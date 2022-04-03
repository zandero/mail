package com.zandero.mail.service.smtp;

import com.zandero.mail.*;
import com.zandero.mail.service.*;
import com.zandero.utils.*;
import org.slf4j.*;

import javax.mail.*;
import java.util.*;

/**
 * Java general purpose Mail service
 */
public class SmtpMailService implements MailService {

    private static final Logger log = LoggerFactory.getLogger(SmtpMailService.class);

    private final String smtpHost;
    private final int smtpPort;

    private final String smtpUsername;
    private final String smtpPassword;

    /**
     * Initialize SMPT service
     *
     * @param url      SMPT server url
     * @param port     SMPT server port
     * @param username username
     * @param password password
     */
    public SmtpMailService(String url, int port, String username, String password) {

        Assert.notNullOrEmptyTrimmed(url, "Missing SMPT server url!");
        Assert.isTrue(port < 0 || port > 9999, "Invalid SMTP port given: " + port);

        smtpHost = url;
        smtpPort = port;

        smtpUsername = StringUtils.trimToNull(username);
        smtpPassword = StringUtils.trimToNull(password);
    }

    /**
     * Sends message out via SMTP
     *
     * @param message to construct mail message
     * @return result of send
     */
    @Override
    public MailSendResult send(MailMessage message) {

        Assert.notNull(message, "Missing mail message!");

        Properties props = new Properties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", smtpPort);

        Session session = getSession(props, smtpUsername, smtpPassword);

        try {
            // build mime message
            Message msg = message.getMessage(session);

            Enumeration enumer = msg.getAllHeaders();
            while (enumer.hasMoreElements()) {
                Header header = (Header) enumer.nextElement();
                log.info(header.getName() + ": " + header.getValue());
            }

            log.info("Getting transport...");

            Transport transport = session.getTransport("smtp");
            log.info("Connecting to SMTP server: " + smtpHost + ":" + smtpPort);

            transport.connect();
            transport.sendMessage(msg, msg.getAllRecipients());

            log.info("Closing transport...");
            transport.close();
        } catch (Exception e) {

            log.error(e.getMessage(), e);
            return MailSendResult.fail();
        }

        return MailSendResult.ok();

    }

    private static Session getSession(Properties props, String username, String password) {

        if (StringUtils.isNullOrEmptyTrimmed(username)) {
            return Session.getInstance(props);
        }

        props.put("mail.smtp.auth", true);
        return Session.getInstance(props, new javax.mail.Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(username, password);
            }
        });
    }
}
