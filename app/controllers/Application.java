package controllers;



import indexing.Email;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import models.DomainBL;
import models.DomainObject;
import models.EmailBL;
import models.KeywordBL;
import models.Links;
import models.MailObjectModel;
import models.SaveSearchSet;
import net.coobird.thumbnailator.Thumbnails;


import org.apache.commons.io.FileUtils;
import org.elasticsearch.common.collect.Iterables;
import org.elasticsearch.common.text.Text;
import org.elasticsearch.index.query.AndFilterBuilder;
import org.elasticsearch.index.query.BaseQueryBuilder;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.FilterBuilders;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeFilterBuilder;

import org.elasticsearch.search.facet.FacetBuilders;
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet;
import org.elasticsearch.search.facet.terms.strings.InternalStringTermsFacet.TermEntry;
import org.elasticsearch.search.sort.SortOrder;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;
import vm.MailsIDToDisplay;
import vm.UrlMapVM;

import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.Transactional;
import com.github.cleverage.elasticsearch.IndexResults;
import com.google.common.base.Splitter;


import controllers.Application.SearchResponse.Domain;
import elastic.MntHighlightBuilder;
import elastic.MntIndexQuery;

public class Application  extends Controller {
	static int IMGWIDTH=270;
	public static Result index() {
		return ok(views.html.home.render());
	}
	
	public static Result download(Long id) {
		MailObjectModel mailObjectModel = MailObjectModel.findMailObjectModelById(id);
		if(mailObjectModel != null && mailObjectModel.mailPath != null)
		{
			String filePath= mailObjectModel.mailPath;
			try {
			return ok(new File(filePath));
			} catch(Exception e) {
				
			}
		}
		 return ok("no image set");
	}
	
	public static Result searchForEmails(String predicate,boolean reverse) {
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"");
		
		RangeFilterBuilder rangeFilterBuilder = null;
		String fromDateStr = searchFilter.data().get("from");
		Date fromDate = null;
		
		if(fromDateStr !=null && fromDateStr.length() > 10 ) {
			try {
				fromDate = simpleDateFormat.parse(fromDateStr);
				rangeFilterBuilder = FilterBuilders.rangeFilter("sentDate");
				rangeFilterBuilder.from(fromDate);
			} catch (ParseException e) {
			}
		}
		
		Date toDate = null;
		String toDateStr = searchFilter.data().get("to");//("EEE MMM dd HH:mm:ss z yyyy");
		if(toDateStr !=null && toDateStr.length() > 10 ) {
			try {
				toDate = simpleDateFormat.parse(toDateStr);
				toDate=new Date(toDate.getTime()+(1000 * 60 * 60 * 24));
				if(rangeFilterBuilder == null)
					rangeFilterBuilder = FilterBuilders.rangeFilter("sentDate");
				rangeFilterBuilder.to(toDate);
			} catch (ParseException e) {
				e.printStackTrace();
			}
		}
		
		AndFilterBuilder andFilterBuilder = null;
		String keyWordsContents = searchFilter.data().get("cntKeyWord"); 
		String keyWordsName = searchFilter.data().get("nameKeyWord");
		String perPage = searchFilter.data().get("rowCount");
		String levelOnekeyWords = searchFilter.data().get("levelOnekeyWord");;
		//System.out.println("keyWordsName = "+keyWordsName);
		/*if(keyWordsContents != null && keyWordsContents.length() > 1 ) {
			if(andFilterBuilder == null) andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(FilterBuilders.queryFilter(QueryBuilders.fieldQuery("description", keyWordsContents)));
		}*/
		
		BaseQueryBuilder queryBuilder;  
		BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		boolean isDescription = keyWordsContents != null && keyWordsContents.length() > 1 ;
		boolean issendersEmail = keyWordsName != null && keyWordsName.length() > 1 ;
		boolean isnestedHtml = levelOnekeyWords != null && levelOnekeyWords.length() > 1;
		
		if(isDescription) {
			queryBuilder = QueryBuilders.queryString(keyWordsContents).defaultField("description");
			booleanQueryBuilder.must(queryBuilder);
		} 
		
		//queryBuilder = QueryBuilders.queryString("emos");
		if(issendersEmail) {
			queryBuilder = QueryBuilders.queryString(keyWordsName).defaultField("sendersEmail");
			booleanQueryBuilder.must(queryBuilder);
		}
		
		if(isnestedHtml ) {
			queryBuilder = QueryBuilders.queryString(levelOnekeyWords).defaultField("nestedHtml.description");
			booleanQueryBuilder.must(queryBuilder);
		}
		
		if(!(isnestedHtml || issendersEmail || isDescription)){
			booleanQueryBuilder.must(QueryBuilders.matchAllQuery());
		}
		
		
		queryBuilder = booleanQueryBuilder;
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
		
		if(rangeFilterBuilder != null) {
			if(andFilterBuilder == null) andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(rangeFilterBuilder);
		}
		
		MntIndexQuery<Email> indexQuery = new MntIndexQuery<Email>(Email.class);
		 
		 if(andFilterBuilder == null) {
			 indexQuery.setBuilder(queryBuilder);
		 } else {
			 indexQuery.setBuilder(QueryBuilders.filteredQuery(queryBuilder, 
					 andFilterBuilder));
		 }
		 ;
		 indexQuery.addHighlights(MntHighlightBuilder.instance().setField("description"));
		 if(isnestedHtml) {
			 indexQuery.addHighlights(MntHighlightBuilder.instance().setField("nestedHtml.description"));
		 }
		 indexQuery.addFacet(FacetBuilders.termsFacet("domain").field("domain"));
		 int count = Integer.parseInt(perPage);
		 // below two lines are for Pagination ---
		 indexQuery.from(Integer.parseInt(searchFilter.data().get("page")) * count);
		 indexQuery.size(count);
		 if(!predicate.equalsIgnoreCase("relevance")) {
			 if(reverse){
				 indexQuery.addSort(predicate, SortOrder.ASC);
			 } else {
				 indexQuery.addSort(predicate, SortOrder.DESC);
			 }
		 }
		 IndexResults<Email> allAndFacetAge = Email.find.search(indexQuery);
		 
		 SearchResponse searchResponse = new SearchResponse(); 
		 searchResponse.emails = new ArrayList<Application.SearchResponse.Email>();
		 for(Email e : allAndFacetAge.results) {
			 double length=e.description.length()*1.7/(1024.00);
			 String extract = "";//e.searchHit.getScore();
			 if(e.searchHit.getHighlightFields() != null && e.searchHit.getHighlightFields().get("description") != null) {
				 for(Text _t : e.searchHit.getHighlightFields().get("description").fragments()){
					 extract += _t.string() + " <strong><font class='customFont'>&bull;&bull;&bull;</font></strong> ";
				 }
				 
			 }
			 
			 if(e.searchHit.getHighlightFields() != null && e.searchHit.getHighlightFields().get("nestedHtml.description") != null) {
				 for(Text _t : e.searchHit.getHighlightFields().get("nestedHtml.description").fragments()){
					 extract += _t.string() + " <strong><font class='customFont'>&bull;&bull;&bull;</font></strong> ";
				 }
				 
			 }
			 
			 if(e.subject.length() > 60){
				 e.subject = e.subject.substring(0, e.subject.length() > 60 ? 60 : e.subject.length()) +" ...";
			 }
			 
			 if(extract.isEmpty()) {
				 extract = e.description.substring(0, e.description.length() > 1800 ? 1800 : e.description.length()) +" ...";
			 }
			 searchResponse.emails.add(new Application.SearchResponse.Email(e.subject,
					 e.domain, e.sentDate, e.sendersEmail, extract, e.mail_objectId,e.getId(),length,extract.length()));
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
	
	
	@SuppressWarnings("rawtypes")
	public static Result saveEmailSearchSet() {
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		String name = searchFilter.data().get("saveSearchName");
		SaveSearchSet saveSearchSet = SaveSearchSet.getSaveSearchSetName(name);
		if(saveSearchSet!= null) {
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

	public static Result showPopUpModal()throws MessagingException, IOException {
		SearchResponse searchResponse = new SearchResponse();  
		Document doc= null;
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class).bindFromRequest();
		Long id= Long.valueOf(searchFilter.data().get("popUpId"));
		System.out.println("url="+id );
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
		public String levelOnekeyWord;
		public int page;
		public int rowCount;
		public String[] domain;
		public String domainChecked;
		public int popUpId;
	}
	public static class BlackListedResponse
	{
		public List<DomainBL> domainList=new ArrayList<DomainBL>();
		public List<EmailBL> emailList=new ArrayList<EmailBL>();
		public List<KeywordBL> keywordList=new ArrayList<KeywordBL>();
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
					String sendersEmail, String extract, Long id,String indexId,Double length,int extractLength) 
			{
				super();
				this.subject = subject;
				this.domain = domain;
				this.date = date;
				this.sendersEmail = sendersEmail;
				this.extract = extract;
				this.id = id;
				this.indexId=indexId;
				this.length=length;
				this.extractLength=extractLength;
			}
			public String subject;
			public String domain;
			public Date date;
			public String sendersEmail;
			public String extract;
			public Long id;
			public String indexId;
			public Double length;
			public int extractLength;
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
		if(mailObjectModel != null && mailObjectModel.mailPath != null)
		{
			double factor;
			String filePath= mailObjectModel.mailPath.replace(".eml", "_thumbnail.png");
			File f=new File(filePath);
			if(!f.exists())
			{
				try 
				{
					filePath=mailObjectModel.mailPath.replace(".eml", ".png");
					File file=new File(filePath);
					BufferedImage originalImage = ImageIO.read(file);
					
					if(originalImage.getWidth()>IMGWIDTH)
					{
						factor=IMGWIDTH*1.0/originalImage.getWidth();
						Thumbnails.of(originalImage)
						.scale(factor).toFile(f);
					}
					else
					{
						return ok(file);
					}
				} 
				catch (IOException e1) {
					e1.printStackTrace();
				}
				try 
				{
					return ok(f);
				} 
				catch(Exception e) {	
				}
			}
			else
			{
				return ok(f);
			}
		}
	 return ok("no image set");
	}
	
	@Transactional
	public static Result getlinkImageByID(long id) {
		List<Links>links = Links.find.where().eq("mail_id.id",id).ne("processedUrl", null).findList();
		List<UrlMapVM> urlMap = new ArrayList<UrlMapVM>();
		
		for(Links link : links) {
			UrlMapVM vm = new UrlMapVM(link.getProcessedUrl(),link.getProcessedUrl().length());
			urlMap.add(vm);
	    }
		return ok(Json.toJson(urlMap));
	}
	
	public static Result addDomainToBL(String domainName)
	{
		//System.out.println("in addDomain DomainName="+domainName);
		DomainBL dom=DomainBL.findDomainblObjectByDomainName(domainName);
		BlackListedResponse blackListedResponse=new BlackListedResponse();
		if(dom == null) {
			DomainBL domain=new DomainBL();
			domain.domain=domainName;
			domain.save();
			dom=DomainBL.findDomainblObjectByDomainName(domainName);
			blackListedResponse.domainList.add(dom);
			return ok(Json.toJson(blackListedResponse));
		}
		blackListedResponse.domainList.add(null);
		return ok(Json.toJson(blackListedResponse));
	}
	public static Result getBlackListed()
	{
		BlackListedResponse blackListedResponse=new BlackListedResponse();
		blackListedResponse.domainList=DomainBL.findDomainblAllObject();
		blackListedResponse.emailList=EmailBL.findEmailblAllObject();
		blackListedResponse.keywordList=KeywordBL.findKeywordblAllObject();
		/*Iterator itr=li.iterator();
		while(itr.hasNext())
		{
			System.out.println(((DomainBL)itr.next()).domain);
		}*/
		return ok(Json.toJson(blackListedResponse));
	}
	public static Result addEmailToBL(String emailAddress)
	{
		//System.out.println("in addDomain DomainName="+domainName);
		EmailBL dom=EmailBL.findEmailblObjectByEmailAddress(emailAddress);
		BlackListedResponse blackListedResponse=new BlackListedResponse();
		if(dom == null) {
			EmailBL emailbl=new EmailBL();
			emailbl.email=emailAddress;
			emailbl.save();
			dom=EmailBL.findEmailblObjectByEmailAddress(emailAddress);
			blackListedResponse.emailList.add(dom);
			return ok(Json.toJson(blackListedResponse));
		}
		blackListedResponse.emailList.add(null);
		return ok(Json.toJson(blackListedResponse));
	}
	public static Result addKeywordToBL(String keyword) {
		//System.out.println("in addDomain DomainName="+domainName);
		KeywordBL key=KeywordBL.findKeywordblObjectByKeyword(keyword);
		BlackListedResponse blackListedResponse=new BlackListedResponse();
		if(key == null) {
			KeywordBL keywordbl=new KeywordBL();
			keywordbl.keyword=keyword;
			keywordbl.save();
			key=KeywordBL.findKeywordblObjectByKeyword(keyword);
			blackListedResponse.keywordList.add(key);
			return ok(Json.toJson(blackListedResponse));
		}
		blackListedResponse.keywordList.add(null);
		return ok(Json.toJson(blackListedResponse));
	}
	public static Result removeBLDomain(long id) {
		return ok(Json.toJson(DomainBL.deleteDomainBLById(id)));
	}
	public static Result removeBLEmail(long id) {
		return ok(Json.toJson(EmailBL.deleteEmailBLById(id)));
	}
	public static Result removeBLKeyword(long id) {
		return ok(Json.toJson(KeywordBL.deleteKeywordBLById(id)));
	}
	public static Result removeEmailData(long id,String indexId) {
		Email e=Email.find.byId(indexId);
		if(e!=null) {
			e.delete();
			//Links.deleteLinksByMailObjectId(id);
			MailObjectModel.deleteMailObjectById(id);
			return ok("record deleted successfully");
		}
		return ok("no record found");
	}
	public static Result getMonthChart(String fromMonth,String toMonth)
	{
		int totalCount=MailObjectModel.getTotalCount(fromMonth,toMonth);
		List<SqlRow> list = MailObjectModel.getMonthChartData(fromMonth,toMonth);
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		for(SqlRow sr:list){
			List<Object> _item = new ArrayList<Object>();
			_item.add(sr.getString("domain"));
			_item.add(sr.getInteger("count")*100.00/totalCount);
			responseList.add(_item);
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}
	public static Result getYearChart(String year)
	{
		int totalCount=MailObjectModel.getTotalCountForYear(year);
		List<SqlRow> list = MailObjectModel.getYearChartData(year);
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		for(SqlRow sr:list){
			List<Object> _item = new ArrayList<Object>();
			_item.add(sr.getString("domain"));
			_item.add(sr.getInteger("count")*100.00/totalCount);
			responseList.add(_item);
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}
	public static Result getAllChart()
	{
		int totalCount=MailObjectModel.getTotalCountForAll();
		List<SqlRow> list = MailObjectModel.getAllChartData();
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		for(SqlRow sr:list){
			List<Object> _item = new ArrayList<Object>();
			_item.add(sr.getString("domain"));
			_item.add(sr.getInteger("count")*100.00/totalCount);
			responseList.add(_item);
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}
	
	public static Result getAllChart1(int count)
	{
		int totalCount=MailObjectModel.getTotalCountForAll();
		List<SqlRow> list = MailObjectModel.getAllChartData();
		Collections.reverse(list);
		System.out.println("hiiiiiii"+(list.size() - 1));
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		
		//System.out.println(list.get(index));
		try {
		for(int i=count;i<count+30;i++){
			
			List<Object> _item = new ArrayList<Object>();
			_item.add(list.get(i).getString("domain"));
			_item.add(list.get(i).getInteger("count"));
			responseList.add(_item);
			
		}
		}catch(IndexOutOfBoundsException e) {
			
			for(int i=count;i>list.size()-1;i++){
				
				List<Object> _item = new ArrayList<Object>();
				_item.add(list.get(i).getString("domain"));
				_item.add(list.get(i).getInteger("count"));
				responseList.add(_item);
				
			}
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}
	
	public static Result getAllChartprev(int prev)
	{
		
		int totalCount=MailObjectModel.getTotalCountForAll();
		List<SqlRow> list = MailObjectModel.getAllChartData();
		Collections.reverse(list);
		System.out.println("hiiiiiii"+(list.size() - 1));
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		/*if(count>list.size()-1){
			count=list.size();
		}*/
		//System.out.println(list.get(index));
		try {
		for(int i=prev-30;i<prev;i++){
			
			List<Object> _item = new ArrayList<Object>();
			_item.add(list.get(i).getString("domain"));
			_item.add(list.get(i).getInteger("count"));
			responseList.add(_item);
			
		}
		}catch(IndexOutOfBoundsException e) {
			
			for(int i=0;i<30;i++){
				
				List<Object> _item = new ArrayList<Object>();
				_item.add(list.get(i).getString("domain"));
				_item.add(list.get(i).getInteger("count"));
				responseList.add(_item);
				
			}
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}
	public static List<Date> stringToDate(String fromMonth,String toMonth) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date toDate;
		Date fromDate;
		try {
			fromMonth="01-"+fromMonth;
			fromDate=formatter.parse(fromMonth);
			String toArr[]=toMonth.split("-");
			int toMonthInt=Integer.parseInt(toArr[0]);
			if(toMonthInt==1||toMonthInt==3||toMonthInt==5||toMonthInt==7||toMonthInt==8||toMonthInt==10||toMonthInt==12) {
				toMonth="31-"+toMonth;
				toDate=formatter.parse(toMonth);
			} else if(toMonthInt==4||toMonthInt==6||toMonthInt==9||toMonthInt==11) {
				toMonth="30-"+toMonth;
				toDate=formatter.parse(toMonth);
			} else {
				int year=Integer.parseInt(toArr[1]);
				if(year%400==0||(year%4==0 && year%100 !=0 && year%200 != 0 && year%300 != 0)) {
					toMonth="29-"+toMonth;
					toDate=formatter.parse(toMonth);
				} else {
					toMonth="28-"+toMonth;
					toDate=formatter.parse(toMonth);
				}
			}
			List<Date> dateList=new ArrayList<Date>();
			dateList.add(fromDate);
			dateList.add(toDate);
			return dateList;
		} catch (ParseException e) {
			e.printStackTrace();
		};
		return null;
	}
	public  static Result statistic(String fromMonth,String toMonth) {
		/// TODO: Ahemd
		List<StatsVM_1> statsVM_1s = new ArrayList<Application.StatsVM_1>();
		List<Date> dateList=stringToDate(fromMonth, toMonth);
		if(dateList!=null){
			System.out.println("fromDate:"+fromMonth +" toDate:"+toMonth);
			List<DomainBL> domainBLs=DomainBL.findDomainblAllObject();
			List<MailObjectModel> moms = MailObjectModel.find.where().between("sentDate", dateList.get(0), dateList.get(1)).eq("status", "1").orderBy("domain").findList();
			String prevDomain = "";
				
			StatsVM_1 vm_1 = null;
			int total = moms.size();
			System.out.println("total Size :"+moms.size());
			for (MailObjectModel mom : moms) {
				if(!prevDomain.equalsIgnoreCase(mom.domain)) {
					vm_1 = new StatsVM_1();
					prevDomain=mom.domain;
					vm_1.domain=mom.domain;
					for(DomainBL domain: domainBLs) {
						if(domain.domain.equalsIgnoreCase(mom.domain)) {
							vm_1.domainStatus=true;
							vm_1.blackDomainListedId=domain.id;
						}
					}
					statsVM_1s.add(vm_1);
				}
				vm_1.add(mom,total);
			}
		}
		/*} catch (ParseException e) {
			e.printStackTrace();
		};*/
		return ok(Json.toJson(statsVM_1s));
	}
	
	public static class StatsVM_1 {
		public int total;
		public String domain;
		public int count = 0;
		public double percentage; 
		public boolean domainStatus=false;
		public Long blackDomainListedId;
		public List<MailsIDToDisplay> mails;
		public void add(MailObjectModel mom,int total) {
			this.total=total;
			if(mails == null) {
				mails = new ArrayList<MailsIDToDisplay>();
			}
			System.out.println(mom.id+" "+mom.mailName+" "+mom.sentDate);
			mails.add(new MailsIDToDisplay(mom.id, mom.mailName, mom.sentDate));
			count++;
			percentage = count*100.00/total; 
			System.out.println("count="+count +" percentage ="+percentage);
		}
		
	}
	
	
	
	public static Result downloadPdf(long id){
		Document doc= null;
		MailObjectModel mailObjectModel =MailObjectModel.findMailObjectModelById(id);
		File file= new File(mailObjectModel.mailPath);
		Session session = Session.getDefaultInstance(new Properties());
		InputStream inMsg;
		try {
			inMsg = new FileInputStream(file);
		
		Message msg = new MimeMessage(session, inMsg);
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
		doc.toString();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ok();
	}
	public static Result getDataSize() {
		String mailFolder=Play.application().configuration()
				.getString("mail.storage.path");
		Double mailFolderSize=(FileUtils.sizeOfDirectory(new File(mailFolder))/(1024.00*1024));
		String elasticFolder=Play.application().configuration()
				.getString("mail.elastic.path");
		Double elasticFolderSize=(FileUtils.sizeOfDirectory(new File(elasticFolder))/(1024.00*1024));
		List<Object> list=new ArrayList<Object>();
		list.add(MailObjectModel.getDataSize());
		list.add(mailFolderSize+elasticFolderSize);
		return ok(Json.toJson(list));
	}
	// List start
	public static Result loadLists() {
		SubscriptionListVM result = new SubscriptionListVM();
		List<DomainObject> unAssignedList = DomainObject.findUnAssignedNames();
		List<DomainObject> assignedList = DomainObject.findAssignedNames();
		List<DomainObject> assignedChildList = DomainObject.findAssignedChildNames();
		result.totalUnAssignedNumber = unAssignedList.size();
		result.totalParentNumber = assignedList.size();
		result.totalChildNumber = assignedChildList.size();
		result.unAssignedList = new ArrayList<SubscriptionVM>();
		for(DomainObject ds : unAssignedList) {
			result.unAssignedList.add(new SubscriptionVM(ds.id,ds.name,ds.color));
		}
		result.assignedList = new ArrayList<SubscriptionVM>();
		for(DomainObject ds : assignedList) {
			result.assignedList.add(new SubscriptionVM(ds.id,ds.name,DomainObject.findAssignedChildNames(ds.id).size(),ds.color));
		}
		result.assignedChildList = new ArrayList<SubscriptionVM>();
		for(DomainObject ds : assignedChildList) {
			result.assignedChildList.add(new SubscriptionVM(ds.id,ds.name,ds.color));
		}
		return ok(Json.toJson(result));
	}
	
	public static Result loadChildList(Long id) {
		SubscriptionListVM result = new SubscriptionListVM();
		List<DomainObject> assignedChildList = DomainObject.findAssignedChildNames(id);
		result.assignedChildList = new ArrayList<SubscriptionVM>();
		for(DomainObject ds : assignedChildList) {
			result.assignedChildList.add(new SubscriptionVM(ds.id,ds.name,ds.color));
		}
		return ok(Json.toJson(result));
	}
	
	public static class SubscriptionVM {
		public Long id;
		public String title;
		public Boolean drag=true;
		public Integer childNumber;
		public String color;
		public SubscriptionVM (Long id,String title,String color) {
			this.title=title;
			this.id=id;
			this.color = color;
		}
		public SubscriptionVM (Long id,String title,Integer childNumber,String color) {
			this.title = title;
			this.id = id;
			this.childNumber  = childNumber;
			this.color = color;
		}
	}
	
	public static class SubscriptionListVM {
		public Integer totalUnAssignedNumber;
		public Integer totalParentNumber;
		public Integer totalChildNumber;
		public List<SubscriptionVM> unAssignedList;
		public List<SubscriptionVM> assignedList;
		public List<SubscriptionVM> assignedChildList;
	}
	
	public static Result addParentSubscription(Long id) {
		DomainObject.addParentSubscription(id);
		return ok();
	}
	
	public static Result removeParentSubscription(Long id) {
		DomainObject.removeParentSubscription(id);
		return ok();
	}
	
	public static Result addChildSubscription(Long cid,Long pid) {
		DomainObject.addChildSubscription(cid,pid);
		return ok();
	}
	
	public static Result removeChildSubscription(Long id) {
		DomainObject.removeChildSubscription(id);
		return ok();
	}
	
	public static Result addParentDomain(String parent) {
		DomainObject.addParentDomain(parent);
		return ok();
	}
	
	//List end
}
