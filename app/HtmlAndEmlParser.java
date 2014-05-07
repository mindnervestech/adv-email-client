import indexing.Email;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
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
import org.elasticsearch.common.collect.Lists;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Entities.EscapeMode;
import org.jsoup.select.Elements;

public class HtmlAndEmlParser {
 
    public static void emlParse() throws Exception  {
    	Document doc= null;
    	List <MailObjectModel> moList =MailObjectModel.find.where().eq("status", false).findList();
		for (MailObjectModel mm:moList)
		{
			Session session = Session.getDefaultInstance(new Properties());
			String urll=mm.mailPath;
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
        	String pathForImage = urll.replace(".eml", "_images");
        	File file6 = new File(pathForImage);
				if (!file6.exists()) {
					file6.mkdir();
					
				}
        	Elements links= new Elements();
        	try {
     			links = new Elements();
     			links =	doc.select("img[src]");
     			int i = 0;
     			for (Element link : links) {
     				
     				pathForImage = pathForImage + File.separator  + "-" + i++ + ".jpg";
     	        	
     				BufferedImage image = null;
     				URL imageUrl = new URL(link.attr("src"));
     				URL url = new URL(imageUrl.toString());
     				UrlValidator urlValidator = new UrlValidator();
 					if(urlValidator.isValid(imageUrl.toString())){
 	    				image = ImageIO.read(url);
 	    				int h = image.getHeight();
 	    				int w = image.getWidth();
 	    				if(h > 100 && w > 100) {
 						  ImageIO.write(image, "jpg",new File(pathForImage));
 	    				}
 					}
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

    public static void saveLinksInDb(MailObjectModel mm,Element link, List<indexing.Links> nestedHtml)  {
		String urlLink = link.attr("href").replaceAll(" ", "");
		UrlValidator urlValidator = new UrlValidator();
		if(urlValidator.isValid(urlLink)) {
			Links linkDB = new Links();
			linkDB.setMail_id(mm);
			linkDB.setUrl(urlLink);
			linkDB.setStatus(true);
			Document doc;
			try {
				doc = Jsoup.connect(urlLink).get();
				String text = doc.body().text();
				linkDB.setHtmlcontent(text);
				linkDB.save();
				nestedHtml.add(new indexing.Links(linkDB.id, text));
			} catch (IOException e) {
				System.out.println(e.getMessage());
			}
		}
			
	}
   

}

