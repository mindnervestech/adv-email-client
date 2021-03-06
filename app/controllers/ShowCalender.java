package controllers;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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

import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.Transactional;

public class ShowCalender extends Controller {

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
	
	@SubjectPresent
	public static Result showCalenderData(Long yearTab) throws ParseException {
		Http.Context context = Http.Context.current();
		String key = context.session().get("isAdmin");
		boolean isAdmin  = false;
		if(key.equals("Y")) {
			isAdmin  = true;
		}
		if(yearTab == 0 ){
			Date now = new Date();
			Integer year = now.getYear();
			yearTab = year.longValue();
		}
		Date currentDate = new Date();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.MONTH, -8);
		cal.set(Calendar.DAY_OF_MONTH, 1);
		Date preDate = cal.getTime();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		String fromDate = sdf.format(currentDate);
		String toDate = sdf.format(preDate);
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("select distinct domain from mail_object_model");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		
		List<String> monthList = new ArrayList<String>();
		
	
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
				 List <SqlRow> list= MailObjectModel.findMailObjectByDomainNameAndDate(mailsToDisplay.domainName,fromDate,toDate);
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
					 		populateArray(str,Integer.parseInt(month),id,subject,status,isAdmin);
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
		 for(int i=1;i<=9;i++) {
				
				switch (cal.get(Calendar.MONTH)) {
				
				case 0: monthList.add("Jan");
                		break;
	            case 1: monthList.add("Feb");
	                    break;
	            case 2: monthList.add("Mar");
	                    break;
	            case 3: monthList.add("Apr");
	            		break;
	            case 4: monthList.add("May");
	            		break;
	            case 5: monthList.add("Jun");
	            		break;
	            case 6: monthList.add("Jul");
	            		break;
	            case 7: monthList.add("Aug");
	            		break;
	            case 8:	monthList.add("Sep");
	            		break;
	            case 9:	monthList.add("Oct");
	            		break;
	            case 10: monthList.add("Nov");
	            		 break;
	            case 11: monthList.add("Dec");
	            		 break;
	            default: break;
	        }
				cal.add(Calendar.MONTH, 1);
			}
		 
		 System.out.println("Month..............     "+monthList);
		return ok(views.html.showCalender.render(mailsToDisplays,monthList,isAdmin));
  }
	
	
	
	
	public static void  populateArray( String model , int month, String email_id, String subject,int status,boolean isAdmin) {
		MailsIDToDisplay display = new MailsIDToDisplay();
		display.date = model;
		display.emailId = email_id;
		display.emailSubject = subject;
		if(status==2) {
			display.isHidden = true;
		} else {
			display.isHidden = false;
		}
		if(isAdmin || !display.isHidden) {
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
	}
	
}	
	
