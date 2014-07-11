package wordcram;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class MNTWordIterator implements Iterator<String>, Iterable<String> {

	private static final String LETTER = " (\\S+or\\S+) .* (\\S+the\\S+).*";
	//private static final String JOINER = "[-.:/'’\\p{M}\\u2032\\u00A0\\u200C\\u200D~]";
	private static final Pattern WORD = Pattern.compile("[a-zA-Z]+");

	private final Matcher m;
	private boolean hasNext;

	public MNTWordIterator(final String text)
	{
		this.m = WORD.matcher(text == null ? "" : text);
		hasNext = m.find();
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}

	public String next()
	{
		if (!hasNext)
		{
			throw new NoSuchElementException();
		}
		final String s = m.group();
		hasNext = m.find();
		return s;
	}

	public boolean hasNext()
	{
		return hasNext;
	}

	@Override
	public Iterator<String> iterator() {
		return this;
	}
	

}
