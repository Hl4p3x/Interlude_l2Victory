package ru.j2dev.gameserver.handler.voicecommands.impl;

import org.apache.commons.lang3.text.StrBuilder;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.templates.item.WeaponTemplate.WeaponType;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.text.NumberFormat;
import java.util.Locale;

public class WhoAmI implements IVoicedCommandHandler {
    private final String[] _commandList;

    public WhoAmI() {
        _commandList = new String[]{"whoami", "whoiam"};
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String args) {
        final Creature target = null;
        if (Config.SERVICES_WHOIAM_COMMAND_ENABLE || player.isGM()) {
            final double hpRegen = Formulas.calcHpRegen(player);
            final double cpRegen = Formulas.calcCpRegen(player);
            final double mpRegen = Formulas.calcMpRegen(player);
            final double hpDrain = player.calcStat(Stats.ABSORB_DAMAGE_PERCENT, 0.0, target, null);
            final double hpGain = player.calcStat(Stats.HEAL_EFFECTIVNESS, 100.0, target, null);
            final double mpGain = player.calcStat(Stats.MANAHEAL_EFFECTIVNESS, 100.0, target, null);
            final double critPerc = 2.0 * player.calcStat(Stats.CRITICAL_DAMAGE, target, null);
            final double critStatic = player.calcStat(Stats.CRITICAL_DAMAGE_STATIC, target, null);
            final double mCritRate = player.calcStat(Stats.MCRITICAL_RATE, target, null);
            final double blowRate = player.calcStat(Stats.FATALBLOW_RATE, target, null);
            final ItemInstance shld = player.getSecondaryWeaponInstance();
            final boolean shield = shld != null && shld.getItemType() == WeaponType.NONE;
            final double shieldDef = shield ? player.calcStat(Stats.SHIELD_DEFENCE, player.getTemplate().getBaseShldDef(), target, null) : 0.0;
            final double shieldRate = shield ? player.calcStat(Stats.SHIELD_RATE, target, null) : 0.0;
            final double xpRate = player.getRateExp();
            final double spRate = player.getRateSp();
            final double dropRate = player.getRateItems();
            final double adenaRate = player.getRateAdena();
            final double spoilRate = player.getRateSpoil();
            final double fireResist = player.calcStat(Element.FIRE.getDefence(), 0.0, target, null);
            final double windResist = player.calcStat(Element.WIND.getDefence(), 0.0, target, null);
            final double waterResist = player.calcStat(Element.WATER.getDefence(), 0.0, target, null);
            final double earthResist = player.calcStat(Element.EARTH.getDefence(), 0.0, target, null);
            final double holyResist = player.calcStat(Element.HOLY.getDefence(), 0.0, target, null);
            final double unholyResist = player.calcStat(Element.UNHOLY.getDefence(), 0.0, target, null);
            final double bleedPower = player.calcStat(Stats.BLEED_POWER, target, null);
            final double bleedResist = player.calcStat(Stats.BLEED_RESIST, target, null);
            final double poisonPower = player.calcStat(Stats.POISON_POWER, target, null);
            final double poisonResist = player.calcStat(Stats.POISON_RESIST, target, null);
            final double stunPower = player.calcStat(Stats.STUN_POWER, target, null);
            final double stunResist = player.calcStat(Stats.STUN_RESIST, target, null);
            final double rootPower = player.calcStat(Stats.ROOT_POWER, target, null);
            final double rootResist = player.calcStat(Stats.ROOT_RESIST, target, null);
            final double sleepPower = player.calcStat(Stats.SLEEP_POWER, target, null);
            final double sleepResist = player.calcStat(Stats.SLEEP_RESIST, target, null);
            final double paralyzePower = player.calcStat(Stats.PARALYZE_POWER, target, null);
            final double paralyzeResist = player.calcStat(Stats.PARALYZE_RESIST, target, null);
            final double mentalPower = player.calcStat(Stats.MENTAL_POWER, target, null);
            final double mentalResist = player.calcStat(Stats.MENTAL_RESIST, target, null);
            final double debuffPower = player.calcStat(Stats.DEBUFF_POWER, target, null);
            final double debuffResist = player.calcStat(Stats.DEBUFF_RESIST, target, null);
            final double cancelPower = player.calcStat(Stats.CANCEL_POWER, target, null);
            final double cancelResist = player.calcStat(Stats.CANCEL_RESIST, target, null);
            final double swordResist = 100.0 - player.calcStat(Stats.SWORD_WPN_VULNERABILITY, target, null);
            final double dualResist = 100.0 - player.calcStat(Stats.DUAL_WPN_VULNERABILITY, target, null);
            final double bluntResist = 100.0 - player.calcStat(Stats.BLUNT_WPN_VULNERABILITY, target, null);
            final double daggerResist = 100.0 - player.calcStat(Stats.DAGGER_WPN_VULNERABILITY, target, null);
            final double bowResist = 100.0 - player.calcStat(Stats.BOW_WPN_VULNERABILITY, target, null);
            final double poleResist = 100.0 - player.calcStat(Stats.POLE_WPN_VULNERABILITY, target, null);
            final double fistResist = 100.0 - player.calcStat(Stats.FIST_WPN_VULNERABILITY, target, null);
            final double critChanceResist = 100.0 - player.calcStat(Stats.CRIT_CHANCE_RECEPTIVE, target, null);
            final double critDamResistStatic = player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, target, null);
            final double critDamResist = 100.0 - 100.0 * (player.calcStat(Stats.CRIT_DAMAGE_RECEPTIVE, 1.0, target, null) - critDamResistStatic);
            final double SkillPower = player.calcStat(Stats.SKILL_POWER, 1.0, target, null);
            final double PvPPhysDmg = player.calcStat(Stats.PVP_PHYS_DMG_BONUS, 1.0, target, null);
            final double PvPSkillDmg = player.calcStat(Stats.PVP_PHYS_SKILL_DMG_BONUS, 1.0, target, null);
            final double MagicPvPSkillDmg = player.calcStat(Stats.PVP_MAGIC_SKILL_DMG_BONUS, 1.0, target, null);
            final double pSkillEvas = player.calcStat(Stats.PSKILL_EVASION, null, null);
            final double reflectDam = player.calcStat(Stats.REFLECT_DAMAGE_PERCENT, target, null);
            final double reflectSMagic = player.calcStat(Stats.REFLECT_MAGIC_SKILL, target, null);
            final double reflectSPhys = player.calcStat(Stats.REFLECT_PHYSIC_SKILL, target, null);
            final double meleePhysRes = player.calcStat(Stats.REFLECT_PSKILL_DAMAGE_PERCENT, target, null);
            final double pReuse = player.calcStat(Stats.PHYSIC_REUSE_RATE, target, null);
            final double mReuse = player.calcStat(Stats.MAGIC_REUSE_RATE, target, null);
            final String dialog = HtmCache.getInstance().getNotNull("command/whoami.htm", player);
            final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);
            df.setMaximumFractionDigits(1);
            df.setMinimumFractionDigits(1);
            final StrBuilder sb = new StrBuilder(dialog);
            sb.replaceFirst("%hpRegen%", df.format(hpRegen));
            sb.replaceFirst("%cpRegen%", df.format(cpRegen));
            sb.replaceFirst("%mpRegen%", df.format(mpRegen));
            sb.replaceFirst("%hpDrain%", df.format(hpDrain));
            sb.replaceFirst("%hpGain%", df.format(hpGain));
            sb.replaceFirst("%mpGain%", df.format(mpGain));
            sb.replaceFirst("%critPerc%", df.format(critPerc));
            sb.replaceFirst("%critStatic%", df.format(critStatic));
            sb.replaceFirst("%mCritRate%", df.format(mCritRate));
            sb.replaceFirst("%blowRate%", df.format(blowRate));
            sb.replaceFirst("%shieldDef%", df.format(shieldDef));
            sb.replaceFirst("%shieldRate%", df.format(shieldRate));
            sb.replaceFirst("%xpRate%", df.format(xpRate));
            sb.replaceFirst("%spRate%", df.format(spRate));
            sb.replaceFirst("%dropRate%", df.format(dropRate));
            sb.replaceFirst("%adenaRate%", df.format(adenaRate));
            sb.replaceFirst("%spoilRate%", df.format(spoilRate));
            sb.replaceFirst("%fireResist%", df.format(fireResist));
            sb.replaceFirst("%windResist%", df.format(windResist));
            sb.replaceFirst("%waterResist%", df.format(waterResist));
            sb.replaceFirst("%earthResist%", df.format(earthResist));
            sb.replaceFirst("%holyResist%", df.format(holyResist));
            sb.replaceFirst("%darkResist%", df.format(unholyResist));
            sb.replaceFirst("%bleedPower%", df.format(bleedPower));
            sb.replaceFirst("%bleedResist%", df.format(bleedResist));
            sb.replaceFirst("%poisonPower%", df.format(poisonPower));
            sb.replaceFirst("%poisonResist%", df.format(poisonResist));
            sb.replaceFirst("%stunPower%", df.format(stunPower));
            sb.replaceFirst("%stunResist%", df.format(stunResist));
            sb.replaceFirst("%rootPower%", df.format(rootPower));
            sb.replaceFirst("%rootResist%", df.format(rootResist));
            sb.replaceFirst("%sleepPower%", df.format(sleepPower));
            sb.replaceFirst("%sleepResist%", df.format(sleepResist));
            sb.replaceFirst("%paralyzePower%", df.format(paralyzePower));
            sb.replaceFirst("%paralyzeResist%", df.format(paralyzeResist));
            sb.replaceFirst("%mentalPower%", df.format(mentalPower));
            sb.replaceFirst("%mentalResist%", df.format(mentalResist));
            sb.replaceFirst("%debuffPower%", df.format(debuffPower));
            sb.replaceFirst("%debuffResist%", df.format(debuffResist));
            sb.replaceFirst("%cancelPower%", df.format(cancelPower));
            sb.replaceFirst("%cancelResist%", df.format(cancelResist));
            sb.replaceFirst("%swordResist%", df.format(swordResist));
            sb.replaceFirst("%dualResist%", df.format(dualResist));
            sb.replaceFirst("%bluntResist%", df.format(bluntResist));
            sb.replaceFirst("%daggerResist%", df.format(daggerResist));
            sb.replaceFirst("%bowResist%", df.format(bowResist));
            sb.replaceFirst("%fistResist%", df.format(fistResist));
            sb.replaceFirst("%poleResist%", df.format(poleResist));
            sb.replaceFirst("%critChanceResist%", df.format(critChanceResist));
            sb.replaceFirst("%critDamResist%", df.format(critDamResist));
            sb.replaceFirst("%SkillPower%", df.format(SkillPower));
            sb.replaceFirst("%PvPPhysDmg%", df.format(PvPPhysDmg));
            sb.replaceFirst("%PvPSkillDmg%", df.format(PvPSkillDmg));
            sb.replaceFirst("%MagicPvPSkillDmg%", df.format(MagicPvPSkillDmg));
            sb.replaceFirst("%pSkillEvas%", df.format(pSkillEvas));
            sb.replaceFirst("%reflectDam%", df.format(reflectDam));
            sb.replaceFirst("%reflectSMagic%", df.format(reflectSMagic));
            sb.replaceFirst("%reflectSPhys%", df.format(reflectSPhys));
            sb.replaceFirst("%meleePhysRes%", df.format(meleePhysRes));
            sb.replaceFirst("%pReuse%", df.format(pReuse));
            sb.replaceFirst("%mReuse%", df.format(mReuse));
            final NpcHtmlMessage msg = new NpcHtmlMessage(0);
            msg.setHtml(HtmlUtils.bbParse(sb.toString()));
            player.sendPacket(msg);
            return false;
        }
        return true;
    }
}
