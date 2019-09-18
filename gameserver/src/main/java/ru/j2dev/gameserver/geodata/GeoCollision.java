package ru.j2dev.gameserver.geodata;


import ru.j2dev.commons.geometry.Shape;

public interface GeoCollision {
    Shape getShape();

    byte[][] getGeoAround();

    void setGeoAround(byte[][] geo);

    boolean isConcrete();
}