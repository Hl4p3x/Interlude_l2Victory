package ru.j2dev.gameserver.handler.voicecommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.gs2as.IGPwdCng;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Cfg implements IVoicedCommandHandler {
    private static final Pattern PASSWORD_BYPASS_PATTERN = Pattern.compile("^([\\w\\d_-]{4,18})\\s+([\\w\\d_-]{4,16})$");
    private static final long PASSWORD_CHANGE_INTERVAL = 3600000L;

    private final String[] _commandList = {"cfg", "menu", "password", "repair"};

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String args) {
        if (command.equalsIgnoreCase(_commandList[0]) || command.equalsIgnoreCase(_commandList[1])) {
            if (args != null) {
                final String[] param = args.split(" ");
                if (param.length == 2) {
                    if ("dli".equalsIgnoreCase(param[0])) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("DroplistIcons", "1", -1L);
                        } else if ("of".equalsIgnoreCase(param[1])) {
                            activeChar.unsetVar("DroplistIcons");
                        }
                    }
                    if ("noe".equalsIgnoreCase(param[0])) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("NoExp", "1", -1L);
                        } else if ("of".equalsIgnoreCase(param[1])) {
                            activeChar.unsetVar("NoExp");
                        }
                    }
                    if ("notraders".equalsIgnoreCase(param[0])) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setNotShowTraders(true);
                            activeChar.setVar("notraders", "true", -1L);
                        } else if ("of".equalsIgnoreCase(param[1])) {
                            activeChar.setNotShowTraders(false);
                            activeChar.unsetVar("notraders");
                        }
                    }
                    if ("buffAnimRange".equalsIgnoreCase(param[0])) {
                        int range = 15 * NumberUtils.toInt(param[1], 0);
                        if (range < 0) {
                            range = -1;
                        } else if (range > 1500) {
                            range = 1500;
                        }
                        activeChar.setBuffAnimRange(range);
                        activeChar.setVar("buffAnimRange", String.valueOf(range), -1L);
                    }
                    if ("noShift".equalsIgnoreCase(param[0])) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("noShift", "1", -1L);
                        } else if (param[1].startsWith("of")) {
                            activeChar.unsetVar("noShift");
                        }
                    }

                    if (param[0].equalsIgnoreCase("ssc")) {
                        if (param[1].equalsIgnoreCase("on") && Config.SHOW_SKILL_CHANCE) {
                            activeChar.setVar("SkillsHideChance", "1", -1);
                        } else if (param[1].equalsIgnoreCase("of")) {
                            activeChar.unsetVar("SkillsHideChance");
                        }
                    }
                    if ("hwidlock".equalsIgnoreCase(param[0]) && activeChar.getNetConnection() != null && activeChar.getNetConnection().getHwid() != null && !activeChar.getNetConnection().getHwid().isEmpty()) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setHWIDLock(activeChar.getNetConnection().getHwid());
                        } else if (param[1].startsWith("of")) {
                            activeChar.setHWIDLock(null);
                        }
                    }
                    if ("iplock".equalsIgnoreCase(param[0]) && activeChar.getNetConnection() != null && activeChar.getNetConnection().getIpAddr() != null && !activeChar.getNetConnection().getIpAddr().isEmpty()) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setIPLock(activeChar.getNetConnection().getIpAddr());
                        } else if (param[1].startsWith("of")) {
                            activeChar.setIPLock(null);
                        }
                    }
                    if ("lang".equalsIgnoreCase(param[0])) {
                        if ("en".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("lang@", "en", -1L);
                        } else if ("ru".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("lang@", "ru", -1L);
                        }
                    }
                    if (Config.SERVICES_ENABLE_NO_CARRIER && "noCarrier".equalsIgnoreCase(param[0])) {
                        int time = NumberUtils.toInt(param[1], 0);
                        if (time <= 0) {
                            time = 0;
                        } else if (time > Config.SERVICES_NO_CARRIER_MAX_TIME) {
                            time = Config.SERVICES_NO_CARRIER_MAX_TIME;
                        } else if (time < Config.SERVICES_NO_CARRIER_MIN_TIME) {
                            time = Config.SERVICES_NO_CARRIER_MIN_TIME;
                        }
                        activeChar.setVar("noCarrier", String.valueOf(time), -1L);
                    }
                    if ("translit".equalsIgnoreCase(param[0])) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("translit", "tl", -1L);
                        } else if ("la".equalsIgnoreCase(param[1])) {
                            activeChar.setVar("translit", "tc", -1L);
                        } else if ("of".equalsIgnoreCase(param[1])) {
                            activeChar.unsetVar("translit");
                        }
                    }
                    if (Config.AUTO_LOOT_INDIVIDUAL && "autoloot".equalsIgnoreCase(param[0])) {
                        if ("on".equalsIgnoreCase(param[1])) {
                            activeChar.setAutoLoot(true);
                            if (Config.AUTO_LOOT_HERBS) {
                                activeChar.setAutoLootHerbs(true);
                            }
                            activeChar.setAutoLootAdena(true);
                            activeChar.sendMessage("Autolooting all.");
                        } else if ("ad".equalsIgnoreCase(param[1])) {
                            activeChar.setAutoLoot(false);
                            activeChar.setAutoLootHerbs(false);
                            activeChar.setAutoLootAdena(true);
                            activeChar.sendMessage("Autolooting adena only.");
                        } else if ("of".equalsIgnoreCase(param[1])) {
                            activeChar.setAutoLoot(false);
                            activeChar.setAutoLootHerbs(false);
                            activeChar.setAutoLootAdena(false);
                            activeChar.sendMessage("Autolooting off.");
                        }
                    }
                }
            }
            final NpcHtmlMessage dialog = new NpcHtmlMessage(5);
            dialog.setFile("command/cfg.htm");
            dialog.replace("%dli%", activeChar.getVarB("DroplistIcons") ? "On" : "Off");
            dialog.replace("%noe%", activeChar.getVarB("NoExp") ? "On" : "Off");
            dialog.replace("%notraders%", activeChar.getVarB("notraders") ? "On" : "Off");
            dialog.replace("%noShift%", activeChar.getVarB("noShift") ? "On" : "Off");
            dialog.replace("%noCarrier%", Config.SERVICES_ENABLE_NO_CARRIER ? (activeChar.getVarB("noCarrier") ? activeChar.getVar("noCarrier") : "0") : "N/A");
            if (activeChar.isAutoLootEnabled()) {
                dialog.replace("%autoloot%", "All");
            } else if (activeChar.isAutoLootAdenaEnabled()) {
                dialog.replace("%autoloot%", "Adena");
            } else {
                dialog.replace("%autoloot%", "Off");
            }
            switch (activeChar.getLangId()) {
                case 0:
                    dialog.replace("%lang%", "En");
                    break;
                case 1:
                    dialog.replace("%lang%", "Ru");
                    break;
                default:
                    dialog.replace("%lang%", "Unk");
                    break;
            }
            if (activeChar.getHWIDLock() != null && activeChar.getNetConnection() != null && activeChar.getNetConnection().getHwid() != null && !activeChar.getNetConnection().getHwid().isEmpty()) {
                dialog.replace("%hwidlock%", "On");
            } else {
                dialog.replace("%hwidlock%", "Off");
            }
            if (activeChar.getIPLock() != null && activeChar.getNetConnection() != null && activeChar.getNetConnection().getIpAddr() != null && !activeChar.getNetConnection().getIpAddr().isEmpty()) {
                dialog.replace("%iplock%", "On");
            } else {
                dialog.replace("%iplock%", "Off");
            }
            if (activeChar.buffAnimRange() < 0) {
                dialog.replace("%buffAnimRange%", "Off");
            } else if (activeChar.buffAnimRange() == 0) {
                if (activeChar.isLangRus()) {
                    dialog.replace("%buffAnimRange%", "\u0421\u0432\u043e\u0438");
                } else {
                    dialog.replace("%buffAnimRange%", "Self");
                }
            } else {
                dialog.replace("%buffAnimRange%", String.valueOf(activeChar.buffAnimRange() / 15));
            }
            final String tl = activeChar.getVar("translit");
            if (tl == null) {
                dialog.replace("%translit%", "Off");
            } else if ("tl".equals(tl)) {
                dialog.replace("%translit%", "On");
            } else {
                dialog.replace("%translit%", "Lt");
            }

            if (!Config.SHOW_SKILL_CHANCE) {
                dialog.replace("%ssc%", "<font color=\"LEVEL\">N/A</font>");
            } else {
                dialog.replace("%ssc%", activeChar.getVarB("SkillsHideChance") ? "On" : "Off");
            }
            activeChar.sendPacket(dialog);
        } else if (command.equalsIgnoreCase(_commandList[2])) {
            final String lastChanged = activeChar.getVar("LastPwdChng");
            boolean canChange = true;
            if (lastChanged != null && !lastChanged.isEmpty()) {
                final long lastChange = Long.parseLong(lastChanged) * 1000L;
                if (lastChange + PASSWORD_CHANGE_INTERVAL > System.currentTimeMillis()) {
                    activeChar.sendMessage("Password can't be change so frequently.");
                    canChange = false;
                }
            }
            if (canChange && !args.isEmpty()) {
                final Matcher m = PASSWORD_BYPASS_PATTERN.matcher(args);
                if (m.find() && m.groupCount() == 2) {
                    final String oldpassword = m.group(1);
                    final String newpassword = m.group(2);
                    AuthServerCommunication.getInstance().sendPacket(new IGPwdCng(activeChar, oldpassword, newpassword));
                    return true;
                }
                activeChar.sendMessage("Password requirement's is not met!");
            }
            final NpcHtmlMessage dialog2 = new NpcHtmlMessage(5);
            dialog2.setFile("command/passchg.htm");
            activeChar.sendPacket(dialog2);
        } else if (command.equalsIgnoreCase(_commandList[3])) {
            final NpcHtmlMessage dialog = new NpcHtmlMessage(5);
            dialog.setFile("command/repair.htm");
            if (args.isEmpty()) {
                final StringBuilder cl = new StringBuilder();
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
                } catch (Exception ex2) {
                    ex2.printStackTrace();
                } finally {
                    DbUtils.closeQuietly(con, fpstmt, rset);
                }
                dialog.replace("%repair%", cl.toString());
            } else {
                Connection con2 = null;
                PreparedStatement fpstmt2 = null;
                ResultSet rset2 = null;
                try {
                    final int charId2 = Integer.parseInt(args);
                    con2 = DatabaseFactory.getInstance().getConnection();
                    fpstmt2 = con2.prepareStatement("SELECT * FROM `characters` WHERE `account_name` = ? AND `obj_Id` = ? AND `online` = 0");
                    fpstmt2.setString(1, activeChar.getAccountName());
                    fpstmt2.setInt(2, charId2);
                    rset2 = fpstmt2.executeQuery();
                    if (!rset2.next()) {
                        activeChar.sendMessage("Character not found.");
                        return true;
                    }
                    final String charName = rset2.getString("char_name");
                    if (World.getPlayer(charId2) != null) {
                        activeChar.sendMessage("Character online.");
                        return true;
                    }
                    DbUtils.close(fpstmt2, rset2);
                    fpstmt2 = con2.prepareStatement("UPDATE `characters` SET `x` = 17867, `y` = 170259, `z` = -3503 WHERE `obj_Id` = ?");
                    fpstmt2.setInt(1, charId2);
                    fpstmt2.executeUpdate();
                    DbUtils.close(fpstmt2);
                    fpstmt2 = con2.prepareStatement("DELETE FROM character_effects_save WHERE object_id=?");
                    fpstmt2.setInt(1, charId2);
                    fpstmt2.executeUpdate();
                    DbUtils.close(fpstmt2);
                    fpstmt2 = con2.prepareStatement("UPDATE `items` SET `location` = \"INVENTORY\" WHERE `location` = \"PAPERDOLL\" AND `owner_id` = ?");
                    fpstmt2.setInt(1, charId2);
                    fpstmt2.executeUpdate();
                    DbUtils.close(fpstmt2);
                    dialog.replace("%repair%", "Character successfully repaired.");
                    activeChar.sendMessage("Character successfully repaired.");
                } catch (Exception ex) {
                    dialog.replace("%repair%", "Character reparation failed.");
                    ex.printStackTrace();
                } finally {
                    DbUtils.closeQuietly(con2, fpstmt2, rset2);
                }
            }
            activeChar.sendPacket(dialog);
        }
        return true;
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }
}
