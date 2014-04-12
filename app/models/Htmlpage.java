package models;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import play.db.ebean.Model;

@Entity
public class Htmlpage extends Model {

	@Id
	public Long id;
	
	public String html_id; 
	
	@OneToMany(cascade=CascadeType.ALL)
	public List<Content> contents;

	@OneToMany(cascade=CascadeType.ALL)
	public List<ImageInfo> images;
	
	public static Finder<Long, Htmlpage> find = new Finder<Long,Htmlpage>(Long.class, Htmlpage.class);
}
