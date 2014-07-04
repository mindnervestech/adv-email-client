package models;


import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlUpdate;


import play.db.ebean.Model;

@Entity
public class Links extends Model {
	
	@Id
	public Long id;

	@Lob
	public String url; 
	
	@Lob
	public String processedUrl = null;
	
	@ManyToOne(cascade=CascadeType.ALL)
	public MailObjectModel mail_id;
	
	public int status=0;
	
	@Lob
	public String htmlcontent;
	
	public String path;
	
	public static Finder<Long, Links> find = new Finder<Long,Links>(Long.class, Links.class);
	
	public static void deleteLinksByMailObjectId(long id)
	{
		SqlUpdate list=Ebean.createSqlUpdate("DELETE FROM links WHERE mail_id_id="+id);
		list.execute();
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public MailObjectModel getMail_id() {
		return mail_id;
	}

	public void setMail_id(MailObjectModel mail_id) {
		this.mail_id = mail_id;
	}

	public int isStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getHtmlcontent() {
		return htmlcontent;
	}

	public void setHtmlcontent(String htmlcontent) {
		this.htmlcontent = htmlcontent;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getProcessedUrl() {
		return processedUrl;
	}

	public void setProcessedUrl(String processedUrl) {
		this.processedUrl = processedUrl;
	}
	
	
	
}
