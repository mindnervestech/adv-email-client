package controllers;

import indexing.Email;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import models.BasicInfo;
import models.DomainBL;
import models.DomainObject;
import models.EmailBL;
import models.KeywordBL;
import models.Links;
import models.MailObjectModel;
import models.SaveSearchSet;
import models.User;
import models.UserPermission;
import net.coobird.thumbnailator.Thumbnails;

import org.apache.commons.io.FileUtils;
import org.codehaus.jackson.JsonNode;
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
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import play.Play;
import play.data.DynamicForm;
import play.data.Form;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Http;
import play.mvc.Result;
import utility.DailyReportPDF;
import views.html.accessFailed;
import vm.MailsIDToDisplay;
import vm.MailsToDisplay;
import vm.UrlMapVM;
import wordcram.Colorers;
import wordcram.Placers;
import wordcram.WordCram;
import be.objectify.deadbolt.core.models.Permission;
import be.objectify.deadbolt.java.actions.SubjectPresent;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.annotation.Transactional;
import com.github.cleverage.elasticsearch.IndexResults;
import com.google.common.base.Splitter;

import controllers.Application.SearchResponse.Domain;
import elastic.MntHighlightBuilder;
import elastic.MntIndexQuery;

public class Application extends Controller implements Job {
	static int IMGWIDTH = 270;
	static final String adminPermission = Play.application().configuration()
			.getString("admin.permission");

	public static Result login() {
		Http.Context context = Http.Context.current();
		DynamicForm requestData = Form.form().bindFromRequest();
		context.session().put("key", requestData.get("key"));
		return redirect("/auth");
	}

	public static Result index() {
		Http.Context context = Http.Context.current();
		DynamicForm requestData = Form.form().bindFromRequest();
		context.session().put("key", requestData.get("key"));
		return redirect("/auth");
	}

	@SubjectPresent
	public static Result authindex() {
		Http.Context context = Http.Context.current();
		User user = (User) context.args.get("currentUser");
		Boolean isAdmin = false;
		if (user.getCompanyId() == 4887) {
			isAdmin = true;
			context.session().put("isAdmin", "Y");
			return ok(views.html.home.render(isAdmin));
		} else {
			isAdmin = false;
			context.session().put("isAdmin", "N");
			for (Permission p : user.getPermissions()) {
				if (((UserPermission) p).getUrl().equalsIgnoreCase(
						adminPermission)) {
					return ok(views.html.home.render(isAdmin));
				}
			}
		}
		return ok(accessFailed.render());
	}

	public static Result download(Long id) {
		MailObjectModel mailObjectModel = MailObjectModel
				.findMailObjectModelById(id);
		if (mailObjectModel != null && mailObjectModel.mailPath != null) {
			String filePath = mailObjectModel.mailPath;
			try {
				return ok(new File(filePath));
			} catch (Exception e) {

			}
		}
		return ok("no image set");
	}

	public static Result searchForEmails(String predicate, boolean reverse) {
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class)
				.bindFromRequest();
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(
				"\"yyyy-MM-dd'T'HH:mm:ss.SSS'Z'\"");

		RangeFilterBuilder rangeFilterBuilder = null;
		String fromDateStr = searchFilter.data().get("from");
		Date fromDate = null;

		if (fromDateStr != null && fromDateStr.length() > 10) {
			try {
				fromDate = simpleDateFormat.parse(fromDateStr);
				rangeFilterBuilder = FilterBuilders.rangeFilter("sentDate");
				rangeFilterBuilder.from(fromDate);
			} catch (ParseException e) {
			}
		}

		Date toDate = null;
		String toDateStr = searchFilter.data().get("to");// ("EEE MMM dd HH:mm:ss z yyyy");
		if (toDateStr != null && toDateStr.length() > 10) {
			try {
				toDate = simpleDateFormat.parse(toDateStr);
				toDate = new Date(toDate.getTime() + (1000 * 60 * 60 * 24));
				if (rangeFilterBuilder == null)
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
		String levelOnekeyWords = searchFilter.data().get("levelOnekeyWord");
	
		// System.out.println("keyWordsName = "+keyWordsName);
		/*
		 * if(keyWordsContents != null && keyWordsContents.length() > 1 ) {
		 * if(andFilterBuilder == null) andFilterBuilder =
		 * FilterBuilders.andFilter();
		 * andFilterBuilder.add(FilterBuilders.queryFilter
		 * (QueryBuilders.fieldQuery("description", keyWordsContents))); }
		 */

		BaseQueryBuilder queryBuilder;
		BoolQueryBuilder booleanQueryBuilder = QueryBuilders.boolQuery();
		boolean isDescription = keyWordsContents != null
				&& keyWordsContents.length() > 1;
		boolean issendersEmail = keyWordsName != null
				&& keyWordsName.length() > 1;
		boolean isnestedHtml = levelOnekeyWords != null
				&& levelOnekeyWords.length() > 1;

		if (isDescription) {
			queryBuilder = QueryBuilders.queryString(keyWordsContents)
					.defaultField("description");
			booleanQueryBuilder.must(queryBuilder);
		}

		// queryBuilder = QueryBuilders.queryString("emos");
		if (issendersEmail) {
			queryBuilder = QueryBuilders.queryString(keyWordsName)
					.defaultField("sendersEmail");
			booleanQueryBuilder.must(queryBuilder);
		}

		if (isnestedHtml) {
			queryBuilder = QueryBuilders.queryString(levelOnekeyWords)
					.defaultField("nestedHtml.description");
			booleanQueryBuilder.must(queryBuilder);
		}

		if (!(isnestedHtml || issendersEmail || isDescription)) {
			booleanQueryBuilder.must(QueryBuilders.matchAllQuery());
		}

		queryBuilder = booleanQueryBuilder;
		String keyWordsSub = searchFilter.data().get("subKeyWord");
		if (keyWordsSub != null && keyWordsSub.length() > 1) {
			if (andFilterBuilder == null)
				andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(FilterBuilders.queryFilter(QueryBuilders
					.fieldQuery("subject", keyWordsSub)));
		}

		String domainChecked = searchFilter.get().domainChecked;
		if (domainChecked != null && domainChecked.length() > 3) {
			Iterable<String> domains = Splitter.on(",").omitEmptyStrings()
					.trimResults().split(domainChecked);
			if (andFilterBuilder == null)
				andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(FilterBuilders.inFilter("domain",
					Iterables.toArray(domains, String.class)));
		}

		if (rangeFilterBuilder != null) {
			if (andFilterBuilder == null)
				andFilterBuilder = FilterBuilders.andFilter();
			andFilterBuilder.add(rangeFilterBuilder);
		}
		Http.Context context = Http.Context.current();
		String key = context.session().get("isAdmin");
		if (key.equals("N")) {
			andFilterBuilder.add(FilterBuilders.notFilter(FilterBuilders
					.termFilter("status", 1)));
		}
		MntIndexQuery<Email> indexQuery = new MntIndexQuery<Email>(Email.class);

		if (andFilterBuilder == null) {
			indexQuery.setBuilder(queryBuilder);
		} else {
			indexQuery.setBuilder(QueryBuilders.filteredQuery(queryBuilder,
					andFilterBuilder));
		}

		indexQuery.addHighlights(MntHighlightBuilder.instance().setField(
				"description"));
		if (isnestedHtml) {
			indexQuery.addHighlights(MntHighlightBuilder.instance().setField(
					"nestedHtml.description"));
		}
		indexQuery.addFacet(FacetBuilders.termsFacet("domain").field("domain"));
		int count = Integer.parseInt(perPage);
		// below two lines are for Pagination ---
		indexQuery.from(Integer.parseInt(searchFilter.data().get("page"))
				* count);
		indexQuery.size(count);
		if (!predicate.equalsIgnoreCase("relevance")) {
			if (reverse) {
				indexQuery.addSort(predicate, SortOrder.ASC);
			} else {
				indexQuery.addSort(predicate, SortOrder.DESC);
			}
		}
		IndexResults<Email> allAndFacetAge = Email.find.search(indexQuery);

		SearchResponse searchResponse = new SearchResponse();
		searchResponse.emails = new ArrayList<Application.SearchResponse.Email>();
		for (Email e : allAndFacetAge.results) {
			double length = e.description.length() * 1.7 / (1024.00);
			String extract = "";// e.searchHit.getScore();
			if (e.searchHit.getHighlightFields() != null
					&& e.searchHit.getHighlightFields().get("description") != null) {
				for (Text _t : e.searchHit.getHighlightFields()
						.get("description").fragments()) {
					extract += _t.string()
							+ " <strong><font class='customFont'>&bull;&bull;&bull;</font></strong> ";
				}

			}

			if (e.searchHit.getHighlightFields() != null
					&& e.searchHit.getHighlightFields().get(
							"nestedHtml.description") != null) {
				for (Text _t : e.searchHit.getHighlightFields()
						.get("nestedHtml.description").fragments()) {
					extract += _t.string()
							+ " <strong><font class='customFont'>&bull;&bull;&bull;</font></strong> ";
				}

			}

			if (e.subject.length() > 60) {
				e.subject = e.subject.substring(0, e.subject.length() > 60 ? 60
						: e.subject.length())
						+ " ...";
			}

			if (extract.isEmpty()) {
				extract = e.description.substring(
						0,
						e.description.length() > 1800 ? 1800 : e.description
								.length())
						+ " ...";
			}
			boolean flag = false;
			if (e.status != null && e.status == 1) {
				flag = true;
			}
			searchResponse.emails
					.add(new Application.SearchResponse.Email(e.subject,
							e.domain, e.sentDate, e.sendersEmail, extract,
							e.mail_objectId, e.getId(), length, extract
									.length(), flag));
		}
		searchResponse.saveSearchSets.addAll(SaveSearchSet.find.all());
		searchResponse.noOFPages = (int) Math.ceil((double) allAndFacetAge
				.getTotalCount() / 10);

		// For Domain Counts
		searchResponse.domainCounts = new ArrayList<Application.SearchResponse.Domain>();
		for (TermEntry te : ((InternalStringTermsFacet) allAndFacetAge.facets
				.facet("domain")).getEntries()) {
			searchResponse.domainCounts.add(new Domain(te.getTerm().string(),
					te.getCount()));
		}

		return ok(Json.toJson(searchResponse));

	}

	@SuppressWarnings("rawtypes")
	public static Result saveEmailSearchSet() {
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class)
				.bindFromRequest();
		String name = searchFilter.data().get("saveSearchName");
		SaveSearchSet saveSearchSet = SaveSearchSet.getSaveSearchSetName(name);
		if (saveSearchSet != null) {
			return ok(Json
					.toJson("name is already in system please modify it."));
		}
		SearchResponse searchResponse = new SearchResponse();
		saveSearchSet = new SaveSearchSet();
		saveSearchSet.name = name;
		saveSearchSet.querySql = request().uri().toString()
				.replace(request().path().toString() + "?", "");
		saveSearchSet.save();
		List list = new ArrayList<SaveSearchSet>();
		list = SaveSearchSet.find.all();
		searchResponse.saveSearchSets.addAll(list);
		return ok(Json.toJson(searchResponse));
	}

	public static Result showPopUpModal() throws MessagingException,
			IOException {
		SearchResponse searchResponse = new SearchResponse();
		Document doc = null;
		Form<SearchFilter> searchFilter = DynamicForm.form(SearchFilter.class)
				.bindFromRequest();
		Long id = Long.valueOf(searchFilter.data().get("popUpId"));
		System.out.println("url=" + id);
		MailObjectModel mailObjectModel = MailObjectModel
				.findMailObjectModelById(id);
		File file = new File(mailObjectModel.mailPath);
		Session session = Session.getDefaultInstance(new Properties());
		InputStream inMsg = new FileInputStream(file);
		Message msg = new MimeMessage(session, inMsg);
		// System.out.println("msg.getContentType();"+msg.getContent());

		String contentType = msg.getContentType().substring(0,
				msg.getContentType().indexOf('/'));
		MimeMultipart obj = null;
		if ("multipart".equals(contentType)) {
			obj = (MimeMultipart) msg.getContent();
			if (obj.getCount() == 0) {
				doc = Jsoup.parse(obj.getBodyPart(0).getContent().toString(),
						"ISO-8859-1");
			} else
				for (int k = 0; k < obj.getCount(); k++) {
					doc = Jsoup.parse(obj.getBodyPart(k).getContent()
							.toString(), "ISO-8859-1");
				}
		} else {
			doc = Jsoup.parse(msg.getContent().toString(), "ISO-8859-1");
		}
		searchResponse.htmlToShowMailPopUp = doc.toString();
		return ok(Json.toJson(searchResponse));
	}

	public static Result showPopUpImages() throws MessagingException,
			IOException {
		SearchResponse searchResponse = new SearchResponse();
		return ok(Json.toJson(searchResponse));
	}

	public static Result showPopUpLinks() throws MessagingException,
			IOException {
		SearchResponse searchResponse = new SearchResponse();
		return ok(Json.toJson(searchResponse));
	}

	public static class SearchFilter {
		public String from;
		public String to;
		public String cntKeyWord;
		public String subKeyWord;
		public String levelOnekeyWord;
		public int page;
		public int rowCount;
		public String[] domain;
		public String domainChecked;
		public int popUpId;
	}

	public static class BlackListedResponse {
		public List<DomainBL> domainList = new ArrayList<DomainBL>();
		public List<EmailBL> emailList = new ArrayList<EmailBL>();
		public List<KeywordBL> keywordList = new ArrayList<KeywordBL>();
	}

	public static class SearchResponse {

		public List<Email> emails = new ArrayList<Email>();
		public List<Domain> domainCounts = new ArrayList<Domain>();
		public List<SaveSearchSet> saveSearchSets = new ArrayList<SaveSearchSet>();
		public long noOFPages;
		public String htmlToShowMailPopUp;

		public static class Email {
			public Email() {
			}

			public Email(String subject, String domain, Date date,
					String sendersEmail, String extract, Long id,
					String indexId, Double length, int extractLength,
					boolean isHidden) {
				super();
				this.subject = subject;
				this.domain = domain;
				this.date = date;
				this.sendersEmail = sendersEmail;
				this.extract = extract;
				this.id = id;
				this.indexId = indexId;
				this.length = length;
				this.extractLength = extractLength;
				this.isHidden = isHidden;
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
			public boolean isHidden;
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

	public static Date convertStringToDate(String dateString) {
		dateString = dateString.substring(1, dateString.lastIndexOf('-') + 3);
		Date date = null;
		DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		try {
			date = sdf.parse(dateString);
		} catch (Exception ex) {
			System.out.println(ex);
		}
		return date;
	}

	@Transactional
	public static Result getCoverImageByID(long id) {
		MailObjectModel mailObjectModel = MailObjectModel
				.findMailObjectModelById(id);
		if (mailObjectModel != null && mailObjectModel.mailPath != null) {
			double factor;
			String filePath = mailObjectModel.mailPath.replace(".eml",
					"_thumbnail.png");
			File f = new File(filePath);
			if (!f.exists()) {
				try {
					filePath = mailObjectModel.mailPath.replace(".eml", ".png");
					File file = new File(filePath);
					BufferedImage originalImage = ImageIO.read(file);

					if (originalImage.getWidth() > IMGWIDTH) {
						factor = IMGWIDTH * 1.0 / originalImage.getWidth();
						Thumbnails.of(originalImage).scale(factor).toFile(f);
					} else {
						return ok(file);
					}
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				try {
					return ok(f);
				} catch (Exception e) {
				}
			} else {
				return ok(f);
			}
		}
		return ok("no image set");
	}

	@Transactional
	public static Result getlinkImageByID(long id) {
		List<Links> links = Links.find.where().eq("mail_id.id", id)
				.ne("processedUrl", null).ne("processedUrl", "").findList();
		List<UrlMapVM> urlMap = new ArrayList<UrlMapVM>();

		for (Links link : links) {
			UrlMapVM vm = new UrlMapVM(link.getProcessedUrl(), link
					.getProcessedUrl().length());
			urlMap.add(vm);
		}
		return ok(Json.toJson(urlMap));
	}

	public static Result addDomainToBL(String domainName) {
		// System.out.println("in addDomain DomainName="+domainName);
		DomainBL dom = DomainBL.findDomainblObjectByDomainName(domainName);
		BlackListedResponse blackListedResponse = new BlackListedResponse();
		if (dom == null) {
			DomainBL domain = new DomainBL();
			domain.domain = domainName;
			domain.save();
			dom = DomainBL.findDomainblObjectByDomainName(domainName);
			blackListedResponse.domainList.add(dom);
			return ok(Json.toJson(blackListedResponse));
		}
		blackListedResponse.domainList.add(null);
		return ok(Json.toJson(blackListedResponse));
	}

	public static Result getBlackListed() {
		BlackListedResponse blackListedResponse = new BlackListedResponse();
		blackListedResponse.domainList = DomainBL.findDomainblAllObject();
		blackListedResponse.emailList = EmailBL.findEmailblAllObject();
		blackListedResponse.keywordList = KeywordBL.findKeywordblAllObject();
		/*
		 * Iterator itr=li.iterator(); while(itr.hasNext()) {
		 * System.out.println(((DomainBL)itr.next()).domain); }
		 */
		return ok(Json.toJson(blackListedResponse));
	}

	public static Result addEmailToBL(String emailAddress) {
		// System.out.println("in addDomain DomainName="+domainName);
		EmailBL dom = EmailBL.findEmailblObjectByEmailAddress(emailAddress);
		BlackListedResponse blackListedResponse = new BlackListedResponse();
		if (dom == null) {
			EmailBL emailbl = new EmailBL();
			emailbl.email = emailAddress;
			emailbl.save();
			dom = EmailBL.findEmailblObjectByEmailAddress(emailAddress);
			blackListedResponse.emailList.add(dom);
			return ok(Json.toJson(blackListedResponse));
		}
		blackListedResponse.emailList.add(null);
		return ok(Json.toJson(blackListedResponse));
	}

	public static Result addKeywordToBL(String keyword) {
		// System.out.println("in addDomain DomainName="+domainName);
		KeywordBL key = KeywordBL.findKeywordblObjectByKeyword(keyword);
		BlackListedResponse blackListedResponse = new BlackListedResponse();
		if (key == null) {
			KeywordBL keywordbl = new KeywordBL();
			keywordbl.keyword = keyword;
			keywordbl.save();
			key = KeywordBL.findKeywordblObjectByKeyword(keyword);
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

	public static Result removeEmailData(long id, String indexId) {
		Email e = Email.find.byId(indexId);
		if (e != null) {
			e.delete();
			// Links.deleteLinksByMailObjectId(id);
			MailObjectModel.deleteMailObjectById(id);
			return ok("record deleted successfully");
		}
		return ok("no record found");
	}

	public static Result getMonthChart(String fromMonth, String toMonth) {
		int totalCount = MailObjectModel.getTotalCount(fromMonth, toMonth);
		List<SqlRow> list = MailObjectModel.getMonthChartData(fromMonth,
				toMonth);
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		for (SqlRow sr : list) {
			List<Object> _item = new ArrayList<Object>();
			_item.add(sr.getString("domain"));
			_item.add(sr.getInteger("count") * 100.00 / totalCount);
			responseList.add(_item);
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}

	public static Result getYearChart(String year) {
		int totalCount = MailObjectModel.getTotalCountForYear(year);
		List<SqlRow> list = MailObjectModel.getYearChartData(year);
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		for (SqlRow sr : list) {
			List<Object> _item = new ArrayList<Object>();
			_item.add(sr.getString("domain"));
			_item.add(sr.getInteger("count") * 100.00 / totalCount);
			responseList.add(_item);
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}

	public static Result getAllChart() {
		int totalCount = MailObjectModel.getTotalCountForAll();
		List<SqlRow> list = MailObjectModel.getAllChartData();
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		for (SqlRow sr : list) {
			List<Object> _item = new ArrayList<Object>();
			_item.add(sr.getString("domain"));
			_item.add(sr.getInteger("count") * 100.00 / totalCount);
			responseList.add(_item);
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}

	public static Result getAllChart1(int count) {
		int totalCount = MailObjectModel.getTotalCountForAll();
		List<SqlRow> list = MailObjectModel.getAllChartData();
		Collections.reverse(list);
		System.out.println("hiiiiiii" + (list.size() - 1));
		List<List<Object>> responseList = new ArrayList<List<Object>>();

		// System.out.println(list.get(index));
		try {
			for (int i = count; i < count + 30; i++) {

				List<Object> _item = new ArrayList<Object>();
				_item.add(list.get(i).getString("domain"));
				_item.add(list.get(i).getInteger("count"));
				responseList.add(_item);

			}
		} catch (IndexOutOfBoundsException e) {

			for (int i = count; i > list.size() - 1; i++) {

				List<Object> _item = new ArrayList<Object>();
				_item.add(list.get(i).getString("domain"));
				_item.add(list.get(i).getInteger("count"));
				responseList.add(_item);

			}
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}

	public static Result getAllChartprev(int prev) {

		int totalCount = MailObjectModel.getTotalCountForAll();
		List<SqlRow> list = MailObjectModel.getAllChartData();
		Collections.reverse(list);
		System.out.println("hiiiiiii" + (list.size() - 1));
		List<List<Object>> responseList = new ArrayList<List<Object>>();
		/*
		 * if(count>list.size()-1){ count=list.size(); }
		 */
		// System.out.println(list.get(index));
		try {
			for (int i = prev - 30; i < prev; i++) {

				List<Object> _item = new ArrayList<Object>();
				_item.add(list.get(i).getString("domain"));
				_item.add(list.get(i).getInteger("count"));
				responseList.add(_item);

			}
		} catch (IndexOutOfBoundsException e) {

			for (int i = 0; i < 30; i++) {

				List<Object> _item = new ArrayList<Object>();
				_item.add(list.get(i).getString("domain"));
				_item.add(list.get(i).getInteger("count"));
				responseList.add(_item);

			}
		}
		System.out.println(Json.toJson(responseList));
		return ok(Json.toJson(responseList));
	}

	public static List<Date> stringToDate(String fromMonth, String toMonth) {
		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");
		Date toDate;
		Date fromDate;
		try {
			fromMonth = "01-" + fromMonth;
			fromDate = formatter.parse(fromMonth);
			String toArr[] = toMonth.split("-");
			int toMonthInt = Integer.parseInt(toArr[0]);
			if (toMonthInt == 1 || toMonthInt == 3 || toMonthInt == 5
					|| toMonthInt == 7 || toMonthInt == 8 || toMonthInt == 10
					|| toMonthInt == 12) {
				toMonth = "31-" + toMonth;
				toDate = formatter.parse(toMonth);
			} else if (toMonthInt == 4 || toMonthInt == 6 || toMonthInt == 9
					|| toMonthInt == 11) {
				toMonth = "30-" + toMonth;
				toDate = formatter.parse(toMonth);
			} else {
				int year = Integer.parseInt(toArr[1]);
				if (year % 400 == 0
						|| (year % 4 == 0 && year % 100 != 0 && year % 200 != 0 && year % 300 != 0)) {
					toMonth = "29-" + toMonth;
					toDate = formatter.parse(toMonth);
				} else {
					toMonth = "28-" + toMonth;
					toDate = formatter.parse(toMonth);
				}
			}
			List<Date> dateList = new ArrayList<Date>();
			dateList.add(fromDate);
			dateList.add(toDate);
			return dateList;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static Result statistic(String fromMonth, String toMonth) {
		// / TODO: Ahemd
		List<StatsVM_1> statsVM_1s = new ArrayList<Application.StatsVM_1>();
		List<Date> dateList = stringToDate(fromMonth, toMonth);
		if (dateList != null) {
			System.out.println("fromDate:" + fromMonth + " toDate:" + toMonth);
			List<DomainBL> domainBLs = DomainBL.findDomainblAllObject();
			List<MailObjectModel> moms = MailObjectModel.find.where()
					.between("sentDate", dateList.get(0), dateList.get(1))
					.eq("status", "1").orderBy("domain").findList();
			String prevDomain = "";

			StatsVM_1 vm_1 = null;
			int total = moms.size();
			System.out.println("total Size :" + moms.size());
			for (MailObjectModel mom : moms) {
				if (!prevDomain.equalsIgnoreCase(mom.domain)) {
					vm_1 = new StatsVM_1();
					prevDomain = mom.domain;
					vm_1.domain = mom.domain;
					for (DomainBL domain : domainBLs) {
						if (domain.domain.equalsIgnoreCase(mom.domain)) {
							vm_1.domainStatus = true;
							vm_1.blackDomainListedId = domain.id;
						}
					}
					statsVM_1s.add(vm_1);
				}
				vm_1.add(mom, total);
			}
		}
		/*
		 * } catch (ParseException e) { e.printStackTrace(); };
		 */
		return ok(Json.toJson(statsVM_1s));
	}

	public static class StatsVM_1 {
		public int total;
		public String domain;
		public int count = 0;
		public double percentage;
		public boolean domainStatus = false;
		public Long blackDomainListedId;
		public List<MailsIDToDisplay> mails;

		public void add(MailObjectModel mom, int total) {
			this.total = total;
			if (mails == null) {
				mails = new ArrayList<MailsIDToDisplay>();
			}
			System.out
					.println(mom.id + " " + mom.mailName + " " + mom.sentDate);
			mails.add(new MailsIDToDisplay(mom.id, mom.mailName, mom.sentDate));
			count++;
			percentage = count * 100.00 / total;
			System.out.println("count=" + count + " percentage =" + percentage);
		}

	}

	public static Result downloadPdf(long id) {
		Document doc = null;
		MailObjectModel mailObjectModel = MailObjectModel
				.findMailObjectModelById(id);
		File file = new File(mailObjectModel.mailPath);
		Session session = Session.getDefaultInstance(new Properties());
		InputStream inMsg;
		try {
			inMsg = new FileInputStream(file);

			Message msg = new MimeMessage(session, inMsg);
			String contentType = msg.getContentType().substring(0,
					msg.getContentType().indexOf('/'));
			MimeMultipart obj = null;
			if ("multipart".equals(contentType)) {
				obj = (MimeMultipart) msg.getContent();
				if (obj.getCount() == 0) {
					doc = Jsoup.parse(obj.getBodyPart(0).getContent()
							.toString(), "ISO-8859-1");
				} else
					for (int k = 0; k < obj.getCount(); k++) {
						doc = Jsoup.parse(obj.getBodyPart(k).getContent()
								.toString(), "ISO-8859-1");
					}
			} else {
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
		String mailFolder = Play.application().configuration()
				.getString("mail.storage.path");
		Double mailFolderSize = (FileUtils
				.sizeOfDirectory(new File(mailFolder)) / (1024.00 * 1024));
		String elasticFolder = Play.application().configuration()
				.getString("mail.elastic.path");
		Double elasticFolderSize = (FileUtils.sizeOfDirectory(new File(
				elasticFolder)) / (1024.00 * 1024));
		List<Object> list = new ArrayList<Object>();
		list.add(MailObjectModel.getDataSize());
		list.add(mailFolderSize + elasticFolderSize);
		return ok(Json.toJson(list));
	}

	// List start
	public static Result loadLists() {
		SubscriptionListVM result = new SubscriptionListVM();
		List<DomainObject> unAssignedList = DomainObject.findUnAssignedNames();
		List<DomainObject> assignedList = DomainObject.findAssignedNames();
		List<DomainObject> assignedChildList = DomainObject
				.findAssignedChildNames();
		result.totalUnAssignedNumber = unAssignedList.size();
		result.totalParentNumber = assignedList.size();
		result.totalChildNumber = assignedChildList.size();
		result.unAssignedList = new ArrayList<SubscriptionVM>();
		for (DomainObject ds : unAssignedList) {
			result.unAssignedList.add(new SubscriptionVM(ds.id, ds.name,
					ds.color));
		}
		result.assignedList = new ArrayList<SubscriptionVM>();
		for (DomainObject ds : assignedList) {
			result.assignedList
					.add(new SubscriptionVM(ds.id, ds.name, DomainObject
							.findAssignedChildNames(ds.id).size(), ds.color));
		}
		result.assignedChildList = new ArrayList<SubscriptionVM>();
		for (DomainObject ds : assignedChildList) {
			result.assignedChildList.add(new SubscriptionVM(ds.id, ds.name,
					ds.color));
		}
		return ok(Json.toJson(result));
	}

	public static Result loadChildList(Long id) {
		SubscriptionListVM result = new SubscriptionListVM();
		List<DomainObject> assignedChildList = DomainObject
				.findAssignedChildNames(id);
		result.assignedChildList = new ArrayList<SubscriptionVM>();
		for (DomainObject ds : assignedChildList) {
			result.assignedChildList.add(new SubscriptionVM(ds.id, ds.name,
					ds.color));
		}
		return ok(Json.toJson(result));
	}

	public static class SubscriptionVM {
		public Long id;
		public String title;
		public Boolean drag = true;
		public Integer childNumber;
		public String color;

		public SubscriptionVM(Long id, String title, String color) {
			this.title = title;
			this.id = id;
			this.color = color;
		}

		public SubscriptionVM(Long id, String title, Integer childNumber,
				String color) {
			this.title = title;
			this.id = id;
			this.childNumber = childNumber;
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

	public static Result addChildSubscription(Long cid, Long pid) {
		DomainObject.addChildSubscription(cid, pid);
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

	public static class BasicInfoVM {
		public String channel_name;
		public String publisher;
		public String publisher_url;
		public String media_kit_url;
		public String username;
		public String password;
		public String last_renewed;
		public String history;
		public String notes;
		public String subscriber;
	}

	public static Result savesaveRenewedDate(String col_name, String record,
			String channel_name) {
		System.out.println("#@@@@@@@===" + record);

		String info = BasicInfo.saveLastRenewedRecord(col_name, record,
				channel_name);
		return ok(Json.toJson(""));
	}

	public static Result saveMailInfoInBasicInfo(String col_name,
			String record, String channel_name) {
		System.out.println("#@@@@@@@===" + col_name);
		String info = BasicInfo.saveRecord(col_name, record, channel_name);
		return ok(Json.toJson(info));
	}

	public static Result getMailInfoFromBasicInfo(String data) {
		SqlRow info = BasicInfo.findRecordByName(data);

		BasicInfoVM bInfo = new BasicInfoVM();
		if (info != null) {
			bInfo.channel_name = info.getString("channel_name");
			bInfo.publisher = info.getString("publisher");
			bInfo.publisher_url = info.getString("publisher_url");
			bInfo.media_kit_url = info.getString("media_kit_url");
			bInfo.username = info.getString("username");
			bInfo.password = info.getString("password");
			bInfo.last_renewed = info.getString("last_renewed");
			bInfo.history = info.getString("history");
			bInfo.notes = info.getString("notes");
			bInfo.subscriber = info.getString("Subscriber");
			return ok(Json.toJson(bInfo));
		} else {
			return null;
		}

	}

	public static Result getWordCloudById(Long id) {
		MailObjectModel mailObject = MailObjectModel
				.findMailObjectModelById(id);
		String filePath = mailObject.mailPath.replace(".eml", "_cloud.svg");
		File f = null;// = new File(filePath);
		// PApplet pApplet = new PApplet();
		// pApplet.setSize(800, 600);
		/*
		 * if(!f.exists()) {
		 */
		try {
			new WordCram()
					.toSvg(filePath, 800, 600)
					.sizedByWeight(12, 120)
					.withPlacer(Placers.horizLine())
					.withFont("Futura-CondensedExtraBold")
					.withColorer(
							Colorers.pickFrom(0x20Fd30, 0x40a4f4, 0x904060,
									0xf08030, 0x180790, 0x1290fa, 0x614090,
									0xf160fc, 0xf180dc, 0x20f0aa, 0x2920aa,
									0xf460d0))
					.fromHtmlFile(mailObject.mailPath).drawAll();
			f = new File(filePath);
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage());
			// e.printStackTrace();
		}
		/* } */
		// System.out.println("done");
		return ok(f);
	}

	public static Result deleteMailById(Long id) {
		MailObjectModel model = MailObjectModel.getMailObjetcById(id);
		Email e = Email.getEmailByMailObjectId(model.id);
		if (e != null) {
			e.delete();
		}
		Links.deleteLinksByMailObjectId(model.id);
		model.delete();
		return ok();
	}

	// List end
	public static Result hideMailByIndexId(Long id) {
		System.out.println("id:" + id);
		MailObjectModel model = MailObjectModel.getMailObjetcById(id);
		Email e = Email.getEmailByMailObjectId(model.id);
		model.setStatus(2);
		model.update();
		e.status = 1;
		e.index();
		return ok();
	}

	public static Result showMailByIndexId(Long id) {
		System.out.println("id:" + id);
		MailObjectModel model = MailObjectModel.getMailObjetcById(id);
		Email e = Email.getEmailByMailObjectId(model.id);
		model.setStatus(1);
		model.update();
		e.status = 0;
		e.index();
		return ok();
	}

	public static Result feedback() {
		String host = "pop.gmail.com";
		final String user = "admin@lab104.net";
		final String password = "Jagbir104";
		String to = "mark@kadekraus.com";

		// Sender's email ID needs to be mentioned
		String from = "web@gmail.com";
		JsonNode jn = request().body().asJson();
		String subject = jn.get("subject").asText();
		String messagess = jn.get("message").asText();
		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.socketFactory.port", "465");
		props.put("mail.smtp.socketFactory.class",
				"javax.net.ssl.SSLSocketFactory");
		Session session = Session.getInstance(props,
				new javax.mail.Authenticator() {
					protected PasswordAuthentication getPasswordAuthentication() {
						return new PasswordAuthentication(user, password);
					}
				});

		// Compose the message
		try {
			MimeMessage message = new MimeMessage(session);
			message.setFrom(new InternetAddress(user));
			message.addRecipient(Message.RecipientType.TO, new InternetAddress(
					to));
			message.setSubject(subject);
			message.setText(messagess);

			// send the message
			Transport.send(message);
		} catch (Exception e) {
			System.out.println("Mail sending failed!");
		}
		return ok();
	}

	
	public static Result mailVariations(int percent) {
		Date d = new Date();
		System.out.println(d);
		Http.Context context = Http.Context.current();
		String key = context.session().get("isAdmin");
		boolean isAdmin  = false;
		if(key.equals("Y")) {
			isAdmin  = true;
		}
		Calendar cal = Calendar.getInstance();  
	    int year = cal.get(cal.YEAR);  
	    int month = cal.get(cal.MONTH)+1; //zero-based  
	    int preMonth = cal.get(cal.MONTH);
	    String date = year+"-"+month+"-"+"01";
	    String preDate = year+"-"+preMonth+"-"+"01";
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("select distinct domain from mail_object_model");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		for(SqlRow sr:resultList){
			MailsToDisplay mailsToDisplay= new MailsToDisplay();
			mailsToDisplay.domainName= sr.getString("domain");
			SqlRow currentMonthCount= MailObjectModel.findMailObjectByDomainNameAndDate(mailsToDisplay.domainName, date);
			int currentCount = currentMonthCount.getInteger("count");
			SqlRow preMonthCount= MailObjectModel.findMailObjectByDomainNameAndDate(mailsToDisplay.domainName, preDate);
			int preCount = preMonthCount.getInteger("count");
			float lossPercent;
			if(preCount > currentCount){
				lossPercent = 100-(currentCount*100/preCount);
				if(lossPercent > percent ){
						String to = "mindnervestech@gmail.com";

						final String from = "mindnervesdemo@gmail.com";
						String host = "smtp.gmail.com";
						final String password = "mindnervesadmin";
						String port = "587";
						Properties properties = new Properties();
						properties.put("mail.smtp.starttls.enable", "true");
						properties.put("mail.smtp.host", host);
						properties.put("mail.smtp.auth", "true"); // password
						properties.put("mail.smtp.port", port);
						Session session = Session.getInstance(properties,
						new javax.mail.Authenticator() {
						protected PasswordAuthentication getPasswordAuthentication() {
							return new PasswordAuthentication(from,
									password);
							}
						});
						try {
							Message message = new MimeMessage(session);
							message.setFrom(new InternetAddress(from));
							message.addRecipients(Message.RecipientType.TO,
									InternetAddress.parse(to));
							message.setSubject("MAIL VARIATION");
							message.setText(String.valueOf(lossPercent));
							Transport.send(message);
							System.out.println("Sent message successfully 21....");
						} catch (Exception mex) {
							mex.printStackTrace();
						}
					}
			}else{
				lossPercent = 100-(preCount*100/currentCount);
			}
			
		}
		return ok();
	}
	public static MailVariastion _mailVariations(int percent) {
		MailVariastion mailVariastion = new MailVariastion();
		List<VariationDetails> variationDetails = new ArrayList<VariationDetails>();
		Date d = new Date();
		Calendar cal = Calendar.getInstance();  
	    int year = cal.get(cal.YEAR);  
	    int month = cal.get(cal.MONTH)+1; //zero-based  
	    int preMonth = cal.get(cal.MONTH);
	    String date = year+"-"+month+"-"+"01";
	    String preDate = year+"-"+preMonth+"-"+"01";
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("select distinct domain from mail_object_model");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		
		//List<String> 
		for(SqlRow sr:resultList){
			MailsToDisplay mailsToDisplay= new MailsToDisplay();
			mailsToDisplay.domainName= sr.getString("domain");
			SqlRow currentMonthCount= MailObjectModel.findMailObjectByDomainNameAndDate(mailsToDisplay.domainName, date);
			int currentCount = currentMonthCount.getInteger("count");
			SqlRow preMonthCount= MailObjectModel.findMailObjectByDomainNameAndDate(mailsToDisplay.domainName, preDate);
			int preCount = preMonthCount.getInteger("count");
			float lossPercent;
			VariationDetails variationDetail = new VariationDetails();
			variationDetail.domain = mailsToDisplay.domainName;
			variationDetail.lastMonthCount = preCount;
			variationDetail.currentMonthCount = currentCount;
			
			if(preCount > currentCount){
				lossPercent = 100-(currentCount*100/preCount);
				if(lossPercent > 20 ){
						//message.setText(String.valueOf(lossPercent));
							variationDetail.lossPercent = lossPercent;
					}
			}else{
				lossPercent = 100-(preCount*100/currentCount);
				variationDetail.lossPercent = lossPercent;
			}
			
			variationDetails.add(variationDetail);
		}
		mailVariastion.variationDetails = variationDetails;
		return mailVariastion;
	}
	public static class MailVariastion{
		public List<VariationDetails> variationDetails;
	}
	
	public static class VariationDetails{
		public String domain;
		public int lastMonthCount;
		public int currentMonthCount;
		public float lossPercent;
	}
	public static class DailyReport {
		public String domain;
		public Integer count;
	}

	public static class MonthReport {
		public List<DailyReport> dailyReports;
	}

	public static class MonthUnprocessReport{
		public List<DailyReport> dailyReports;
	}
	public static class TodayReport {
		public List<DailyReport> dailyReports;
	}

	public static class UnprocessTodayReport {
		public List<DailyReport> dailyReports;
	}
	
	public static class TotalUnprocessReport {
		public List<DailyReport> dailyReports;
	}

	public static class DomainList{
		public List<DailyReport> dailyReports;
	}
	public static class RecentDomainList{
		public List<DailyReport> dailyReports;
	}
	public static class HostDomainList{
		public List<DailyReport> dailyReports;
	}
	public static class AllDailyReport {
		public MonthReport monthReport;
		public TodayReport todayReport;
		public MonthUnprocessReport monthUnprocessReport;
		public TotalUnprocessReport totalUnprocessReport;
		public DomainList domainList;
		public RecentDomainList recentDomainList;
		public HostDomainList hostDomainList;
		public MailVariastion mailVariastion;
	}

	final static String pdfPath = Play.application().configuration()
			.getString("mail.storage.pdfPath");

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		System.out.println("new date = " + new Date());
		MailVariastion mailVariastion = _mailVariations(20);
		
		HostDomainList hostDomainList = new HostDomainList();
		java.sql.Connection conn = null;
    	try {
			Class.forName("com.mysql.jdbc.Driver");
			conn = null;
			conn = DriverManager.getConnection("jdbc:mysql://enter-agora.com:3307/agora_scrape","agora_jagbir", "gane3Ecehema6a");
			java.sql.Statement smt = conn.createStatement();
			ResultSet userRS =  smt.executeQuery("select distinct(path) as path from hosts");
			//List<UserRole>  userRole = new ArrayList<UserRole>();
			//List<UserPermission> userPermission = new ArrayList<UserPermission>();
			
			
			List<DailyReport> _dailyMonthReport = new ArrayList<DailyReport>();
			while(userRS.next()) {
				
				URL aURL = new URL(userRS.getString("path"));
				DailyReport report = new DailyReport();
				report.domain = aURL.getAuthority();
				//report.count = row.getInteger("count");
				_dailyMonthReport.add(report);
			}
			hostDomainList.dailyReports = _dailyMonthReport;
			
    	}catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		catch (SQLException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	finally {
    		if(conn!=null) {
    			try {
					conn.close();
				} catch (SQLException e) {
				}
    		}
    	}
		List<SqlRow> totalUnprocess = MailObjectModel.getTotalUnprocessReportCount();
		List<SqlRow> thirtydays = MailObjectModel.getLastThirtyDaysMailRecord();
		List<SqlRow> thirtydaysUnprocessed = MailObjectModel.getLastThirtyDaysUnprocessedMailRecord();
		List<SqlRow> model = MailObjectModel.getTodaysMails();
		List<SqlRow> distinctDomainList = MailObjectModel.getAllDistinctDomains();
		List<SqlRow> recentDomainList = MailObjectModel.getRecentlyAddedDomains();

		MonthReport monthReport = new MonthReport();
		List<DailyReport> dailyMonthReport = new ArrayList<DailyReport>();
		for (SqlRow row : thirtydays) {
			DailyReport report = new DailyReport();
			report.domain = row.getString("domain");
			report.count = row.getInteger("count");
			dailyMonthReport.add(report);
		}
		monthReport.dailyReports = dailyMonthReport;
		
		dailyMonthReport = new ArrayList<DailyReport>();
		TodayReport todayReport = new TodayReport();
		for (SqlRow row : model) {
			DailyReport report = new DailyReport();
			report.domain = row.getString("domain");
			report.count = row.getInteger("count");
			dailyMonthReport.add(report);
		}
		todayReport.dailyReports = dailyMonthReport;
		
		MonthUnprocessReport monthUnprocessReport = new MonthUnprocessReport();
		dailyMonthReport = new ArrayList<DailyReport>();
		for (SqlRow row : thirtydaysUnprocessed) {
			DailyReport report = new DailyReport();
			report.count = row.getInteger("count");
			dailyMonthReport.add(report);
		}
		monthUnprocessReport.dailyReports = dailyMonthReport;
		
		TotalUnprocessReport totalUnprocessReport = new TotalUnprocessReport();
		dailyMonthReport = new ArrayList<DailyReport>();
		for (SqlRow row : totalUnprocess) {
			DailyReport report = new DailyReport();
			report.count = row.getInteger("count");
			dailyMonthReport.add(report);
		}
		totalUnprocessReport.dailyReports = dailyMonthReport;
		
		DomainList domainList = new DomainList();
		dailyMonthReport = new ArrayList<DailyReport>();
		for (SqlRow row : distinctDomainList) {
			DailyReport report = new DailyReport();
			report.domain = row.getString("domain");
			dailyMonthReport.add(report);
		}
		domainList.dailyReports = dailyMonthReport;
		

		RecentDomainList recentDomainList2 = new RecentDomainList();
		dailyMonthReport = new ArrayList<DailyReport>();
		for (SqlRow row : recentDomainList) {
			DailyReport report = new DailyReport();
			report.domain = row.getString("domain");
			dailyMonthReport.add(report);
		}
		recentDomainList2.dailyReports = dailyMonthReport;
		

		for(DailyReport object1 : hostDomainList.dailyReports){
		   for(DailyReport object2: domainList.dailyReports){
		       if(object1.domain.equals(object2.domain)){
		           object2.domain = object2.domain+ " (Present in Agora-D)";
		       }
		    }
		}
		AllDailyReport report = new AllDailyReport();
		report.monthReport = monthReport;
		report.todayReport = todayReport;
		report.monthUnprocessReport = monthUnprocessReport;
		report.totalUnprocessReport = totalUnprocessReport;
		report.domainList = domainList;
		report.recentDomainList = recentDomainList2;
		report.hostDomainList = hostDomainList;
		report.mailVariastion = mailVariastion;
		DailyReportPDF.generateDailyReportPdf(report, pdfPath);

		
		try {

		GMailServer.sendMail("Report", "Daily Report Pdf",
					"admin@lab104.net", "Jagbir104",
					"mindnervestech@gmail.com", pdfPath);
			// GMailServer.sendMail();
			// EmailUtils.send();
		} catch (Exception e) {
			e.printStackTrace();
		}

		System.out.println("Email Sent Succesfully..." + new Date());
	}

	public static class DomainTreeVM {
		public Integer parent_id;
		public String title;
		public String label;
		public int id;
		public Integer domainId;
		public Boolean collapsed = true;
		public Integer count;
		public List<DomainTreeVM> children = new ArrayList<DomainTreeVM>();
	}
	public static Result loadAllDomainTree(){
		List<SqlRow> mailObjectModel = MailObjectModel.getAllDistinctDomains();
		List<SqlRow> objects = DomainObject.getAllDistinctDomains();
	
		if(!objects.isEmpty()){
			for(SqlRow object1 : mailObjectModel ){
				boolean flag = false;
			   for(SqlRow object2: objects ){
			       if(object1.getString("domain").equals(object2.getString("domain"))){
			           flag = true;
			       }
			    }
			   if(!flag){
		    	   saveNewDomainObject(object1.getString("domain"));
		       }
			}
		}else{
			for(SqlRow object1 : mailObjectModel ){
				saveNewDomainObject(object1.getString("domain"));
			}
		}
		List<DomainTreeVM> list = new ArrayList<DomainTreeVM>();
		List<SqlRow> models = DomainObject.getAllDomains();
		
		for (SqlRow sr : models) {
			DomainTreeVM _item = new DomainTreeVM();
			_item.title = sr.getString("name");
			_item.id = sr.getInteger("id");
			int count = MailObjectModel.getDomainMailCountByName(sr.getString("name"));
			_item.count = count;
			_item.label = sr.getString("name");
			_item.parent_id = sr.getInteger("parent_id");
			List<DomainObject> domainObject = DomainObject.getChildsOfDomain(sr.getInteger("id"));
			for(DomainObject object : domainObject){
				
				DomainTreeVM vm = new DomainTreeVM();
				vm.label = object.name;
				/*List<DomainObject> _domainObject = DomainObject.getChildsOfDomain(Integer.parseInt(object.id.toString()));
				for(DomainObject _object : domainObject){
					DomainTreeVM _vm = new DomainTreeVM();
					vm.label = _object.name;
					
				}*/
				
				vm.children = getChild(object);
				vm.parent_id = object.parentId;
				vm.title = object.name;
				vm.id = Integer.parseInt(object.id.toString());
				_item.children.add(vm);
			}
			list.add(_item);
		}
		return ok(Json.toJson(list));
	}
	
	public static void saveNewDomainObject(String domain){
	   DomainObject domainObject = new DomainObject();
 	   domainObject.name = domain;
 	   domainObject.save();
 	   domainObject.parentId = Integer.parseInt(domainObject.id.toString());
 	   domainObject.update();
	}
	private static List<DomainTreeVM> getChild(DomainObject object) {
		List<DomainTreeVM> childVms = new ArrayList<>();
		List<DomainObject> _domainObject = DomainObject.getChildsOfDomain(Integer.parseInt(object.id.toString()));
		for(DomainObject _object : _domainObject){
			DomainTreeVM _vm = new DomainTreeVM();
			_vm.label = _object.name;
			_vm.parent_id = _object.parentId;
			_vm.title = _object.name;
			_vm.id = Integer.parseInt(_object.id.toString());
			_vm.children = getChild(_object);
			childVms.add(_vm);
		}	
		return childVms;
	}

	
	public static class SubDomainVM{
		public int id;
		public String name;
		public int count;
		public String parent;
		public List<String> domainList;
		public List<ChildVm> allChildList;
		public List<ChildVm> child;
		
	}
	public static class ChildVm {
		public int id;
		public String label;
	}
	public static Result loadsubmedia(int id){
		
		DomainObject domainObject = DomainObject.getDomainById(id);
		SubDomainVM domainVM = new SubDomainVM();
		domainVM.id = Integer.parseInt(domainObject.id.toString());
		domainVM.name = domainObject.name;
		int count = MailObjectModel.getDomainMailCountByName(domainObject.name);
		domainVM.count = count;
		domainVM.parent = DomainObject.getDomainById(domainObject.parentId).name;
		List<SqlRow> domainObjects = DomainObject.getAllDomains();
		List<String> list = new ArrayList<String>();
		for(SqlRow row: domainObjects){
			list.add(row.getString("name"));
		}
		if(!list.contains(domainVM.parent)){
			list.add(domainVM.parent);
		}
		
		List<DomainObject> domainObjects2 = DomainObject.getChildsOfDomain(id);
		List<ChildVm> childVms = new ArrayList<>();
		for(DomainObject object : domainObjects2){
			ChildVm childVm = new ChildVm();
			childVm.id = Integer.parseInt(object.id.toString());
			//childVm.label = object.name;
			childVms.add(childVm);
		}
		domainVM.child = childVms;
		
		childVms = new ArrayList<>();
		List<DomainObject> _domainObjects = DomainObject.getAllChildList(id);
		for(DomainObject object : _domainObjects){
			ChildVm childVm = new ChildVm();
			childVm.id = Integer.parseInt(object.id.toString());
			childVm.label = object.name;
			childVms.add(childVm);
		}
		domainVM.allChildList = childVms;
		domainVM.domainList = list;
		return ok(Json.toJson(domainVM));
	}
	
	public static Result assignParent(){
		JsonNode json = request().body().asJson();
		SubDomainVM domainVM = Json.fromJson(json, SubDomainVM.class);
		DomainObject _domainObject = DomainObject.getDomainByName(domainVM.parent);
		DomainObject domainObject = DomainObject.getDomainById(domainVM.id);
		domainObject.parentId = Integer.parseInt(_domainObject.id.toString());
		domainObject.save();
		return ok();
	}
	public static class AssignedChild {
		public int parent;
		public List<ChildVm> childData;
	}
	public static Result assignChild(){
		JsonNode json = request().body().asJson();
		AssignedChild assignedChild = Json.fromJson(json, AssignedChild.class);
		DomainObject domainObject = DomainObject.getDomainById(assignedChild.parent);
		/*domainObject.parentId = assignedChild.parent;
		domainObject.save();*/
		List<DomainObject> domainObjects = DomainObject.getChildsOfDomain(Integer.parseInt(domainObject.id.toString()));
		for(DomainObject object : domainObjects){
			object.parentId = Integer.parseInt(object.id.toString());
			object.save();
		}
		for(ChildVm vm : assignedChild.childData){
			DomainObject object = DomainObject.getDomainById(vm.id);
				object.parentId = Integer.parseInt(domainObject.id.toString());
				object.save();
		}
		return ok();
	}
}
