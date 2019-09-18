package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

import java.util.Calendar;

public class March8Event extends GlobalEvent {
    private static final long LENGTH = 604800000L;
    private final Calendar _calendar;

    public March8Event(final MultiValueSet<String> set) {
        super(set);
        _calendar = Calendar.getInstance();
    }

    @Override
    public void initEvent() {
    }

    @Override
    public void startEvent() {
        super.startEvent();
        Announcements.getInstance().announceToAll("Test startEvent");
    }

    @Override
    public void stopEvent() {
        super.stopEvent();
        Announcements.getInstance().announceToAll("Test stopEvent");
    }

    @Override
    public void reCalcNextTime(final boolean onInit) {
        clearActions();
        if (onInit) {
            _calendar.set(Calendar.MONTH, 2);
            _calendar.set(Calendar.DATE, 8);
            _calendar.set(Calendar.HOUR_OF_DAY, 0);
            _calendar.set(Calendar.MINUTE, 0);
            _calendar.set(Calendar.SECOND, 0);
            if (_calendar.getTimeInMillis() + LENGTH < System.currentTimeMillis()) {
                _calendar.add(Calendar.YEAR, 1);
            }
        } else {
            _calendar.add(Calendar.YEAR, 1);
        }
        registerActions();
    }

    @Override
    protected long startTimeMillis() {
        return _calendar.getTimeInMillis();
    }
}
