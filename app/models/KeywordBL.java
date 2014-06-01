package models;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.Id;

import com.avaje.ebean.Ebean;
import com.avaje.ebean.SqlQuery;

import play.db.ebean.Model;
import play.db.ebean.Model.Finder;

@Entity
public class KeywordBL extends Model{
	@Id
	public Long id;
	
	public String keyword;
	
	private static Finder<Long, KeywordBL> find = new Finder<Long,KeywordBL>(Long.class, KeywordBL.class);
	public static List<KeywordBL> findKeywordblAllObject() {
		return find.all();
	}
	public static KeywordBL findKeywordBLById(long id) {
		return Ebean.find(KeywordBL.class, id);
	}
	public static KeywordBL findKeywordblObjectByKeyword(String keyword) {
		return find.where().ieq("keyword", keyword).findUnique();
	}
	public static boolean deleteKeywordBLById(long id) {
		KeywordBL keywordBl=findKeywordBLById(id);
		if(keywordBl!=null) {
			keywordBl.delete();
			return true;
		}
		else {
			return false;
		}
	}
	public static boolean findKeywordblObjectFromKeywordList(List keywordList) {
		String query="select * from keyword_bl where ucase(keyword) in (:list)";
		SqlQuery sqlQuery=Ebean.createSqlQuery(query);
		sqlQuery.setParameter("list", keywordList);
		List list=sqlQuery.findList();
		if(list!=null&&!list.isEmpty()) {
			return true;
		}
		else {
			return false;
		}
	}
}
