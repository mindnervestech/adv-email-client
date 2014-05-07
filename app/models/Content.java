package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;

import play.db.ebean.Model;

@Entity
public class Content extends Model {
	
	@Id
	public Long id;
	
	
	@Lob
	public String description;
	
	public static Finder<Long, Content> find = new Finder<Long,Content>(Long.class, Content.class);

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
