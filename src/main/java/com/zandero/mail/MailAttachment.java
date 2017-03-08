package com.zandero.mail;

public class MailAttachment {

	public String type;
	public byte[] content;
	public String fileName;

	private MailAttachment() {
	}

	public MailAttachment(String type, byte[] content, String fileName) {
		this.type = type;
		this.content = content;
		this.fileName = fileName;
	}

	public MailAttachment(String type, String content, String fileName) {
		this.type = type;
		this.content = content.getBytes();
		this.fileName = fileName;
	}
}
