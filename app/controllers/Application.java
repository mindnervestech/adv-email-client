package controllers;



import indexing.Email;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.StringTokenizer;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import models.MailObjectModel;
import models.SaveSearchSet;

import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;
import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet;
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet.TermEntry;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.Expr;
import com.avaje.ebean.Expression;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.Transactional;
import com.github.cleverage.elasticsearch.IndexQuery;
import com.github.cleverage.elasticsearch.IndexResults;
import com.google.common.base.Splitter;

import controllers.Application.SearchResponse.Domain;

public class Application  extends Controller {
	
	public static Result index() {
		return ok(views.html.home.render());
	}
	
	public static Result searchForEmails() {
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
		
		RangeFilterBuilder rangeFilterBuilder = FilterBuilders.rangeFilter("sentDate");
		String fromDateStr = searchFilter.data().get("from");
		Date fromDate = null;
		
		if(fromDateStr !=null && fromDateStr.length() > 10 ) {
			try {
				fromDate = simpleDateFormat.parse(fromDateStr);
				rangeFilterBuilder.from(fromDate);
			} catch (ParseException e) {
			}
		}
		
		Date toDate = null;
		String toDateStr = searchFilter.data().get("to");//("EEE MMM dd HH:mm:ss z yyyy");
		if(toDateStr !=null && toDateStr.length() > 10 ) {
			try {
				toDate = simpleDateFormat.parse(toDateStr);
				rangeFilterBuilder.to(toDate.getTime());
			} catch (ParseException e) {
			}
		}
		
		AndFilterBuilder andFilterBuilder = null;
		String keyWordsContents = searchFilter.data().get("cntKeyWord"); 
		if(keyWordsContents != null && keyWordsContents.length() > 1 ) {
			if(andFilterBuilder == null) andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(FilterBuilders.queryFilter(QueryBuilders.fieldQuery("description", keyWordsContents)));
		}
		
		String keyWordsSub = searchFilter.data().get("subKeyWord"); 
		if(keyWordsSub != null && keyWordsSub.length() > 1 ) {
			if(andFilterBuilder == null) andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(FilterBuilders.queryFilter(QueryBuilders.fieldQuery("subject",keyWordsSub)));
		}
		
		String domainChecked =  searchFilter.get().domainChecked;
		if(domainChecked != null && domainChecked.length() > 3 ) {
			Iterable<String> domains = Splitter.on(",").omitEmptyStrings().trimResults().split(domainChecked);
			if(andFilterBuilder == null) andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(FilterBuilders.inFilter("domain",Iterables.toArray(domains, String.class)));
		}
		
		 IndexQuery<Email> indexQuery = Email.find.query();
		 if(andFilterBuilder == null) {
			 indexQuery.setBuilder(QueryBuilders.matchAllQuery());
		 } else {
			 indexQuery.setBuilder(QueryBuilders.filteredQuery(QueryBuilders.matchAllQuery(), 
					 andFilterBuilder));
		 }
		 
		 indexQuery.addFacet(FacetBuilders.termsFacet("domain").field("domain"));
		 
		 // below two lines are for Pagination ---
		 indexQuery.from(searchFilter.get().page*10);
		 indexQuery.size(10);
		 
		 
		 IndexResults<Email> allAndFacetAge = Email.find.search(indexQuery);
		 
		 SearchResponse searchResponse = new SearchResponse(); 
		 searchResponse.emails = new ArrayList<Application.SearchResponse.Email>();
		 for(Email e : allAndFacetAge.results) {
			 searchResponse.emails.add(new Application.SearchResponse.Email(e.subject,
					 e.domain,e.sentDate,e.description,e.mail_objectId));
		 }
		 searchResponse.saveSearchSets.addAll(SaveSearchSet.find.all());
		 searchResponse.noOFPages = (int) Math.ceil((double)allAndFacetAge.getTotalCount()/10);
		 
		 // For Domain Counts
		 searchResponse.domainCounts = new ArrayList<Application.SearchResponse.Domain>();
		 for(TermEntry te : ((InternalStringTermsFacet)allAndFacetAge.facets.facet("domain")).getEntries()) {
			 searchResponse.domainCounts.add(new Domain(te.getTerm().string(), te.getCount()));
		 }
		 
		 return ok(Json.toJson(searchResponse));
		
	}
	
	@Deprecated
	public static Result searchForEmailsV1() {
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		String keyWordsContents = searchFilter.data().get("cntKeyWord"); 
		String keyWordsSub = searchFilter.data().get("subKeyWord"); 
		String fromDate = searchFilter.data().get("from");
		String toDate = searchFilter.data().get("to");
		String domainChecked =  searchFilter.get().domainChecked; 
		
		SearchResponse searchResponse = new SearchResponse();
	        
		if( (keyWordsSub==null || keyWordsSub.isEmpty()) &&(keyWordsContents==null || keyWordsContents.isEmpty()) && (fromDate== null ) && (toDate == null ))
		{
			return ok(Json.toJson(searchResponse));
		}
		
		StringBuilder query = new StringBuilder();
		
		getResultFromDb(searchFilter,domainChecked, keyWordsContents, keyWordsSub, fromDate,
				toDate, searchResponse, query);
		return ok(Json.toJson(searchResponse));
	}

	
	
	@SuppressWarnings("rawtypes")
	public static Result saveEmailSearchSet()
	{
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		String name = searchFilter.data().get("saveSearchName");
		SaveSearchSet saveSearchSet = SaveSearchSet.getSaveSearchSetName(name);
		if(saveSearchSet!= null)
		{
			return ok(Json.toJson("name is already in system please modify it."));
		}
		SearchResponse searchResponse = new SearchResponse();  
	    saveSearchSet = new SaveSearchSet();
		saveSearchSet.name=name;
		saveSearchSet.querySql=request().uri().toString().replace(request().path().toString()+"?", "");
		saveSearchSet.save();
		List list= new ArrayList<SaveSearchSet>();
		list=SaveSearchSet.find.all();
		searchResponse.saveSearchSets.addAll(list);
		return ok(Json.toJson(searchResponse));
	}

	public static Result showPopUpModal()throws MessagingException, IOException
	{
		SearchResponse searchResponse = new SearchResponse();  
		Document doc= null;
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		int id= searchFilter.get().popUpId;
		System.out.println("url="+id);
		MailObjectModel mailObjectModel =MailObjectModel.findMailObjectModelById(id);
		File file= new File(mailObjectModel.mailPath);
		Session session = Session.getDefaultInstance(new Properties());
		InputStream inMsg = new FileInputStream(file);
		Message msg = new MimeMessage(session, inMsg);
		//System.out.println("msg.getContentType();"+msg.getContent());
		
		String contentType=msg.getContentType().substring(0,msg.getContentType().indexOf('/'));
		MimeMultipart obj=null;
		if("multipart".equals(contentType))
		{
			obj = (MimeMultipart)msg.getContent();
			if(obj.getCount()==0)
			{
				doc = Jsoup.parse(obj.getBodyPart(0).getContent().toString(), "ISO-8859-1");
			}
			else
			for(int k=0;k<obj.getCount();k++) 
			{
				doc = Jsoup.parse(obj.getBodyPart(k).getContent().toString(), "ISO-8859-1");
			}
		}
		else
		{
			doc = Jsoup.parse(msg.getContent().toString(), "ISO-8859-1");
		}
		searchResponse.htmlToShowMailPopUp=doc.toString();
		return ok(Json.toJson(searchResponse));
	}
	
	public static Result showPopUpImages()throws MessagingException, IOException
	{
		SearchResponse searchResponse = new SearchResponse();  
		return ok(Json.toJson(searchResponse));
	}
	public static Result showPopUpLinks()throws MessagingException, IOException
	{
		SearchResponse searchResponse = new SearchResponse();  
		return ok(Json.toJson(searchResponse));
	}
	public static class  SearchFilter {
		public String from;
		public String to ;
		public String cntKeyWord;
		public String subKeyWord;
		public int page;
		public int rowCount;
		public String[] domain;
		public String domainChecked;
		public int popUpId;
	}
	
	
	public static class SearchResponse {
		
		public List<Email> emails = new ArrayList<Email>();
		public List<Domain> domainCounts= new ArrayList<Domain>();
		public List<SaveSearchSet> saveSearchSets= new ArrayList<SaveSearchSet>();
		public long noOFPages;
		public String htmlToShowMailPopUp;
		public static class Email {
			public Email(){}
			public Email(String subject, String domain, Date date,
					String extract, Long id) 
			{
				super();
				this.subject = subject;
				this.domain = domain;
				this.date = date;
				this.extract = extract;
				this.id = id;
			}
			public String subject;
			public String domain;
			public Date date;
			public String extract;
			public Long id;
		}
		
		public static class Domain {
			public Domain(String name, int count) {
				super();
				this.name = name;
				this.count = count;
			}
			public String name;
			public int count;
		}
	}
	public static Date convertStringToDate(String dateString)
	{
		dateString=dateString.substring(1, dateString.lastIndexOf('-')+3);
	    Date date = null;
	    DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
	    try{
	        date = sdf.parse(dateString);
	    }
	    catch ( Exception ex ){
	        System.out.println(ex);
	    }
	    return date;
	}
	@Transactional
	public static Result getCoverImageByID(long id) {
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
	
	public static void getResultFromDb(Form<SearchFilter> searchFilter,String domainChecked,
			String keyWordsContents, String keyWordsSub, String fromDate,
			String toDate, SearchResponse searchResponse, StringBuilder query) {
	    List<String> strs= new ArrayList<String>();
		query.append("select domain , count(*) as sub_count from mail_object_model ");
		if(fromDate!=null &&  toDate== null)
		{
			query.append(" where sent_Date >= '"+fromDate+"'");
		}
		if(toDate!=null  && fromDate==null)
		{
			if(query.toString().contains(" where "))
			{
				query.append(" or sent_Date <= '"+toDate+"'");
			}
			else
			query.append(" where sent_Date <= '"+toDate+"'");
		}
	
		if(fromDate!=null  && toDate!=null )
		{
			query.append(" where sent_Date between " +fromDate+ " and " +toDate);
		}
		if( keyWordsSub!=null && !keyWordsSub.isEmpty())
		{
			if(query.toString().contains(" where "))
			{
				query.append(" or mail_name like '%"+keyWordsSub+"%'");
			}
			else
			query.append(" where mail_name like '%"+keyWordsSub+"%'");
		}
		/*if( keyWordsContents!=null && !keyWordsContents.isEmpty())
		{
			if(query.toString().contains(" where "))
			{
				query.append(" or mail_name like '%"+keyWordsContents+"%'");
			}
			else
			query.append(" where mail_name like '%"+keyWordsContents+"%'");
		}*/
		/*	if( domain!=null && !domain.isEmpty())
			{
				if(query.toString().contains(" where "))
				{
					query.append(" or domain = '"+domain +"'" );
				}
				else
				query.append(" where domain = '"+domain +"'");
			}*/
		query.append(" group by domain ;");
		
		List <SqlRow> resultList = Ebean.createSqlQuery(query.toString()).findList();
		for(SqlRow rs: resultList)
		{
			searchResponse.domainCounts.add(new Domain(rs.getString("domain"), rs.getInteger("sub_count")));
		}
		List arrrayEmailObj= new ArrayList<MailObjectModel>();
		List<Expression> expressions = new ArrayList<Expression>();
		 Date dateFrom = convertStringToDate(fromDate);
		 Date dateTo = convertStringToDate(toDate);
		
		if(fromDate!=null &&  toDate== null)
		{
			expressions.add(Expr.gt("sentDate",dateFrom));
		}
		if(toDate!=null  && fromDate==null)
		{
			expressions.add(Expr.lt("sentDate",dateTo));
		}
		if(fromDate!=null  && toDate!=null )
		{
			expressions.add(Expr.between("sentDate",dateFrom,dateTo));
		}
		if( keyWordsSub!=null && !keyWordsSub.isEmpty())
		{
			expressions.add(Expr.like("mailName",keyWordsSub));
		}
		if( keyWordsContents!=null && !keyWordsContents.isEmpty())
		{
			expressions.add(Expr.like("mailName",keyWordsContents));
		}
		StringTokenizer tk = new StringTokenizer(domainChecked, ",");
		while(tk.hasMoreTokens())
		{
			String  str=tk.nextToken();
			strs.add(str);
		}
		if(!domainChecked.isEmpty() && domainChecked != null)
		{
			expressions.add(Expr.in("domain",strs));
		}
		if(expressions.size()!=0)
		{
			Expression exp=expressions.get(0);
			for(int i =1;i<expressions.size();i++){
				exp = Expr.and(exp, expressions.get(i));
			}
			arrrayEmailObj=MailObjectModel.find.where().add(exp).findList();
			//System.out.println("sz---"+arrrayEmailObj.size());
			searchResponse.noOFPages =(int) Math.ceil((double)arrrayEmailObj.size()/10);
			arrrayEmailObj= MailObjectModel.find.where().add(exp).setMaxRows(searchFilter.get().rowCount).
					setFirstRow(searchFilter.get().page*10).findList();
		}
		List list= new ArrayList<SaveSearchSet>();
		list=SaveSearchSet.find.all();
		searchResponse.saveSearchSets.addAll(list);
		searchResponse.emails.addAll(arrrayEmailObj);
	}
}
