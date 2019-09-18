package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class SpecialMonsterInstance extends MonsterInstance {
    public SpecialMonsterInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public boolean canChampion() {
        return Config.ALT_CHAMPION_CAN_BE_SPECIAL_MONSTERS;
    }
}
