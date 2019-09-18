package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.gs2as.IGPwdCng;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PersonalCabinet extends Functions implements IVoicedCommandHandler {
    private static final Pattern PASSWORD_BYPASS_PATTERN = Pattern.compile("^([\\w\\d_-]{4,18})\\s+([\\w\\d_-]{4,16})$");
    private static final long PASSWORD_CHANGE_INTERVAL = 3600000L;
    private static final String[] _voicedCommands = {"my", "cfg", "pc", "personal_cabinet", "repair", "password"};

    @Override
    public boolean useVoicedCommand(String command, final Player activeChar, final String target) {
        if (activeChar == null) {
            return false;
        }
        command = command.intern();
        if (command.equalsIgnoreCase(_voicedCommands[0]) || command.equalsIgnoreCase(_voicedCommands[1]) || command.equalsIgnoreCase(_voicedCommands[2]) || command.equalsIgnoreCase(_voicedCommands[3])) {
            if (!target.isEmpty()) {
                if (target.startsWith("news")) {
                    showPage(activeChar, "news.html");
                }
                if (target.startsWith("rules")) {
                    showPage(activeChar, "rules.html");
                } else if (target.startsWith("shop")) {
                    showPage(activeChar, "shop.html");
                } else if (target.startsWith("pass")) {
                    showPage(activeChar, "passcng.html");
                } else if (target.startsWith("faq")) {
                    showPage(activeChar, "faq.html");
                } else if (target.startsWith("event")) {
                    showPage(activeChar, "event.html");
                }
            } else {
                showPage(activeChar, "news.html");
            }
            return true;
        }
        if (command.equalsIgnoreCase(_voicedCommands[5])) {
            final String lastChanged = activeChar.getVar("LastPwdChng");
            if (lastChanged != null && !lastChanged.isEmpty()) {
                final long lastChange = Long.parseLong(lastChanged) * 1000L;
                if (lastChange + PASSWORD_CHANGE_INTERVAL > System.currentTimeMillis()) {
                    activeChar.sendMessage("Password can't be change so frequently.");
                    return false;
                }
            }
            if (!target.isEmpty()) {
                final Matcher m = PASSWORD_BYPASS_PATTERN.matcher(target);
                if (m.find() && m.groupCount() == 2) {
                    final String oldpassword = m.group(1);
                    final String newpassword = m.group(2);
                    AuthServerCommunication.getInstance().sendPacket(new IGPwdCng(activeChar, oldpassword, newpassword));
                    return true;
                }
                activeChar.sendMessage("Password requirement's is not met!");
            } else {
                activeChar.sendMessage("Password can't be empty!");
            }
            return true;
        }
        if (!command.equalsIgnoreCase(_voicedCommands[4])) {
            return false;
        }
        if (target.isEmpty()) {
            final NpcHtmlMessage msg = new NpcHtmlMessage(activeChar, null);
            msg.setFile("mods/pc/repair.html");
            StringBuilder cl = new StringBuilder();
            Connection con = null;
            PreparedStatement fpstmt = null;
            ResultSet rset = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                fpstmt = con.prepareStatement("SELECT `obj_Id`, `char_name` FROM `characters` WHERE `account_name` = ? AND `online` = 0");
                fpstmt.setString(1, activeChar.getAccountName());
                rset = fpstmt.executeQuery();
                while (rset.next()) {
                    final String charName = rset.getString("char_name");
                    final int charId = rset.getInt("obj_Id");
                    cl.append("<a action=\"bypass -h user_repair ").append(charId).append("\">").append(charName).append("</a><br1>");
                }
            } catch (Exception ignored) {
            } finally {
                DbUtils.closeQuietly(con, fpstmt, rset);
            }
            msg.replace("%repair%", cl.toString());
            activeChar.sendPacket(msg);
            return true;
        }
        Connection con2 = null;
        PreparedStatement fpstmt2 = null;
        ResultSet rset2 = null;
        try {
            final int charId2 = Integer.parseInt(target);
            con2 = DatabaseFactory.getInstance().getConnection();
            fpstmt2 = con2.prepareStatement("SELECT * FROM `characters` WHERE `account_name` = ? AND `obj_Id` = ? AND `online` = 0");
            fpstmt2.setString(1, activeChar.getAccountName());
            fpstmt2.setInt(2, charId2);
            rset2 = fpstmt2.executeQuery();
            if (!rset2.next()) {
                activeChar.sendMessage("Character not found.");
                return true;
            }
            if (World.getPlayer(charId2) != null) {
                activeChar.sendMessage("Character online.");
                return true;
            }
            DbUtils.close(fpstmt2, rset2);
            fpstmt2 = con2.prepareStatement("UPDATE `characters` SET `x` = 17867, `y` = 170259, `z` = -3503 WHERE `obj_Id` = ?");
            fpstmt2.setInt(1, charId2);
            fpstmt2.executeUpdate();
            DbUtils.close(fpstmt2);
            fpstmt2 = con2.prepareStatement("DELETE FROM `character_shortcuts` WHERE `char_obj_id` = ?");
            fpstmt2.setInt(1, charId2);
            fpstmt2.executeUpdate();
            DbUtils.close(fpstmt2);
            fpstmt2 = con2.prepareStatement("UPDATE `items` SET `loc` = \"INVENTORY\" WHERE `loc` = \"PAPERDOLL\" AND `owner_id` = ? AND `item_id` NOT IN (13530, 13531, 13532, 13533, 13534, 13535, 13536, 13537, 13538, 10281, 10283, 10282, 10286, 10284, 10285, 10287, 10289, 10290, 10288, 10294, 10292, 10291, 10293, 10280, 10612)");
            fpstmt2.setInt(1, charId2);
            fpstmt2.executeUpdate();
            DbUtils.close(fpstmt2);
            fpstmt2 = con2.prepareStatement("UPDATE `items` SET `loc` = \"WAREHOUSE\" WHERE `loc` = \"INVENTORY\" AND `owner_id` = ? AND `item_id` NOT IN (13530, 13531, 13532, 13533, 13534, 13535, 13536, 13537, 13538, 10281, 10283, 10282, 10286, 10284, 10285, 10287, 10289, 10290, 10288, 10294, 10292, 10291, 10293, 10280, 10612)");
            fpstmt2.setInt(1, charId2);
            fpstmt2.executeUpdate();
            DbUtils.close(fpstmt2);
            activeChar.sendMessage("Character successfully repaired.");
        } catch (Exception ignored) {
        } finally {
            DbUtils.closeQuietly(con2, fpstmt2, rset2);
        }
        return true;
    }

    private void showPage(final Player activeChar, final String page) {
        show("mods/pc/".concat(page), activeChar);
    }

    @Override
    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
