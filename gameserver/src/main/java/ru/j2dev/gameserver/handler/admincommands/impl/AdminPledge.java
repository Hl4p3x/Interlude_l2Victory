package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.VillageMasterInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.SubUnit;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeShowInfoUpdate;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PledgeStatusChanged;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.utils.Util;

import java.util.StringTokenizer;

public class AdminPledge implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (activeChar.getPlayerAccess() == null || !activeChar.getPlayerAccess().CanEditPledge || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
            return false;
        }
        final Player target = (Player) activeChar.getTarget();
        if (fullString.startsWith("admin_pledge")) {
            final StringTokenizer st = new StringTokenizer(fullString);
            st.nextToken();
            final String action = st.nextToken();
            if ("create".equals(action)) {
                try {
                    if (target == null) {
                        activeChar.sendPacket(Msg.INVALID_TARGET);
                        return false;
                    }
                    if (target.getPlayer().getLevel() < 10) {
                        activeChar.sendPacket(Msg.YOU_ARE_NOT_QUALIFIED_TO_CREATE_A_CLAN);
                        return false;
                    }
                    final String pledgeName = st.nextToken();
                    if (pledgeName.length() > 16) {
                        activeChar.sendPacket(Msg.CLAN_NAMES_LENGTH_IS_INCORRECT);
                        return false;
                    }
                    if (!Util.isMatchingRegexp(pledgeName, Config.CLAN_NAME_TEMPLATE)) {
                        activeChar.sendPacket(Msg.CLAN_NAME_IS_INCORRECT);
                        return false;
                    }
                    final Clan clan = ClanTable.getInstance().createClan(target, pledgeName);
                    if (clan != null) {
                        target.sendPacket(clan.listAll());
                        target.sendPacket(new PledgeShowInfoUpdate(clan), Msg.CLAN_HAS_BEEN_CREATED);
                        target.updatePledgeClass();
                        target.sendUserInfo(true);
                        return true;
                    }
                    activeChar.sendPacket(Msg.THIS_NAME_ALREADY_EXISTS);
                    return false;
                } catch (Exception ex) {
                    return false;
                }
            }
            if ("setlevel".equals(action)) {
                if (target.getClan() == null) {
                    activeChar.sendPacket(Msg.INVALID_TARGET);
                    return false;
                }
                try {
                    final int level = Integer.parseInt(st.nextToken());
                    final Clan clan = target.getClan();
                    activeChar.sendMessage("You set level " + level + " for clan " + clan.getName());
                    clan.setLevel(level);
                    clan.updateClanInDB();
                    if (level == 5) {
                        target.sendPacket(Msg.NOW_THAT_YOUR_CLAN_LEVEL_IS_ABOVE_LEVEL_5_IT_CAN_ACCUMULATE_CLAN_REPUTATION_POINTS);
                    }
                    final PledgeShowInfoUpdate pu = new PledgeShowInfoUpdate(clan);
                    final PledgeStatusChanged ps = new PledgeStatusChanged(clan);
                    for (final Player member : clan.getOnlineMembers(0)) {
                        member.updatePledgeClass();
                        member.sendPacket(Msg.CLANS_SKILL_LEVEL_HAS_INCREASED, pu, ps);
                        member.broadcastUserInfo(true);
                    }
                    return true;
                } catch (Exception ex2) {
                    return false;
                }
            }
            switch (action) {
                case "resetcreate":
                    if (target.getClan() == null) {
                        activeChar.sendPacket(Msg.INVALID_TARGET);
                        return false;
                    }
                    target.getClan().setExpelledMemberTime(0L);
                    activeChar.sendMessage("The penalty for creating a clan has been lifted for " + target.getName());
                    break;
                case "resetwait":
                    target.setLeaveClanTime(0L);
                    activeChar.sendMessage("The penalty for leaving a clan has been lifted for " + target.getName());
                    break;
                case "addrep":
                    try {
                        final int rep = Integer.parseInt(st.nextToken());
                        if (target.getClan() == null || target.getClan().getLevel() < 5) {
                            activeChar.sendPacket(Msg.INVALID_TARGET);
                            return false;
                        }
                        target.getClan().incReputation(rep, false, "admin_manual");
                        activeChar.sendMessage("Added " + rep + " clan points to clan " + target.getClan().getName() + ".");
                    } catch (NumberFormatException nfe) {
                        activeChar.sendMessage("Please specify a number of clan points to add.");
                    }
                    break;
                case "setleader":
                    final Clan clan2 = target.getClan();
                    if (target.getClan() == null) {
                        activeChar.sendPacket(Msg.INVALID_TARGET);
                        return false;
                    }
                    String newLeaderName = null;
                    if (st.hasMoreTokens()) {
                        newLeaderName = st.nextToken();
                    } else {
                        newLeaderName = target.getName();
                    }
                    final SubUnit mainUnit = clan2.getSubUnit(0);
                    final UnitMember newLeader = mainUnit.getUnitMember(newLeaderName);
                    if (newLeader == null) {
                        activeChar.sendPacket(Msg.INVALID_TARGET);
                        return false;
                    }
                    VillageMasterInstance.setLeader(activeChar, clan2, mainUnit, newLeader);
                    break;
            }
        }
        return false;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_pledge
    }
}
