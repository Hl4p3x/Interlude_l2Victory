package ru.j2dev.dataparser.holder.eventdata;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author KilRoy
 */
public class EventTemplate {
    private final String eventName;
    private String eventMakerName;
    private boolean register;
    private int panelId;
    private int npcTick;
    private int dropItemChance;
    private ZonedDateTime startEventTime;
    private ZonedDateTime endEventTime;
    private ZonedDateTime startDropTime;
    private ZonedDateTime endDropTime;
    private final List<EventItemInfoDrop> dropItem = new ArrayList<>();

    public EventTemplate(final String eventName) {
        this.eventName = eventName;
    }

    public String getEventName() {
        return eventName;
    }

    public String getEventMakerName() {
        return eventMakerName;
    }

    public void setEventMakerName(final String eventMakerName) {
        this.eventMakerName = eventMakerName;
    }

    public List<EventItemInfoDrop> getDropItem() {
        return dropItem;
    }

    public void addDropItem(final EventItemInfoDrop item) {
        dropItem.add(item);
    }

    public boolean isRegister() {
        return register;
    }

    public void setRegister(final boolean register) {
        this.register = register;
    }

    public int getPanelId() {
        return panelId;
    }

    public void setPanelId(final int panelId) {
        this.panelId = panelId;
    }

    public int getNpcTick() {
        return npcTick;
    }

    public void setNpcTick(final int npcTick) {
        this.npcTick = npcTick;
    }

    public int getDropItemChance() {
        return dropItemChance;
    }

    public void setDropItemChance(final int dropItemChance) {
        this.dropItemChance = dropItemChance;
    }

    public ZonedDateTime getStartEventTime() {
        return startEventTime;
    }

    public void setStartEventTime(final ZonedDateTime startEventTime) {
        this.startEventTime = startEventTime;
    }

    public ZonedDateTime getEndEventTime() {
        return endEventTime;
    }

    public void setEndEventTime(final ZonedDateTime endEventTime) {
        this.endEventTime = endEventTime;
    }

    public ZonedDateTime getStartDropTime() {
        return startDropTime;
    }

    public void setStartDropTime(final ZonedDateTime startDropTime) {
        this.startDropTime = startDropTime;
    }

    public ZonedDateTime getEndDropTime() {
        return endDropTime;
    }

    public void setEndDropTime(final ZonedDateTime endDropTime) {
        this.endDropTime = endDropTime;
    }
}