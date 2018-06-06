package com.zandero.mail;

import org.junit.jupiter.api.Test;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.util.SharedByteArrayInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;


public class MailMessageTest {

	@Test
	void to() {

		MailMessage message = new MailMessage();
		message.to("mail@email.com");

		assertNotNull(message.emails);
		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// set name
		message.to("mail@email.com", "name");
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());
	}

	@Test
	void to_list() {

		List<String> emails = new ArrayList<>();

		MailMessage message = new MailMessage();
		message.to(emails);
		assertNull(message.emails);

		// 2.
		emails.add("mail@email.com");
		message.to(emails);
		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// 3.
		emails.add("mail2@email.com");
		message.to(emails);

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.TO).get("mail@email.com"));

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail2@email.com"));
		assertNull(message.emails.get(Message.RecipientType.TO).get("mail2@email.com"));
		assertEquals(2, message.emails.get(Message.RecipientType.TO).size());
	}

	@Test
	void to_emailName() {

		MailMessage message = new MailMessage();
		message.to("mail@email.com", "name");

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// 2.
		message.to("mail@email.com", "new");
		message.to("mail2@email.com", "name2");

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("new", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.TO).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.TO).size());
	}

	@Test
	void to_map() {

		HashMap<String, String> emailsNames = new HashMap<>();

		MailMessage message = new MailMessage();
		message.to(emailsNames);

		assertNull(message.emails);

		//
		emailsNames.put("mail@email.com", null);
		message.to(emailsNames);

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// 1.
		emailsNames.put("mail@email.com", "name");
		message.to(emailsNames);

		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// 2.
		emailsNames.put("mail@email.com", "new");
		emailsNames.put("mail2@email.com", "name2");
		message.to(emailsNames);

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("new", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.TO).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.TO).size());
	}

	@Test
	void to_lists() {

		List<String> emails = new ArrayList<>();
		List<String> names = new ArrayList<>();

		MailMessage message = new MailMessage();
		message.to(emails, names);

		assertNull(message.emails);

		//
		emails.add("mail@email.com");
		message.to(emails, names);

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// 1.
		names.add("name");
		message.to(emails, names);

		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.TO).size());

		// 2.
		emails.add("mail2@email.com");
		names.add("name2");
		message.to(emails, names);

		assertNotNull(message.emails.get(Message.RecipientType.TO));
		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.TO).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.TO).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.TO).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.TO).size());
	}

	@Test
	void testInvalid_to_1() {

		MailMessage massage = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to((String)null));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_1_2() {

		MailMessage massage = new MailMessage();
		String email = "  ";
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(email));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_2() {

		MailMessage massage = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to((String)null, null));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_2_1() {

		MailMessage massage = new MailMessage();
		String email = "  ";
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(email, null));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_3() {

		MailMessage massage = new MailMessage();
		List<String> emails = new ArrayList<>();
		emails.add(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_3_1() {

		MailMessage massage = new MailMessage();
		List<String> emails = new ArrayList<>();
		emails.add("  ");
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_4() {

		MailMessage massage = new MailMessage();
		Map<String, String> emails = new HashMap<>();
		emails.put(null, null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_4_1() {

		MailMessage massage = new MailMessage();
		Map<String, String> emails = new HashMap<>();
		emails.put("  ", null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_5() {

		MailMessage massage = new MailMessage();
		List<String> emails = new ArrayList<>();
		List<String> names = new ArrayList<>();
		emails.add(null);
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails, names));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_5_1() {

		MailMessage massage = new MailMessage();
		List<String> emails = new ArrayList<>();
		List<String> names = new ArrayList<>();
		emails.add("  ");
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails, names));
		assertEquals("Missing to email address!", e.getMessage());
	}

	@Test
	void testInvalid_to_5_2() {

		MailMessage massage = new MailMessage();
		List<String> emails = new ArrayList<>();
		List<String> names = new ArrayList<>();
		emails.add("email");
		emails.add("email2");

		names.add("name");
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> massage.to(emails, names));
		assertEquals("Names and emails list must have same number of items!", e.getMessage());
	}

	// CC
	@Test
	void cc() {

		MailMessage message = new MailMessage();
		message.cc("mail@email.com");

		assertNotNull(message.emails);
		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// set name
		message.cc("mail@email.com", "name");
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());
	}

	@Test
	void cc_list() {

		List<String> emails = new ArrayList<>();

		MailMessage message = new MailMessage();
		message.cc(emails);
		assertNull(message.emails);

		// 2.
		emails.add("mail@email.com");
		message.cc(emails);
		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.CC).get("mail"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// 3.
		emails.add("mail2@email.com");
		message.cc(emails);

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.CC).get("mail"));

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail2@email.com"));
		assertNull(message.emails.get(Message.RecipientType.CC).get("mail2"));
		assertEquals(2, message.emails.get(Message.RecipientType.CC).size());
	}

	@Test
	void cc_emailName() {

		MailMessage message = new MailMessage();
		message.cc("mail@email.com", "name");

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// 2.
		message.cc("mail@email.com", "new");
		message.cc("mail2@email.com", "name2");

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("new", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.CC).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.CC).size());
	}

	@Test
	void cc_map() {

		HashMap<String, String> emailsNames = new HashMap<>();

		MailMessage message = new MailMessage();
		message.cc(emailsNames);

		assertNull(message.emails);

		//
		emailsNames.put("mail@email.com", null);
		message.cc(emailsNames);

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// 1.
		emailsNames.put("mail@email.com", "name");
		message.cc(emailsNames);

		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// 2.
		emailsNames.put("mail@email.com", "new");
		emailsNames.put("mail2@email.com", "name2");
		message.cc(emailsNames);

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("new", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.CC).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.CC).size());
	}

	@Test
	void cc_lists() {

		List<String> emails = new ArrayList<>();
		List<String> names = new ArrayList<>();

		MailMessage message = new MailMessage();
		message.cc(emails, names);

		assertNull(message.emails);

		//
		emails.add("mail@email.com");
		message.cc(emails, names);

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// 1.
		names.add("name");
		message.cc(emails, names);

		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.CC).size());

		// 2.
		emails.add("mail2@email.com");
		names.add("name2");
		message.cc(emails, names);

		assertNotNull(message.emails.get(Message.RecipientType.CC));
		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.CC).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.CC).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.CC).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.CC).size());
	}

	// BCC
	@Test
	void bcc() {

		MailMessage message = new MailMessage();
		message.bcc("mail@email.com");

		assertNotNull(message.emails);
		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// set name
		message.bcc("mail@email.com", "name");
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());
	}

	@Test
	void bcc_list() {

		List<String> emails = new ArrayList<>();

		MailMessage message = new MailMessage();
		message.bcc(emails);
		assertNull(message.emails);

		// 2.
		emails.add("mail@email.com");
		message.bcc(emails);
		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// 3.
		emails.add("mail2@email.com");
		message.bcc(emails);

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail2@email.com"));
		assertNull(message.emails.get(Message.RecipientType.BCC).get("mail2@email.com"));
		assertEquals(2, message.emails.get(Message.RecipientType.BCC).size());
	}

	@Test
	void bcc_emailName() {

		MailMessage message = new MailMessage();
		message.bcc("mail@email.com", "name");

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// 2.
		message.bcc("mail@email.com", "new");
		message.bcc("mail2@email.com", "name2");

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("new", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.BCC).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.BCC).size());
	}

	@Test
	void bcc_map() {

		HashMap<String, String> emailsNames = new HashMap<>();

		MailMessage message = new MailMessage();
		message.bcc(emailsNames);

		assertNull(message.emails);

		//
		emailsNames.put("mail@email.com", null);
		message.bcc(emailsNames);

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// 1.
		emailsNames.put("mail@email.com", "name");
		message.bcc(emailsNames);

		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// 2.
		emailsNames.put("mail@email.com", "new");
		emailsNames.put("mail2@email.com", "name2");
		message.bcc(emailsNames);

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("new", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.BCC).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.BCC).size());
	}

	@Test
	void bcc_lists() {

		List<String> emails = new ArrayList<>();
		List<String> names = new ArrayList<>();

		MailMessage message = new MailMessage();
		message.bcc(emails, names);

		assertNull(message.emails);

		//
		emails.add("mail@email.com");
		message.bcc(emails, names);

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertNull(message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// 1.
		names.add("name");
		message.bcc(emails, names);

		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));
		assertEquals(1, message.emails.get(Message.RecipientType.BCC).size());

		// 2.
		emails.add("mail2@email.com");
		names.add("name2");
		message.bcc(emails, names);

		assertNotNull(message.emails.get(Message.RecipientType.BCC));
		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail@email.com"));
		assertEquals("name", message.emails.get(Message.RecipientType.BCC).get("mail@email.com"));

		assertTrue(message.emails.get(Message.RecipientType.BCC).containsKey("mail2@email.com"));
		assertEquals("name2", message.emails.get(Message.RecipientType.BCC).get("mail2@email.com"));

		assertEquals(2, message.emails.get(Message.RecipientType.BCC).size());
	}

	@Test
	void from() {

		MailMessage message = new MailMessage();
		message.from(" a@a.com ");

		assertEquals("a@a.com", message.getFromEmail());
		assertNull(message.getFromName());

		// once set only

		message.from(" b@b.com ", "  c  ");
		assertEquals("b@b.com", message.getFromEmail());
		assertEquals("c", message.getFromName());

		message.from(" D@d.com ", "");
		assertEquals("d@d.com", message.getFromEmail());
		assertNull(message.getFromName());
	}

	@Test
	void testFrom_fail() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.from("  "));
		assertEquals("Missing from email address!", e.getMessage());
	}

	@Test
	void testFrom_fail2() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.from("  ", "bla"));
		assertEquals("Missing from email address!", e.getMessage());
	}

	@Test
	void testDefaultFrom_fail() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.defaultFrom("  ", "bla"));
		assertEquals("Missing from email address!", e.getMessage());
	}

	@Test
	void defaultFrom() {

		MailMessage message = new MailMessage();
		message.defaultFrom(" a@a.com ", "  b  ");

		assertEquals("a@a.com", message.getFromEmail());
		assertEquals("b", message.getFromName());

		message.from("c@c.com");
		assertEquals("c@c.com", message.getFromEmail());
		assertNull(message.getFromName());

		message.defaultFrom(" a@a.com ", "  b  ");
		assertEquals("c@c.com", message.getFromEmail());
		assertNull(message.getFromName());
	}

	@Test
	void defaultFrom_2() {

		MailMessage message = new MailMessage();
		message.from("c@c.com");
		assertEquals("c@c.com", message.getFromEmail());
		assertNull(message.getFromName());

		message.defaultFrom(" a@a.com ", "  b  ");

		assertEquals("c@c.com", message.getFromEmail());
		assertNull(message.getFromName());
	}

	@Test
	void defaultFrom_3() {

		MailMessage message = new MailMessage();
		message.from(" a@a.com ", null);
		assertEquals("a@a.com", message.getFromEmail());
		assertNull(message.getFromName());

		// override name if not given in from
		message.defaultFrom(" a@a.com ", "  b  ");

		assertEquals("a@a.com", message.getFromEmail());
		assertEquals("b", message.getFromName());

	}

	@Test
	void subject() {

		MailMessage message = new MailMessage();
		message.subject(null);
		assertNull(message.getSubject());

		message.subject("");
		assertNull(message.getSubject());

		message.subject(" ");
		assertNull(message.getSubject());

		message.subject("  aaa ");
		assertEquals("aaa", message.getSubject());
	}

	@Test
	void content() {

		MailMessage message = new MailMessage();
		message.content(null);
		assertNull(message.getContent());

		message.content("");
		assertNull(message.getContent());

		message.content(" ");
		assertNull(message.getContent());

		message.content("  aaa ");
		assertEquals("aaa", message.getContent());
	}

	@Test
	void headers() {

		MailMessage message = new MailMessage();
		message.headers(" a ", " b ");

		assertEquals(1, message.getHeaders().size());
		assertEquals("b", message.getHeaders().get("a"));

		Map<String, String> map = new HashMap<>();
		message.headers(map);
		assertEquals(1, message.getHeaders().size());
		assertEquals("b", message.getHeaders().get("a"));

		map.put("  a ", "bb");
		message.headers(map);
		assertEquals(1, message.getHeaders().size());
		assertEquals("bb", message.getHeaders().get("a"));

		map.put("b", "c");
		message.headers(map);
		assertEquals(2, message.getHeaders().size());
		assertEquals("bb", message.getHeaders().get("a"));
		assertEquals("c", message.getHeaders().get("b"));
	}

	@Test
	void headers_fail1() {

		MailMessage message = new MailMessage();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.headers("  ", "b"));
		assertEquals("Missing header name!", e.getMessage());
	}

	@Test
	void headers_fail2() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.headers(" test ", " "));
		assertEquals("Missing header value!", e.getMessage());
	}

	@Test
	void exclude() {

		MailMessage message = new MailMessage();
		assertFalse(message.excluded("a"));

		message.exclude("a@email.com");
		message.exclude(" a@email.com ");
		message.exclude(" b@email.com ");

		assertEquals(2, message.getExcludedEmails().size());
		assertTrue(message.excluded("a@email.com"));
		assertTrue(message.excluded("b@email.com"));
	}

	@Test
	void exclude_fail() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.exclude("  "));
		assertEquals("Missing excluded email address!", e.getMessage());
	}

	@Test
	void attachment() {

		MailMessage message = new MailMessage();
		message.attachment(" 1 ", " 2 ", " 3 ");

		assertEquals("1", new String(message.getAttachments().get(0).content));
		assertEquals("2", message.getAttachments().get(0).fileName);
		assertEquals("3", message.getAttachments().get(0).type);
	}

	@Test
	void attachment_fail1() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.attachment(" ", "2", "3"));
		assertEquals("Missing attachment content!", e.getMessage());
	}

	@Test
	void attachment_fail2() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.attachment("1", " ", "3"));
		assertEquals("Missing attachment file name!", e.getMessage());
	}

	@Test
	void attachment_fail3() {

		MailMessage message = new MailMessage();

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.attachment("1", "2", " "));
		assertEquals("Missing attachment mime type!", e.getMessage());
	}

	private Session getSession() {

		Properties props = new Properties();
		return Session.getDefaultInstance(props, null);
	}

	@Test
	void getMessage_basic() throws MessagingException, IOException {

		MailMessage message = new MailMessage();
		message.from("from@email.com")
			   .to("some@guy.com")
			   .subject("Hello")
			   .content("Test");

		MimeMessage mime = message.getMessage(getSession());

		assertEquals("\"from@email.com\" <from@email.com>", mime.getFrom()[0].toString());
		assertEquals("\"some@guy.com\" <some@guy.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());

		assertEquals("Hello", mime.getSubject());
		assertEquals("Test", mime.getContent());
		assertEquals("text/plain; charset=UTF-8", mime.getContentType());
	}

	@Test
	void getMessage_basic_2() throws MessagingException, IOException {

		MailMessage message = new MailMessage();
		message.from("from@email.com").to("some@guy.com")
			   .subject("Hello")
			   .html("Test");

		MimeMessage mime = message.getMessage(getSession());

		assertEquals("\"from@email.com\" <from@email.com>", mime.getFrom()[0].toString());
		assertEquals("\"some@guy.com\" <some@guy.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());

		assertEquals("Hello", mime.getSubject());
		assertEquals("Test", mime.getContent());
		assertEquals("text/html; charset=UTF-8", mime.getContentType());
	}

	@Test
	void getMessage_withCCandBCC() throws MessagingException, IOException {

		MailMessage message = new MailMessage();
		message.from("from@email.com")
			   .to("one@one.com")
			   .bcc("two@two.com")
			   .cc("three@three.com")
			   .subject("Hello")
			   .content("Test");

		MimeMessage mime = message.getMessage(getSession());

		assertEquals("\"from@email.com\" <from@email.com>", mime.getFrom()[0].toString());
		assertEquals("\"one@one.com\" <one@one.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());
		assertEquals("\"two@two.com\" <two@two.com>", mime.getRecipients(Message.RecipientType.BCC)[0].toString());
		assertEquals("\"three@three.com\" <three@three.com>", mime.getRecipients(Message.RecipientType.CC)[0].toString());

		assertEquals("Hello", mime.getSubject());
		assertEquals("Test", mime.getContent());
	}

	@Test
	void getMessage_withEmailAndName() throws MessagingException, IOException {

		MailMessage message = new MailMessage();
		message.from("from@email.com", "from")
			   .to("one@one.com", "one")
			   .bcc("two@two.com", "two")
			   .cc("three@three.com", "three")
			   .subject("Hello")
			   .content("Test");

		MimeMessage mime = message.getMessage(getSession());

		assertEquals("from <from@email.com>", mime.getFrom()[0].toString());
		assertEquals("one <one@one.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());
		assertEquals("two <two@two.com>", mime.getRecipients(Message.RecipientType.BCC)[0].toString());
		assertEquals("three <three@three.com>", mime.getRecipients(Message.RecipientType.CC)[0].toString());

		assertEquals("Hello", mime.getSubject());
		assertEquals("Test", mime.getContent());
	}

	@Test
	void getMessage_withEmailAndNameMultiple() throws MessagingException, IOException {

		MailMessage message = new MailMessage();

		Map<String, String> listTo = new LinkedHashMap<>();
		listTo.put("one1@one.com", "one1");
		listTo.put("one2@one.com", "one2");
		listTo.put("one3@one.com", "one3");

		Map<String, String> listBcc = new LinkedHashMap<>();
		listBcc.put("two1@two.com", "two1");
		listBcc.put("two2@two.com", "two2");
		listBcc.put("two3@two.com", "two3");

		Map<String, String> listCc = new LinkedHashMap<>();
		listCc.put("three1@three.com", "three1");
		listCc.put("three2@three.com", "three2");
		listCc.put("three3@three.com", "three3");

		message.from("from@email.com", "from")
			   .to(listTo)
			   .bcc(listBcc)
			   .cc(listCc)
			   .subject("Hello")
			   .content("Test")
			   .headers("one", "two")
			   .exclude("one1@one.com");

		MimeMessage mime = message.getMessage(getSession());

		assertEquals("from <from@email.com>", mime.getFrom()[0].toString());

		assertEquals("one2 <one2@one.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());
		assertEquals("one3 <one3@one.com>", mime.getRecipients(Message.RecipientType.TO)[1].toString());

		assertEquals("two1 <two1@two.com>", mime.getRecipients(Message.RecipientType.BCC)[0].toString());
		assertEquals("two2 <two2@two.com>", mime.getRecipients(Message.RecipientType.BCC)[1].toString());
		assertEquals("two3 <two3@two.com>", mime.getRecipients(Message.RecipientType.BCC)[2].toString());

		assertEquals("three1 <three1@three.com>", mime.getRecipients(Message.RecipientType.CC)[0].toString());
		assertEquals("three2 <three2@three.com>", mime.getRecipients(Message.RecipientType.CC)[1].toString());
		assertEquals("three3 <three3@three.com>", mime.getRecipients(Message.RecipientType.CC)[2].toString());

		assertEquals("Hello", mime.getSubject());
		assertEquals("Test", mime.getContent());

		assertEquals("two", mime.getHeader("one")[0]);
	}

	@Test
	void getMessage_attachment() throws MessagingException, IOException {

		MailMessage message = new MailMessage();
		message.from("from@email.com")
			   .to("some@guy.com")
			   .subject("Hello")
			   .content("Test")
			   .html("<a>Test</a>")
			   .attachment("AAA", "some.file", "txt/html");

		MimeMessage mime = message.getMessage(getSession());
		assertEquals("\"from@email.com\" <from@email.com>", mime.getFrom()[0].toString());
		assertEquals("\"some@guy.com\" <some@guy.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());

		assertEquals("Hello", mime.getSubject());
		assertTrue(mime.getContent() instanceof MimeMultipart);
		MimeMultipart multipart = (MimeMultipart) mime.getContent();

		assertEquals(3, multipart.getCount());

		assertTrue(multipart.getBodyPart(0) instanceof MimeBodyPart);
		MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(0);
		assertEquals("Test", part.getContent());
		assertEquals("text/plain; charset=UTF-8", part.getContentType());

		assertTrue(multipart.getBodyPart(1) instanceof MimeBodyPart);
		part = (MimeBodyPart) multipart.getBodyPart(1);
		assertEquals("<a>Test</a>", part.getContent());
		assertEquals("text/html; charset=UTF-8", part.getContentType());

		part = (MimeBodyPart) multipart.getBodyPart(2);
		assertTrue(part.getContent() instanceof SharedByteArrayInputStream);
		assertEquals("txt/html; name=some.file", part.getContentType());
	}

	@Test
	void getMessage_attachment_2() throws MessagingException, IOException {

		MailMessage message = new MailMessage();
		message.from("from@email.com")
			   .to("some@guy.com")
			   .subject("Hello")
			   .html("test")
			   .attachment("Test", "some.file", "txt/html");

		MimeMessage mime = message.getMessage(getSession());
		assertEquals("\"from@email.com\" <from@email.com>", mime.getFrom()[0].toString());
		assertEquals("\"some@guy.com\" <some@guy.com>", mime.getRecipients(Message.RecipientType.TO)[0].toString());

		assertEquals("Hello", mime.getSubject());
		assertTrue(mime.getContent() instanceof MimeMultipart);
		MimeMultipart multipart = (MimeMultipart) mime.getContent();

		assertEquals(2, multipart.getCount());

		assertTrue(multipart.getBodyPart(0) instanceof MimeBodyPart);
		MimeBodyPart part = (MimeBodyPart) multipart.getBodyPart(0);
		assertEquals("test", part.getContent());
		assertEquals("text/html; charset=UTF-8", part.getContentType());

		part = (MimeBodyPart) multipart.getBodyPart(1);
		assertTrue(part.getContent() instanceof ByteArrayInputStream);
		assertEquals("txt/html; name=some.file", part.getContentType());
	}

	@Test
	void getMessage_missingAll() {

		MailMessage message = new MailMessage();
		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.getMessage(getSession()));
		assertEquals("No email address given!", e.getMessage());
	}

	@Test
	void getMessage_missingTo() {

		MailMessage message = new MailMessage();
		message.cc("test@test.com");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.getMessage(getSession()));
		assertEquals("Missing to email address(es)!", e.getMessage());
	}

	@Test
	void getMessage_missingTo_excludeAll() {

		MailMessage message = new MailMessage();
		message.to("test@test.com")
			   .to("test2@test.com")
			   .to("test3@test.com")
			   .exclude("test@test.com")
			   .exclude("test2@test.com")
			   .exclude("test3@test.com");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.getMessage(getSession()));
		assertEquals("All to email address(es) are excluded!", e.getMessage());
	}

	@Test
	void getMessage_missingFrom() {

		MailMessage message = new MailMessage();
		message.to("test@test.com");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.getMessage(getSession()));
		assertEquals("Missing from email address!", e.getMessage());
	}

	@Test
	void getMessage_missingSubject() {

		MailMessage message = new MailMessage();
		message.to("test@test.com")
			   .from("from@from.com");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.getMessage(getSession()));
		assertEquals("Missing email subject!", e.getMessage());
	}

	@Test
	void getMessage_missingContent() {

		MailMessage message = new MailMessage();
		message.to("test@test.com")
			   .from("from@from.com")
			   .subject("subject");

		IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> message.getMessage(getSession()));
		assertEquals("Missing email content!", e.getMessage());
	}
}