package wordcram;

import processing.core.PVector;

public class SpiralWordNudger implements WordNudger {

    // Who knows? this seems to be good, but it seems to depend on the font --
    // bigger fonts need a bigger thetaIncrement.
    private float thetaIncrement = (float) (Math.PI * 0.03);

    public PVector nudgeFor(Word w, int attempt) {
        float rad = powerMap(0.6f, attempt, 0, 600, 1, 100);

        thetaIncrement = powerMap(1, attempt, 0, 600, 0.5f, 0.3f);
        float theta = thetaIncrement * attempt;
        float x = cos(theta) * rad;
        float y = sin(theta) * rad;
        return new PVector(x, y);
    }
    
    static public final float cos(float angle) {
        return (float)Math.cos(angle);
      }
    
    static public final float sin(float angle) {
        return (float)Math.sin(angle);
      }

    private float powerMap(float power, float v, float min1, float max1,
            float min2, float max2) {

        float val = norm(v, min1, max1);
        val = pow(val, power);
        return lerp(min2, max2, val);
    }
    
    static public final float lerp(float start, float stop, float amt) {
        return start + (stop-start) * amt;
      }
    
    static public final float norm(float value, float start, float stop) {
        return (value - start) / (stop - start);
      }
    
    static public final float pow(float n, float e) {
        return (float)Math.pow(n, e);
      }

}
