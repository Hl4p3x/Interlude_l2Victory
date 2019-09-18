package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.stats.Env;

import java.util.ArrayList;
import java.util.List;

public class EffectDiscord extends Effect {
    public EffectDiscord(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        final int skilldiff = _effected.getLevel() - _skill.getMagicLevel();
        final int lvldiff = _effected.getLevel() - _effector.getLevel();
        if (skilldiff > 10 || (skilldiff > 5 && Rnd.chance(30)) || Rnd.chance(Math.abs(lvldiff) * 2)) {
            return false;
        }
        final boolean multitargets = _skill.isAoE();
        if (!_effected.isMonster()) {
            if (!multitargets) {
                getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            }
            return false;
        }
        if (_effected.isFearImmune() || _effected.isRaid()) {
            if (!multitargets) {
                getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
            }
            return false;
        }
        final Player player = _effected.getPlayer();
        if (player != null) {
            final SiegeEvent<?, ?> siegeEvent = player.getEvent(SiegeEvent.class);
            if (_effected.isSummon() && siegeEvent != null && siegeEvent.containsSiegeSummon((SummonInstance) _effected)) {
                if (!multitargets) {
                    getEffector().sendPacket(SystemMsg.THAT_IS_AN_INCORRECT_TARGET);
                }
                return false;
            }
        }
        if (_effected.isInZonePeace()) {
            if (!multitargets) {
                getEffector().sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE);
            }
            return false;
        }
        return super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        _effected.startConfused();
        onActionTime();
    }

    @Override
    public void onExit() {
        super.onExit();
        if (!_effected.stopConfused()) {
            _effected.abortAttack(true, true);
            _effected.abortCast(true, true);
            _effected.stopMove();
            _effected.getAI().setAttackTarget(null);
            _effected.setWalking();
            _effected.getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
        }
    }

    @Override
    public boolean onActionTime() {
        final List<Creature> targetList = new ArrayList<>();
        for (final Creature character : _effected.getAroundCharacters(900, 200)) {
            if (character.isNpc() && character != getEffected()) {
                targetList.add(character);
            }
        }
        if (targetList.isEmpty()) {
            return true;
        }
        final Creature target = targetList.get(Rnd.get(targetList.size()));
        _effected.setRunning();
        _effected.getAI().Attack(target, true, false);
        return false;
    }
}
