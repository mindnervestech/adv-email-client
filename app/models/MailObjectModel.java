package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;

import org.codehaus.jackson.annotate.JsonIgnore;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
@Entity
public class MailObjectModel extends Model{
	@Id
	public Long id;
	
	public String mailName;
	
	public String mailPath;
	
	public boolean status= false;
	
	public String domain;
	
	public String sendersEmail;
	public Date sentDate;
	public Date receivedDate;
	
	@OneToOne
	@JsonIgnore
	public Content content;
	
	public static Model.Finder<Long,MailObjectModel> find = new Model.Finder<Long,MailObjectModel>(Long.class, MailObjectModel.class);
	
	public static MailObjectModel findMailObjectModelById(long id) {
		return Ebean.find(MailObjectModel.class, id);
	}
	
	public String getContents() {
		if(content == null ) return "";
		return content.getDescription().substring(0,content.getDescription().length() > 1000 ? 1000 : content.getDescription().length()) +" ...";
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public void setContent(Content content) {
		this.content = content;
	}
	
}
