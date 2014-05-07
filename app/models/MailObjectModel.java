package models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;

import org.codehaus.jackson.annotate.JsonIgnore;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
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
	
	@OneToMany
	public Links links;
	@OneToOne
	@JsonIgnore
	public Content content;
	
	public static Model.Finder<Long,MailObjectModel> find = new Model.Finder<Long,MailObjectModel>(Long.class, MailObjectModel.class);
	
	public static MailObjectModel findMailObjectModelById(long id) {
		return Ebean.find(MailObjectModel.class, id);
	}
	public static List<SqlRow> findMailObjectByDomainName(String domainName, String yearTab) {
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT mail_name,id,DATE_FORMAT(sent_date, '%m') AS month,sent_date "+ 
						" FROM mail_object_model where "+yearTab+" = date_format(sent_date,'%Y') AND domain = '"+domainName+
						"' GROUP BY DATE_FORMAT(sent_date, '%d-%m-%Y') ");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList;
	}
	
	public static List<String> getDisticntDate() {
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		List<String> strList = new ArrayList<String>();
		query.append("select distinct YEAR(sent_date) as year from mail_object_model");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		 for(SqlRow sr:resultList){
			 strList.add(sr.getString("year"));
		}
		strList.removeAll(Collections.singleton(null));  
		return strList;
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
