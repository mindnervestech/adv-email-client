import gui.ava.html.image.generator.HtmlImageGenerator;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.internet.MimeMultipart;

import models.MailObjectModel;

import org.apache.commons.validator.UrlValidator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import play.Play;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;

public class EmailWriteFile {
	static final String rootDir = Play.application().configuration()
			.getString("mail.storage.path");
	static SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd HH:mm:ss z yyyy");
	
	public static void main() throws Exception {
    	Store store = MailConnection.getStore();
        Folder folder;
		try {
			folder = getFolderObj(store);
		} catch (Exception e1) {
			store = MailConnection.getStore();
			folder = getFolderObj(store);
		}
		//FlagTerm ft = new FlagTerm(new Flags(Flags.Flag.SEEN), false);
		Message message[] = folder.getMessages();//.search(ft);
		
		System.out.println("folder.hasNewMessages(): " + folder.hasNewMessages() +" Got mails at "+ message.length);
        
        // save message.
      	for (int i = 0; i < message.length; i++) {
      		MailObjectModel mm = new MailObjectModel();
			mm.mailName=message[i].getSubject();
			mm.sendersEmail=message[i].getFrom()[0].toString();
            createRootDir();
            String domain = createDomainDir(message, i);
            String dateStr=message[i].getSentDate().toString();
        	Date dt = sdf.parse(dateStr);
        	Calendar calendar = Calendar.getInstance();
        	calendar.setTime(dt);
            String fileName= calendar.get(Calendar.HOUR_OF_DAY) +""+calendar.get(Calendar.MINUTE) +""+ calendar.get(Calendar.SECOND);
            createYearDir(domain, calendar.get(Calendar.YEAR));
            createMonthDir(domain, calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR));
            createDayDir(domain, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE));
            createimgDir(domain, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE),fileName,i);
            
        	String path = rootDir + File.separator + domain + File.separator + calendar.get(Calendar.YEAR) + File.separator + calendar.get(Calendar.MONTH) + File.separator + calendar.get(Calendar.DATE) + File.separator + fileName+"-"+i+".eml";
    		mm.receivedDate = message[i].getReceivedDate();
            mm.sentDate = message[i].getSentDate();
            mm.mailPath = path;
            mm.domain = domain;
    		mm.save();
    		
    	
    		 /*ActorSystem  actorSystem = Akka.system();
    		 actorSystem.scheduler().scheduleOnce(Duration.create(0, TimeUnit.MILLISECONDS), 
    				 new Runnable() {

						@Override
						public void run() {
							Html2Image.fromHtml(doc1.toString()).getImageRenderer()
				    		.setWidth(200).setAutoHeight(true).setWriteCompressionQuality(0.7f)
				    		.saveImage(imgPath.replace(".eml",".png"));
							HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
				    		imageGenerator.loadHtml(doc1.toString());
				    		imageGenerator.saveAsImage(imgPath.replace(".eml",".png"));
				    		
				    		
				    	}
    			 
    		 }, actorSystem.dispatcher());*/
    		 
    		
    		 
	        OutputStream out = null;
			try {
				out = new FileOutputStream(path);
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
			
        }
      	folder.setFlags(message, new Flags(Flags.Flag.SEEN), true);
        
        folder.close(true);
        //store.close();
    }

	public static void createRootDir() {
		File file = new File(rootDir);
		if (!file.exists()) {
			file.mkdir();
		}
	}

	public static void createDayDir(String domain, int year, int month,
			int day) {
		File file5 = new File(rootDir + File.separator + domain
				+ File.separator + year + File.separator + month
				+ File.separator + day);
		if (!file5.exists()) {
			file5.mkdir();
		}
	}
	
	public static void createimgDir(String domain, int year, int month,
			int day , String filename , int i) {
		File file6 = new File(rootDir + File.separator + domain
				+ File.separator + year + File.separator + month
				+ File.separator + day + File.separator + filename+"-"+i+"_images");
		if (!file6.exists()) {
			file6.mkdir();
			
		}
	}
	

	public static void createMonthDir(String domain, int month , int year) {
		File file4 = new File(rootDir + File.separator + domain
				+ File.separator + year + File.separator + month);
		if (!file4.exists()) {
			file4.mkdir();
		}
	}

	public static void createYearDir(String domain, int year) {
		
		File file3 = new File(rootDir + File.separator + domain
				+ File.separator + year);
		if (!file3.exists()) {
			file3.mkdir();
		}
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

	public static Folder getFolderObj(Store store) throws Exception {
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
	
	

	

}
