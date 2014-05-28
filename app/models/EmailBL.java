package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Ebean;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;
@Entity
public class EmailBL extends Model{
	@Id
	public Long id;
	
	public String email;
	
	private static Finder<Long, EmailBL> find = new Finder<Long,EmailBL>(Long.class, EmailBL.class);
	public static EmailBL findEmailBLById(long id) {
		return Ebean.find(EmailBL.class, id);
	}
	public static EmailBL findEmailblObjectByEmailAddress(String emailAddress)
	{
		return find.where().ilike("email", "%"+emailAddress+"%").findUnique();
	}
	public static List<EmailBL> findEmailblAllObject()
	{
		return find.all();
	}
	public static boolean deleteEmailBLById(long id)
	{
		EmailBL emailBl=findEmailBLById(id);
		if(emailBl!=null)
		{
			emailBl.delete();
			return true;
		}
		else
		{
			return false;
		}
	}
}
