package indexing;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexUtils;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexMapping;
import com.github.cleverage.elasticsearch.annotations.IndexType;

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
	
	@JsonProperty("id")
	public Long mail_objectId;
	
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
