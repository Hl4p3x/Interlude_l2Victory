package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.ArrayList;
import java.util.List;

public class NegateStats extends Skill {
    private final List<Stats> _negateStats;
    private final boolean _negateOffensive;
    private final int _negateCount;

    public NegateStats(final StatsSet set) {
        super(set);
        final String[] negateStats = set.getString("negateStats", "").split(" ");
        _negateStats = new ArrayList<>(negateStats.length);
        for (final String stat : negateStats) {
            if (!stat.isEmpty()) {
                _negateStats.add(Stats.valueOfXml(stat));
            }
        }
        _negateOffensive = set.getBool("negateDebuffs", false);
        _negateCount = set.getInteger("negateCount", 0);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null) {
                if (!_negateOffensive && !Formulas.calcSkillSuccess(activeChar, target, this, getActivateRate())) {
                    activeChar.sendPacket(new SystemMessage(139).addString(target.getName()).addSkillName(getId(), getLevel()));
                } else {
                    int count = 0;
                    final List<Effect> effects = target.getEffectList().getAllEffects();
                    for (final Stats stat : _negateStats) {
                        for (final Effect e : effects) {
                            final Skill skill = e.getSkill();
                            if (!skill.isOffensive() && skill.getMagicLevel() > getMagicLevel() && Rnd.chance(skill.getMagicLevel() - getMagicLevel())) {
                                ++count;
                            } else {
                                if (skill.isOffensive() == _negateOffensive && containsStat(e, stat) && skill.isCancelable()) {
                                    target.sendPacket(new SystemMessage(749).addSkillName(e.getSkill().getId(), e.getSkill().getDisplayLevel()));
                                    e.exit();
                                    ++count;
                                }
                                if (_negateCount > 0 && count >= _negateCount) {
                                    break;
                                }
                                continue;
                            }
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

    private boolean containsStat(final Effect e, final Stats stat) {
        for (final FuncTemplate ft : e.getTemplate().getAttachedFuncs()) {
            if (ft.getStat() == stat) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean isOffensive() {
        return !_negateOffensive;
    }

    public List<Stats> getNegateStats() {
        return _negateStats;
    }
}
