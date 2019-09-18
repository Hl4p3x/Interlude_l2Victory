package ru.j2dev.gameserver.listener.zone;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Zone;

public interface OnZoneEnterLeaveListener extends Listener<Zone> {
    void onZoneEnter(final Zone p0, final Creature p1);

    void onZoneLeave(final Zone p0, final Creature p1);
}
