package quadtreeindex;

import java.util.Set;

/**
 * Created by ananya on 30/10/15.
 */

public class featureObject extends Point2D {
    private double rating;
    private int type;
    private Set<String> keyWords;

    public double getRating() {
        return rating;
    }

    public void setRating(double rating) {
        this.rating = rating;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Set<String> getKeyWords() {
        return keyWords;
    }

    public void setKeyWords(Set<String> keyWords) {
        this.keyWords = keyWords;
    }
}
