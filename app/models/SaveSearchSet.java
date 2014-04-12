package models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Ebean;

import play.db.ebean.Model;
@Entity
public class SaveSearchSet extends Model{

	@Id
	public long id;
	
	@Column(unique=true)
	public String name;
	
	public String querySql;
	
	public static Finder<Integer, SaveSearchSet> find = new Finder<Integer,SaveSearchSet>(Integer.class, SaveSearchSet.class);
	
	public static SaveSearchSet getSaveSearchSetName(String name){
	      return Ebean.find(SaveSearchSet.class).where().eq("name",name).findUnique();
	   }
	public static SaveSearchSet getSaveSearchSetById(int id){
	      return Ebean.find(SaveSearchSet.class).where().eq("id",id).findUnique();
	   }

}
