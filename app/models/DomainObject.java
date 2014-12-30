package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import play.db.ebean.Model;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlRow;
import com.avaje.ebean.SqlUpdate;

@Entity
public class DomainObject extends Model {
	
	@Id
	public Long id;
	public String name;
	public Long assigned=0l;
	public int parentId;
	public String color=null;
	
	public static Finder<Long, DomainObject> find = new Finder<Long,DomainObject>(Long.class, DomainObject.class);

	public static List<DomainObject> findUnAssignedNames() {
		return find.where().eq("assigned", 0).findList();
	}
	
	public static List<DomainObject> findAssignedChildNames() {
		return find.where().eq("assigned", 1).ne("parentId", null).findList();
	}
	
	public static List<DomainObject> findAssignedNames() {
		return find.where().eq("assigned", 1).eq("parentId", null).findList();
	}
	
	public static List<DomainObject> findAssignedChildNames(Long parentId) {
		return find.where().eq("assigned", 1).eq("parentId", parentId).findList();
	}
	
	public static void addParentSubscription(Long id) {
		SqlUpdate list=Ebean.createSqlUpdate("UPDATE domain_object SET assigned = 1 , color = '"+getRandomColor()+"' WHERE id = "+id);
		list.execute();
	}
	
	public static void removeParentSubscription(Long id) {
		List<DomainObject> li = find.where().eq("parentId", id).findList();
		for(DomainObject ds : li) {
			SqlUpdate list=Ebean.createSqlUpdate("UPDATE domain_object SET assigned = 0 , parent_id = NULL , color = NULL WHERE id = "+ds.id);
			list.execute();
		}
		SqlUpdate list=Ebean.createSqlUpdate("UPDATE domain_object SET assigned = 0 , color = NULL WHERE id = "+id);
		list.execute();
	}
	
	public static void addChildSubscription(Long cid,Long pid) {
		DomainObject parent = Ebean.find(DomainObject.class, pid);
		SqlUpdate list=Ebean.createSqlUpdate("UPDATE domain_object SET assigned = 1 , parent_id = "+pid+" , color = '"+parent.color+"' WHERE id = "+cid);
		list.execute();
	}
	
	public static void removeChildSubscription(Long id) {
		SqlUpdate list=Ebean.createSqlUpdate("UPDATE domain_object SET assigned = 0 , parent_id = NULL , color = NULL WHERE id = "+id);
		list.execute();
	}
	
	public static void addParentDomain(String parent) {
		DomainObject dom = new DomainObject();
		dom.name = parent;
		dom.assigned = 1l;
		dom.color = getRandomColor();
		dom.save();
	}
	
	public static String getRandomColor() {
	 	   String[]  letters = "0123456789ABCDEF".split("");
	 	   String color = "#";
	 	   for (int i = 0; i < 6; i++ ) {
	 	       color += letters[(int)Math.floor(Math.random() * 16)+1];
	 	   }
	 	   return color;
	 	}

	public static List<SqlRow> getAllDomains() {
		return Ebean.createSqlQuery("SELECT * FROM domain_object where parent_id = id ").findList();
	}

	public static List<DomainObject> getChildsOfDomain(Integer integer) {
		return find.where().eq("parent_id", integer).raw("parent_id <> id").findList();
	}

	public static DomainObject getDomainById(int id) {
		return find.where().eq("id", id).findUnique();
	}

	public static String getDomianByParent(int parentId) {
		// TODO Auto-generated method stub
		return null;
	}

	public static List<DomainObject> getAllChildList(int id) {
		return find.where().raw("id <> " +id).findList();
	}

	public static DomainObject getDomainByName(String parent) {
		return find.where().eq("name", parent).findUnique();
	}

	public static List<SqlRow> getAllDistinctDomains() {
		return Ebean.createSqlQuery("select distinct(name) as domain from domain_object").findList();
	}

	

	
}
