package indexing;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Query;

import org.codehaus.jackson.annotate.JsonProperty;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexResults;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

import elastic.MntIndexQuery;

@IndexType(name = "email")

public class Email extends Index {

	public static Finder<Email> find = new Finder<Email>(Email.class);
	
	@JsonProperty("contents")
	public String description;
	
	@JsonProperty("mailName")
	public String subject;
	
	public Date sentDate;
	
	@JsonProperty("domain")
	public String domain;
	public String sender;
	public String sendersEmail;
	
	public Integer status;
	@JsonProperty("id")
	public Long mail_objectId;
	
	public List<Links> nestedHtml = new ArrayList<Links>();
	
	public static Email getEmailByMailObjectId(Long id) {
		QueryBuilder qb = QueryBuilders.matchQuery("mail_objectId", id);
		MntIndexQuery<Email> indexQuery = new MntIndexQuery<Email>(Email.class);
		indexQuery.setBuilder(qb);
		List<Email> list = Email.find.search(indexQuery).getResults(); 
		if(list==null || list.isEmpty()) {
			return null;
		} else {
			return list.get(0);
		}
		 
	}
	
	@Override
	public Indexable fromIndex(Map map) {
		if (map == null) {
            return this;
        }
		this.description = (String) map.get("description");
		this.subject = (String) map.get("subject");
		this.domain = (String) map.get("domain");
		this.sendersEmail = (String) map.get("sendersEmail");
		this.mail_objectId = (Long) IndexUtils.convertValue(map.get("mail_objectId"), Long.class);
		this.sentDate = (Date) IndexUtils.convertValue(map.get("sentDate"), Date.class);
		this.nestedHtml = IndexUtils.getIndexables(map,"nestedHtml", Links.class);
		this.status = (Integer)IndexUtils.convertValue(map.get("status"), Integer.class);
		return this;
	}
	
	@Override
	public Map<String,Object> toIndex() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("description", description);
		map.put("subject", subject);
		map.put("domain", domain);
		map.put("sendersEmail", sendersEmail);
		map.put("sentDate", sentDate);
		map.put("mail_objectId", mail_objectId);
		map.put("nestedHtml", IndexUtils.toIndex(nestedHtml));
		map.put("status", status);
		return map;
	}
	
	@Override
    public String toString() {
        return "email: {" +
                "description:" + description  + "\n" +
                "subject:" + subject + "\n" +
                '}';
    }

}
