package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Aggression extends Skill {
    private final boolean _unaggring;
    private final boolean _silent;

    public Aggression(final StatsSet set) {
        super(set);
        _unaggring = set.getBool("unaggroing", false);
        _silent = set.getBool("silent", false);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        int effect = _effectPoint;
        if (isSSPossible() && (activeChar.getChargedSoulShot() || activeChar.getChargedSpiritShot() > 0)) {
            effect *= 2;
        }

        for (final Creature target : targets) {
            if (target != null) {
                if (!target.isAutoAttackable(activeChar)) {
                    continue;
                }
                if (target.isNpc()) {
                    if (_unaggring) {
                        if (target.isNpc() && activeChar.isPlayable()) {
                            ((NpcInstance) target).getAggroList().addDamageHate(activeChar, 0, -effect);
                        }
                    } else {
                        target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, effect);
                        if (!_silent) {
                            target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, activeChar, 0);
                        }
                    }
                } else if (target.isPlayable() && !target.isDebuffImmune()) {
                    target.setTarget(activeChar);
                }
                getEffects(activeChar, target, getActivateRate() > 0, false);
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
