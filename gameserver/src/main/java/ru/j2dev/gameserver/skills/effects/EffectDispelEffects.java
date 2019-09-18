package ru.j2dev.gameserver.skills.effects;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SysMsgContainer;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.Stats;

import java.util.*;

public class EffectDispelEffects extends Effect {
    private final String _dispelType;
    private final int _cancelRate;
    private final String[] _stackTypes;
    private final int _negateCount;
    private final int _reApplyDelay;

    public EffectDispelEffects(final Env env, final EffectTemplate template) {
        super(env, template);
        _dispelType = template.getParam().getString("dispelType", "");
        _cancelRate = template.getParam().getInteger("cancelRate", 0);
        _negateCount = template.getParam().getInteger("negateCount", 5);
        _stackTypes = template.getParam().getString("negateStackTypes", "").split(";");
        _reApplyDelay = template.getParam().getInteger("reApplyDelay", 0);
    }

    @Override
    public void onStart() {
        final List<Effect> musicList = new ArrayList<>();
        final List<Effect> buffList = new ArrayList<>();
        for (final Effect e : _effected.getEffectList().getAllEffects()) {
            switch (_dispelType) {
                case "cancellation":
                    if (e.isOffensive() || e.getSkill().isToggle() || !e.isCancelable()) {
                        continue;
                    }
                    if (e.getSkill().isMusic()) {
                        musicList.add(e);
                    } else {
                        buffList.add(e);
                    }
                    break;
                case "bane":
                    if (!e.isCancelable() || (!ArrayUtils.contains(_stackTypes, e.getStackType()) && !ArrayUtils.contains(_stackTypes, e.getStackType2()))) {
                        continue;
                    }
                    buffList.add(e);
                    break;
                default:
                    if (!"cleanse".equals(_dispelType) || !e.isOffensive() || !e.isCancelable()) {
                        continue;
                    }
                    buffList.add(e);
                    break;
            }
        }
        List<Effect> _effectList = new ArrayList<>();
        _effectList.addAll(musicList);
        _effectList.addAll(buffList);
        if (_effectList.isEmpty()) {
            return;
        }
        double cancel_res_multiplier = _effected.calcStat(Stats.CANCEL_RESIST, 0.0, null, null);
        Collections.shuffle(_effectList);
        _effectList = _effectList.subList(0, Math.min(_negateCount, _effectList.size()));
        final Set<Skill> _stopSkills = new HashSet<>();
        for (final Effect e2 : _effectList) {
            final double eml = e2.getSkill().getMagicLevel();
            final double dml = getSkill().getMagicLevel() - ((eml == 0.0) ? _effected.getLevel() : eml);
            final int buffTime = e2.getTimeLeft();
            cancel_res_multiplier = 1.0 - cancel_res_multiplier * 0.01;
            final double prelimChance = (2.0 * dml + _cancelRate + buffTime / 120) * cancel_res_multiplier;
            if (Rnd.chance(calcSkillChanceLimits(prelimChance, _effector.isPlayable()))) {
                _stopSkills.add(e2.getSkill());
            }
        }
        for (final Skill stopSkill : _stopSkills) {
            _effected.getEffectList().stopEffect(stopSkill);
            _effected.sendPacket(((SysMsgContainer) new SystemMessage2(SystemMsg.THE_EFFECT_OF_S1_HAS_BEEN_REMOVED)).addSkillName(stopSkill));
        }
        if (_effected.isPlayer() && _reApplyDelay > 0) {
            final HardReference<Player> reApplyRef = _effected.getPlayer().getRef();
            final List<Skill> reApplySkills = new LinkedList<>();
            for (final Skill stopSkill2 : _stopSkills) {
                if (!stopSkill2.isOffensive() && !stopSkill2.isToggle()) {
                    if (stopSkill2.isTrigger()) {
                        continue;
                    }
                    reApplySkills.add(stopSkill2);
                }
            }
            ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                @Override
                public void runImpl() {
                    final Player player = reApplyRef.get();
                    if (player == null || player.isLogoutStarted() || player.isOutOfControl() || player.isDead() || player.isInDuel() || player.isAlikeDead() || player.isOlyParticipant() || player.isFlying() || player.isSitting() || player.getTeam() != TeamType.NONE || player.isInStoreMode()) {
                        return;
                    }
                    for (final Skill reApplySkill : reApplySkills) {
                        reApplySkill.getEffects(player, player, false, false);
                    }
                }
            }, _reApplyDelay * 1000L);
        }
    }

    private double calcSkillChanceLimits(final double prelimChance, final boolean isPlayable) {
        if ("bane".equals(_dispelType)) {
            if (prelimChance < 40.0) {
                return 40.0;
            }
            if (prelimChance > 90.0) {
                return 90.0;
            }
        } else {
            if ("cancellation".equals(_dispelType)) {
                return Math.max(Config.SKILLS_DISPEL_MOD_MIN, Math.min(Config.SKILLS_DISPEL_MOD_MAX, prelimChance));
            }
            if ("cleanse".equals(_dispelType)) {
                return _cancelRate;
            }
        }
        return prelimChance;
    }

    @Override
    protected boolean onActionTime() {
        return false;
    }
}
