package ru.j2dev.gameserver.stats;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.HitCondBonusHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillMagicType;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.model.base.BaseStats;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.base.HitCondBonusType;
import ru.j2dev.gameserver.model.base.SkillTrait;
import ru.j2dev.gameserver.model.instances.ReflectionBossInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.PositionUtils;

public class Formulas {
    public static double calcHpRegen(final Creature cha) {
        double init;
        if (cha.isPlayer()) {
            init = ((cha.getLevel() <= 10) ? (1.5 + cha.getLevel() / 20.0) : (1.4 + cha.getLevel() / 10.0)) * cha.getLevelMod();
        } else {
            init = cha.getTemplate().getBaseHpReg();
        }
        if (cha.isPlayable()) {
            init *= BaseStats.CON.calcBonus(cha);
            if (cha.isSummon()) {
                init *= 2.0;
            }
        }
        return cha.calcStat(Stats.REGENERATE_HP_RATE, init, null, null);
    }

    public static double calcMpRegen(final Creature cha) {
        double init;
        if (cha.isPlayer()) {
            init = (0.87 + cha.getLevel() * 0.03) * cha.getLevelMod();
        } else {
            init = cha.getTemplate().getBaseMpReg();
        }
        if (cha.isPlayable()) {
            init *= BaseStats.MEN.calcBonus(cha);
            if (cha.isSummon()) {
                init *= 2.0;
            }
        }
        return cha.calcStat(Stats.REGENERATE_MP_RATE, init, null, null);
    }

    public static double calcCpRegen(final Creature cha) {
        final double init = (1.5 + cha.getLevel() / 10) * cha.getLevelMod() * BaseStats.CON.calcBonus(cha);
        return cha.calcStat(Stats.REGENERATE_CP_RATE, init, null, null);
    }

    public static AttackInfo calcPhysDam(final Creature attacker, final Creature target, final Skill skill, final boolean dual, final boolean blow, final boolean ss, final boolean onCrit) {
        final AttackInfo info = new AttackInfo();
        info.damage = attacker.getPAtk(target);
        info.defence = target.getPDef(attacker);
        info.crit_static = attacker.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, skill);
        info.death_rcpt = 0.01 * target.calcStat(Stats.DEATH_VULNERABILITY, attacker, skill);
        info.lethal1 = ((skill == null) ? 0.0 : (skill.getLethal1() * info.death_rcpt));
        info.lethal2 = ((skill == null) ? 0.0 : (skill.getLethal2() * info.death_rcpt));
        info.crit = Rnd.chance(calcCrit(attacker, target, skill, blow));
        info.shld = ((skill == null || !skill.getShieldIgnore()) && calcShldUse(attacker, target));
        info.lethal = false;
        info.miss = false;
        final boolean isPvP = attacker.isPlayable() && target.isPlayable();
        if (info.shld) {
            info.defence += target.getShldDef();
        }
        info.defence = Math.max(info.defence, 1.0);
        if (skill != null) {
            if (!blow && !target.isLethalImmune()) {
                if (Rnd.chance(info.lethal1)) {
                    if (target.isPlayer()) {
                        info.lethal = true;
                        info.lethal_dmg = target.getCurrentCp();
                        target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
                    } else {
                        info.lethal_dmg = target.getCurrentHp() / 2.0;
                    }
                    attacker.sendPacket(Msg.HALF_KILL);
                } else if (Rnd.chance(info.lethal2)) {
                    if (target.isPlayer()) {
                        info.lethal = true;
                        info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1.1;
                        target.sendPacket(SystemMsg.LETHAL_STRIKE);
                    } else {
                        info.lethal_dmg = target.getCurrentHp() - 1.0;
                    }
                    attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
                }
            }
            if (skill.getPower(target) == 0.0) {
                info.damage = 0.0;
                return info;
            }
            if (blow && !skill.isBehind() && ss) {
                info.damage *= 2.04;
            }
            if (skill.isChargeBoost()) {
                info.damage = attacker.calcStat(Stats.SKILL_POWER, info.damage + skill.getPower(target), null, null);
            } else {
                info.damage += attacker.calcStat(Stats.SKILL_POWER, skill.getPower(target), null, null);
            }
            if (blow && skill.isBehind() && ss) {
                info.damage *= 1.5;
            }
            if (!skill.isChargeBoost()) {
                info.damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;
            }
            if (blow) {
                info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, skill);
                info.damage = target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, skill);
                info.damage += 6.1 * info.crit_static;
            }
            if (skill.isChargeBoost()) {
                info.damage *= 0.8 + 0.2 * (attacker.getIncreasedForce() + Math.max(skill.getNumCharges(), 0));
            }
            if (info.crit) {
                info.damage *= 2.0;
            }
        } else {
            info.damage *= 1.0 + (Rnd.get() * attacker.getRandomDamage() * 2.0 - attacker.getRandomDamage()) / 100.0;
            if (dual) {
                info.damage /= 2.0;
            }
            if (info.crit) {
                info.damage *= 0.01 * attacker.calcStat(Stats.CRITICAL_DAMAGE, target, null);
                info.damage = 2.0 * target.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, info.damage, attacker, null);
                info.damage += info.crit_static;
            }
        }
        if (skill == null || !skill.isChargeBoost()) {
            switch (PositionUtils.getDirectionTo(target, attacker)) {
                case BEHIND: {
                    info.damage *= 1.1;
                    break;
                }
                case SIDE: {
                    info.damage *= 1.05;
                    break;
                }
            }
        }
        if (ss) {
            info.damage *= (blow ? 1.0 : 2.0);
        }
        info.damage *= 70.0 / info.defence;
        info.damage = attacker.calcStat(Stats.PHYSICAL_DAMAGE, info.damage, target, skill);
        if (info.shld && Rnd.chance(5)) {
            info.damage = 1.0;
        }
        if (isPvP) {
            if (skill == null) {
                info.damage *= attacker.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0, null, null);
                info.damage /= target.calcStat(Stats.PVP_PHYS_DEFENCE_BONUS, 1.0, null, null);
            } else {
                info.damage *= attacker.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0, null, null);
                info.damage /= target.calcStat(Stats.PVP_PHYS_SKILL_DEFENCE_BONUS, 1.0, null, null);
            }
        }
        if (skill != null) {
            if (info.shld) {
                if (info.damage == 1.0) {
                    target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
                } else {
                    target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
                }
            }
            if (info.damage > 1.0 && !skill.hasEffects() && Rnd.chance(target.calcStat(Stats.PSKILL_EVASION, 0.0, attacker, skill))) {
                attacker.sendPacket(new SystemMessage(43));
                target.sendPacket(new SystemMessage(42).addName(attacker));
                info.damage = 0.0;
            }
            if (info.damage > 1.0 && skill.isDeathlink()) {
                info.damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
            }
            if (onCrit && !calcBlow(attacker, target, skill)) {
                info.miss = true;
                info.damage = 0.0;
                attacker.sendPacket(new SystemMessage(43));
            }
            if (blow) {
                if (Rnd.chance(info.lethal1)) {
                    if (target.isPlayer()) {
                        info.lethal = true;
                        info.lethal_dmg = target.getCurrentCp();
                        target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
                    } else if (target.isLethalImmune()) {
                        info.damage *= 2.0;
                    } else {
                        info.lethal_dmg = target.getCurrentHp() / 2.0;
                    }
                    attacker.sendPacket(Msg.HALF_KILL);
                } else if (Rnd.chance(info.lethal2)) {
                    if (target.isPlayer()) {
                        info.lethal = true;
                        info.lethal_dmg = target.getCurrentHp() + target.getCurrentCp() - 1.1;
                        target.sendPacket(SystemMsg.LETHAL_STRIKE);
                    } else if (target.isLethalImmune()) {
                        info.damage *= 3.0;
                    } else {
                        info.lethal_dmg = target.getCurrentHp() - 1.0;
                    }
                    attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
                }
            }
            if (info.damage > 0.0) {
                attacker.displayGiveDamageMessage(target, (int) info.damage, info.crit || blow, false, false, false);
            }
            if (target.isStunned() && calcStunBreak(attacker, target, info.crit)) {
                target.getEffectList().stopEffects(EffectType.Stun);
            }
            if (calcCastBreak(attacker, target, info.damage)) {
                target.abortCast(false, true);
            }
        }
        return info;
    }

    public static double calcMagicDam(final Creature attacker, final Creature target, final Skill skill, final int sps) {
        final boolean isPvP = attacker.isPlayable() && target.isPlayable();
        final boolean shield = skill.getShieldIgnore() && calcShldUse(attacker, target);
        double mAtk = attacker.getMAtk(target, skill);
        if (sps == 2) {
            mAtk *= 4.0;
        } else if (sps == 1) {
            mAtk *= 2.0;
        }
        double mdef = target.getMDef(null, skill);
        if (shield) {
            mdef += target.getShldDef();
        }
        if (mdef == 0.0) {
            mdef = 1.0;
        }
        double power = skill.getPower(target);
        double lethalDamage = 0.0;
        if (Rnd.chance(skill.getLethal1())) {
            if (target.isPlayer()) {
                lethalDamage = target.getCurrentCp();
                target.sendPacket(Msg.CP_DISAPPEARS_WHEN_HIT_WITH_A_HALF_KILL_SKILL);
            } else if (!target.isLethalImmune()) {
                lethalDamage = target.getCurrentHp() / 2.0;
            } else {
                power *= 2.0;
            }
            attacker.sendPacket(Msg.HALF_KILL);
        } else if (Rnd.chance(skill.getLethal2())) {
            if (target.isPlayer()) {
                lethalDamage = target.getCurrentHp() + target.getCurrentCp() - 1.1;
                target.sendPacket(SystemMsg.LETHAL_STRIKE);
            } else if (!target.isLethalImmune()) {
                lethalDamage = target.getCurrentHp() - 1.0;
            } else {
                power *= 3.0;
            }
            attacker.sendPacket(SystemMsg.YOUR_LETHAL_STRIKE_WAS_SUCCESSFUL);
        }
        if (power == 0.0) {
            if (lethalDamage > 0.0) {
                attacker.displayGiveDamageMessage(target, (int) lethalDamage, false, false, false, false);
            }
            return lethalDamage;
        }
        double damage = 91.0 * power * Math.sqrt(mAtk) / mdef;
        final boolean crit = calcMCrit(attacker, target, attacker.getMagicCriticalRate(target, skill));
        if (crit) {
            damage *= attacker.calcStat(Stats.MCRITICAL_DAMAGE, (attacker.isPlayable() && target.isPlayable()) ? Config.MCRITICAL_CRIT_POWER : Config.MCRITICAL_CRIT_POWER, target, skill);
        }
        damage = attacker.calcStat(Stats.MAGIC_DAMAGE, damage, target, skill);
        if (shield) {
            if (Rnd.chance(5)) {
                damage = 0.0;
                target.sendPacket(SystemMsg.YOUR_EXCELLENT_SHIELD_DEFENSE_WAS_A_SUCCESS);
                attacker.sendPacket(new SystemMessage(159).addName(attacker));
            } else {
                target.sendPacket(SystemMsg.YOUR_SHIELD_DEFENSE_HAS_SUCCEEDED);
                attacker.sendPacket(new SystemMessage(2151));
            }
        }
        final int mLevel = (skill.getMagicLevel() == 0) ? attacker.getLevel() : skill.getMagicLevel();
        final int levelDiff = target.getLevel() - mLevel;
        if (damage > 1.0 && skill.isDeathlink()) {
            damage *= 1.8 * (1.0 - attacker.getCurrentHpRatio());
        }
        if (damage > 1.0 && skill.isBasedOnTargetDebuff()) {
            damage *= 1.0 + 0.05 * Math.min(36, target.getEffectList().getAllEffects().size());
        }
        damage += lethalDamage;
        if (skill.getSkillType() == SkillType.MANADAM) {
            damage = Math.max(1.0, damage / 4.0);
        }
        if (isPvP && damage > 1.0) {
            damage *= attacker.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0, null, null);
            damage /= target.calcStat(Stats.PVP_MAGIC_SKILL_DEFENCE_BONUS, 1.0, null, null);
        }
        final boolean gradePenalty = attacker.isPlayer() && ((Player) attacker).getGradePenalty() > 0;
        final double lvlMod = 4.0 * Math.max(1.0, (target.getLevel() >= 80) ? ((levelDiff - 4) * 1.6) : ((double) ((levelDiff - 14) * 2)));
        final double magic_rcpt = target.calcStat(Stats.MAGIC_RESIST, attacker, skill) - attacker.calcStat(Stats.MAGIC_POWER, target, skill);
        final double failChance = gradePenalty ? 95.0 : Math.min(lvlMod * (1.0 + magic_rcpt / 100.0), 95.0);
        final double resistChance = gradePenalty ? 95.0 : (5 * Math.max(levelDiff - 10, 1));
        if (attacker.isPlayer() && ((Player) attacker).isDebug()) {
            attacker.sendMessage("Fail chance " + (int) failChance + "/" + (int) resistChance);
        }
        if (Rnd.chance(failChance)) {
            if (Rnd.chance(resistChance)) {
                damage = 0.0;
                final SystemMessage msg = new SystemMessage(158);
                attacker.sendPacket(msg);
                final SystemMessage msg2 = new SystemMessage(159).addName(attacker);
                target.sendPacket(msg2);
            } else {
                damage /= 2.0;
                final SystemMessage msg = new SystemMessage(158);
                attacker.sendPacket(msg);
                final SystemMessage msg2 = new SystemMessage(159).addName(attacker);
                target.sendPacket(msg2);
            }
        }
        if (damage > 0.0) {
            attacker.displayGiveDamageMessage(target, (int) damage, crit, false, false, true);
        }
        if (calcCastBreak(attacker, target, damage)) {
            target.abortCast(false, true);
        }
        return damage;
    }

    public static boolean calcStunBreak(final Creature activeChar, final Creature target, final boolean crit) {
        double chance = crit ? 25.0 : 10.0 - (activeChar.calcStat(Stats.STUN_POWER, 0., activeChar, null) - target.calcStat(Stats.STUN_RESIST, 0., target, null)) * 0.5;
        chance = Math.min(chance, crit ? 50.0 : 25.0);
        chance = Math.max(1.0, chance);
        return Rnd.chance(chance);
    }

    /**
     * Returns true in case of fatal blow success
     */
    private static boolean calcBlowOld(final Creature activeChar, final Creature target, final Skill skill) {
        final WeaponTemplate weapon = activeChar.getActiveWeaponItem();

        final double base_weapon_crit = weapon == null ? 4. : weapon.getCritical();
        final double dex_bonus = BaseStats.DEX.calcBonus(activeChar);
        final double crit_height_bonus = calcCriticalHeightBonus(activeChar, target);
        final double buffs_mult = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
        final double skill_mod = skill.isBehind() ? 3.0 : 2.0;

        double chance = dex_bonus * base_weapon_crit * buffs_mult * crit_height_bonus * skill_mod;

        if (!target.isInCombat()) {
            chance *= 1.1;
        }

        switch (PositionUtils.getDirectionTo(target, activeChar)) {
            case BEHIND:
                if (skill.isBehind()) {
                    chance = 100.0;
                } else {
                    chance *= 1.1;
                }
                break;
            case SIDE:
                chance *= 1.05;
                break;
            case NONE:
                break;
            case FRONT:
                if (skill.isBehind()) {
                    chance = 3.0;
                }
                break;
        }
        chance = Math.max(chance, Math.min(chance, skill.isBehind() ? 3 : 5));
        chance = Math.max(Math.min(chance, skill.isBehind() ? 100 : 80), skill.isBehind() ? 3 : 5);

        final boolean success = Rnd.get(1000) <= chance * 10;
        // Отображение шанса прохождения скила
        if (Config.SHOW_SKILL_CHANCE && activeChar.isPlayer() && (activeChar.getPlayer().getVarB("SkillsHideChance") || activeChar.getPlayer().isGM())) {
            activeChar.sendMessage(new CustomMessage("ru.j2dev.gameserver.skills.Formulas.Chance", activeChar.getPlayer()).addString(skill.getName()).addNumber((long) chance));
        }
        if (activeChar.getPlayer() != null && activeChar.getPlayer().isGM()) {
            activeChar.sendMessage("[F] skill: " + skill + " is " + success);
            activeChar.sendMessage("[F] chance: " + (int) chance);
            activeChar.sendMessage("[F] direction: " + PositionUtils.getDirectionTo(target, activeChar));
        }
        if (success && activeChar.isPlayer()) {
            activeChar.sendPacket(new PlaySound("skillsound.critical_hit_02"));
        }

        return success;
    }

    /**
     * Returns true in case of fatal blow success
     */
    public static boolean calcBlowChance(Creature activeChar, Creature target, Skill skill) {
        double chance;

        WeaponTemplate weapon = activeChar.getActiveWeaponItem();
        if (weapon != null)
            chance = weapon.getCritical();
        else
            chance = activeChar.getTemplate().getBaseCritRate();

        int effectBonus = skill.getBaseBlowRate();
        chance *= BaseStats.DEX.calcBonus(activeChar);
        chance *= calcCriticalHeightBonus(activeChar, target);
        chance *= calcCriticalRatePosBonus(activeChar, target);
        chance *= (effectBonus + 100) / 100;
        chance = activeChar.calcStat(Stats.FATALBLOW_RATE, chance, null, null);
        chance = Math.min(chance, 80); // Blow Chance Cap


        final boolean success = Rnd.chance(chance);
        // Отображение шанса прохождения скила
        if (Config.SHOW_SKILL_CHANCE && activeChar.isPlayer() && (activeChar.getPlayer().getVarB("SkillsHideChance") || activeChar.getPlayer().isGM())) {
            activeChar.sendMessage(new CustomMessage("ru.j2dev.gameserver.skills.Formulas.Chance", activeChar.getPlayer()).addString(skill.getName()).addNumber((long) chance));
        }
        if (activeChar.getPlayer() != null && activeChar.getPlayer().isGM()) {
            activeChar.sendMessage("[F] skill: " + skill + " is " + success);
            activeChar.sendMessage("[F] chance: " + chance);
            activeChar.sendMessage("[F] direction: " + PositionUtils.getDirectionTo(target, activeChar));
        }
        if (success && activeChar.isPlayer()) {
            activeChar.sendPacket(new PlaySound("skillsound.critical_hit_02"));
        }

        return success;
    }

    /**
     * Returns true in case of fatal blow success
     */
    public static boolean calcBlow(Creature activeChar, Creature target, Skill skill) {
        double chance;

        WeaponTemplate weapon = activeChar.getActiveWeaponItem();

        final double dex_bonus = BaseStats.DEX.calcBonus(activeChar);
        final double base_weapon_crit = weapon == null ? activeChar.getTemplate().getBaseCritRate() / 10 : weapon.getCritical();

        final int base_rate = skill.getBaseBlowRate(); // TODO: PTS реализация эффектов
        final double base_skill_blowrate = (base_rate + 100) / 100;
        final double crit_height_bonus = calcCriticalHeightBonus(activeChar, target);
        final double fatal_blow_rate_multiplier = activeChar.calcStat(Stats.FATALBLOW_RATE, target, skill);
        final double skill_res_multiplier = (1 - target.calcStat(Stats.PSKILL_EVASION, activeChar, skill) / 100);

        // dex бонус. применяем кап.
        chance = dex_bonus;
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);

        // бонус оружия. применяем кап.
        chance *= base_weapon_crit;
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);

        // скил бонус. применяем кап.
        chance *= base_skill_blowrate;
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);

        // бонус расположения. применяем кап.
        chance *= crit_height_bonus;
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);

        // бонус от баффов. применяем кап.
        chance *= fatal_blow_rate_multiplier;
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);

        // резисты. применяем кап.
        chance *= skill_res_multiplier;
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);

        if (!target.isInCombat()) {
            chance *= 1.1;
            chance = Math.min(skill.isBehind() ? 100 : 80, chance);
        }

        switch (PositionUtils.getDirectionTo(target, activeChar)) {
            case BEHIND:
                chance *= 1.1;
                break;
            case SIDE:
                chance *= 1.05;
                break;
            case NONE:
                break;
            case FRONT:
                if (skill.isBehind()) {
                    chance = 3.0;
                }
                break;
        }
        chance = Math.min(skill.isBehind() ? 100 : 80, chance);
        final boolean success = Rnd.chance(chance);

        // Отображение шанса прохождения скила
        if (Config.SHOW_SKILL_CHANCE && activeChar.isPlayer() && (activeChar.getPlayer().getVarB("SkillsHideChance") || activeChar.getPlayer().isGM())) {
            activeChar.sendMessage(new CustomMessage("ru.j2dev.gameserver.skills.Formulas.Chance", activeChar.getPlayer()).addString(skill.getName()).addNumber((long) chance));
        }
        if (activeChar.getPlayer() != null && activeChar.getPlayer().isGM()) {
            activeChar.sendMessage("[F] skill: " + skill + " is " + success);
            activeChar.sendMessage("[F] chance: " + chance);
            activeChar.sendMessage("[F] direction: " + PositionUtils.getDirectionTo(target, activeChar));
        }
        if (success && activeChar.isPlayer()) {
            activeChar.sendPacket(new PlaySound("skillsound.critical_hit_02"));
        }

        return success;
    }

    private static double calcCriticalHeightBonus(Creature attacker, Creature target) {
        int diffZ = attacker.getZ() - target.getZ();
        diffZ = Math.min(25, Math.max(-25, diffZ));
        return (diffZ * 4.0 / 5.0 + 10) / 100 + 1;
    }

    private static double calcCriticalRatePosBonus(Creature attacker, Creature target) {
        double res = 0;
        switch (PositionUtils.getDirectionTo(target, attacker)) {
            case FRONT:
                attacker.calcStat(Stats.CRITICAL_RATE_FRONT_BONUS, 1, target, null);

                break;
            case SIDE:
                res = attacker.calcStat(Stats.CRITICAL_RATE_SIDE_BONUS, 1.1, target, null);
                break;
            case BEHIND:
                res = attacker.calcStat(Stats.CRITICAL_RATE_BACK_BONUS, 1.3, target, null);
                break;
        }

        return res;
    }

    public static double[] calcCriticalPosBonus(Creature attacker, Creature target) {
        double[] res = new double[3];
        switch (PositionUtils.getDirectionTo(target, attacker)) {
            case FRONT:
                res[0] = 0.;
                res[1] = (attacker.calcStat(Stats.BLOW_CRITICAL_DAMAGE_FRONT, 1, target, null) - 1) * 0.5 + 1;
                res[2] = 0; // must be a p_critical_damage_position diff
                break;
            case SIDE:
                res[0] = 0.05;
                res[1] = (attacker.calcStat(Stats.BLOW_CRITICAL_DAMAGE_SIDE, 1, target, null) - 1) * 0.5 + 1;
                res[2] = 0;
                break;
            case BEHIND:
                res[0] = 0.2;
                res[1] = (attacker.calcStat(Stats.BLOW_CRITICAL_DAMAGE_BACK, 1, target, null) - 1) * 0.5 + 1;
                res[2] = 0;
                break;
        }

        return res;
    }

    public static double calcCrit(final Creature attacker, final Creature target, final Skill skill, final boolean blow) {
        if (attacker.isPlayer() && attacker.getActiveWeaponItem() == null) {
            return 0.0;
        }
        if (skill != null) {
            return skill.getCriticalRate() * (blow ? BaseStats.STR.calcBonus(attacker) : BaseStats.STR.calcBonus(attacker)) * 0.01 * attacker.calcStat(Stats.SKILL_CRIT_CHANCE_MOD, target, skill);
        }
        double rate = attacker.getCriticalHit(target, null) * 0.01 * target.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, attacker, skill);
        switch (PositionUtils.getDirectionTo(target, attacker)) {
            case BEHIND: {
                rate *= 1.1;
                break;
            }
            case SIDE: {
                rate *= 1.05;
                break;
            }
        }
        return rate / 10.0;
    }

    public static boolean calcMCrit(final Creature attacker, final Creature target, final double mRate) {
        // floating point random gives more accuracy calculation, because argument also floating point
        if (attacker != null && attacker.isNpc()) {
            return Rnd.get() * 100.0 <= Math.min(Config.ALT_NPC_LIM_MCRIT, mRate);
        }
        return Rnd.get() * 100.0 <= Math.min(Config.LIM_MCRIT, mRate);
    }

    /**
     * @param attacker - тот кто атакует
     * @param target   - тот кто кастует
     * @param damage   - дамаг который прилетел
     * @return - Возращает шанс сбития каста (офлайк формула сбития каста (c) PaInKiLlEr)
     */
    public static boolean calcCastBreak(final Creature attacker, final Creature target, final double damage) {
        // формула не действует на рейдов или на цель которая не кастует скил
        if (target == null || target.isInvul() || target.isRaid() || !target.isCastingNow()) {
            return false;
        }

        final Skill skill = target.getCastingSkill();

        if (skill == null)
            return false;

        // TODO: убрать куда-то
        if (skill.getSkillType() == SkillType.TAKECASTLE) {
            return false;
        }

        // Сбиты могут быть только те скиллы, у которых параметр is_magic не равен 0
        if (skill.getMagicType() == SkillMagicType.PHYSIC) {
            return false;
        }

        final double reduce_interrupt_multiplier = target.calcStat(Stats.CAST_INTERRUPT, 1, null, null);
        final double reduce_interrupt_adder = target.calcStat(Stats.CAST_INTERRUPT, 0, null, null);
        final int base_interrupt_factor = (int) (100 * Math.floor(damage * reduce_interrupt_multiplier + reduce_interrupt_adder) / target.getMaxHp());
        // floor - округлить вниз от того, что стоит в скобках.
        // damage - дамаг, который прилетел кастеру.
        // getMaxHp() - максимальное хп кастера.
        // reduce_interrupt_adder - сумма статы Stats.CAST_INTERRUPT
        // reduce_interrupt_multiplier - множитель статы Stats.CAST_INTERRUPT

        // Теперь посчитаем level_factor
        final double level_factor = target.getLevel() + 0.125 * target.getMEN() - attacker.getLevel();
        double final_interrupt_chance = 0;
        if (level_factor >= base_interrupt_factor) {
            final_interrupt_chance = 5;
        }

        if (level_factor < base_interrupt_factor) {
            final_interrupt_chance = Math.min(Math.floor((2 * base_interrupt_factor)), 98);
        }

        return Rnd.chance(final_interrupt_chance);
    }

    public static int calcPAtkSpd(final double rate) {
        final double base = 500.0 / rate;
        int result = (int) (base * 1000.0 * 0.9777777791023254);
        if (base * 1000.0 > result) {
            result += (int) (-result - base * -1000.0);
        }
        return result;
    }

    public static int calcMAtkSpd(final Creature attacker, final Skill skill, final double skillTime) {
        if (skill.isMagic()) {
            return (int) (skillTime * 333.0 / Math.max(attacker.getMAtkSpd(), 1));
        }
        return (int) (skillTime * 333.0 / Math.max(attacker.getPAtkSpd(), 1));
    }

    public static double calcCastSpeedFactor(Creature attacker, Skill skill) {
        if (skill.isSkillTimePermanent()) {
            return 1;
        }
        if (skill.isMagic()) {
            return attacker.getMAtkSpd() * (attacker.getChargedSpiritShot() > 0 ? 1.40 : 1.) / 333.;
        }

        return attacker.getPAtkSpd() / 333.;
    }

    public static long calcSkillReuseDelay(final Creature actor, final Skill skill) {
        long reuseDelay = skill.getReuseDelay();
        if (actor.isMonster()) {
            reuseDelay = skill.getReuseForMonsters();
        }
        if (skill.isReuseDelayPermanent() || skill.isHandler() || skill.isItemSkill()) {
            return reuseDelay;
        }
        if (actor.getSkillMastery(skill.getId()) == 1) {
            actor.removeSkillMastery(skill.getId());
            return 0L;
        }
        if (skill.isMusic()) {
            return (long) actor.calcStat(Stats.MUSIC_REUSE_RATE, reuseDelay, null, skill) * 333L / Math.max(actor.getMAtkSpd(), 1);
        }
        if (skill.isMagic()) {
            return (long) actor.calcStat(Stats.MAGIC_REUSE_RATE, reuseDelay, null, skill) * 333L / Math.max(actor.getMAtkSpd(), 1);
        }
        return (long) actor.calcStat(Stats.PHYSIC_REUSE_RATE, reuseDelay, null, skill) * 333L / Math.max(actor.getPAtkSpd(), 1);
    }

    public static boolean calcHitMiss(final Creature attacker, final Creature target) {
        int chance = (80 + (2 * (attacker.getAccuracy() - target.getEvasionRate(attacker)))) * 10;
        chance = Math.max(chance, 200);
        chance = Math.min(chance, 980);
        chance *= getConditionBonus(attacker, target);
        return Rnd.get(Config.HIT_MISS_MODIFICATOR) > chance;
    }

    private static double getConditionBonus(final Creature attacker, final Creature target) {
        double mod = 100.0;
        if (attacker.getZ() - target.getZ() > 50) {
            mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.HIGH);
        } else if (attacker.getZ() - target.getZ() < -50) {
            mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.LOW);
        }
        if (GameTimeController.getInstance().isNowNight()) {
            mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.DARK);
        }

        switch (PositionUtils.getDirectionTo(attacker, target)) {
            case BEHIND: {
                mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.BACK);
                break;
            }
            case SIDE: {
                mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.SIDE);
                break;
            }
            default: {
                mod += HitCondBonusHolder.getInstance().getHitCondBonus(HitCondBonusType.AHEAD);
                break;
            }
        }
        return Math.max(mod / 100.0, 0.0);
    }

    public static boolean calcShldUse(final Creature attacker, final Creature target) {
        final WeaponTemplate template = target.getSecondaryWeaponItem();
        if (template == null || template.getItemType() != WeaponType.NONE) {
            return false;
        }
        final int angle = (int) target.calcStat(Stats.SHIELD_ANGLE, attacker, null);
        return PositionUtils.isFacing(target, attacker, angle) && Rnd.chance((int) target.calcStat(Stats.SHIELD_RATE, attacker, null));
    }

    public static boolean calcSkillSuccess(final Env env, final EffectTemplate et, final int spiritshot) {
        if (env.value == -1.0) {
            return true;
        }
        env.value = Math.max(Math.min(env.value, 100.0), 1.0);
        final double base = env.value;
        final Skill skill = env.skill;
        if (!skill.isOffensive() && skill != null) {
            if (env.character.isPlayer() && Config.SHOW_SKILL_CHANCE) {
                final Player player = env.character.getPlayer();
                // Отображение шанса прохождения скила
                if (player != null && env.skill != null && player.getVarB("SkillsHideChance")) {
                    showSkillChance(player, env.skill, et, env.value);
                }
            }
            return Rnd.chance(env.value);
        }
        final Creature caster = env.character;
        final Creature target = env.target;
        boolean debugCaster = false;
        boolean debugTarget = false;
        boolean debugGlobal = false;
        if (Config.ALT_DEBUG_ENABLED) {
            debugCaster = (caster.getPlayer() != null && caster.getPlayer().isDebug());
            debugTarget = (target.getPlayer() != null && target.getPlayer().isDebug());
            final boolean debugPvP = Config.ALT_DEBUG_PVP_ENABLED && debugCaster && debugTarget && (!Config.ALT_DEBUG_PVP_DUEL_ONLY || (caster.getPlayer().isInDuel() && target.getPlayer().isInDuel()));
            debugGlobal = (debugPvP || (Config.ALT_DEBUG_PVE_ENABLED && ((debugCaster && target.isMonster()) || (debugTarget && caster.isMonster()))));
        }
        double statMod = 1.0;
        if (skill.getSaveVs() != null) {
            statMod = skill.getSaveVs().calcChanceMod(target);
            env.value *= statMod;
        }
        env.value = Math.max(env.value, 1.0);
        double mAtkMod = 1.0;
        int ssMod;
        if (skill.isMagic()) {
            final int mdef = Math.max(1, target.getMDef(target, skill));
            double matk = caster.getMAtk(target, skill);
            if (skill.isSSPossible()) {
                switch (spiritshot) {
                    case 2: {
                        ssMod = 4;
                        break;
                    }
                    case 1: {
                        ssMod = 2;
                        break;
                    }
                    default: {
                        ssMod = 1;
                        break;
                    }
                }
                matk *= ssMod;
            }
            mAtkMod = Config.SKILLS_CHANCE_MOD * Math.pow(matk, Config.SKILLS_CHANCE_POW) / mdef;
            env.value *= mAtkMod;
            env.value = Math.max(env.value, 1.0);
        }
        double lvlDependMod = skill.getLevelModifier();
        if (lvlDependMod != 0.0) {
            final int attackLevel = (skill.getMagicLevel() > 0) ? skill.getMagicLevel() : caster.getLevel();
            lvlDependMod = 1.0 + (attackLevel - target.getLevel()) * 0.03 * lvlDependMod;
            if (lvlDependMod < 0.0) {
                lvlDependMod = 0.0;
            } else if (lvlDependMod > 2.0) {
                lvlDependMod = 2.0;
            }
            env.value *= lvlDependMod;
        }
        double vulnMod = 0.0;
        double profMod = 0.0;
        double resMod = 1.0;
        double debuffMod = 1.0;
        if (!skill.isIgnoreResists()) {
            debuffMod = 1.0 - target.calcStat(Stats.DEBUFF_RESIST, caster, skill) / 120.0;
            if (debuffMod != 1.0) {
                if (debuffMod == Double.NEGATIVE_INFINITY) {
                    if (debugGlobal) {
                        if (debugCaster) {
                            caster.getPlayer().sendMessage("Full debuff immunity");
                        }
                        if (debugTarget) {
                            target.getPlayer().sendMessage("Full debuff immunity");
                        }
                    }
                    return false;
                }
                if (debuffMod == Double.POSITIVE_INFINITY) {
                    if (debugGlobal) {
                        if (debugCaster) {
                            caster.getPlayer().sendMessage("Full debuff vulnerability");
                        }
                        if (debugTarget) {
                            target.getPlayer().sendMessage("Full debuff vulnerability");
                        }
                    }
                    return true;
                }
                debuffMod = Math.max(debuffMod, 0.0);
                env.value *= debuffMod;
            }
            final SkillTrait trait = skill.getTraitType();
            if (trait != null) {
                vulnMod = trait.calcVuln(env);
                profMod = trait.calcProf(env);
                final double maxResist = 90.0 + profMod * 0.85;
                resMod = (maxResist - vulnMod) / 60.0;
            }
            if (resMod != 1.0) {
                if (resMod == Double.NEGATIVE_INFINITY) {
                    if (debugGlobal) {
                        if (debugCaster) {
                            caster.getPlayer().sendMessage("Full immunity");
                        }
                        if (debugTarget) {
                            target.getPlayer().sendMessage("Full immunity");
                        }
                    }
                    return false;
                }
                if (resMod == Double.POSITIVE_INFINITY) {
                    if (debugGlobal) {
                        if (debugCaster) {
                            caster.getPlayer().sendMessage("Full vulnerability");
                        }
                        if (debugTarget) {
                            target.getPlayer().sendMessage("Full vulnerability");
                        }
                    }
                    return true;
                }
                resMod = Math.max(resMod, 0.0);
                env.value *= resMod;
            }
        }
        double elementMod = 0.0;
        final Element element = skill.getElement();
        if (element != Element.NONE) {
            elementMod = skill.getElementPower();
            final Element attackElement = getAttackElement(caster, target);
            if (attackElement == element) {
                elementMod += caster.calcStat(element.getAttack(), 0.0);
            }
            elementMod -= target.calcStat(element.getDefence(), 0.0);
            elementMod = Math.round(elementMod / 10.0);
            env.value += elementMod;
        }
        env.value = Math.max(env.value, Math.min(base, Config.SKILLS_CHANCE_MIN));
        env.value = Math.max(Math.min(env.value, Config.SKILLS_CHANCE_CAP), 1.0);
        if (caster.isPlayer() && Config.SHOW_SKILL_CHANCE) {
            final Player player = caster.getPlayer();
            // Отображение шанса прохождения скила
            if (player != null && skill != null && player.getVarB("SkillsHideChance")) {
                showSkillChance(player, skill, et, env.value);
            }
        }
        final boolean result = Rnd.chance((int) env.value);
        if (debugGlobal) {
            final StringBuilder stat = new StringBuilder(100);
            if (et == null) {
                stat.append(skill.getName());
            } else {
                stat.append(et._effectType.name());
            }
            stat.append(" AR:");
            stat.append((int) base);
            stat.append(" ");
            if (skill.getSaveVs() != null) {
                stat.append(skill.getSaveVs().name());
                stat.append(":");
                stat.append(String.format("%1.1f", statMod));
            }
            if (skill.isMagic()) {
                stat.append(" ");
                stat.append(" mAtk:");
                stat.append(String.format("%1.1f", mAtkMod));
            }
            if (skill.getTraitType() != null) {
                stat.append(" ");
                stat.append(skill.getTraitType().name());
            }
            stat.append(" ");
            stat.append(String.format("%1.1f", resMod));
            stat.append("(");
            stat.append(String.format("%1.1f", profMod));
            stat.append("/");
            stat.append(String.format("%1.1f", vulnMod));
            if (debuffMod != 0.0) {
                stat.append("+");
                stat.append(String.format("%1.1f", debuffMod));
            }
            stat.append(") lvl:");
            stat.append(String.format("%1.1f", lvlDependMod));
            stat.append(" elem:");
            stat.append((int) elementMod);
            stat.append(" Chance:");
            stat.append(String.format("%1.1f", env.value));
            if (!result) {
                stat.append(" failed");
            }
            if (debugCaster) {
                caster.getPlayer().sendMessage(stat.toString());
            }
            if (debugTarget) {
                target.getPlayer().sendMessage(stat.toString());
            }
        }
        return result;
    }

    public static boolean calcSkillSuccess(final Creature player, final Creature target, final Skill skill, final int activateRate) {
        final Env env = new Env();
        env.character = player;
        env.target = target;
        env.skill = skill;
        env.value = activateRate;
        return calcSkillSuccess(env, null, player.getChargedSpiritShot());
    }

    public static void calcSkillMastery(final Skill skill, final Creature activeChar) {
        if (skill.isHandler()) {
            return;
        }
        if ((activeChar.getSkillLevel(331) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getINT(), null, skill) >= Rnd.get(5000)) || (activeChar.getSkillLevel(330) > 0 && activeChar.calcStat(Stats.SKILL_MASTERY, activeChar.getSTR(), null, skill) >= Rnd.get(5000))) {
            final SkillType type = skill.getSkillType();
            int masteryLevel;
            if (skill.isMusic() || type == SkillType.BUFF || type == SkillType.HOT || type == SkillType.HEAL_PERCENT) {
                masteryLevel = 2;
            } else if (type == SkillType.HEAL) {
                masteryLevel = 3;
            } else {
                masteryLevel = 1;
            }
            if (masteryLevel > 0) {
                activeChar.setSkillMastery(skill.getId(), masteryLevel);
            }
        }
    }

    public static double calcDamageResists(final Skill skill, final Creature attacker, final Creature defender, double value) {
        if (attacker == defender) {
            return value;
        }
        if (attacker.isBoss()) {
            value *= Config.RATE_EPIC_ATTACK;
        } else if (attacker.isRaid() || attacker instanceof ReflectionBossInstance) {
            value *= Config.RATE_RAID_ATTACK;
        }
        if (defender.isBoss()) {
            value /= Config.RATE_EPIC_DEFENSE;
        } else if (defender.isRaid() || defender instanceof ReflectionBossInstance) {
            value /= Config.RATE_RAID_DEFENSE;
        }
        final Player pAttacker = attacker.getPlayer();
        final int diff = defender.getLevel() - ((pAttacker != null) ? pAttacker.getLevel() : attacker.getLevel());
        if (attacker.isPlayable() && defender.isMonster() && defender.getLevel() >= 78 && diff > 2) {
            value *= 0.7 / Math.pow(diff - 2, 0.25);
        }
        Element element;
        double power = 0.0;
        if (skill != null) {
            element = skill.getElement();
            power = skill.getElementPower();
        } else {
            element = getAttackElement(attacker, defender);
        }
        if (element == Element.NONE) {
            return value;
        }
        if (pAttacker != null && pAttacker.isGM() && Config.DEBUG) {
            pAttacker.sendMessage("Element: " + element.name());
            pAttacker.sendMessage("Attack: " + attacker.calcStat(element.getAttack(), power));
            pAttacker.sendMessage("Defence: " + defender.calcStat(element.getDefence(), 0.0));
            pAttacker.sendMessage("Modifier: " + getElementMod(defender.calcStat(element.getDefence(), 0.0), attacker.calcStat(element.getAttack(), power)));
        }
        return value * getElementMod(defender.calcStat(element.getDefence(), 0.0), attacker.calcStat(element.getAttack(), power));
    }

    private static double getElementMod(double defense, double attack) {
        if (defense < 0.0) {
            attack += -defense;
            defense = 0.0;
        }
        final double attrAtk = 1.0 + attack / 100.0;
        final double attrDef = 1.0 + defense / 100.0;
        return attrAtk / attrDef;
    }

    public static Element getAttackElement(final Creature attacker, final Creature target) {
        double max = Double.MIN_VALUE;
        Element result = Element.NONE;
        for (final Element e : Element.VALUES) {
            double val = attacker.calcStat(e.getAttack(), 0.0, null, null);
            if (val > 0.0) {
                if (target != null) {
                    val -= target.calcStat(e.getDefence(), 0.0, null, null);
                }
                if (val > max) {
                    result = e;
                    max = val;
                }
            }
        }
        return result;
    }

    private static void showSkillChance(final Player player, final Skill skill, final EffectTemplate et, final double chance) {
        player.sendMessage(new CustomMessage("ru.j2dev.gameserver.skills.Formulas.Chance", player).addString(et == null ? skill.getName() : skill.getName() + " effect: " + et._effectType.name()).addNumber(Math.round(chance)));

    }

    public static class AttackInfo {
        public double damage;
        public double defence;
        public double crit_static;
        public double death_rcpt;
        public double lethal1;
        public double lethal2;
        public double lethal_dmg;
        public boolean crit;
        public boolean shld;
        public boolean lethal;
        public boolean miss;

        public AttackInfo() {
            damage = 0.0;
            defence = 0.0;
            crit_static = 0.0;
            death_rcpt = 0.0;
            lethal1 = 0.0;
            lethal2 = 0.0;
            lethal_dmg = 0.0;
            crit = false;
            shld = false;
            lethal = false;
            miss = false;
        }
    }
}
