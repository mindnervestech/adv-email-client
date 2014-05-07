package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.codehaus.jackson.annotate.JsonIgnore;

import com.avaje.ebean.Ebean;

import play.db.ebean.Model;

@Entity
public class ImageInfo extends Model {

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public MailObjectModel getMailObjectModel() {
		return mailObjectModel;
	}

	public void setMailObjectModel(MailObjectModel mailObjectModel) {
		this.mailObjectModel = mailObjectModel;
	}

	

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public byte[] getImage_byte() {
		return image_byte;
	}

	public void setImage_byte(byte[] image_byte) {
		this.image_byte = image_byte;
	}

	public String getAlt() {
		return alt;
	}

	public void setAlt(String alt) {
		this.alt = alt;
	}

	public static Finder<Integer, ImageInfo> getFind() {
		return find;
	}

	public static void setFind(Finder<Integer, ImageInfo> find) {
		ImageInfo.find = find;
	}

	@Id
	public long id;
	
	@JsonIgnore
	@ManyToOne
	public MailObjectModel mailObjectModel; 
	
	
	
	@Lob
	public String url;
	
	@Lob
    public byte[] image_byte;
	@Lob
	public String alt;

	public static Finder<Integer, ImageInfo> find = new Finder<Integer,ImageInfo>(Integer.class, ImageInfo.class);
	
	public static List<ImageInfo> findImageInfoById(long id) {
		List<ImageInfo> imageInfos =  ImageInfo.find.where().eq("mailObjectModel.id", id).findList();
		return imageInfos;
	}
}
