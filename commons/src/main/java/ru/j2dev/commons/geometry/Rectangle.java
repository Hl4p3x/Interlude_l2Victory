package ru.j2dev.commons.geometry;

public class Rectangle extends AbstractShape {
    public Rectangle(final int x1, final int y1, final int x2, final int y2) {
        min.x = Math.min(x1, x2);
        min.y = Math.min(y1, y2);
        max.x = Math.max(x1, x2);
        max.y = Math.max(y1, y2);
    }

    @Override
    public Rectangle setZmax(final int z) {
        max.z = z;
        return this;
    }

    @Override
    public Rectangle setZmin(final int z) {
        min.z = z;
        return this;
    }

    @Override
    public boolean isInside(final int x, final int y) {
        return (x >= min.x) && (x <= max.x) && (y >= min.y) && (y <= max.y);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append('[');
        sb.append(min).append(", ").append(max);
        sb.append(']');
        return sb.toString();
    }
}
