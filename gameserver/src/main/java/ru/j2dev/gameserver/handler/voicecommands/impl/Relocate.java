package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.skills.skillclasses.Call;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class Relocate extends Functions implements IVoicedCommandHandler {
    private static final String LAST_USE_TIMESTAMP_VAR = "summonClanLastUse";
    private static final String DISPLAY_NAME = "Clan Summon";
    private final String[] _commandList = {"summon_clan", "km-all-to-me", "rcm"};

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String args) {
        if (!Config.SERVICES_CLAN_SUMMON_COMMAND_ENABLE) {
            return false;
        }
        final Clan cl = player.getClan();
        if (cl == null) {
            player.sendMessage("You are not a clan member.");
            return false;
        }
        if (cl.getLeaderId() != player.getObjectId()) {
            player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return false;
        }
        final SystemMessage msg = Call.canSummonHere(player);
        if (msg != null) {
            player.sendMessage("Clan Summon is started");
            player.sendPacket(msg);
            return false;
        }
        final List<Player> clanMembersOnline = cl.getOnlineMembers(player.getObjectId());
        if (clanMembersOnline.size() < 1) {
            player.sendMessage("No clan members online");
            return false;
        }
        if (Functions.getItemCount(player, Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_ITEM) < Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_PRICE) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return false;
        }
        final long now = System.currentTimeMillis() / 1000L;
        final long lastUseTimestamp = player.getVarLong(LAST_USE_TIMESTAMP_VAR, 0L);
        if (now - lastUseTimestamp < Config.REUSE_DELAY_FOR_CLAN_SUMMON) {
            player.sendPacket(new SystemMessage(48).addString(DISPLAY_NAME));
            return false;
        }
        Functions.removeItem(player, Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_ITEM, Config.SERVICES_CLAN_SUMMON_COMMAND_SELL_PRICE);
        player.sendMessage(player.isLangRus() ? "Призыв клана начался" : "Clan Summon is started");
        player.setVar(LAST_USE_TIMESTAMP_VAR, now, -1L);
        clanMembersOnline.stream()
                .filter(member -> Call.canBeSummoned(member) == null)
                .forEach(member -> member.summonCharacterRequest(player, Location.findPointToStay(player.getX(), player.getY(), player.getZ(), 100, 150, player.getReflection().getGeoIndex()), Config.SERVICES_CLAN_SUMMON_COMMAND_SUMMON_CRYSTAL_COUNT));
        return true;
    }
}