package elastic;


public class MntHighlightBuilder {
	public int numberOfFragments = 20;
	public int fragmentSize = 120;
	public String field;
	
	public static MntHighlightBuilder  instance() {
		return new MntHighlightBuilder();
	}
	
	public MntHighlightBuilder setField(String field) {
		this.field = field;
		return this;
	}

	public MntHighlightBuilder setNumberOfFragments(int numberOfFragments) {
		this.numberOfFragments = numberOfFragments;
		return this;
	}

	public MntHighlightBuilder setFragmentSize(int fragmentSize) {
		this.fragmentSize = fragmentSize;
		return this;
	}
}
