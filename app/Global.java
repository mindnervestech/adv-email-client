import gui.ava.html.image.generator.HtmlImageGenerator;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.validation.ConstraintValidatorContext;

import org.apache.commons.validator.UrlValidator;
import org.hibernate.validator.internal.constraintvalidators.URLValidator;

import models.Links;
import play.Application;
import play.GlobalSettings;
import play.Logger;
import play.libs.Akka;
import scala.concurrent.duration.Duration;
import akka.actor.ActorSystem;

public class Global extends GlobalSettings {
	
	public static final String  APP_ENV_LOCAL = "local";
	public static final String  APP_ENV_VAR = "CURRENT_APPNAME";
	@Override
	public void onStart(Application app) {
		ActorSystem  actorSystem = Akka.system();
		 actorSystem.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(1, TimeUnit.MINUTES),
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
		
		 ActorSystem  actorSystem1 = Akka.system();
		 actorSystem1.scheduler().schedule(
				Duration.create(0, TimeUnit.MILLISECONDS),
				Duration.create(1, TimeUnit.MINUTES),
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
		 
		 ActorSystem actorSystemJob = Akka.system();
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
								try {
									HtmlImageGenerator imageGenerator1 = new HtmlImageGenerator();
									imageGenerator1.loadUrl(link.getUrl());
									imageGenerator1.saveAsImage(file +File.separator+ imageName);
									link.setPath(file + imageName);
									link.update();
								} catch (Exception e) {
									System.out.println("while parsing links in global " + e.getMessage());
									link.setPath("BAD_URL");
									link.update();
								}
									
							}
						}
					}, actorSystemJob.dispatcher());
	}
	@Override
	  public void onStop(Application app) {
	    Logger.info("Application shutdown...");
	  }  
}