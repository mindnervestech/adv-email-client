import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.SimpleTrigger;
import org.quartz.impl.StdSchedulerFactory;

import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;

public class Global extends GlobalSettings {
	public static final int CHAR_LEN=200;
	public static final String  APP_ENV_LOCAL = "local";
	public static final String  APP_ENV_VAR = "CURRENT_APPNAME";
	@Override
	public void onStart(Application app) {
		/*
		ActorSystem  actorSystem2 = Akka.system();
		 actorSystem2.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(24, TimeUnit.HOURS),
				new Runnable() {
					public void run() {
							try {
								HtmlAndEmlParserTest.emlParseLink();
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
					}, actorSystem2.dispatcher()
				);*/
		try{
		SchedulerFactory sf = new StdSchedulerFactory();
		Scheduler sched = sf.getScheduler();
		sched.start();
		JobDetail jd = new JobDetail("myjob", sched.DEFAULT_GROUP,
				controllers.Application.class);
		CronTrigger ct=new CronTrigger("cronTrigger","group2","0 00 5 * * ?");
		/*SimpleTrigger st = new SimpleTrigger("mytrigger", sched.DEFAULT_GROUP,
				new Date(), null, SimpleTrigger.REPEAT_INDEFINITELY,
				60L * 1000L);*/
		
		sched.scheduleJob(jd, ct);
		}catch(Exception e){
			e.printStackTrace();
		}
		ActorSystem  actorSystem1 = Akka.system();
		 actorSystem1.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(30, TimeUnit.MINUTES),
				new Runnable() {
					public void run() {
							try {
								HtmlAndEmlParser.emlParse();
							} catch (Exception e) {
								e.printStackTrace();
							}
					}
					}, actorSystem1.dispatcher()
				);
		 
		
		ActorSystem  actorSystem = Akka.system();
		 actorSystem.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(2, TimeUnit.HOURS),
				new Runnable() {
					public void run() {
						 try {
							EmailWriteFile.main();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					}, actorSystem.dispatcher()
				);
		/* ActorSystem  actorSystem3 = Akka.system();
		 actorSystem3.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(24, TimeUnit.HOURS),
				new Runnable() {
					public void run() {
						 try {
							HtmlAndEmlParserTest.deletePersonalMails();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					}, actorSystem3.dispatcher()
				);
		
		 ActorSystem  actorSystem2 = Akka.system();
		    actorSystem2.scheduler().schedule(Duration.create(0, TimeUnit.MILLISECONDS),Duration.create(30, TimeUnit.MINUTES), 
				 new Runnable() {

						@Override
						public void run() {
							System.out.println("Processing Link");
							HtmlAndEmlParser.processLinks();
				    	}
			 
		     }, actorSystem2.dispatcher()); */
		 
		/* ActorSystem actorSystemJob = Akka.system();
			actorSystemJob.scheduler().schedule(
					Duration.create(0, TimeUnit.MILLISECONDS),
					Duration.create(1, TimeUnit.MINUTES), new Runnable() {
						@Override
						public void run() {
							List<Links> links = Links.find.where().eq("path", null).findList();
							
							for (Links link : links) {
								String imageFolderPath = link.mail_id.mailPath;
								imageFolderPath = imageFolderPath.replace(".eml","");
								File file = new File(imageFolderPath);
								if (!file.exists()) {
									if (file.mkdir()) {
										System.out.println("Directory is created!");
									} else {
										System.out.println("Failed to create directory!");
									}
								}
								String imageName = link.id + ".png";
								//UrlValidator urlValidator = new UrlValidator();
								//if(urlValidator.isValid(link.getUrl())){
								Document doc;
								try {
									WebDriver driver = new HtmlUnitDriver();
							        driver.get(link.getUrl());
							        String htmlText=driver.getPageSource();
							        doc =  Jsoup.parse(htmlText, "ISO-8859-1");
									doc.select("style").remove();
									doc.select("meta").remove();
									doc.select("script").remove();
									doc.select("link").remove();
									// System.out.println(doc.toString());
									htmlText = doc.toString();
						        	//htmlText = htmlText.substring(0, htmlText.length() > 16000 ? 16000 : htmlText.length()-1);
						        	doc = Jsoup.parseBodyFragment(htmlText, "ISO-8859-1");
						        	doc.outputSettings().escapeMode(EscapeMode.xhtml);
									HtmlImageGenerator imageGenerator1 = new HtmlImageGenerator();
									String html=adjustHtml(doc.toString());
									imageGenerator1.loadUrl(link.getUrl());
									imageGenerator1.loadHtml(html);
									imageGenerator1.saveAsImage(file + File.separator + imageName);
									link.setPath(file + File.separator + imageName);
									link.update();
								} catch (Exception e) {
									System.out.println("while parsing links in global " + e.getMessage());
									link.setPath("BAD_URL");
									link.update();
								}
									
							}
						}
					}, actorSystemJob.dispatcher());*/
	}
	private static String adjustHtml(String original)
	{
		String find="<br/>";
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
	}
	@Override
	  public void onStop(Application app) {
	    Logger.info("Application shutdown...");
	  }  
}