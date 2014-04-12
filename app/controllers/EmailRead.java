package controllers;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;

import play.Play;

public class EmailRead {
	 static final String rootDir = Play.application().configuration().getString("mail.storage.path");
    public static void main(String args[]) throws Exception {

    	  // Get system properties
        Properties properties = System.getProperties();

        // Get the default Session object.
        Session session = Session.getDefaultInstance(properties, null);
        InputStream inMsg = new FileInputStream(rootDir+File.separator+"gmail.com"+File.separator+"2014"+File.separator+"Mar"+File.separator+"11"+File.separator+"170226-0.eml");
        Message msg = new MimeMessage(session, inMsg);
      /*  System.out.println("SentDate : " +  msg.getContent());
        System.out.println("SentDate : " + msg.getSentDate());
        System.out.println("From : " + msg.getFrom()[0]);
        System.out.println("Subject : " + msg.getSubject());
        System.out.print("Message : "+ msg.getAllRecipients());*/
    }
    

}