package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ExServerPrimitive extends L2GameServerPacket {
    private final String _name;
    private final int _x;
    private final int _y;
    private final int _z;
    private final List<Point> _points;
    private final List<Line> _lines;

    public ExServerPrimitive(final String name, final int x, final int y, final int z) {
        _points = new ArrayList<>();
        _lines = new ArrayList<>();
        _name = name;
        _x = x;
        _y = y;
        _z = z;
    }

    public ExServerPrimitive(final String name, final Location locational) {
        this(name, locational.getX(), locational.getY(), locational.getZ());
    }

    public void addPoint(final String name, final int color, final boolean isNameColored, final int x, final int y, final int z) {
        _points.add(new Point(name, color, isNameColored, x, y, z));
    }

    public void addGeoPoint(final String name, final int color, final boolean isNameColored, final int x, final int y, final int l) {
        addPoint(name, color, isNameColored, new Location(x, y, (short) ((short) (l & 0xFFF0) >> 1)).geo2world());
    }

    public void addPoint(final String name, final int color, final boolean isNameColored, final Location locational) {
        addPoint(name, color, isNameColored, locational.getX(), locational.getY(), locational.getZ());
    }

    public void addPoint(final int color, final int x, final int y, final int z) {
        addPoint("", color, false, x, y, z);
    }

    public void addPoint(final int color, final Location locational) {
        addPoint("", color, false, locational);
    }

    public void addPoint(final String name, final Color color, final boolean isNameColored, final int x, final int y, final int z) {
        addPoint(name, color.getRGB(), isNameColored, x, y, z);
    }

    public void addPoint(final String name, final Color color, final boolean isNameColored, final Location locational) {
        addPoint(name, color.getRGB(), isNameColored, locational);
    }

    public void addPoint(final Color color, final int x, final int y, final int z) {
        addPoint("", color, false, x, y, z);
    }

    public void addPoint(final Color color, final Location locational) {
        addPoint("", color, false, locational);
    }

    public void addLine(final String name, final int color, final boolean isNameColored, final int x, final int y, final int z, final int x2, final int y2, final int z2) {
        _lines.add(new Line(name, color, isNameColored, x, y, z, x2, y2, z2));
    }

    public void addLine(final String name, final int color, final boolean isNameColored, final Location locational, final int x2, final int y2, final int z2) {
        addLine(name, color, isNameColored, locational.getX(), locational.getY(), locational.getZ(), x2, y2, z2);
    }

    public void addLine(final String name, final int color, final boolean isNameColored, final int x, final int y, final int z, final Location locational2) {
        addLine(name, color, isNameColored, x, y, z, locational2.getX(), locational2.getY(), locational2.getZ());
    }

    public void addLine(final String name, final int color, final boolean isNameColored, final Location locational, final Location locational2) {
        addLine(name, color, isNameColored, locational, locational2.getX(), locational2.getY(), locational2.getZ());
    }

    public void addLine(final int color, final int x, final int y, final int z, final int x2, final int y2, final int z2) {
        addLine("", color, false, x, y, z, x2, y2, z2);
    }

    public void addLine(final int color, final Location locational, final int x2, final int y2, final int z2) {
        addLine("", color, false, locational, x2, y2, z2);
    }

    public void addLine(final int color, final int x, final int y, final int z, final Location locational2) {
        addLine("", color, false, x, y, z, locational2);
    }

    public void addLine(final int color, final Location locational, final Location locational2) {
        addLine("", color, false, locational, locational2);
    }

    public void addLine(final String name, final Color color, final boolean isNameColored, final int x, final int y, final int z, final int x2, final int y2, final int z2) {
        addLine(name, color.getRGB(), isNameColored, x, y, z, x2, y2, z2);
    }

    public void addLine(final String name, final Color color, final boolean isNameColored, final Location locational, final int x2, final int y2, final int z2) {
        addLine(name, color.getRGB(), isNameColored, locational, x2, y2, z2);
    }

    public void addLine(final String name, final Color color, final boolean isNameColored, final int x, final int y, final int z, final Location locational2) {
        addLine(name, color.getRGB(), isNameColored, x, y, z, locational2);
    }

    public void addLine(final String name, final Color color, final boolean isNameColored, final Location locational, final Location locational2) {
        addLine(name, color.getRGB(), isNameColored, locational, locational2);
    }

    public void addLine(final Color color, final int x, final int y, final int z, final int x2, final int y2, final int z2) {
        addLine("", color, false, x, y, z, x2, y2, z2);
    }

    public void addLine(final Color color, final Location locational, final int x2, final int y2, final int z2) {
        addLine("", color, false, locational, x2, y2, z2);
    }

    public void addLine(final Color color, final int x, final int y, final int z, final Location locational2) {
        addLine("", color, false, x, y, z, locational2);
    }

    public void addLine(final Color color, final Location locational, final Location locational2) {
        addLine("", color, false, locational, locational2);
    }

    @Override
    protected void writeImpl() {
        writeC(0xfe);
        writeH(36);
        writeS(_name);
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeD(0xffff);
        writeD(0xffff);
        writeD(_points.size() + _lines.size());
        _points.forEach(point -> {
            writeC(1);
            writeS(point.getName());
            final int color = point.getColor();
            writeD(color >> 16 & 0xFF);
            writeD(color >> 8 & 0xFF);
            writeD(color & 0xFF);
            writeD(point.isNameColored() ? 1 : 0);
            writeD(point.getX());
            writeD(point.getY());
            writeD(point.getZ());
        });
        _lines.forEach(line -> {
            writeC(2);
            writeS(line.getName());
            final int color = line.getColor();
            writeD(color >> 16 & 0xFF);
            writeD(color >> 8 & 0xFF);
            writeD(color & 0xFF);
            writeD(line.isNameColored() ? 1 : 0);
            writeD(line.getX());
            writeD(line.getY());
            writeD(line.getZ());
            writeD(line.getX2());
            writeD(line.getY2());
            writeD(line.getZ2());
        });
    }

    private static class Point {
        private final String _name;
        private final int _color;
        private final boolean _isNameColored;
        private final int _x;
        private final int _y;
        private final int _z;

        public Point(final String name, final int color, final boolean isNameColored, final int x, final int y, final int z) {
            _name = name;
            _color = color;
            _isNameColored = isNameColored;
            _x = x;
            _y = y;
            _z = z;
        }

        public String getName() {
            return _name;
        }

        public int getColor() {
            return _color;
        }

        public boolean isNameColored() {
            return _isNameColored;
        }

        public int getX() {
            return _x;
        }

        public int getY() {
            return _y;
        }

        public int getZ() {
            return _z;
        }
    }

    private static class Line extends Point {
        private final int _x2;
        private final int _y2;
        private final int _z2;

        public Line(final String name, final int color, final boolean isNameColored, final int x, final int y, final int z, final int x2, final int y2, final int z2) {
            super(name, color, isNameColored, x, y, z);
            _x2 = x2;
            _y2 = y2;
            _z2 = z2;
        }

        public int getX2() {
            return _x2;
        }

        public int getY2() {
            return _y2;
        }

        public int getZ2() {
            return _z2;
        }
    }
}
