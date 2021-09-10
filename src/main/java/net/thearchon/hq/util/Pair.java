package net.thearchon.hq.util;

public class Pair<X, Y> {

    private X x;
    private Y y;

    public Pair() {
        
    }

    public Pair(X x, Y y) {
        this.x = x;
        this.y = y;
    }

    public void setX(X x) {
        this.x = x;
    }

    public void setY(Y y) {
        this.y = y;
    }

    public X getX() {
        return x;
    }

    public Y getY() {
        return y;
    }

    @Override
    public String toString() {
        return "(" + x + ", " + y + ')';
    }
}
