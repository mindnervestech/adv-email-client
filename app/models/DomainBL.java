package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;


import com.avaje.ebean.Ebean;

import play.db.ebean.Model;

@Entity
public class DomainBL extends Model {
	
	@Id
	public Long id;
	
	public String domain;
	
	public static Finder<Long, DomainBL> find = new Finder<Long,DomainBL>(Long.class, DomainBL.class);

	public static DomainBL findDomainBLById(long id) {
		return Ebean.find(DomainBL.class, id);
	}
	public static DomainBL findDomainblObjectByDomainName(String domainName)
	{
		return find.where().ieq("domain", domainName).findUnique();
	}
	public static List<DomainBL> findDomainblAllObject()
	{
		return find.all();
	}
	public static boolean deleteDomainBLById(long id)
	{
		DomainBL domainBl=findDomainBLById(id);
		if(domainBl!=null)
		{
			domainBl.delete();
			return true;
		}
		else
		{
			return false;
		}
	}
}
