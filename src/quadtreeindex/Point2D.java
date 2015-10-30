package quadtreeindex;

import java.util.Set;

/**
 * Created by ananya on 30/10/15.
 *
 * x -> longitude
 * y -> latitude
 */

public abstract class Point2D {
    private double x;
    private double y;

    public double getX() {
        return x;
    }

    public void setX(double x) {
        this.x = x;
    }

    public double getY() {
        return y;
    }

    public void setY(double y) {
        this.y = y;
    }
}
