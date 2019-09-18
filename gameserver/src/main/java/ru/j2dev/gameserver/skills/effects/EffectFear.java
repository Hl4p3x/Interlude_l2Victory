package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.geodata.GeoEngine;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.utils.PositionUtils;

public final class EffectFear extends Effect {
    private static final double FEAR_RANGE = 2600.0;

    public EffectFear(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        if (_effected.isFearImmune()) {
            getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            return false;
        }
        final Player player = _effected.getPlayer();
        if (player != null) {
            final SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
            if (_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((SummonInstance) _effected)) {
                getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
                return false;
            }
        }
        if (_effected.isInZonePeace()) {
            getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
            return false;
        }
        return super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!_effected.startFear()) {
            _effected.abortAttack(true, true);
            _effected.abortCast(true, true);
            _effected.stopMove();
        }
        final double angle = Math.toRadians(PositionUtils.calculateAngleFrom(_effector, _effected));
        final int oldX = _effected.getX();
        final int oldY = _effected.getY();
        final int x = oldX + (int) (FEAR_RANGE * Math.cos(angle));
        final int y = oldY + (int) (FEAR_RANGE * Math.sin(angle));
        _effected.setRunning();
        _effected.moveToLocation(GeoEngine.moveCheck(oldX, oldY, _effected.getZ(), x, y, _effected.getGeoIndex()), 0, false);
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.stopFear();
        _effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
    }

    @Override
    public boolean onActionTime() {
        return false;
    }
}