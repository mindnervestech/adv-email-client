package wordcram;

import processing.core.PApplet;

/**
 * Sizers contains pre-made {@link WordSizer} implementations that you might
 * find useful. Right now, it's only {@link Sizers#byWeight(int, int)} and
 * {@link Sizers#byRank(int, int)}.
 * @see WordSizer
 */
public class Sizers {

    /**
     * Returns a WordSizer that sizes words by their weight, where the
     * "heaviest" word will be sized at <code>maxSize</code>.
     * <p>
     * To be specific, the font size for each word will be calculated with:
     *
     * <pre>
     * PApplet.lerp(minSize, maxSize, (float) word.weight)
     * </pre>
     *
     * @param minSize
     *            the size to draw a Word with weight = 0
     * @param maxSize
     *            the size to draw a Word with weight = 1
     * @return the WordSizer
     */
    public static WordSizer byWeight(final int minSize, final int maxSize) {
        return new WordSizer() {
            public float sizeFor(Word word, int wordRank, int wordCount) {
                return lerp(minSize, maxSize, word.weight);
            }
        };
    }
    
    static public final float lerp(float start, float stop, float amt) {
        return start + (stop-start) * amt;
    }

    /**
     * Returns a WordSizer that sizes words by their rank. The first word will
     * be sized at <code>maxSize</code>.
     * <p>
     * To be specific, the font size for each word will be calculated with:
     *
     * <pre>
     * PApplet.map(wordRank, 0, wordCount, maxSize, minSize)
     * </pre>
     *
     * @param minSize
     *            the size to draw the last Word
     * @param maxSize
     *            the size to draw the first Word
     * @return the WordSizer
     */
    public static WordSizer byRank(final int minSize, final int maxSize) {
        return new WordSizer() {
            public float sizeFor(Word word, int wordRank, int wordCount) {
                return map(wordRank, 0, wordCount, maxSize, minSize);
            }
        };
    }
    
    static public final float map(float value,
            float start1, float stop1,
            float start2, float stop2) {
    	return start2 + (stop2 - start2) * ((value - start1) / (stop1 - start1));
    }

    // TODO try exponent scales, rather than linear.
}
