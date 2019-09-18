package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.EnchantSkillHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEnchantSkillList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShortCutRegister;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.skills.TimeStamp;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.SkillEnchant;
import ru.j2dev.gameserver.utils.Log;

import java.util.Map;

public class RequestExEnchantSkill extends L2GameClientPacket {
    private static final Logger LOG = LoggerFactory.getLogger(RequestExEnchantSkill.class);

    private int _skillId;
    private int _skillLvl;

    protected static void updateSkillShortcuts(final Player player, final int skillId, final int skillLevel) {
        player.getAllShortCuts().stream().filter(sc -> sc.getId() == skillId && sc.getType() == 2).map(sc -> new ShortCut(sc.getSlot(), sc.getPage(), sc.getType(), sc.getId(), skillLevel, 1)).forEach(newsc -> {
            player.sendPacket(new ShortCutRegister(player, newsc));
            player.registerShortCut(newsc);
        });
    }

    @Override
    protected void readImpl() {
        _skillId = readD();
        _skillLvl = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        if (player.getClassId().getLevel() < 4 || player.getLevel() < 76) {
            player.sendPacket(new SystemMessage(1438));
            return;
        }
        final Skill currSkill = player.getKnownSkill(_skillId);
        if (currSkill == null) {
            player.sendPacket(new SystemMessage(1438));
            return;
        }
        final int currSkillLevel = currSkill.getLevel();
        final int currSkillBaseLevel = currSkill.getBaseLevel();
        final Map<Integer, SkillEnchant> skillEnchLevels = EnchantSkillHolder.getInstance().getLevelsOf(_skillId);
        if (skillEnchLevels == null || skillEnchLevels.isEmpty()) {
            player.sendPacket(new SystemMessage(1438));
            return;
        }
        final SkillEnchant currSkillEnch = skillEnchLevels.get(currSkillLevel);
        final SkillEnchant newSkillEnch = skillEnchLevels.get(_skillLvl);
        if (newSkillEnch == null) {
            player.sendPacket(new SystemMessage(1438));
            return;
        }
        if (currSkillEnch != null) {
            if (currSkillEnch.getRouteId() != newSkillEnch.getRouteId() || newSkillEnch.getEnchantLevel() != currSkillEnch.getEnchantLevel() + 1) {
                player.sendPacket(new SystemMessage(1438));
                return;
            }
        } else if (newSkillEnch.getEnchantLevel() != 1 || currSkillLevel != currSkillBaseLevel) {
            player.sendPacket(new SystemMessage(1438));
            LOG.warn("Player \"" + player + "\" trying to use enchant  exploit" + currSkill + " to " + _skillLvl + "(enchant level " + newSkillEnch.getEnchantLevel() + ")");
            return;
        }
        final int[] chances = newSkillEnch.getChances();
        final int minPlayerLevel = Experience.LEVEL.length - chances.length - 1;
        if (player.getLevel() < minPlayerLevel) {
            sendPacket(new SystemMessage(607).addNumber(minPlayerLevel));
            return;
        }
        if (player.getSp() < newSkillEnch.getSp()) {
            sendPacket(new SystemMessage(1443));
            return;
        }
        if (player.getExp() < newSkillEnch.getExp()) {
            sendPacket(new SystemMessage(1444));
            return;
        }
        if (newSkillEnch.getItemId() > 0 && newSkillEnch.getItemCount() > 0L && Functions.removeItem(player, newSkillEnch.getItemId(), newSkillEnch.getItemCount()) < newSkillEnch.getItemCount()) {
            sendPacket(Msg.ITEMS_REQUIRED_FOR_SKILL_ENCHANT_ARE_INSUFFICIENT);
            return;
        }
        final int chanceIdx = Math.max(0, Math.min(player.getLevel() - minPlayerLevel, chances.length - 1));
        final int chance = chances[chanceIdx];
        player.addExpAndSp(-1L * newSkillEnch.getExp(), -1 * newSkillEnch.getSp());
        player.sendPacket(new SystemMessage(538).addNumber(newSkillEnch.getSp()));
        player.sendPacket(new SystemMessage(539).addNumber(newSkillEnch.getExp()));
        final TimeStamp currSkillReuseTimeStamp = player.getSkillReuse(currSkill);
        Skill newSkill;
        if (Rnd.chance(chance)) {
            newSkill = SkillTable.getInstance().getInfo(newSkillEnch.getSkillId(), newSkillEnch.getSkillLevel());
            player.sendPacket(new SystemMessage(1440).addSkillName(_skillId, _skillLvl));
            Log.add(player.getName() + "|Successfully enchanted|" + _skillId + "|to+" + _skillLvl + "|" + chance, "enchant_skills");
        } else {
            newSkill = SkillTable.getInstance().getInfo(currSkill.getId(), currSkill.getBaseLevel());
            player.sendPacket(new SystemMessage(1441).addSkillName(_skillId, _skillLvl));
            Log.add(player.getName() + "|Failed to enchant|" + _skillId + "|to+" + _skillLvl + "|" + chance, "enchant_skills");
        }
        if (currSkillReuseTimeStamp != null && currSkillReuseTimeStamp.hasNotPassed()) {
            player.disableSkill(newSkill, currSkillReuseTimeStamp.getReuseCurrent());
        }
        player.addSkill(newSkill, true);
        player.sendSkillList();
        updateSkillShortcuts(player, _skillId, _skillLvl);
        player.sendPacket(ExEnchantSkillList.packetFor(player));
    }
}
