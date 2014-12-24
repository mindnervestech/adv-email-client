package controllers;

import java.util.Properties;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

public class GMailServer extends javax.mail.Authenticator {
       public static void sendMail(String subject, String body,
			final String sender, final String password, String recipients, String attachFile) throws AddressException,
			MessagingException {
	//public static void main(String args[]) throws AddressException, MessagingException{
		String mailhost = "smtp.gmail.com";
		Session session;
		Properties props = new Properties();
		props.setProperty("mail.transport.protocol", "smtp");
		props.setProperty("mail.smtp.host", mailhost);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.port", "587");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.debug", "true");
		props.put("mail.smtp.socketFactory.fallback", "false");
		props.setProperty("mail.smtp.quitwait", "false");
		Authenticator auth = new Authenticator() {
			public PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(sender,
						password);
			}
		};
		session = Session.getInstance(props, auth);
		MimeMessage message = new MimeMessage(session);
		/*
		 * DataHandler handler = new DataHandler(new ByteArrayDataSource(
		 * "hello".getBytes(), "text/plain"));
		 */

		message.setSender(new InternetAddress("mindnervesdemo@gmail.com"));
		message.setSubject(subject);
		BodyPart messageBodyPart = new MimeBodyPart();

		// Now set the actual message
		messageBodyPart.setText(body);

		// Create a multipar message
		Multipart multipart = new MimeMultipart();

		// Set text message part
		multipart.addBodyPart(messageBodyPart);

		// Part two is attachment
		messageBodyPart = new MimeBodyPart();
		String filename = attachFile;
		DataSource source = new FileDataSource(filename);
		messageBodyPart.setDataHandler(new DataHandler(source));
		messageBodyPart.setFileName(filename);
		multipart.addBodyPart(messageBodyPart);
		message.setContent(multipart);

		message.setRecipient(Message.RecipientType.TO, new InternetAddress(
				"nageshdalave@gmail.com"));
		Transport.send(message);

	}

	
}