package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class ChestInstance extends MonsterInstance {
    public ChestInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    public void tryOpen(final Player opener, final Skill skill) {
        getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, opener, 100);
    }

    @Override
    public boolean canChampion() {
        return false;
    }
}
