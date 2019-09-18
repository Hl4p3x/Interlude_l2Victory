package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;

public final class PetBabyInstance extends PetInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(PetBabyInstance.class);
    private static final int HealTrick = 4717;
    private static final int GreaterHealTrick = 4718;
    private static final int GreaterHeal = 5195;
    private static final int BattleHeal = 5590;
    private static final int Recharge = 5200;
    private static final int Pet_Haste = 5186;
    private static final int Pet_Vampiric_Rage = 5187;
    private static final int Pet_Regeneration = 5188;
    private static final int Pet_Blessed_Body = 5189;
    private static final int Pet_Blessed_Soul = 5190;
    private static final int Pet_Guidance = 5191;
    private static final int Pet_Wind_Walk = 5192;
    private static final int Pet_Acumen = 5193;
    private static final int Pet_Empower = 5194;
    private static final int Pet_Concentration = 5201;
    private static final int Pet_Might = 5586;
    private static final int Pet_Shield = 5587;
    private static final int Pet_Focus = 5588;
    private static final int Pet_Death_Wisper = 5589;
    private static final int Pet_Weapon_Maintenance = 5987;
    private static final int Pet_Armor_Maintenance = 5988;
    private static final int WindShackle = 5196;
    private static final int Hex = 5197;
    private static final int Slow = 5198;
    private static final int CurseGloom = 5199;
    private static final Skill[][] COUGAR_BUFFS = {{SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3)}, {SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5189, 6)}, {SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5186, 2)}, {SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5186, 2), SkillTable.getInstance().getInfo(5187, 4), SkillTable.getInstance().getInfo(5588, 3)}};
    private static final Skill[][] BUFFALO_BUFFS = {{SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6)}, {SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5191, 3)}, {SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5191, 3), SkillTable.getInstance().getInfo(5187, 4), SkillTable.getInstance().getInfo(5186, 2)}, {SkillTable.getInstance().getInfo(5586, 3), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5191, 3), SkillTable.getInstance().getInfo(5187, 4), SkillTable.getInstance().getInfo(5186, 2), SkillTable.getInstance().getInfo(5588, 3), SkillTable.getInstance().getInfo(5589, 3)}};
    private static final Skill[][] KOOKABURRA_BUFFS = {{SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6)}, {SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3)}, {SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5201, 6)}, {SkillTable.getInstance().getInfo(5194, 3), SkillTable.getInstance().getInfo(5190, 6), SkillTable.getInstance().getInfo(5189, 6), SkillTable.getInstance().getInfo(5587, 3), SkillTable.getInstance().getInfo(5193, 3), SkillTable.getInstance().getInfo(5201, 6)}};

    private Future<?> _actionTask;
    private boolean _buffEnabled;

    public PetBabyInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control, final int _currentLevel, final long exp) {
        super(objectId, template, owner, control, _currentLevel, exp);
        _buffEnabled = true;
    }

    public PetBabyInstance(final int objectId, final NpcTemplate template, final Player owner, final ItemInstance control) {
        super(objectId, template, owner, control);
        _buffEnabled = true;
    }

    public Skill[] getBuffs() {
        switch (getNpcId()) {
            case 16036: {
                return COUGAR_BUFFS[getBuffLevel()];
            }
            case 16034: {
                return BUFFALO_BUFFS[getBuffLevel()];
            }
            case 16035: {
                return KOOKABURRA_BUFFS[getBuffLevel()];
            }
            default: {
                return Skill.EMPTY_ARRAY;
            }
        }
    }

    public Skill onActionTask() {
        try {
            final Player owner = getPlayer();
            if (!owner.isDead() && !owner.isInvul() && !isCastingNow()) {
                if (getEffectList().getEffectsCountForSkill(5753) > 0) {
                    return null;
                }
                if (getEffectList().getEffectsCountForSkill(5771) > 0) {
                    return null;
                }
                final boolean improved = PetDataTable.isImprovedBabyPet(getNpcId());
                Skill skill = null;
                if (!Config.ALT_PET_HEAL_BATTLE_ONLY || owner.isInCombat()) {
                    final double curHp = owner.getCurrentHpPercents();
                    if (curHp < 90.0 && Rnd.chance((100.0 - curHp) / 3.0)) {
                        if (curHp < 33.0) {
                            skill = SkillTable.getInstance().getInfo(improved ? 5590 : 4718, getHealLevel());
                        } else if (getNpcId() != 16035) {
                            skill = SkillTable.getInstance().getInfo(improved ? 5195 : 4717, getHealLevel());
                        }
                    }
                    if (skill == null && getNpcId() == 16035) {
                        final double curMp = owner.getCurrentMpPercents();
                        if (curMp < 66.0 && Rnd.chance((100.0 - curMp) / 2.0)) {
                            skill = SkillTable.getInstance().getInfo(5200, getRechargeLevel());
                        }
                    }
                    if (skill != null && skill.checkCondition(this, owner, false, !isFollowMode(), true)) {
                        setTarget(owner);
                        getAI().Cast(skill, owner, false, !isFollowMode());
                        return skill;
                    }
                }
                if (!improved || owner.isInOfflineMode() || owner.getEffectList().getEffectsCountForSkill(5771) > 0) {
                    return null;
                }
                outer:
                for (final Skill buff : getBuffs()) {
                    if (getCurrentMp() >= buff.getMpConsume2()) {
                        for (final Effect ef : owner.getEffectList().getAllEffects()) {
                            if (checkEffect(ef, buff)) {
                                continue outer;
                            }
                        }
                        if (buff.checkCondition(this, owner, false, !isFollowMode(), true)) {
                            setTarget(owner);
                            getAI().Cast(buff, owner, false, !isFollowMode());
                            return buff;
                        }
                        return null;
                    }
                }
            }
        } catch (Throwable e) {
            LOGGER.warn("Pet [#" + getNpcId() + "] a buff task error has occurred: " + e);
            LOGGER.error("", e);
        }
        return null;
    }

    private boolean checkEffect(final Effect ef, final Skill skill) {
        return ef != null && ef.isInUse() && EffectList.checkStackType(ef.getTemplate(), skill.getEffectTemplates()[0]) && ef.getStackOrder() >= skill.getEffectTemplates()[0]._stackOrder && (ef.getTimeLeft() > 10 || (ef.getNext() != null && checkEffect(ef.getNext(), skill)));
    }

    public synchronized void stopBuffTask() {
        if (_actionTask != null) {
            _actionTask.cancel(false);
            _actionTask = null;
        }
    }

    public synchronized void startBuffTask() {
        if (_actionTask != null) {
            stopBuffTask();
        }
        if (_actionTask == null && !isDead()) {
            _actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), 5000L);
        }
    }

    public boolean isBuffEnabled() {
        return _buffEnabled;
    }

    public void triggerBuff() {
        _buffEnabled = !_buffEnabled;
    }

    @Override
    protected void onDeath(final Creature killer) {
        stopBuffTask();
        super.onDeath(killer);
    }

    @Override
    public void doRevive() {
        super.doRevive();
        startBuffTask();
    }

    @Override
    public void unSummon() {
        stopBuffTask();
        super.unSummon();
    }

    public int getHealLevel() {
        return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 12), 1), 12);
    }

    public int getRechargeLevel() {
        return Math.min(Math.max((getLevel() - getMinLevel()) / ((80 - getMinLevel()) / 8), 1), 8);
    }

    public int getBuffLevel() {
        return Math.min(Math.max((getLevel() - 55) / 5, 0), 3);
    }

    @Override
    public int getSoulshotConsumeCount() {
        return 1;
    }

    @Override
    public int getSpiritshotConsumeCount() {
        return 1;
    }

    class ActionTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final Skill skill = onActionTask();
            _actionTask = ThreadPoolManager.getInstance().schedule(new ActionTask(), (skill == null) ? 1000L : ((long) (skill.getHitTime() * 333 / Math.max(getMAtkSpd(), 1) - 100)));
        }
    }
}
