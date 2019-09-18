package ru.j2dev.gameserver.listener.actor.door;

import ru.j2dev.gameserver.listener.CharListener;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public interface OnOpenCloseListener extends CharListener {
    void onOpen(final DoorInstance p0);

    void onClose(final DoorInstance p0);
}
