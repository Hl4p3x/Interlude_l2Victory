package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Formulas.AttackInfo;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Spoil extends Skill {
    public Spoil(final StatsSet set) {
        super(set);
    }

    public boolean ALT_SPOILFORMULA;


    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (!activeChar.isPlayer()) {
            return;
        }
        final int ss = isSSPossible() ? (isMagic() ? activeChar.getChargedSpiritShot() : (activeChar.getChargedSoulShot() ? 2 : 0)) : 0;
        if (ss > 0 && getPower() > 0.0) {
            activeChar.unChargeShots(false);
        }
        for (final Creature target : targets) {
            if (target != null && !target.isDead()) {
                boolean success = false;
                if (target.isMonster()) {
                    final MonsterInstance monster = (MonsterInstance) target;
                    if (((MonsterInstance) target).isSpoiled()) {
                        activeChar.sendPacket(Msg.ALREADY_SPOILED);
                    } else if (ALT_SPOILFORMULA) {
                        final int monsterLevel = monster.getLevel();
                        final int modifier = monsterLevel - getMagicLevel();
                        double rateOfSpoil = Math.max(getActivateRate(), 80);
                        if (modifier > 8) {
                            rateOfSpoil -= rateOfSpoil * (modifier - 8) * 5 / 100.0;
                        }
                        rateOfSpoil *= getMagicLevel() / monsterLevel;
                        rateOfSpoil = Math.max(Config.MINIMUM_SPOIL_RATE, Math.min(rateOfSpoil, 99.0));
                        success = Rnd.chance(rateOfSpoil);
                    } else {
                        success = Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate());
                    }


                    if (success && monster.setSpoiled((Player) activeChar)) {
                        activeChar.sendPacket(Msg.THE_SPOIL_CONDITION_HAS_BEEN_ACTIVATED);
                    } else {
                        activeChar.sendPacket(new SystemMessage(SystemMessage.S1_HAS_FAILED).addSkillName(_id, getDisplayLevel()));
                    }
                }
                if (getPower() > 0.0) {
                    double damage;
                    if (isMagic()) {
                        damage = Formulas.calcMagicDam(activeChar, target, this, ss);
                    } else {
                        final AttackInfo info = Formulas.calcPhysDam(activeChar, target, this, false, false, ss > 0, false);
                        damage = info.damage;
                        if (info.lethal_dmg > 0.0) {
                            target.reduceCurrentHp(info.lethal_dmg, activeChar, this, true, true, false, false, false, false, false);
                        }
                    }
                    target.reduceCurrentHp(damage, activeChar, this, true, true, false, true, false, false, true);
                    target.doCounterAttack(this, activeChar, false);
                }
                getEffects(activeChar, target, false, false);
                target.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, activeChar, Math.max(_effectPoint, 1));
            }
        }
    }
}
