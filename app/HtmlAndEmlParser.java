import indexing.Email;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import models.ImageInfo;
import models.Links;
import models.MailObjectModel;

import org.apache.commons.validator.UrlValidator;
import org.apache.http.util.ByteArrayBuffer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

import com.google.common.base.Strings;

public class HtmlAndEmlParser {
 
    public static void emlParse() throws Exception  {
    	Document doc= null;
    	List <MailObjectModel> moList =MailObjectModel.find.where().eq("status", false).findList();
    	System.out.println("No of mails to be processed  from FS" + moList.size());
		for (MailObjectModel mm:moList)
		{
			Session session = Session.getDefaultInstance(new Properties());
			String urll=mm.mailPath;
			
			System.out.println("Processing mail from FS" + urll);
			
			File file= new File(urll);
			InputStream inMsg = new FileInputStream(file);
			
			Message msg = new MimeMessage(session, inMsg);
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
        	Elements linksHref = doc.select("a[href]");
        	String rootpathForImage = urll.replace(".eml", "_images");
        	File file6 = new File(rootpathForImage);
				if (!file6.exists()) {
					file6.mkdir();
					
				}
        	Elements links= new Elements();
        	try {
     			links = new Elements();
     			links =	doc.select("img[src]");
     			int i = 0;
     			//System.out.println("No of links got for mail  " + links.size());
					
     			for (Element link : links) {
     				String pathForImage = rootpathForImage + File.separator  + "_" + i++ + ".jpg";
     				BufferedImage image = null;
     				//System.out.println("Looking for URL " + link.attr("src"));
     				//UrlValidator urlValidator = new UrlValidator();
     				//System.out.println("Looking for URL validation " + link.attr("src"));
 					//if(urlValidator.isValid(link.attr("src"))){
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
 		 		 		System.out.println(e.getMessage());
 		 	    	}
 					//}
 				}		
 	    	} catch (Exception e) {
 		 		e.printStackTrace();
 	    	}
			
			models.Content content= new models.Content();
			content.setDescription(doc.text());
			content.save();
			
			mm.setStatus(true);
			mm.setContent(content);
			mm.update();
			
			// THIS IS FOR ELASTIC SEARCH....
			Email email = new Email();
			email.description = doc.text();
			email.subject = mm.mailName;
			email.domain = mm.domain;
			email.sentDate = mm.sentDate;
			email.mail_objectId = mm.id;
			email.sendersEmail = mm.sendersEmail;
			
			for (Element link : linksHref) {
				saveLinksInDb(mm, link , email.nestedHtml);
			}
			
			email.index();
			
			//for (Element link : links) {
				//saveImageInDb(mm, hp, link);
			//}
		}
    }

    public static void saveImageInDb(MailObjectModel mm,Element link)  {
		ByteArrayBuffer baf = new ByteArrayBuffer(1000);
		String srcAttr=link.attr("src").replaceAll(" ", "");
		String altAttr=link.attr("alt");
		UrlValidator urlValidator = new UrlValidator();
		if(!urlValidator.isValid(srcAttr)) {
			return;
		}
		URLConnection ucon;
		BufferedInputStream bis =null;
	    try {
			URL imageUrl = new URL(srcAttr);
			ucon = imageUrl.openConnection();
			InputStream is = ucon.getInputStream();
			bis = new BufferedInputStream(is);
			int current = 0;
			while ((current = bis.read()) != -1) {
			    baf.append((byte) current);
			}
			ImageInfo img = new ImageInfo();
			img.image_byte = baf.toByteArray();
			img.mailObjectModel=mm;
			img.url=srcAttr;
			img.alt=altAttr;
			img.save();
		} catch (Exception e) {
			System.out.println(e.getMessage());
		} finally{
			try {
				bis.close();
			} catch (Exception e) {
			}
		}
			
	}

    public static void saveLinksInDb(MailObjectModel mm, Element link, List<indexing.Links> nestedHtml)  {
		String urlLink = link.attr("href").replaceAll(" ", "");
		//UrlValidator urlValidator = new UrlValidator();
		//if(urlValidator.isValid(urlLink)) {
			//System.out.println("Saving link in mail  " + urlLink);
			Links linkDB = new Links();
			linkDB.setMail_id(mm);
			linkDB.setUrl(urlLink);
			linkDB.setStatus(1);
			Document doc;
			try {
				doc = Jsoup.connect(urlLink).get();
				String text = doc.body().text();
				if(Strings.isNullOrEmpty(text))
				{
					linkDB.setStatus(2);
				}
				linkDB.setHtmlcontent(text);
				linkDB.save();
				nestedHtml.add(new indexing.Links(linkDB.id, text));
			} catch (Exception e) {
				System.out.println(e.getMessage());
			}
		//}
			
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
   

}

