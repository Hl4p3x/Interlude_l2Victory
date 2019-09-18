package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.gameserver.listener.event.OnPvpEventStartStopListener;

/**
 * Created by JunkyFunky
 * on 20.05.2018 14:31
 * group j2dev
 */
@HideAccess
@StringEncryption
public class PvpEventListenerList extends ListenerList<PvpEvent> {
    void onStart(PvpEvent pvpEvent) {
        if (!getListeners().isEmpty()) {
            getListeners().forEach(listener -> ((OnPvpEventStartStopListener) listener).onStartPvpEvent(pvpEvent));
        }
    }

    void onStop(PvpEvent pvpEvent) {
        if (!getListeners().isEmpty()) {
            getListeners().forEach(listener -> ((OnPvpEventStartStopListener) listener).onStopPvpEvent(pvpEvent));
        }
    }
}
