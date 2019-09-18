package ru.j2dev.gameserver.handler.admincommands.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.Files;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Objects;

public class AdminChangeAccessLevel implements IAdminCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminChangeAccessLevel.class);

    private static void showModersPannel(final Player activeChar) {
        final NpcHtmlMessage reply = new NpcHtmlMessage(5);
        StringBuilder html = new StringBuilder("Moderators managment panel.<br>");
        final File dir = new File("config/GMAccess.d/");
        if (!dir.exists() || !dir.isDirectory()) {
            html.append("Error: Can't open permissions folder.");
            reply.setHtml(html.toString());
            activeChar.sendPacket(reply);
            return;
        }
        html.append("<p align=right>");
        html.append("<button width=120 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_moders_add\" value=\"Add modrator\">");
        html.append("</p><br>");
        html.append("<center><font color=LEVEL>Moderators:</font></center>");
        html.append("<table width=285>");
        for (final File f : Objects.requireNonNull(dir.listFiles())) {
            if (!f.isDirectory() && f.getName().startsWith("m")) {
                if (f.getName().endsWith(".xml")) {
                    final int oid = Integer.parseInt(f.getName().substring(1, 10));
                    String pName = getPlayerNameByObjId(oid);
                    boolean on = false;
                    if (pName == null || pName.isEmpty()) {
                        pName = "" + oid;
                    } else {
                        on = (GameObjectsStorage.getPlayer(pName) != null);
                    }
                    html.append("<tr>");
                    html.append("<td width=140>").append(pName);
                    html.append(on ? " <font color=\"33CC66\">(on)</font>" : "");
                    html.append("</td>");
                    html.append("<td width=45><button width=50 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_moders_log ").append(oid).append("\" value=\"Logs\"></td>");
                    html.append("<td width=45><button width=20 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\" action=\"bypass -h admin_moders_del ").append(oid).append("\" value=\"X\"></td>");
                    html.append("</tr>");
                }
            }
        }
        html.append("</table>");
        reply.setHtml(html.toString());
        activeChar.sendPacket(reply);
    }

    private static String getPlayerNameByObjId(final int oid) {
        String pName = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT `char_name` FROM `characters` WHERE `obj_Id`=\"" + oid + "\" LIMIT 1");
            rset = statement.executeQuery();
            if (rset.next()) {
                pName = rset.getString(1);
            }
        } catch (Exception e) {
            LOGGER.warn("SQL Error: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return pName;
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanGmEdit) {
            return false;
        }
        switch (command) {
            case admin_changelvl: {
                if (wordList.length == 2) {
                    final int lvl = Integer.parseInt(wordList[1]);
                    if (activeChar.getTarget().isPlayer()) {
                        ((Player) activeChar.getTarget()).setAccessLevel(lvl);
                    }
                    break;
                }
                if (wordList.length == 3) {
                    final int lvl = Integer.parseInt(wordList[2]);
                    final Player player = GameObjectsStorage.getPlayer(wordList[1]);
                    if (player != null) {
                        player.setAccessLevel(lvl);
                    }
                    break;
                }
                break;
            }
            case admin_moders: {
                showModersPannel(activeChar);
                break;
            }
            case admin_moders_add: {
                if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                    activeChar.sendMessage("Incorrect target. Please select a player.");
                    showModersPannel(activeChar);
                    return false;
                }
                final Player modAdd = activeChar.getTarget().getPlayer();
                if (Config.gmlist.containsKey(modAdd.getObjectId())) {
                    activeChar.sendMessage("Error: Moderator " + modAdd.getName() + " already in server access list.");
                    showModersPannel(activeChar);
                    return false;
                }
                final String newFName = "m" + modAdd.getObjectId() + ".xml";
                if (!Files.copyFile("config/GMAccess.d/template/moderator.xml", "config/GMAccess.d/" + newFName)) {
                    activeChar.sendMessage("Error: Failed to copy access-file.");
                    showModersPannel(activeChar);
                    return false;
                }
                StringBuilder res = new StringBuilder();
                try {
                    final BufferedReader in = new BufferedReader(new FileReader("config/GMAccess.d/" + newFName));
                    String str;
                    while ((str = in.readLine()) != null) {
                        res.append(str).append("\n");
                    }
                    in.close();
                    res = new StringBuilder(res.toString().replaceFirst("ObjIdPlayer", "" + modAdd.getObjectId()));
                    Files.writeFile("config/GMAccess.d/" + newFName, res.toString());
                } catch (Exception e) {
                    activeChar.sendMessage("Error: Failed to modify object ID in access-file.");
                    final File fDel = new File("config/GMAccess.d/" + newFName);
                    if (fDel.exists()) {
                        fDel.delete();
                    }
                    showModersPannel(activeChar);
                    return false;
                }
                final File af = new File("config/GMAccess.d/" + newFName);
                if (!af.exists()) {
                    activeChar.sendMessage("Error: Failed to read access-file for " + modAdd.getName());
                    showModersPannel(activeChar);
                    return false;
                }
                Config.loadGMAccess(af);
                modAdd.setPlayerAccess(Config.gmlist.get(modAdd.getObjectId()));
                activeChar.sendMessage("Moderator " + modAdd.getName() + " added.");
                showModersPannel(activeChar);
                break;
            }
            case admin_moders_del: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Please specify moderator object ID to delete moderator.");
                    showModersPannel(activeChar);
                    return false;
                }
                final int oid = Integer.parseInt(wordList[1]);
                if (!Config.gmlist.containsKey(oid)) {
                    activeChar.sendMessage("Error: Moderator with object ID " + oid + " not found in server access lits.");
                    showModersPannel(activeChar);
                    return false;
                }
                Config.gmlist.remove(oid);
                final Player modDel = GameObjectsStorage.getPlayer(oid);
                if (modDel != null) {
                    modDel.setPlayerAccess(null);
                }
                final String fname = "m" + oid + ".xml";
                final File f = new File("config/GMAccess.d/" + fname);
                if (!f.exists() || !f.isFile() || !f.delete()) {
                    activeChar.sendMessage("Error: Can't delete access-file: " + fname);
                    showModersPannel(activeChar);
                    return false;
                }
                if (modDel != null) {
                    activeChar.sendMessage("Moderator " + modDel.getName() + " deleted.");
                } else {
                    activeChar.sendMessage("Moderator with object ID " + oid + " deleted.");
                }
                showModersPannel(activeChar);
                break;
            }
            case admin_penalty: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("USAGE: //penalty charName [count] [reason]");
                    return false;
                }
                int count = 1;
                if (wordList.length > 2) {
                    count = Integer.parseInt(wordList[2]);
                }
                String reason = "\u043d\u0435 \u0443\u043a\u0430\u0437\u0430\u043d\u0430";
                if (wordList.length > 3) {
                    reason = wordList[3];
                }
                int oId;
                final Player player2 = GameObjectsStorage.getPlayer(wordList[1]);
                if (player2 != null && player2.getPlayerAccess().CanBanChat) {
                    oId = player2.getObjectId();
                    int oldPenaltyCount = 0;
                    final String oldPenalty = player2.getVar("penaltyChatCount");
                    if (oldPenalty != null) {
                        oldPenaltyCount = Integer.parseInt(oldPenalty);
                    }
                    player2.setVar("penaltyChatCount", "" + (oldPenaltyCount + count), -1L);
                } else {
                    oId = mysql.simple_get_int("obj_Id", "characters", "`char_name`='" + wordList[1] + "'");
                    if (oId > 0) {
                        final Integer oldCount = (Integer) mysql.get("SELECT `value` FROM character_variables WHERE `obj_id` = " + oId + " AND `name` = 'penaltyChatCount'");
                        mysql.set("REPLACE INTO character_variables (obj_id, type, name, value, expire_time) VALUES (" + oId + ",'user-var','penaltyChatCount','" + (oldCount + count) + "',-1)");
                    }
                }
                if (oId <= 0) {
                    break;
                }
                if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD) {
                    Announcements.getInstance().announceToAll(activeChar + " \u043e\u0448\u0442\u0440\u0430\u0444\u043e\u0432\u0430\u043b \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u0430 " + wordList[1] + " \u043d\u0430 " + count + ", \u043f\u0440\u0438\u0447\u0438\u043d\u0430: " + reason + ".");
                    break;
                }
                Announcements.shout(activeChar, activeChar + " \u043e\u0448\u0442\u0440\u0430\u0444\u043e\u0432\u0430\u043b \u043c\u043e\u0434\u0435\u0440\u0430\u0442\u043e\u0440\u0430 " + wordList[1] + " \u043d\u0430 " + count + ", \u043f\u0440\u0438\u0447\u0438\u043d\u0430: " + reason + ".", ChatType.CRITICAL_ANNOUNCE);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_changelvl,
        admin_moders,
        admin_moders_add,
        admin_moders_del,
        admin_penalty
    }
}
