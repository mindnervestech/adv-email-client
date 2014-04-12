//import gui.ava.html.image.generator.HtmlImageGenerator;

//import java.awt.Dimension;
import gui.ava.html.image.generator.HtmlImageGenerator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import models.MailObjectModel;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Entities.EscapeMode;

import play.Play;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;

public class EmailWriteFile {
	static final String rootDir = Play.application().configuration()
			.getString("mail.storage.path");

	public static void main(String args[]) throws MessagingException {
    	Store store = MailConnection.getStore();
        Folder folder = getFolderObj(store);
        Message[] message = folder.getMessages();
        folder.setFlags(message, new Flags(Flags.Flag.SEEN), true);
        
        // save message.
      	for (int i = 0; i < message.length; i++) {
      		MailObjectModel mm = new MailObjectModel();
			mm.mailName=message[i].getSubject();
			mm.sendersEmail=message[i].getFrom()[0].toString();
			//System.out.println("----123456-----"+ mm.sendersEmail);
            createRootDir();
            String domain1 = createDomainDir(message, i);
            String dateStr=message[i].getSentDate().toString();
        	String contentType=message[i].getContentType().substring(0,message[i].getContentType().indexOf('/'));
        	int startIndex=dateStr.indexOf("IST");
            String fileName=dateStr.substring(startIndex-9, startIndex).trim().replaceAll(":","");
            String year = createYearDir(domain1, dateStr, startIndex);
            String month = createMonthDir(domain1, dateStr, year);
            String day=dateStr.substring(8,10).trim();
            createDayDir(domain1, year, month, day);
            Document doc;
            String htmlText = "";
        	MimeMultipart obj = null;
        	try {
	        	if("multipart".equals(contentType)) {
	    				obj = (MimeMultipart)message[i].getContent();
	    				OutputStream out = new FileOutputStream(rootDir+File.separator+domain1+File.separator+year+File.separator+month+File.separator+day+File.separator+fileName+"-"+i+".eml");
	    				message[i].writeTo(out);
	    				out.close();
	    				if(obj.getCount() == 0) {
	    					htmlText = obj.getBodyPart(0).getContent().toString();
	    				} else {
	    					htmlText = obj.getBodyPart(obj.getCount() -1 ).getContent().toString();
	    				}
	        			
	    		} else {
	    			htmlText= message[i].getContent().toString();
	    		}
        	} catch (IOException e) {
				e.printStackTrace();
				doc = null;
			}
        	htmlText = htmlText.substring(0, htmlText.length() > 5000 ? 5000 : htmlText.length()-1);
        	doc = Jsoup.parseBodyFragment(htmlText, "ISO-8859-1");
        	doc.outputSettings().escapeMode(EscapeMode.xhtml);
    		mm.receivedDate=message[i].getReceivedDate();
            mm.sentDate=message[i].getSentDate();
            mm.mailPath=rootDir+File.separator+domain1+File.separator+year+File.separator+month+File.separator+day+File.separator+fileName+"-"+i+".eml";
            mm.domain=domain1;
    		mm.save();
    		
    		final String imgPath=mm.mailPath;
    		final Document doc1 = doc;
    		
    		 ActorSystem  actorSystem = Akka.system();
    		 actorSystem.scheduler().scheduleOnce(Duration.create(0, TimeUnit.MILLISECONDS), 
    				 new Runnable() {

						@Override
						public void run() {
							/*Html2Image.fromHtml(doc1.toString()).getImageRenderer()
				    		.setWidth(200).setAutoHeight(true).setWriteCompressionQuality(0.7f)
				    		.saveImage(imgPath.replace(".eml",".png"));*/
							HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
				    		imageGenerator.loadHtml(doc1.toString());
				    		imageGenerator.saveAsImage(imgPath.replace(".eml",".png"));
						}
    			 
    		 }, actorSystem.dispatcher());
    		 
	        OutputStream out = null;
			try {
				out = new FileOutputStream(rootDir+File.separator+domain1+File.separator+year+File.separator+month+File.separator+day+File.separator+fileName+"-"+i+".eml");
				message[i].writeTo(out);  
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if(out != null) out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
        	


	    	/*
            Dimension dimension = new Dimension(100, 100);
            imageGenerator.setSize(dimension);
           // imageGenerator.    		
	    	
		    imageGenerator.saveAsImage(imgPath.replace(".eml",".png"));
			imageGenerator.saveAsHtmlWithMap(imgPath.replace(".eml",".html"),imgPath.replace(".eml",".png"));*/
		    
        }
        folder.close(true);
        //store.close();
    }

	public static void createRootDir() {
		File file = new File(rootDir);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static void createDayDir(String domain1, String year, String month,
			String day) {
		File file5 = new File(rootDir + File.separator + domain1
				+ File.separator + year + File.separator + month
				+ File.separator + day);
		if (!file5.exists()) {
			file5.mkdir();
		}
	}

	public static String createMonthDir(String domain1, String dateStr,
			String year) {
		String month = dateStr.substring(4, 7).trim();
		File file4 = new File(rootDir + File.separator + domain1
				+ File.separator + year + File.separator + month);
		if (!file4.exists()) {
			file4.mkdir();
		}
		return month;
	}

	public static String createYearDir(String domain1, String dateStr,
			int startIndex) {
		String year = dateStr.substring(startIndex + 3, dateStr.length())
				.trim();
		File file3 = new File(rootDir + File.separator + domain1
				+ File.separator + year);
		if (!file3.exists()) {
			file3.mkdir();
		}
		return year;
	}

	public static String createDomainDir(Message[] message, int i)
			throws MessagingException {
		String domain = message[i].getFrom()[0].toString();
		int indexOfAt = domain.lastIndexOf('@');
		String domain1 = domain.substring(indexOfAt + 1, domain.length() - 1);
		File file2 = new File(rootDir + File.separator + domain1);
		if (!file2.exists()) {
			file2.mkdir();
		}
		return domain1;
	}

	public static Folder getFolderObj(Store store) throws MessagingException {
		// Create a Folder object corresponding to the given name.
		Folder folder = store.getFolder("inbox");
		// Open the Folder.
		folder.open(Folder.READ_ONLY);
		return folder;
	}
	
	public static class MailConnection {
		static MailConnection connection;
		private static String host = "pop.gmail.com";
		private static String user = "admin@lab104.net";
		private static String password = "dipesh104";

		Store store;
		private MailConnection() {
			
			Session session = Session.getDefaultInstance(new Properties());
			try {
				store = session.getStore("pop3s");
				try {
					store.connect(host, user, password);
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			} catch (NoSuchProviderException e) {
				e.printStackTrace();
			}
		}
		
		private boolean isConnected() {
			return store.isConnected();
		}
		
		public static Store getStore() {
		
			if(connection == null) {
				connection = new MailConnection();
			}
			
			if(connection.isConnected()) {
				return connection.store;
			} else {
				try {
					connection.store.connect(host, user, password);
					return connection.store;
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (Exception e) {
					connection = null;
				}
			}
			
			return null;
		}
	}

	public static Store getConnection() throws NoSuchProviderException,
			MessagingException {
		String host = "pop.gmail.com";
		String user = "admin@lab104.net";
		String password = "dipesh104";

		// Get the default Session object.
		Session session = Session.getDefaultInstance(new Properties());

		// Get a Store object that implements the specified protocol.
		Store store = session.getStore("pop3s");

		// Connect to the current host using the specified username and
		// password.
		store.connect(host, user, password);
		return store;
	}

}