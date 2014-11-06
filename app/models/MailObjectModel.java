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
import com.avaje.ebean.SqlUpdate;
@Entity
public class MailObjectModel extends Model{
	@Id
	public Long id;
	
	public String mailName;
	
	public String mailPath;
	
	public int status= 0;
	
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
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
	
	public static void deleteMailObjectById(long id)
	{
		SqlUpdate list=Ebean.createSqlUpdate("UPDATE mail_object_model SET status=3 WHERE id="+id);
		list.execute();
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
	public static int getTotalCount(String fromMonth,String toMonth){
		String fromArr[]=fromMonth.split("-");
		int fromYear=Integer.parseInt(fromArr[1]);
		int fromMonthInt=Integer.parseInt(fromArr[0]);
		String toArr[]=toMonth.split("-");
		int toYear=Integer.parseInt(toArr[1]);
		int toMonthInt=Integer.parseInt(toArr[0]);
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		System.out.println("SELECT COUNT(*) as count FROM mail_object_model WHERE MONTH(sent_date) >="+fromMonthInt+" and MONTH(sent_date) <="+toMonthInt+" and YEAR(sent_date) >= "+fromYear +" and YEAR(sent_date) <="+toYear+" and (status=1 or status=0)");
		query.append("SELECT COUNT(*) as count FROM mail_object_model WHERE MONTH(sent_date) >="+fromMonthInt+" and MONTH(sent_date) <="+toMonthInt+" and YEAR(sent_date) >= "+fromYear +" and YEAR(sent_date) <="+toYear+" and (status=1 or status=0)");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList.get(0).getInteger("count");
	}
	public static int getTotalCountForYear(String year){
		int yearInt=Integer.parseInt(year);
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT COUNT(*) as count FROM mail_object_model WHERE YEAR(sent_date)="+yearInt +" and (status=1 or status=0)");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList.get(0).getInteger("count");
	}
	public static int getTotalCountForAll(){
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT COUNT(*) as count FROM mail_object_model WHERE status=1 or status=0");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList.get(0).getInteger("count");
	}
	
	public static List<MailObjectModel> getUnprocessedMailObjectIds() {
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT id,mail_path FROM mail_object_model WHERE id NOT IN (select distinct(mail_id_id) FROM links where mail_id_id > 10000 and mail_id_id < 16712)");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		List<MailObjectModel> result = new ArrayList<MailObjectModel>();
		for(SqlRow row:resultList) {
			MailObjectModel model = new MailObjectModel();
			model.id = row.getLong("id");
			model.mailPath = row.getString("mail_path");
			result.add(model);
		}
		return result;
	}
	
	public static List<SqlRow> getMonthChartData(String fromMonth,String toMonth)
	{
		String fromArr[]=fromMonth.split("-");
		int fromYear=Integer.parseInt(fromArr[1]);
		int fromMonthInt=Integer.parseInt(fromArr[0]);
		String toArr[]=toMonth.split("-");
		int toYear=Integer.parseInt(toArr[1]);
		int toMonthInt=Integer.parseInt(toArr[0]);
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		System.out.println("SELECT domain, COUNT(*) as count FROM mail_object_model WHERE MONTH(sent_date) >="+fromMonthInt+" and MONTH(sent_date) <="+toMonthInt+" and YEAR(sent_date) >= "+fromYear +" and YEAR(sent_date) <="+toYear+" and (status=1 or status=0) GROUP BY domain");
		query.append("SELECT domain, COUNT(*) as count FROM mail_object_model WHERE MONTH(sent_date) >="+fromMonthInt+" and MONTH(sent_date) <="+toMonthInt+" and YEAR(sent_date) >= "+fromYear +" and YEAR(sent_date) <="+toYear+" and (status=1 or status=0) GROUP BY domain");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList;
	}
	public static List<SqlRow> getYearChartData(String year)
	{
		int yearInt=Integer.parseInt(year);
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT domain, COUNT(*) as count FROM mail_object_model WHERE YEAR(sent_date)="+year +" and (status=1 or status=0) GROUP BY domain");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList;
	}
	public static List<SqlRow> getAllChartData()
	{
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT domain, COUNT(*) as count FROM mail_object_model WHERE status=1 or status=0 GROUP BY domain");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList;
	}
	public static Double getDataSize() {
		StringBuilder query = new StringBuilder();
		List<SqlRow> resultList = null;
		query.append("SELECT table_schema 'Data Base Name', SUM( data_length + index_length) / 1024 / 1024 'Data Base Size in MB' FROM information_schema.TABLES WHERE table_schema = 'mail' GROUP BY table_schema;");
		resultList = Ebean.createSqlQuery(query.toString()).findList();
		return resultList.get(0).getDouble("Data Base Size in MB");
	}
	
	public static List<MailObjectModel> getAllPersonalMailObjectModels() {
		return find.where().like("mailName", "Re:%").findList();
	}
	public static MailObjectModel getMailObjetcById(Long id2) {
		// TODO Auto-generated method stub
		return find.where().eq("id", id2).findUnique();
	}
}
