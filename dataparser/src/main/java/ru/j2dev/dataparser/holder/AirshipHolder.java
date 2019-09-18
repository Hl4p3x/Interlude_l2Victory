package ru.j2dev.dataparser.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.dataparser.annotations.Element;
import ru.j2dev.dataparser.annotations.ElementArray;
import ru.j2dev.dataparser.holder.airship.AirPort;
import ru.j2dev.dataparser.holder.airship.AirShip;
import ru.j2dev.dataparser.holder.airship.AirshipArea;

import java.util.List;

/**
 * @author : Camelion
 * @date : 24.08.12 12:23
 * <p/>
 * Воздушные корабли и воздушные аэропорты
 */
public class AirshipHolder extends AbstractHolder {
    private static final AirshipHolder ourInstance = new AirshipHolder();
    @Element(start = "airport_begin", end = "airport_end")
    private List<AirPort> airPorts;
    @Element(start = "airship_begin", end = "airship_end")
    private List<AirShip> airShips;
    @ElementArray(start = "airship_area_begin", end = "airship_area_end")
    private AirshipArea[] airshipAreas;

    private AirshipHolder() {
    }

    public static AirshipHolder getInstance() {
        return ourInstance;
    }

    @Override
    public int size() {
        return airPorts.size() + airShips.size() + airshipAreas.length;
    }

    public List<AirPort> getAirPorts() {
        return airPorts;
    }

    public List<AirShip> getAirShips() {
        return airShips;
    }

    public AirshipArea[] getAirshipAreas() {
        return airshipAreas;
    }

    @Override
    public void clear() {
        airPorts.clear();
        airShips.clear();
        airshipAreas = null;
    }
}