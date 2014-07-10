package wordcram.text;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import processing.core.PApplet;

public class WebPage implements TextSource {

    private String url;
    private String cssSelector;
    private PApplet parent;

    public WebPage(String url, String cssSelector, PApplet parent) {
        this.url = url;
        this.cssSelector = cssSelector;
        this.parent = parent;
    }

    
    public String getText() {
        try {
        	return new Html2Text().text( readFile(url,Charset.defaultCharset()), cssSelector);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        return null;
    }
    
    static String readFile(String path, Charset encoding) 
    		  throws IOException 
    		{
    		  byte[] encoded = Files.readAllBytes(Paths.get(path));
    		  return new String(encoded, encoding);
    		}

}
