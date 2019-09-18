package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.model.entity.events.GlobalEvent;

import java.io.Serializable;

public interface InitableObject extends Serializable {
    void initObject(final GlobalEvent p0);
}
