package models;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
@Entity
public class Domain extends Model{

	@Id
	public long id;
	
	public String domainName;
	
	@ManyToOne
	public MailObjectModel mailObjectModel; 
	
	public static Finder<Integer, Domain> find = new Finder<Integer,Domain>(Integer.class, Domain.class);
}
