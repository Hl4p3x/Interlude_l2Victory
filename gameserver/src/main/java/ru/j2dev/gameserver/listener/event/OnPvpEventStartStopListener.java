package ru.j2dev.gameserver.listener.event;

import ru.j2dev.gameserver.listener.PvpEventListener;
import ru.j2dev.gameserver.model.event.PvpEvent;

/**
 * Created by JunkyFunky
 * on 20.05.2018 13:58
 * group j2dev
 */
public interface OnPvpEventStartStopListener extends PvpEventListener {
    void onStartPvpEvent(PvpEvent pvpEvent);
    void onStopPvpEvent(PvpEvent pvpEvent);
}
