import gui.ava.html.image.generator.HtmlImageGenerator;
import indexing.Email;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import models.Links;
import models.MailObjectModel;

import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;

import com.google.common.base.Strings;

public class HtmlAndEmlParserTest {
	static final int CHAR_LEN=200;
    public static void emlParseLink() throws Exception  {
    	ExecutorService executor = Executors.newFixedThreadPool(3);
    	Document doc= null;
    	System.out.println("Reading from FS" );
		
    	List <MailObjectModel> mails =MailObjectModel.getUnprocessedMailObjectIds();
    	System.out.println("No of mails to be processed  from FS" + mails.size());
		for (MailObjectModel mm:mails)
		{
			Session session = Session.getDefaultInstance(new Properties());
			final String  urll=mm.mailPath;
			System.out.println(mm.mailPath);
			System.out.println("Processing mail from FS" + urll);
			
			File file= new File(urll);
			InputStream inMsg = new FileInputStream(file);
			
			Message msg = new MimeMessage(session, inMsg);
			inMsg.close();
			
			String contentType=msg.getContentType().substring(0,msg.getContentType().indexOf('/'));
			
			MimeMultipart obj=null;
			String htmlText = "";
			Object bodyContent;
			if("multipart".equals(contentType)) {
				obj = (MimeMultipart)msg.getContent();
				if(obj.getCount() == 0) {
					bodyContent = obj.getBodyPart(0).getContent();
				} else {
					bodyContent = obj.getBodyPart(obj.getCount() -1 ).getContent();
				}
				
			}
			else {
				bodyContent = msg.getContent();
			}
			if(bodyContent instanceof String) {
				htmlText = bodyContent.toString();
			} else {
				htmlText = "Attachement Found";
			}
        	doc = Jsoup.parseBodyFragment(htmlText, "ISO-8859-1");
        	doc.outputSettings().escapeMode(EscapeMode.xhtml);
        	doc.select("style").remove();
			doc.select("meta").remove();
			doc.select("script").remove();
			doc.select("link").remove();
			// System.out.println(doc.toString());
			htmlText = doc.toString();
        	
/*			final String  html=adjustHtml(htmlText.toString());
			executor.execute(new Runnable() {

						@Override
						public void run() {
							try {
							HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
							imageGenerator.loadHtml(html);
				    		imageGenerator.saveAsImage(urll.replace(".eml",".png"));
				    		System.gc();
							}catch(Exception e) {
								
							}
				    	}
   			 
   		     });*/
			/*ActorSystem  actorSystem = Akka.system();
   		    actorSystem.scheduler().scheduleOnce(Duration.create(0, TimeUnit.MILLISECONDS), 
   				 
   				 new Runnable() {

						@Override
						public void run() {
							System.out.println("Saving Mail Image now");
							HtmlImageGenerator imageGenerator = new HtmlImageGenerator();
							imageGenerator.loadHtml(html);
				    		imageGenerator.saveAsImage(urll.replace(".eml",".png"));
				    		System.gc();
				    	}
   			 
   		     }, actorSystem.dispatcher());*/
   		 System.gc();
    		/*String rootpathForImage = urll.replace(".eml", "_images");
        	File file6 = new File(rootpathForImage);
				if (!file6.exists()) {
					file6.mkdir();
					
				}
        	Elements links= new Elements();
        	try {
     			links = new Elements();
     			links =	doc.select("img[src]");
     			int i = 0;
     		//	System.out.println("Saving Images now");	
     			for (Element link : links) {
     				String pathForImage = rootpathForImage + File.separator  + "_" + i++ + ".jpg";
     				BufferedImage image = null;
 					try {
     					URL imageUrl = new URL(link.attr("src"));
 						//System.out.println("Saving image to FS from " + imageUrl.toString());
 	    				image = ImageIO.read(imageUrl);
 	    				int h = image.getHeight();
 	    				int w = image.getWidth();
 	    				if(h > 100 && w > 100) {
 						  ImageIO.write(image, "jpg",new File(pathForImage));
 	    				}
 					} catch (Exception e) {
 		 		 	//	System.out.println(e.getMessage());
 		 	    	}
 				}		
 	    	} catch (Exception e) {
 		 		//e.printStackTrace();
 	    	}*/
			
//			models.Content content= new models.Content();
//			content.setDescription(doc.text());
//			content.save();
			
		
			
			// THIS IS FOR ELASTIC SEARCH....
			/*Email email = new Email();
			email.description = doc.text();
			email.subject = mm.mailName;
			email.domain = mm.domain;
			email.sentDate = mm.sentDate;
			email.mail_objectId = mm.id;
			email.sendersEmail = mm.sendersEmail;
			*/
   		 
   		 	Email email = Email.getEmailByMailObjectId(mm.id);
			Elements linksHref = doc.select("a[href]");
			//System.out.println("Saving Links now");
			for (Element link : linksHref) {
				saveLinksInDb(mm, link , email.nestedHtml);
			}
			
			
			email.index();
			//mm.setStatus(1);
			//mm.setContent(content);
			
			//System.out.println("Saving Links Done");
			
			//for (Element link : links) {
			//	saveImageInDb(mm, hp, link);
			//}
			//mm.update();
		}
		executor.shutdown();
		executor.awaitTermination(1, TimeUnit.MINUTES);
    }
    public static String adjustHtml(String original) {
		try {
    	String find="<br />";
		int pIndex=original.indexOf("<p");
		while(pIndex!=-1)
		{
			 int lastPIndex=original.indexOf("</p>",pIndex+"<p>".length());
			 int lastIndex = 0;
			 int firstIndex=pIndex;
			 while (firstIndex != -1 && firstIndex<lastPIndex) 
			 {
		    	firstIndex = original.indexOf(find, firstIndex);
		    	if (firstIndex != -1) {
		    		firstIndex += find.length();
		    		lastIndex=original.indexOf(find, firstIndex);
			    	if(lastIndex!=-1&&lastIndex<lastPIndex)
			    	{
			    		if(firstIndex+CHAR_LEN<lastIndex)
			    		{
			    			original=original.substring(0, firstIndex+CHAR_LEN)+find+original.substring(firstIndex+CHAR_LEN);
			    		}
			    	}
			    	else if(firstIndex+CHAR_LEN<lastPIndex)
			    	{
			    		while(firstIndex+CHAR_LEN<lastPIndex)
			    		{
			    			if(firstIndex+CHAR_LEN<original.length())
			    			{
			    				original=original.substring(0, firstIndex+CHAR_LEN)+find+original.substring(firstIndex+CHAR_LEN);
			    				firstIndex=firstIndex+CHAR_LEN+find.length();
			    			}
			    		}
			    	}
		    	}
		    	else if(firstIndex+CHAR_LEN<lastPIndex)
		    	{
		    		while(firstIndex+CHAR_LEN<lastPIndex)
		    		{
		    			if(firstIndex+CHAR_LEN<original.length())
		    			{
		    				original=original.substring(0, firstIndex+CHAR_LEN)+find+original.substring(firstIndex+CHAR_LEN);
		    				firstIndex=firstIndex+CHAR_LEN+find.length();
		    			}
		    		}
		    	}
		    		
		    }
			 pIndex=original.indexOf("<p",pIndex+"<p>".length());
		 }
		return original;
		} catch(Exception e) {
			//e.printStackTrace();
			return original;
		}
	}
   
    public static void saveLinksInDb(MailObjectModel mm, Element link, List<indexing.Links> nestedHtml)  {
		String urlLink = link.attr("href").replaceAll(" ", "");
		Links linkDB = new Links();
		linkDB.setMail_id(mm);
		linkDB.setUrl(urlLink);
		linkDB.setStatus(1);
		Document doc;
		int a=3, tried = 0;
		try {
			do {
				Response response = Jsoup.connect(urlLink.replaceAll(" ", "%20")).followRedirects(false).execute();
				a = response.statusCode() / 100;
				if(a == 3) {
					if(response.headers().get("Location") != null) {
						urlLink =response.headers().get("Location");
					} else {
						urlLink =response.headers().get("location");
					}
				} else if (a != 2) {
					urlLink = null;
				}
				tried++;
			} while(a == 3 && tried < 3) ;
			
			if(urlLink == null) {
				return;
			}
			
			linkDB.processedUrl = urlLink;
			doc = Jsoup.connect(urlLink).get();
			String text = doc.body().text();
			if(Strings.isNullOrEmpty(text)) {
				//linkDB.setStatus(2);
				WebDriver driver = new HtmlUnitDriver();
			    try {
			        driver.get(urlLink);
			        doc = Jsoup.parse(driver.getPageSource());
			        text = doc.body().text();
			    } finally {
			        try {
			        	driver.close();
			        } catch(Exception e){
			        		
			        }
			    }
			}
			if(Strings.isNullOrEmpty(text)) {
					linkDB.setHtmlcontent("UNPROCESSED");
					linkDB.setStatus(2);
			}
			linkDB.save();
			nestedHtml.add(new indexing.Links(linkDB.id, text));
		} catch (Exception e) {
			//e.printStackTrace();
		}
		
	}
    
    public static void processLinks() {
    	List<Links> linkList = Links.find.where().eq("processedUrl", null).setMaxRows(8000).findList();
    	for(Links link : linkList) {
    		String urlLink =link.getUrl();
    		urlLink = process(urlLink);
    		if(urlLink != null) {
    			link.setProcessedUrl(urlLink);
    			link.update();
    		}
    	}
    }
    
    public static String process(String urlLink) {
    	int a=3;
		try {
			do {
				Response response = Jsoup.connect(urlLink.replaceAll(" ", "%20")).followRedirects(false).execute();
				a = response.statusCode() / 100;
				if(a == 3) {
					if(response.headers().get("Location") != null) {
						urlLink =response.headers().get("Location");
					} else {
						urlLink =response.headers().get("location");
					}
				} else if (a != 2) {
					return "";
				}
			}while(a==3);
		} catch (Exception e) {
			//e.printStackTrace();
			return "";
		}
		return urlLink;
    }
    /*public static void main(String[] args) {
    	Document doc;
		try {
			//HtmlUnitDriver driver = new HtmlUnitDriver(true);
			final WebClient webClient = new WebClient();
			final HtmlPage startPage = webClient.getPage("http://en.wikipedia.org/wiki/Screen_Award_for_Best_Film");
			
			 
			//driver.get("http://sm.labx.com:80/track?type=click&enid=ZWFzPTEmbWFpbGluZ2lkPTEzNTYxNiZtZXNzYWdlaWQ9OTM1MDAmZGF0YWJhc2VpZD0xMDAwJnNlcmlhbD0xNjc4OTQxNSZlbWFpbGlkPWFkbWluQGxhYjEwNC5uZXQmdXNlcmlkPTFfMTA4OTUmdGFyZ2V0aWQ9JmZsPSZleHRyYT1NdWx0aXZhcmlhdGVJZD0mJiY=&&&2058&&&http://www.labx.com/v2/adsearch/detail3.cfm?adnumb=510254");
			
			//doc = Jsoup.connect("").userAgent("Mozilla").get();
			//String text = doc.toString();
			System.out.println(startPage.asText());
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}
	}*/
   
	public static void deletePersonalMails(){
		System.out.println("in delete personal mail");
		List<MailObjectModel> models = MailObjectModel.getAllPersonalMailObjectModels();
		for(MailObjectModel model : models) {
			Email e=Email.getEmailByMailObjectId(model.id);
			if(e!=null) {
				e.delete();
			}
			Links.deleteLinksByMailObjectId(model.id);
			model.delete();
		}
	}
}

