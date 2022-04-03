package com.zandero.mail;

/**
 * Mail attachemnt to be added to email message
 */
public class MailAttachment {

    /**
     * Attachment type
     */
    public String type;

    /**
     * Attachment content
     */
    public byte[] content;

    /**
     * Attachment file name
     */
    public String fileName;

    private MailAttachment() {
    }

    /**
     * Creates new mail attachment
     *
     * @param type     mime type of attachment
     * @param content  to be attached
     * @param fileName file name
     */
    public MailAttachment(String type, byte[] content, String fileName) {
        this.type = type;
        this.content = content;
        this.fileName = fileName;
    }

    /**
     * Creates new mail attachment
     *
     * @param type     mime type of attachment
     * @param content  to be attached
     * @param fileName file name
     */
    public MailAttachment(String type, String content, String fileName) {
        this.type = type;
        this.content = content.getBytes();
        this.fileName = fileName;
    }
}
