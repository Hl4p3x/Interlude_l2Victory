package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Die;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class DeadManInstance extends NpcInstance {
    public DeadManInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        setAI(new CharacterAI(this));
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        setCurrentHp(0.0, false);
        broadcastPacket(new Die(this));
        setWalking();
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
    }

    @Override
    public boolean isInvul() {
        return true;
    }

    @Override
    public boolean isBlocked() {
        return true;
    }
}
