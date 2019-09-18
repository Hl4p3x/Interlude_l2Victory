package ru.j2dev.gameserver.templates.npc.superPoint;

import java.util.ArrayList;
import java.util.List;

public class SuperPoint {
    private boolean isRunning;
    private SuperPointType type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    private String name;
    private List<SuperPoinCoordinate> points = new ArrayList<>();

    public void addMoveCoordinats(SuperPoinCoordinate coords) {
        points.add(coords);
    }

    public List<SuperPoinCoordinate> getCoordinats() {
        return points;
    }

    public SuperPointType getType() {
        return type;
    }

    public void setType(SuperPointType type) {
        this.type = type;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public void setRunning(boolean isRunning) {
        this.isRunning = isRunning;
    }
}