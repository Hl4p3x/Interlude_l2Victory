package ru.j2dev.gameserver.listener.actor.door.impl;

import ru.j2dev.gameserver.listener.actor.door.OnOpenCloseListener;
import ru.j2dev.gameserver.model.instances.DoorInstance;

public class MasterOnOpenCloseListenerImpl implements OnOpenCloseListener {
    private final DoorInstance _door;

    public MasterOnOpenCloseListenerImpl(final DoorInstance door) {
        _door = door;
    }

    @Override
    public void onOpen(final DoorInstance doorInstance) {
        _door.openMe();
    }

    @Override
    public void onClose(final DoorInstance doorInstance) {
        _door.closeMe();
    }
}
