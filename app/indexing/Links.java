package indexing;

import java.util.HashMap;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonProperty;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.Indexable;
import com.github.cleverage.elasticsearch.annotations.IndexType;

@IndexType(name = "links")
public class Links  extends Index {
	
	public static Finder<Links> find = new Finder<Links>(Links.class);

	@JsonProperty("contents")
	public String description;
	
	@JsonProperty("id")
	public String id; 
	
	public Links(){}
	
	
	public Links(Long id, String description) {
		this.id = id+"";
		this.description = description;
	}

	@Override
	public Indexable fromIndex(Map map) {
		if (map == null) {
            return this;
        }
		this.description = (String) map.get("description");
		this.id = (String) map.get("id");
		return this;
	}

	@Override
	public Map toIndex() {
		HashMap<String,Object> map = new HashMap<String,Object>();
		map.put("description", description);
		map.put("id", id);
		return map;
	}

}
