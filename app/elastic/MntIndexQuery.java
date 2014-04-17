package elastic;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.Validate;
import org.elasticsearch.action.search.SearchRequestBuilder;

import com.github.cleverage.elasticsearch.Index;
import com.github.cleverage.elasticsearch.IndexQueryPath;

public class MntIndexQuery<T extends Index> extends com.github.cleverage.elasticsearch.IndexQuery<T>{

	private List<MntHighlightBuilder> highlights = new ArrayList<MntHighlightBuilder>();
    
	
	public MntIndexQuery(Class<T> clazz) {
		super(clazz);
	}
	
	public MntIndexQuery<T> addHighlights(MntHighlightBuilder highlight) {
        Validate.notNull(highlight, "highlight cannot be null");
        highlights.add(highlight);
        return this;
    }
	
	public SearchRequestBuilder getSearchRequestBuilder(IndexQueryPath indexQueryPath) {
		SearchRequestBuilder requestBuilder =  super.getSearchRequestBuilder(indexQueryPath);
        for (MntHighlightBuilder highlight : highlights) {
            requestBuilder.addHighlightedField(highlight.field, highlight.fragmentSize/*, highlight.numberOfFragments*/);
            requestBuilder.setHighlighterPreTags("<strong style='background-color: aquamarine;'>");
            requestBuilder.setHighlighterPostTags("</strong>");
        }
        
        return requestBuilder;
	}
	
	

}
