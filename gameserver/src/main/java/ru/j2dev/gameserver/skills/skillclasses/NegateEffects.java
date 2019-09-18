package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class NegateEffects extends Skill {
    private final boolean _onlyPhysical;
    private final boolean _negateDebuffs;
    private final Map<EffectType, Integer> _negateEffects;
    private final Map<String, Integer> _negateStackType;

    public NegateEffects(final StatsSet set) {
        super(set);
        _negateEffects = new HashMap<>();
        _negateStackType = new HashMap<>();
        final String[] negateEffectsString = set.getString("negateEffects", "").split(";");
        for (String aNegateEffectsString : negateEffectsString) {
            if (!aNegateEffectsString.isEmpty()) {
                final String[] entry = aNegateEffectsString.split(":");
                _negateEffects.put(Enum.valueOf(EffectType.class, entry[0]), (entry.length > 1) ? Integer.decode(entry[1]) : Integer.MAX_VALUE);
            }
        }
        final String[] negateStackTypeString = set.getString("negateStackType", "").split(";");
        for (String aNegateStackTypeString : negateStackTypeString) {
            if (!aNegateStackTypeString.isEmpty()) {
                final String[] entry2 = aNegateStackTypeString.split(":");
                _negateStackType.put(entry2[0], (entry2.length > 1) ? Integer.decode(entry2[1]) : Integer.MAX_VALUE);
            }
        }
        _onlyPhysical = set.getBool("onlyPhysical", false);
        _negateDebuffs = set.getBool("negateDebuffs", true);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (!_negateDebuffs && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())) {
                    activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getDisplayId(), getDisplayLevel()));
                } else {
                    if (!_negateEffects.isEmpty()) {
                        for (final Entry<EffectType, Integer> e : _negateEffects.entrySet()) {
                            negateEffectAtPower(target, e.getKey(), e.getValue());
                        }
                    }
                    if (!_negateStackType.isEmpty()) {
                        for (final Entry<String, Integer> e2 : _negateStackType.entrySet()) {
                            negateEffectAtPower(target, e2.getKey(), e2.getValue());
                        }
                    }
                    getEffects(activeChar, target, getActivateRate() > 0, false);
                }
            }
        }
        if (isSSPossible()) {
            activeChar.unChargeShots(isMagic());
        }
    }

    private void negateEffectAtPower(final Creature target, final EffectType type, final int power) {
        for (final Effect e : target.getEffectList().getAllEffects()) {
            final Skill skill = e.getSkill();
            if ((!_onlyPhysical || !skill.isMagic()) && skill.isCancelable()) {
                if (skill.isOffensive() && !_negateDebuffs) {
                    continue;
                }
                if (!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel())) {
                    continue;
                }
                if (e.getEffectType() != type || e.getStackOrder() > power) {
                    continue;
                }
                e.exit();
            }
        }
    }

    private void negateEffectAtPower(final Creature target, final String stackType, final int power) {
        for (final Effect e : target.getEffectList().getAllEffects()) {
            final Skill skill = e.getSkill();
            if ((!_onlyPhysical || !skill.isMagic()) && skill.isCancelable()) {
                if (skill.isOffensive() && !_negateDebuffs) {
                    continue;
                }
                if (!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel())) {
                    continue;
                }
                if (!e.isStackTypeMatch(stackType) || e.getStackOrder() > power) {
                    continue;
                }
                e.exit();
            }
        }
    }
}
