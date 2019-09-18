package ru.j2dev.commons.geometry;

public class Circle extends AbstractShape {
    protected final Point2D c;
    protected final int r;

    public Circle(final Point2D center, final int radius) {
        c = center;
        r = radius;
        min.x = (c.x - r);
        max.x = (c.x + r);
        min.y = (c.y - r);
        max.y = (c.y + r);
    }

    public Circle(final int x, final int y, final int radius) {
        this(new Point2D(x, y), radius);
    }

    @Override
    public Circle setZmax(final int z) {
        max.z = z;
        return this;
    }

    @Override
    public Circle setZmin(final int z) {
        min.z = z;
        return this;
    }

    @Override
    public boolean isInside(final int x, final int y) {
        return (x - c.x) * (c.x - x) + (y - c.y) * (c.y - y) <= r * r;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(c).append("{ radius: ").append(r).append('}');
        sb.append(']');
        return sb.toString();
    }
}
