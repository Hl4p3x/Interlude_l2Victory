package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.EnchantSkillHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExEnchantSkillInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.SkillEnchant;

import java.util.Map;

public class RequestExEnchantSkillInfo extends L2GameClientPacket {
    private int _skillId;
    private int _skillLvl;

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
        } else if (newSkillEnch.getEnchantLevel() != 1) {
            player.sendPacket(new SystemMessage(1438));
            return;
        }
        final int[] chances = newSkillEnch.getChances();
        final int minPlayerLevel = Experience.LEVEL.length - chances.length - 1;
        if (player.getLevel() < minPlayerLevel) {
            sendPacket(new SystemMessage(607).addNumber(minPlayerLevel));
            return;
        }
        final int chanceIdx = Math.max(0, Math.min(player.getLevel() - minPlayerLevel, chances.length - 1));
        final int chance = chances[chanceIdx];
        final ExEnchantSkillInfo esi = new ExEnchantSkillInfo(newSkillEnch.getSkillId(), newSkillEnch.getSkillLevel(), newSkillEnch.getSp(), newSkillEnch.getExp(), chance);
        if (newSkillEnch.getItemId() > 0 && newSkillEnch.getItemCount() > 0L) {
            esi.addNeededItem(newSkillEnch.getItemId(), newSkillEnch.getItemCount());
        }
        player.sendPacket(esi);
    }
}
