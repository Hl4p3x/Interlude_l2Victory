package ru.j2dev.gameserver.listener.event;

import ru.j2dev.gameserver.listener.EventListener;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

public interface OnStartStopListener extends EventListener {
    void onStart(final GlobalEvent p0);

    void onStop(final GlobalEvent p0);
}
