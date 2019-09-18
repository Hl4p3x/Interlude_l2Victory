package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.CastleManorManager;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class AdminManor implements IAdminCommandHandler {
    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().Menu) {
            return false;
        }
        final StringTokenizer st = new StringTokenizer(fullString);
        fullString = st.nextToken();
        switch (fullString) {
            case "admin_manor":
                showMainPage(activeChar);
                break;
            case "admin_manor_reset":
                int castleId = 0;
                try {
                    castleId = Integer.parseInt(st.nextToken());
                } catch (Exception ignored) {
                }
                if (castleId > 0) {
                    final Castle castle = ResidenceHolder.getInstance().getResidence(Castle.class, castleId);
                    if(castle != null) {
                        castle.setCropProcure(new ArrayList<>(), 0);
                        castle.setCropProcure(new ArrayList<>(), 1);
                        castle.setSeedProduction(new ArrayList<>(), 0);
                        castle.setSeedProduction(new ArrayList<>(), 1);
                        castle.saveCropData();
                        castle.saveSeedData();
                        activeChar.sendMessage("Manor data for " + castle.getName() + " was nulled");
                    }
                } else {
                    for (final Castle castle2 : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
                        castle2.setCropProcure(new ArrayList<>(), 0);
                        castle2.setCropProcure(new ArrayList<>(), 1);
                        castle2.setSeedProduction(new ArrayList<>(), 0);
                        castle2.setSeedProduction(new ArrayList<>(), 1);
                        castle2.saveCropData();
                        castle2.saveSeedData();
                    }
                    activeChar.sendMessage("Manor data was nulled");
                }
                showMainPage(activeChar);
                break;
            case "admin_manor_save":
                CastleManorManager.getInstance().save();
                activeChar.sendMessage("Manor System: all data saved");
                showMainPage(activeChar);
                break;
            case "admin_manor_disable":
                final boolean mode = CastleManorManager.getInstance().isDisabled();
                CastleManorManager.getInstance().setDisabled(!mode);
                if (mode) {
                    activeChar.sendMessage("Manor System: enabled");
                } else {
                    activeChar.sendMessage("Manor System: disabled");
                }
                showMainPage(activeChar);
                break;
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void showMainPage(final Player activeChar) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<center><font color=\"LEVEL\"> [Manor System] </font></center><br>");
        replyMSG.append("<table width=\"100%\">");
        replyMSG.append("<tr><td>Disabled: ").append(CastleManorManager.getInstance().isDisabled() ? "yes" : "no").append("</td>");
        replyMSG.append("<td>Under Maintenance: ").append(CastleManorManager.getInstance().isUnderMaintenance() ? "yes" : "no").append("</td></tr>");
        replyMSG.append("<tr><td>Approved: ").append(ServerVariables.getBool("ManorApproved") ? "yes" : "no").append("</td></tr>");
        replyMSG.append("</table>");
        replyMSG.append("<center><table>");
        replyMSG.append("<tr><td><button value=\"").append(CastleManorManager.getInstance().isDisabled() ? "Enable" : "Disable").append("\" action=\"bypass -h admin_manor_disable\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td><button value=\"Reset\" action=\"bypass -h admin_manor_reset\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
        replyMSG.append("<tr><td><button value=\"Refresh\" action=\"bypass -h admin_manor\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td><button value=\"Back\" action=\"bypass -h admin_admin\" width=110 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>");
        replyMSG.append("</table></center>");
        replyMSG.append("<br><center>Castle Information:<table width=\"100%\">");
        replyMSG.append("<tr><td></td><td>Current Period</td><td>Next Period</td></tr>");
        for (final Castle c : ResidenceHolder.getInstance().getResidenceList(Castle.class)) {
            replyMSG.append("<tr><td>").append(c.getName()).append("</td><td>").append(c.getManorCost(0)).append("a</td><td>").append(c.getManorCost(1)).append("a</td></tr>");
        }
        replyMSG.append("</table><br>");
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    private enum Commands {
        admin_manor,
        admin_manor_reset,
        admin_manor_save,
        admin_manor_disable
    }
}
