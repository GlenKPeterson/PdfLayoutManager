package com.planbase.pdf.layoutmanager;

public class XyPair {
    public static final XyPair ORIGIN = new XyPair(0f, 0f);
    private final float x;
    private final float y;
    private XyPair(float xCoord, float yCoord) { x = xCoord; y = yCoord; }
    public static XyPair of(float x, float y) {
        if ((x == 0f) && (y == 0f)) { return ORIGIN; }
        return new XyPair(x, y);
    }
    public float x() { return x; }
    public float y() { return y; }
    public XyPair x(float newX) { return of(newX, y); }
    public XyPair y(float newY) { return of(x, newY); }

    public XyPair minus(XyPair that) { return of(this.x - that.x(), this.y - that.y()); }
    public XyPair plus(XyPair that) { return of(this.x + that.x(), this.y + that.y()); }
    public XyPair maxXandY(XyPair that) {
        if ((this.x >= that.x()) && (this.y >= that.y())) { return this; }
        if ((this.x <= that.x()) && (this.y <= that.y())) { return that; }
        return of((this.x > that.x()) ? this.x : that.x(),
                  (this.y > that.y()) ? this.y : that.y());
    }
    public XyPair maxXMinY(XyPair that) {
        if ((this.x >= that.x()) && (this.y <= that.y())) { return this; }
        if ((this.x <= that.x()) && (this.y >= that.y())) { return that; }
        return of((this.x > that.x()) ? this.x : that.x(),
                  (this.y < that.y()) ? this.y : that.y());
    }

    @Override
    public String toString() {
        return "XyPair(" + x + ", " + y + ")";
    }
}
