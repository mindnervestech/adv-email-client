import java.util.List;
import java.util.concurrent.TimeUnit;

import models.MailObjectModel;

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
							EmailWriteFile.main(null);
						} catch (Exception e) {
							// TODO Auto-generated catch block
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
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
					}
					}, actorSystem1.dispatcher()
				);
	}
	@Override
	  public void onStop(Application app) {
	    Logger.info("Application shutdown...");
	  }  
}