package wordcram.text;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import processing.core.PApplet;

public class TextFile implements TextSource {

    private String path;

    // TODO if we move all .text.* classes into WordCram, we can kill this, and
    // use pkg-local methods for setting the parent...
    
    public TextFile(String path) {
        this.path = path;
        
    }

    public String getText() {
        try {
			return readFile(path,Charset.defaultCharset());
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
