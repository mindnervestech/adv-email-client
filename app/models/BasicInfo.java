package models;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;

@Entity
public class BasicInfo extends Model {

	@Id
	public Long id;

	public String channel_name;

	public String publisher;

	public String publisher_url;

	public String media_kit_url;
	
	public String fileUrl;

	public String username;

	public String password;

	public Date last_renewed;

	public String history;
	
	@Lob
	public String notes;

	public String subscriber;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getChannel_name() {
		return channel_name;
	}

	public void setChannel_name(String channel_name) {
		this.channel_name = channel_name;
	}

	public String getPublisher() {
		return publisher;
	}

	public void setPublisher(String publisher) {
		this.publisher = publisher;
	}

	public String getPublisher_url() {
		return publisher_url;
	}

	public void setPublisher_url(String publisher_url) {
		this.publisher_url = publisher_url;
	}

	public String getMedia_kit_url() {
		return media_kit_url;
	}

	public void setMedia_kit_url(String media_kit_url) {
		this.media_kit_url = media_kit_url;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public Date getLast_renewed() {
		return last_renewed;
	}

	public void setLast_renewed(Date last_renewed) {
		this.last_renewed = last_renewed;
	}

	public String getHistory() {
		return history;
	}

	public void setHistory(String history) {
		this.history = history;
	}

	public String getNotes() {
		return notes;
	}

	public void setNotes(String notes) {
		this.notes = notes;
	}

	public String getSubscriber() {
		return subscriber;
	}

	public void setSubscriber(String subscriber) {
		this.subscriber = subscriber;
	}

	public String getFileUrl() {
		return fileUrl;
	}

	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}

	public static Finder<Integer,BasicInfo> find = new Finder<>(Integer.class,BasicInfo.class);
	
	public static SqlRow findRecordByName(String data) {
		SqlRow b = Ebean
				.createSqlQuery(
						"select channel_name, publisher, publisher_url, media_kit_url, file_url, username, password, last_renewed, history, notes, Subscriber from basic_info where channel_name='"+ data+"'").findUnique();
		BasicInfo info = new BasicInfo();
		if (b != null) {
			System.out.println(b.getString("channel_name"));
			return b;
		} else {
			info.setChannel_name(data);
			info.save();
			SqlRow binfo = Ebean
					.createSqlQuery(
							"select channel_name, publisher, publisher_url, media_kit_url, file_url, username, password, last_renewed, history, notes, Subscriber from basic_info where channel_name='"+ data+"'").findUnique();
			return binfo;
		}
	}

	public static String saveRecord(String col_name, String record,
			String channel_name) {
		Ebean.createSqlUpdate("update basic_info set "+col_name+" = '"+record+"'where  channel_name ='"+channel_name+"'" ).execute();
		return "sucess";
	}

	public static String saveLastRenewedRecord(String col_name, String record,
			String channel_name) {
		Ebean.createSqlUpdate("update basic_info set "+col_name+" = '"+record+"' where  channel_name ='"+channel_name+"'" ).execute();
		return "sucess";
	}

	public static BasicInfo getByName(String name) {
		return find.where().eq("channel_name", name).findUnique();
	}
	
	
	/*
	 * public static void deleteLinksByMailObjectId(long id) { SqlUpdate
	 * list=Ebean.createSqlUpdate("DELETE FROM links WHERE mail_id_id="+id);
	 * list.execute(); }
	 */

}
 