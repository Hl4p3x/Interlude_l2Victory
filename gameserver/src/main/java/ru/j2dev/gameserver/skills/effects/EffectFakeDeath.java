package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ChangeWaitType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Revive;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;

public final class EffectFakeDeath extends Effect {
    public static final int FAKE_DEATH_OFF = 0;
    public static final int FAKE_DEATH_ON = 1;
    public static final int FAKE_DEATH_FAILED = 2;
    private final int _failChance;

    public EffectFakeDeath(final Env env, final EffectTemplate template) {
        super(env, template);
        _failChance = template.getParam().getInteger("failChance", 0);
    }

    @Override
    public void onStart() {
        super.onStart();
        final Player player = (Player) getEffected();
        player.abortAttack(true, false);
        if (player.isMoving()) {
            player.stopMove();
        }
        if (_failChance > 0 && Rnd.chance(_failChance)) {
            player.setFakeDeath(FAKE_DEATH_FAILED);
        } else {
            player.setFakeDeath(FAKE_DEATH_ON);
            player.getAI().notifyEvent(CtrlEvent.EVT_FAKE_DEATH, null, null);
        }
        player.broadcastPacket(new ChangeWaitType(player, 2));
        player.broadcastCharInfo();
    }

    @Override
    public void onExit() {
        super.onExit();
        final Player player = (Player) getEffected();
        player.setNonAggroTime(System.currentTimeMillis() + 5000L);
        player.setFakeDeath(FAKE_DEATH_OFF);
        player.broadcastPacket(new ChangeWaitType(player, 3));
        player.broadcastPacket(new Revive(player));
        player.broadcastCharInfo();
    }

    @Override
    public boolean onActionTime() {
        if (getEffected().isDead()) {
            return false;
        }
        final double manaDam = calc();
        if (manaDam > getEffected().getCurrentMp() && getSkill().isToggle()) {
            getEffected().sendPacket(Msg.NOT_ENOUGH_MP);
            getEffected().sendPacket(new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
            return false;
        }
        getEffected().reduceCurrentMp(manaDam, null);
        return true;
    }
}
