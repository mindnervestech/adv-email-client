package controllers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import models.MailObjectModel;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import vm.MailsIDToDisplay;
import vm.MailsToDisplay;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.Transactional;

public class ShowTab extends Controller {

	static List<MailsIDToDisplay> jan;
	static List<MailsIDToDisplay> feb;
	static List<MailsIDToDisplay> mar;
	static List<MailsIDToDisplay> apr;
	static List<MailsIDToDisplay> may;
	static List<MailsIDToDisplay> jun;
	static List<MailsIDToDisplay> jul;
	static List<MailsIDToDisplay> aug;
	static List<MailsIDToDisplay> sep;
	static List<MailsIDToDisplay> oct;
	static List<MailsIDToDisplay> nov;
	static List<MailsIDToDisplay> dec;
	
	public static Result showTables(Long yearTab) throws ParseException {
		Http.Context context = Http.Context.current();
		String key = context.session().get("isAdmin");
		boolean isAdmin  = false;
		if(key.equals("Y")) {
			isAdmin  = true;
		}
		if(yearTab == 0 ){
			yearTab = 2014L;
		}
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("select distinct domain from mail_object_model");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		
		List<String> yearList = new ArrayList<String>();
		yearList = MailObjectModel.getDisticntDate(); 
		Collections.reverse(yearList);
	
		List<MailsToDisplay> mailsToDisplays =  new ArrayList<MailsToDisplay>();
		 for(SqlRow sr:resultList)
			{
			 jan = new ArrayList<MailsIDToDisplay>();
			 feb = new ArrayList<MailsIDToDisplay>();
			 mar = new ArrayList<MailsIDToDisplay>();
			 apr = new ArrayList<MailsIDToDisplay>();
			 may = new ArrayList<MailsIDToDisplay>();
			 jun = new ArrayList<MailsIDToDisplay>();
			 jul = new ArrayList<MailsIDToDisplay>();
			 aug = new ArrayList<MailsIDToDisplay>();
			 sep = new ArrayList<MailsIDToDisplay>();
			 oct = new ArrayList<MailsIDToDisplay>();
			 nov = new ArrayList<MailsIDToDisplay>();
			 dec = new ArrayList<MailsIDToDisplay>();
			 Map<String,List<MailsIDToDisplay>> map = new HashMap<String,List<MailsIDToDisplay>>();
				 MailsToDisplay mailsToDisplay= new MailsToDisplay();
				 mailsToDisplay.domainName= sr.getString("domain");
				 List <SqlRow> list= MailObjectModel.findMailObjectByDomainName(mailsToDisplay.domainName,Long.toString(yearTab));
				 for(int i=0;i<list.size();i++)
					 {
					 	String date = list.get(i).getString("sent_date");
					 	String id = list.get(i).getString("id");
					 	String subject = list.get(i).getString("mail_name");
					 	System.out.println("subject = "+subject);
					 	String month = list.get(i).getString("month");
					 	int status = list.get(i).getInteger("status");
					 	if(month != null){
					 		Date date2= new SimpleDateFormat("yyyy-MM-dd hh:mm:ss").parse(date);
					 		SimpleDateFormat format = new SimpleDateFormat("dd.MM.yyyy");
					 		String str =format.format(date2);		 		
					 		populateArray(str,Integer.parseInt(month),id,subject,status);
					 	}
					 }
				 
				 map.put("Jan", jan);
				 map.put("Feb", feb);
				 map.put("Mar", mar);
				 map.put("Apr", apr);
				 map.put("May", may);
				 map.put("Jun", jun);
				 map.put("Jul", jul);
				 map.put("Aug", aug);
				 map.put("Sep", sep);
				 map.put("Oct", oct);
				 map.put("Nov", nov);
				 map.put("Dec", dec);
				 mailsToDisplay.monthDates = map;
				 mailsToDisplays.add(mailsToDisplay);
			}
		return ok(views.html.showTable.render(mailsToDisplays,yearList,isAdmin));
  }
	
	
	
	
	public static void  populateArray( String model , int month, String email_id, String subject,int status) {
		MailsIDToDisplay display = new MailsIDToDisplay();
		display.date = model;
		display.emailId = email_id;
		display.emailSubject = subject;
		if(status==2) {
			display.isHidden = true;
		} else {
			display.isHidden = false;
		}
		switch (month) {
		
	            case 1: jan.add(display);
	                    break;
	            case 2: feb.add(display);
	                    break;
	            case 3: mar.add(display);
	            		break;
	            case 4: apr.add(display);
	            		break;
	            case 5: may.add(display);
	            		break;
	            case 6: jun.add(display);
	            		break;
	            case 7: jul.add(display);
	            		break;
	            case 8:	aug.add(display);
	            		break;
	            case 9:	sep.add(display);
	            		break;
	            case 10: oct.add(display);
	            		 break;
	            case 11: nov.add(display);
	            		 break;
	            case 12: dec.add(display);
	            	 	 break;
	            default: break;
	        }
	}
	
	
	@Transactional
	public static Result getTableCoverImageByID(long id) {
	MailObjectModel mailObjectModel = MailObjectModel.findMailObjectModelById(id);
	if(mailObjectModel.mailPath != null)
	{
		String filePath= mailObjectModel.mailPath.replace(".eml", ".png");
		try {
		return ok(new File(filePath));
		} catch(Exception e) {
			
		}
	}
	 return ok("no image set");
	}
	
	
	
}