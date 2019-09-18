package ru.j2dev.gameserver.skills.effects;

import gnu.trove.iterator.TObjectIntIterator;
import gnu.trove.map.hash.TObjectIntHashMap;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.listener.actor.OnCurrentHpDamageListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;

public final class EffectCurseOfLifeFlow extends Effect {
    private final TObjectIntHashMap<HardReference<? extends Creature>> _damageList = new TObjectIntHashMap<>();
    private CurseOfLifeFlowListener _listener;

    public EffectCurseOfLifeFlow(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public void onStart() {
        super.onStart();
        _listener = new CurseOfLifeFlowListener();
        _effected.addListener(_listener);
    }

    @Override
    public void onExit() {
        super.onExit();
        _effected.removeListener(_listener);
        _listener = null;
    }

    @Override
    public boolean onActionTime() {
        if (_effected.isDead()) {
            return false;
        }
        final TObjectIntIterator<HardReference<? extends Creature>> iterator = _damageList.iterator();
        while (iterator.hasNext()) {
            iterator.advance();
            final Creature damager = iterator.key().get();
            if (damager != null && !damager.isDead()) {
                if (damager.isCurrentHpFull()) {
                    continue;
                }
                final int damage = iterator.value();
                if (damage <= 0) {
                    continue;
                }
                final double max_heal = calc();
                final double heal = Math.min(damage, max_heal);
                final double newHp = Math.min(damager.getCurrentHp() + heal, damager.getMaxHp());
                damager.sendPacket(new SystemMessage(1066).addNumber((long) (newHp - damager.getCurrentHp())));
                damager.setCurrentHp(newHp, false);
            }
        }
        _damageList.clear();
        return true;
    }

    private class CurseOfLifeFlowListener implements OnCurrentHpDamageListener {
        @Override
        public void onCurrentHpDamage(final Creature actor, final double damage, final Creature attacker, final Skill skill) {
            if (attacker == actor || attacker == _effected) {
                return;
            }
            final int old_damage = _damageList.get(attacker.getRef());
            _damageList.put(attacker.getRef(), (old_damage == 0) ? ((int) damage) : (old_damage + (int) damage));
        }
    }
}
