package ru.j2dev.gameserver.model.items.attachment;

import ru.j2dev.gameserver.model.Player;

public interface PickableAttachment extends ItemAttachment {
    boolean canPickUp(final Player p0);

    void pickUp(final Player p0);
}
