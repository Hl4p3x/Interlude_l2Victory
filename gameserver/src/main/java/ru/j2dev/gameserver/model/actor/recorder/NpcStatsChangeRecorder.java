package ru.j2dev.gameserver.model.actor.recorder;

import ru.j2dev.gameserver.model.instances.NpcInstance;

public class NpcStatsChangeRecorder extends CharStatsChangeRecorder<NpcInstance> {
    public NpcStatsChangeRecorder(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onSendChanges() {
        super.onSendChanges();
        if ((_changes & 0x1) == 0x1) {
            _activeChar.broadcastCharInfo();
        }
    }
}
