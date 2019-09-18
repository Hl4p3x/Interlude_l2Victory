package ru.j2dev.gameserver.handler.admincommands.impl;

import org.apache.commons.lang3.math.NumberUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.PlayerClass;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExPCCafePointInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillList;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.Util;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AdminEditChar implements IAdminCommandHandler {
    public static void showCharacterList(final Player activeChar, Player player) {
        if (player == null) {
            final GameObject target = activeChar.getTarget();
            if (target == null || !target.isPlayer()) {
                return;
            }
            player = (Player) target;
        } else {
            activeChar.setTarget(player);
        }
        String clanName = "No Clan";
        if (player.getClan() != null) {
            clanName = player.getClan().getName() + "/" + player.getClan().getLevel();
        }
        final NumberFormat df = NumberFormat.getNumberInstance(Locale.ENGLISH);
        df.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(1);
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<table width=260><tr>");
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("</tr></table><br1>");
        replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td width=100>Account/IP:</td><td>").append(player.getAccountName()).append("/<a action=\"bypass -h admin_show_characters_by_ip ").append(player.getIP()).append("\">").append(player.getIP()).append("</a></td></tr>");
        if (player.getNetConnection() != null && player.getNetConnection().getHwid() != null && !player.getNetConnection().getHwid().isEmpty()) {
            final String hwid = player.getNetConnection().getHwid();
            replyMSG.append("<tr><td width=100>HWID:</td><td>").append(hwid).append("</td></tr>");
        }
        replyMSG.append("<tr><td width=100>Name/Level:</td><td>").append(player.getName()).append("/").append(player.getLevel()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Class/Id:</td><td>").append(player.getTemplate().className).append("/").append(player.getClassId().getId()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Clan/Level:</td><td>").append(clanName).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Exp/Sp:</td><td>").append(player.getExp()).append("/").append(player.getSp()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Cur/Max Hp:</td><td>").append((int) player.getCurrentHp()).append("/").append(player.getMaxHp()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Cur/Max Mp:</td><td>").append((int) player.getCurrentMp()).append("/").append(player.getMaxMp()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Cur/Max Load:</td><td>").append(player.getCurrentLoad()).append("/").append(player.getMaxLoad()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Patk/Matk:</td><td>").append(player.getPAtk(null)).append("/").append(player.getMAtk(null, null)).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Pdef/Mdef:</td><td>").append(player.getPDef(null)).append("/").append(player.getMDef(null, null)).append("</td></tr>");
        replyMSG.append("<tr><td width=100>PAtkSpd/MAtkSpd:</td><td>").append(player.getPAtkSpd()).append("/").append(player.getMAtkSpd()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Acc/Evas:</td><td>").append(player.getAccuracy()).append("/").append(player.getEvasionRate(null)).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Crit/MCrit:</td><td>").append(player.getCriticalHit(null, null)).append("/").append(df.format(player.getMagicCriticalRate(null, null))).append("%</td></tr>");
        replyMSG.append("<tr><td width=100>Walk/Run:</td><td>").append(player.getWalkSpeed()).append("/").append(player.getRunSpeed()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>PvP/PK:</td><td>").append(player.getPvpKills()).append("/").append(player.getPkKills()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Coordinates:</td><td>").append(player.getX()).append(",").append(player.getY()).append(",").append(player.getZ()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>AI:</td><td>").append(player.getAI().getIntention()).append("/").append(player.getAI().getNextAction()).append("</td></tr>");
        replyMSG.append("<tr><td width=100>Direction:</td><td>").append(PositionUtils.getDirectionTo(player, activeChar)).append("</td></tr>");
        replyMSG.append("</table><br1>");
        replyMSG.append("<table<tr>");
        replyMSG.append("<td><button value=\"Skills\" action=\"bypass -h admin_show_skills\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td><button value=\"Effects\" action=\"bypass -h admin_show_effects\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td><button value=\"Actions\" action=\"bypass -h admin_character_actions\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("</tr><tr>");
        replyMSG.append("<td><button value=\"Stats\" action=\"bypass -h admin_edit_character\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td><button value=\"Exp & Sp\" action=\"bypass -h admin_add_exp_sp_to_character\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td></td>");
        replyMSG.append("</tr></table></body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (activeChar.getPlayerAccess().CanRename) {
            if (fullString.startsWith("admin_settitle")) {
                try {
                    final String val = fullString.substring(15);
                    final GameObject target = activeChar.getTarget();
                    Player player;
                    if (target == null) {
                        return false;
                    }
                    if (target.isPlayer()) {
                        player = (Player) target;
                        player.setTitle(val);
                        player.sendMessage("Your title has been changed by a GM");
                        player.sendChanges();
                    } else if (target.isNpc()) {
                        ((NpcInstance) target).setTitle(val);
                        target.decayMe();
                        target.spawnMe();
                    }
                    return true;
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("You need to specify the new title.");
                    return false;
                }
            }
            if (fullString.startsWith("admin_setclass")) {
                try {
                    final String val = fullString.substring(15);
                    final int id = Integer.parseInt(val.trim());
                    GameObject target2 = activeChar.getTarget();
                    if (target2 == null || !target2.isPlayer()) {
                        target2 = activeChar;
                    }
                    if (id > 118) {
                        activeChar.sendMessage("There are no classes over 118 id.");
                        return false;
                    }
                    final Player player2 = target2.getPlayer();
                    player2.setClassId(id, false, false);
                    player2.sendMessage("Your class has been changed by a GM");
                    player2.broadcastCharInfo();
                    return true;
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("You need to specify the new class id.");
                    return false;
                }
            }
            if (fullString.startsWith("admin_setname")) {
                try {
                    final String val = fullString.substring(14);
                    final GameObject target = activeChar.getTarget();
                    if (target == null || !target.isPlayer()) {
                        return false;
                    }
                    final Player player = (Player) target;
                    if (mysql.simple_get_int("count(*)", "characters", "`char_name` like '" + val + "'") > 0) {
                        activeChar.sendMessage("Name already exist.");
                        return false;
                    }
                    Log.add("Character " + player.getName() + " renamed to " + val + " by GM " + activeChar.getName(), "renames");
                    player.reName(val, true);
                    player.sendMessage("Your name has been changed by a GM");
                    return true;
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("You need to specify the new name.");
                    return false;
                }
            }
        }
        if (!activeChar.getPlayerAccess().CanEditChar && !activeChar.getPlayerAccess().CanViewChar) {
            return false;
        }
        if ("admin_current_player".equals(fullString)) {
            showCharacterList(activeChar, null);
        } else if (fullString.startsWith("admin_character_list")) {
            try {
                final String val = fullString.substring(21);
                final Player target3 = GameObjectsStorage.getPlayer(val);
                showCharacterList(activeChar, target3);
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        } else if (fullString.startsWith("admin_show_characters_by_ip")) {
            try {
                final String sciArgsTxt = fullString.substring(28).trim();
                final String[] sciArgs = sciArgsTxt.split("\\s+");
                listCharactersByIp(activeChar, sciArgs[0], (sciArgs.length > 1) ? Integer.parseInt(sciArgs[1]) : 0);
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        } else if (fullString.startsWith("admin_show_characters")) {
            try {
                final String val = fullString.substring(22);
                final int page = Integer.parseInt(val);
                listCharacters(activeChar, page);
            } catch (StringIndexOutOfBoundsException ignored) {
            }
        } else if (fullString.startsWith("admin_find_character")) {
            try {
                final String val = fullString.substring(21);
                findCharacter(activeChar, val);
            } catch (StringIndexOutOfBoundsException e) {
                activeChar.sendMessage("You didnt enter a character name to find.");
                listCharacters(activeChar, 0);
            }
        } else {
            if (!activeChar.getPlayerAccess().CanEditChar) {
                return false;
            }
            if ("admin_edit_character".equals(fullString)) {
                editCharacter(activeChar);
            } else if ("admin_character_actions".equals(fullString)) {
                showCharacterActions(activeChar);
            } else if ("admin_nokarma".equals(fullString)) {
                setTargetKarma(activeChar, 0);
            } else if (fullString.startsWith("admin_setkarma")) {
                try {
                    final String val = fullString.substring(15);
                    final int karma = Integer.parseInt(val);
                    setTargetKarma(activeChar, karma);
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("Please specify new karma value.");
                }
            } else if (fullString.startsWith("admin_save_modifications")) {
                try {
                    final String val = fullString.substring(24);
                    adminModifyCharacter(activeChar, val);
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("Error while modifying character.");
                    listCharacters(activeChar, 0);
                }
            } else if ("admin_rec".equals(fullString)) {
                final GameObject target4 = activeChar.getTarget();
                Player player3;
                if (target4 == null || !target4.isPlayer()) {
                    return false;
                }
                player3 = (Player) target4;
                player3.setGivableRec(player3.getGivableRec() + 1);
                player3.sendMessage("You have been recommended by a GM");
                player3.broadcastCharInfo();
            } else if (fullString.startsWith("admin_rec")) {
                try {
                    final String val = fullString.substring(10);
                    final int recVal = Integer.parseInt(val);
                    final GameObject target2 = activeChar.getTarget();
                    Player player2;
                    if (target2 == null || !target2.isPlayer()) {
                        return false;
                    }
                    player2 = (Player) target2;
                    player2.setGivableRec(player2.getGivableRec() + recVal);
                    player2.sendMessage("You have been recommended by a GM");
                    player2.broadcastCharInfo();
                } catch (NumberFormatException e2) {
                    activeChar.sendMessage("Command format is //rec <number>");
                }
            } else if (fullString.startsWith("admin_sethero")) {
                final GameObject target4 = activeChar.getTarget();
                Player player3;
                if (wordList.length > 1 && wordList[1] != null) {
                    player3 = GameObjectsStorage.getPlayer(wordList[1]);
                    if (player3 == null) {
                        activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
                        return false;
                    }
                } else {
                    if (target4 == null || !target4.isPlayer()) {
                        activeChar.sendMessage("You must specify the name or target character.");
                        return false;
                    }
                    player3 = (Player) target4;
                }
                if (player3.isHero()) {
                    player3.setHero(false);
                    player3.updatePledgeClass();
                    HeroManager.removeSkills(player3);
                } else {
                    player3.setHero(true);
                    player3.updatePledgeClass();
                    HeroManager.addSkills(player3);
                }
                player3.sendMessage("Admin has changed your hero status.");
                player3.broadcastUserInfo(true);
            } else if (fullString.startsWith("admin_setnoble")) {
                final GameObject target4 = activeChar.getTarget();
                Player player3;
                if (wordList.length > 1 && wordList[1] != null) {
                    player3 = GameObjectsStorage.getPlayer(wordList[1]);
                    if (player3 == null) {
                        activeChar.sendMessage("Character " + wordList[1] + " not found in game.");
                        return false;
                    }
                } else {
                    if (target4 == null || !target4.isPlayer()) {
                        activeChar.sendMessage("You must specify the name or target character.");
                        return false;
                    }
                    player3 = (Player) target4;
                }
                if (player3.isNoble()) {
                    player3.setNoble(false);
                    NoblessManager.getInstance().addNoble(player3);
                    player3.sendMessage("Admin changed your noble status, now you are not nobless.");
                } else {
                    player3.setNoble(true);
                    NoblessManager.getInstance().addNoble(player3);
                    player3.sendMessage("Admin changed your noble status, now you are Nobless.");
                }
                player3.updatePledgeClass();
                player3.updateNobleSkills();
                player3.sendPacket(new SkillList(player3));
                player3.broadcastUserInfo(true);
            } else if (fullString.startsWith("admin_setsex")) {
                final GameObject target4 = activeChar.getTarget();
                Player player3;
                if (target4 == null || !target4.isPlayer()) {
                    return false;
                }
                player3 = (Player) target4;
                player3.changeSex();
                player3.sendMessage("Your gender has been changed by a GM");
                player3.broadcastUserInfo(true);
            } else if (fullString.startsWith("admin_setcolor")) {
                try {
                    final String val = fullString.substring(15);
                    final GameObject target = activeChar.getTarget();
                    Player player;
                    if (target == null || !target.isPlayer()) {
                        return false;
                    }
                    player = (Player) target;
                    player.setNameColor(Integer.decode("0x" + val));
                    player.sendMessage("Your name color has been changed by a GM");
                    player.broadcastUserInfo(true);
                } catch (StringIndexOutOfBoundsException e) {
                    activeChar.sendMessage("You need to specify the new color.");
                }
            } else if (fullString.startsWith("admin_add_exp_sp_to_character")) {
                addExpSp(activeChar);
            } else if (fullString.startsWith("admin_add_exp_sp")) {
                try {
                    final String val = fullString.substring(16).trim();
                    final String[] vals = val.split(" ");
                    final long exp = NumberUtils.toLong(vals[0], 0L);
                    final int sp = (vals.length > 1) ? NumberUtils.toInt(vals[1], 0) : 0;
                    adminAddExpSp(activeChar, exp, sp);
                } catch (Exception e3) {
                    activeChar.sendMessage("Usage: //add_exp_sp <exp> <sp>");
                }
            } else if (fullString.startsWith("admin_trans")) {
                final StringTokenizer st = new StringTokenizer(fullString);
                if (st.countTokens() > 1) {
                    st.nextToken();
                    int transformId;
                    try {
                        transformId = Integer.parseInt(st.nextToken());
                    } catch (Exception e4) {
                        activeChar.sendMessage("Specify a valid integer value.");
                        return false;
                    }
                    if (transformId != 0 && activeChar.getTransformation() != 0) {
                        activeChar.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
                        return false;
                    }
                    activeChar.setTransformation(transformId);
                    activeChar.sendMessage("Transforming...");
                } else {
                    activeChar.sendMessage("Usage: //trans <ID>");
                }
            } else if (fullString.startsWith("admin_setsubclass")) {
                final GameObject target4 = activeChar.getTarget();
                if (target4 == null || !target4.isPlayer()) {
                    activeChar.sendPacket(Msg.SELECT_TARGET);
                    return false;
                }
                final Player player3 = (Player) target4;
                final StringTokenizer st2 = new StringTokenizer(fullString);
                if (st2.countTokens() > 1) {
                    st2.nextToken();
                    final int classId = Short.parseShort(st2.nextToken());
                    if (!player3.addSubClass(classId, true)) {
                        activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.SubclassCouldNotBeAdded", activeChar));
                        return false;
                    }
                    player3.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
                } else {
                    setSubclass(activeChar, player3);
                }
            } else if (fullString.startsWith("admin_setbday")) {
                final String msgUsage = "Usage: //setbday YYYY-MM-DD";
                final String date = fullString.substring(14);
                if (date.length() != 10 || !Util.isMatchingRegexp(date, "[0-9]{4}-[0-9]{2}-[0-9]{2}")) {
                    activeChar.sendMessage(msgUsage);
                    return false;
                }
                final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    dateFormat.parse(date);
                } catch (ParseException e5) {
                    activeChar.sendMessage(msgUsage);
                }
                if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                    activeChar.sendMessage("Please select a character.");
                    return false;
                }
                if (!mysql.set("update characters set createtime = UNIX_TIMESTAMP('" + date + "') where obj_Id = " + activeChar.getTarget().getObjectId())) {
                    activeChar.sendMessage(msgUsage);
                    return false;
                }
                activeChar.sendMessage("New Birthday for " + activeChar.getTarget().getName() + ": " + date);
                activeChar.getTarget().getPlayer().sendMessage("Admin changed your birthday to: " + date);
            } else if (fullString.startsWith("admin_give_item")) {
                if (wordList.length < 3) {
                    activeChar.sendMessage("Usage: //give_item id count <target>");
                    return false;
                }
                final int id2 = Integer.parseInt(wordList[1]);
                final int count = Integer.parseInt(wordList[2]);
                if (id2 < 1 || count < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                    activeChar.sendMessage("Usage: //give_item id count <target>");
                    return false;
                }
                ItemFunctions.addItem(activeChar.getTarget().getPlayer(), id2, count, true);
            } else if (fullString.startsWith("admin_remove_item")) {
                Player target5 = null;
                boolean help = false;
                if (wordList.length >= 3) {
                    final int id3 = Integer.parseInt(wordList[1]);
                    final int count2 = Integer.parseInt(wordList[2]);
                    if (wordList.length > 3) {
                        target5 = World.getPlayer(wordList[3]);
                    }
                    if (target5 == null && activeChar.getTarget() != null) {
                        target5 = activeChar.getTarget().getPlayer();
                    }
                    if (target5 != null && id3 > 0 && count2 > 0) {
                        final long haveCount = ItemFunctions.getItemCount(target5, id3);
                        if (haveCount < count2) {
                            help = true;
                            activeChar.sendMessage("Failed: '" + target5.getName() + "' have only " + haveCount + " items.");
                        } else {
                            help = true;
                            activeChar.sendMessage("Removed " + ItemFunctions.removeItem(target5, id3, count2, true) + " from '" + target5.getName() + "'");
                        }
                    }
                }
                if (!help) {
                    activeChar.sendMessage("Usage: //remove_item id count <target>");
                    return false;
                }
            } else if (fullString.startsWith("admin_add_bang")) {
                if (!Config.ALT_PCBANG_POINTS_ENABLED) {
                    activeChar.sendMessage("Error! Pc Bang Points service disabled!");
                    return true;
                }
                if (wordList.length < 1) {
                    activeChar.sendMessage("Usage: //add_bang count <target>");
                    return false;
                }
                final int count3 = Integer.parseInt(wordList[1]);
                if (count3 < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                    activeChar.sendMessage("Usage: //add_bang count <target>");
                    return false;
                }
                final Player target3 = activeChar.getTarget().getPlayer();
                target3.addPcBangPoints(count3, false);
                activeChar.sendMessage("You have added " + count3 + " Pc Bang Points to " + target3.getName());
            } else if (fullString.startsWith("admin_set_bang")) {
                if (!Config.ALT_PCBANG_POINTS_ENABLED) {
                    activeChar.sendMessage("Error! Pc Bang Points service disabled!");
                    return true;
                }
                if (wordList.length < 1) {
                    activeChar.sendMessage("Usage: //set_bang count <target>");
                    return false;
                }
                final int count3 = Integer.parseInt(wordList[1]);
                if (count3 < 1 || activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                    activeChar.sendMessage("Usage: //set_bang count <target>");
                    return false;
                }
                final Player target3 = activeChar.getTarget().getPlayer();
                target3.setPcBangPoints(count3);
                target3.sendMessage("Your Pc Bang Points count is now " + count3);
                target3.sendPacket(new ExPCCafePointInfo(target3, count3, 1, 2, 12));
                activeChar.sendMessage("You have set " + target3.getName() + "'s Pc Bang Points to " + count3);
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void listCharactersByIp(final Player activeChar, final String IP, int page) {
        final List<Player> players = new LinkedList<>();
        GameObjectsStorage.getPlayers().stream().filter(player -> player != null && !player.isInOfflineMode() && player.isConnected()).filter(player -> player.getNetConnection() != null).forEach(player -> {
            final String playerIp = player.getNetConnection().getIpAddr();
            if (!IP.trim().equals(playerIp)) {
                return;
            }
            players.add(player);
        });
        final int MaxCharactersPerPage = 20;
        int MaxPages = players.size() / MaxCharactersPerPage;
        if (players.size() > MaxCharactersPerPage * MaxPages) {
            ++MaxPages;
        }
        if (page > MaxPages) {
            page = MaxPages;
        }
        final int CharactersStart = MaxCharactersPerPage * page;
        int CharactersEnd = players.size();
        if (CharactersEnd - CharactersStart > MaxCharactersPerPage) {
            CharactersEnd = CharactersStart + MaxCharactersPerPage;
        }
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<table width=260><tr>");
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("</tr></table>");
        replyMSG.append("<br><br>");
        replyMSG.append("<center>Characters with IP \"").append(IP).append("\"</center>");
        for (int x = 0; x < MaxPages; ++x) {
            final int pagenr = x + 1;
            replyMSG.append("<center><a action=\"bypass -h admin_show_characters_by_ip ").append(IP).append(" ").append(x).append("\">Page ").append(pagenr).append("</a></center>");
        }
        replyMSG.append("<br>");
        replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
        for (int i = CharactersStart; i < CharactersEnd; ++i) {
            final Player p = players.get(i);
            replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list ").append(p.getName()).append("\">").append(p.getName()).append("</a></td><td width=110>").append(p.getTemplate().className).append("</td><td width=40>").append(p.getLevel()).append("</td></tr>");
        }
        replyMSG.append("</table>");
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    private void listCharacters(final Player activeChar, int page) {
        final Collection<Player> p0 = GameObjectsStorage.getPlayers(player -> !player.isPhantom());
        Player[] players = p0.toArray(new Player[0]);
        final int MaxCharactersPerPage = 20;
        int MaxPages = players.length / MaxCharactersPerPage;
        if (players.length > MaxCharactersPerPage * MaxPages) {
            ++MaxPages;
        }
        if (page > MaxPages) {
            page = MaxPages;
        }
        final int CharactersStart = MaxCharactersPerPage * page;
        int CharactersEnd = players.length;
        if (CharactersEnd - CharactersStart > MaxCharactersPerPage) {
            CharactersEnd = CharactersStart + MaxCharactersPerPage;
        }
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<table width=260><tr>");
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("</tr></table>");
        replyMSG.append("<br><br>");
        replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td width=270>You can find a character by writing his name and</td></tr>");
        replyMSG.append("<tr><td width=270>clicking Find bellow.<br></td></tr>");
        replyMSG.append("<tr><td width=270>Note: Names should be written case sensitive.</td></tr>");
        replyMSG.append("</table><br>");
        replyMSG.append("<center><table><tr><td>");
        replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
        replyMSG.append("</td></tr></table></center><br><br>");
        for (int x = 0; x < MaxPages; ++x) {
            final int pagenr = x + 1;
            replyMSG.append("<center><a action=\"bypass -h admin_show_characters ").append(x).append("\">Page ").append(pagenr).append("</a></center>");
        }
        replyMSG.append("<br>");
        replyMSG.append("<table width=270>");
        replyMSG.append("<tr><td width=80>Name:</td><td width=110>Class:</td><td width=40>Level:</td></tr>");
        for (int i = CharactersStart; i < CharactersEnd; ++i) {
            final Player p = players[i];
            replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list ").append(p.getName()).append("\">").append(p.getName()).append("</a></td><td width=110>").append(p.getTemplate().className).append("</td><td width=40>").append(p.getLevel()).append("</td></tr>");
        }
        replyMSG.append("</table>");
        replyMSG.append("</body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    private void setTargetKarma(final Player activeChar, final int newKarma) {
        final GameObject target = activeChar.getTarget();
        if (target == null) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        if (target.isPlayer()) {
            final Player player = (Player) target;
            if (newKarma >= 0) {
                final int oldKarma = player.getKarma();
                player.setKarma(newKarma);
                player.sendMessage("Admin has changed your karma from " + oldKarma + " to " + newKarma + ".");
                activeChar.sendMessage("Successfully Changed karma for " + player.getName() + " from (" + oldKarma + ") to (" + newKarma + ").");
            } else {
                activeChar.sendMessage("You must enter a value for karma greater than or equal to 0.");
            }
        }
    }

    private void adminModifyCharacter(final Player activeChar, final String modifications) {
        final GameObject target = activeChar.getTarget();
        if (target == null || !target.isPlayer()) {
            activeChar.sendPacket(Msg.SELECT_TARGET);
            return;
        }
        final Player player = (Player) target;
        final String[] strvals = modifications.split("&");
        final Integer[] vals = new Integer[strvals.length];
        for (int i = 0; i < strvals.length; ++i) {
            strvals[i] = strvals[i].trim();
            vals[i] = (strvals[i].isEmpty() ? null : Integer.valueOf(strvals[i]));
        }
        if (vals[0] != null) {
            player.setCurrentHp(vals[0], false);
        }
        if (vals[1] != null) {
            player.setCurrentMp(vals[1]);
        }
        if (vals[2] != null) {
            player.setKarma(vals[2]);
        }
        if (vals[3] != null) {
            player.setPvpFlag(vals[3]);
        }
        if (vals[4] != null) {
            player.setPvpKills(vals[4]);
        }
        if (vals[5] != null) {
            player.setClassId(vals[5], true, false);
        }
        editCharacter(activeChar);
        player.broadcastCharInfo();
        player.decayMe();
        player.spawnMe(activeChar.getLoc());
    }

    private void editCharacter(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target == null || !target.isPlayer()) {
            activeChar.sendPacket(Msg.SELECT_TARGET);
            return;
        }
        final Player player = (Player) target;
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        String replyMSG = "<html><body>" + "<table width=260><tr>" +
                "<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "<td width=180><center>Character Selection Menu</center></td>" +
                "<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                "</tr></table>" +
                "<br><br>" +
                "<center>Editing character: " + player.getName() + "</center><br>" +
                "<table width=250>" +
                "<tr><td width=40></td><td width=70>Curent:</td><td width=70>Max:</td><td width=70></td></tr>" +
                "<tr><td width=40>HP:</td><td width=70>" + player.getCurrentHp() + "</td><td width=70>" + player.getMaxHp() + "</td><td width=70>Karma: " + player.getKarma() + "</td></tr>" +
                "<tr><td width=40>MP:</td><td width=70>" + player.getCurrentMp() + "</td><td width=70>" + player.getMaxMp() + "</td><td width=70>Pvp Kills: " + player.getPvpKills() + "</td></tr>" +
                "<tr><td width=40>Load:</td><td width=70>" + player.getCurrentLoad() + "</td><td width=70>" + player.getMaxLoad() + "</td><td width=70>Pvp Flag: " + player.getPvpFlag() + "</td></tr>" +
                "</table>" +
                "<table width=270><tr><td>Class<?> Template Id: " + player.getClassId() + "/" + player.getClassId().getId() + "</td></tr></table><br>" +
                "<table width=270>" +
                "<tr><td>Note: Fill all values before saving the modifications.</td></tr>" +
                "</table><br>" +
                "<table width=270>" +
                "<tr><td width=50>Hp:</td><td><edit var=\"hp\" width=50></td><td width=50>Mp:</td><td><edit var=\"mp\" width=50></td></tr>" +
                "<tr><td width=50>Pvp Flag:</td><td><edit var=\"pvpflag\" width=50></td><td width=50>Karma:</td><td><edit var=\"karma\" width=50></td></tr>" +
                "<tr><td width=50>Class<?> Id:</td><td><edit var=\"classid\" width=50></td><td width=50>Pvp Kills:</td><td><edit var=\"pvpkills\" width=50></td></tr>" +
                "</table><br>" +
                "<center><button value=\"Save Changes\" action=\"bypass -h admin_save_modifications $hp & $mp & $karma & $pvpflag & $pvpkills & $classid &\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></center><br>" +
                "</body></html>";
        adminReply.setHtml(replyMSG);
        activeChar.sendPacket(adminReply);
    }

    private void showCharacterActions(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayer()) {
            final Player player = (Player) target;
            final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            String replyMSG = "<html><body>" + "<table width=260><tr>" +
                    "<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td width=180><center>Character Selection Menu</center></td>" +
                    "<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "</tr></table><br><br>" +
                    "<center>Admin Actions for: " + player.getName() + "</center><br>" +
                    "<center><table width=200><tr>" +
                    "<td width=100>Argument(*):</td><td width=100><edit var=\"arg\" width=100></td>" +
                    "</tr></table><br></center>" +
                    "<table width=270>" +
                    "<tr><td width=90><button value=\"Teleport\" action=\"bypass -h admin_teleportto " + player.getName() + "\" width=85 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td width=90><button value=\"Recall\" action=\"bypass -h admin_recall " + player.getName() + "\" width=85 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td width=90><button value=\"Quests\" action=\"bypass -h admin_quests " + player.getName() + "\" width=85 height=20 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td></tr>" +
                    "</body></html>";
            adminReply.setHtml(replyMSG);
            activeChar.sendPacket(adminReply);
        }
    }

    private void findCharacter(final Player activeChar, final String CharacterToFind) {
        final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
        int CharactersFound = 0;
        final StringBuilder replyMSG = new StringBuilder("<html><body>");
        replyMSG.append("<table width=260><tr>");
        replyMSG.append("<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("<td width=180><center>Character Selection Menu</center></td>");
        replyMSG.append("<td width=40><button value=\"Back\" action=\"bypass -h admin_show_characters 0\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>");
        replyMSG.append("</tr></table>");
        replyMSG.append("<br><br>");
        for (final Player element : GameObjectsStorage.getPlayers()) {
            if (element.getName().startsWith(CharacterToFind)) {
                ++CharactersFound;
                replyMSG.append("<table width=270>");
                replyMSG.append("<tr><td width=80>Name</td><td width=110>Class</td><td width=40>Level</td></tr>");
                replyMSG.append("<tr><td width=80><a action=\"bypass -h admin_character_list ").append(element.getName()).append("\">").append(element.getName()).append("</a></td><td width=110>").append(element.getTemplate().className).append("</td><td width=40>").append(element.getLevel()).append("</td></tr>");
                replyMSG.append("</table>");
            }
        }
        if (CharactersFound == 0) {
            replyMSG.append("<table width=270>");
            replyMSG.append("<tr><td width=270>Your search did not find any characters.</td></tr>");
            replyMSG.append("<tr><td width=270>Please try again.<br></td></tr>");
            replyMSG.append("</table><br>");
            replyMSG.append("<center><table><tr><td>");
            replyMSG.append("<edit var=\"character_name\" width=80></td><td><button value=\"Find\" action=\"bypass -h admin_find_character $character_name\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\">");
            replyMSG.append("</td></tr></table></center>");
        } else {
            replyMSG.append("<center><br>Found ").append(CharactersFound).append(" character");
            if (CharactersFound == 1) {
                replyMSG.append(".");
            } else if (CharactersFound > 1) {
                replyMSG.append("s.");
            }
        }
        replyMSG.append("</center></body></html>");
        adminReply.setHtml(replyMSG.toString());
        activeChar.sendPacket(adminReply);
    }

    private void addExpSp(final Player activeChar) {
        final GameObject target = activeChar.getTarget();
        if (target != null && target.isPlayer() && (activeChar == target || activeChar.getPlayerAccess().CanEditCharAll)) {
            final Player player = (Player) target;
            final NpcHtmlMessage adminReply = new NpcHtmlMessage(5);
            String replyMSG = "<html><body>" + "<table width=260><tr>" +
                    "<td width=40><button value=\"Main\" action=\"bypass -h admin_admin\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "<td width=180><center>Character Selection Menu</center></td>" +
                    "<td width=40><button value=\"Back\" action=\"bypass -h admin_current_player\" width=40 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "</tr></table>" +
                    "<br><br>" +
                    "<table width=270><tr><td>Name: " + player.getName() + "</td></tr>" +
                    "<tr><td>Lv: " + player.getLevel() + " " + player.getTemplate().className + "</td></tr>" +
                    "<tr><td>Exp: " + player.getExp() + "</td></tr>" +
                    "<tr><td>Sp: " + player.getSp() + "</td></tr></table>" +
                    "<br><table width=270><tr><td>Note: Dont forget that modifying players skills can</td></tr>" +
                    "<tr><td>ruin the game...</td></tr></table><br>" +
                    "<table width=270><tr><td>Note: Fill all values before saving the modifications.,</td></tr>" +
                    "<tr><td>Note: Use 0 if no changes are needed.</td></tr></table><br>" +
                    "<center><table><tr>" +
                    "<td>Exp: <edit var=\"exp_to_add\" width=50></td>" +
                    "<td>Sp:  <edit var=\"sp_to_add\" width=50></td>" +
                    "<td>&nbsp;<button value=\"Save Changes\" action=\"bypass -h admin_add_exp_sp $exp_to_add $sp_to_add\" width=80 height=15 back=\"sek.cbui94\" fore=\"sek.cbui94\"></td>" +
                    "</tr></table></center>" +
                    "</body></html>";
            adminReply.setHtml(replyMSG);
            activeChar.sendPacket(adminReply);
            return;
        }
        activeChar.sendPacket(Msg.INVALID_TARGET);
    }

    private void adminAddExpSp(final Player activeChar, final long exp, final int sp) {
        if (!activeChar.getPlayerAccess().CanEditCharAll) {
            activeChar.sendMessage("You have not enough privileges, for use this function.");
            return;
        }
        final GameObject target = activeChar.getTarget();
        if (target == null) {
            activeChar.sendPacket(Msg.SELECT_TARGET);
            return;
        }
        if (!target.isPlayable()) {
            activeChar.sendPacket(Msg.INVALID_TARGET);
            return;
        }
        final Playable playable = (Playable) target;
        playable.addExpAndSp(exp, sp);
        activeChar.sendMessage("Added " + exp + " experience and " + sp + " SP to " + playable.getName() + ".");
    }

    private void setSubclass(final Player activeChar, final Player player) {
        final StringBuilder content = new StringBuilder("<html><body>");
        final NpcHtmlMessage html = new NpcHtmlMessage(5);
        final Set<PlayerClass> subsAvailable = getAvailableSubClasses(player);
        if (subsAvailable != null && !subsAvailable.isEmpty()) {
            content.append("Add Subclass:<br>Which subclass do you wish to add?<br>");
            for (final PlayerClass subClass : subsAvailable) {
                content.append("<a action=\"bypass -h admin_setsubclass ").append(subClass.ordinal()).append("\">").append(formatClassForDisplay(subClass)).append("</a><br>");
            }
            content.append("</body></html>");
            html.setHtml(content.toString());
            activeChar.sendPacket(html);
            return;
        }
        activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2VillageMasterInstance.NoSubAtThisTime", activeChar));
    }

    private Set<PlayerClass> getAvailableSubClasses(final Player player) {
        final int charClassId = player.getBaseClassId();
        final PlayerClass currClass = PlayerClass.values()[charClassId];
        final Set<PlayerClass> availSubs = currClass.getAvailableSubclasses();
        if (availSubs == null) {
            return null;
        }
        availSubs.remove(currClass);
        for (final PlayerClass availSub : availSubs) {
            for (final SubClass subClass : player.getSubClasses().values()) {
                if (availSub.ordinal() == subClass.getClassId()) {
                    availSubs.remove(availSub);
                } else {
                    final ClassId parent = ClassId.VALUES[availSub.ordinal()].getParent(player.getSex());
                    if (parent != null && parent.getId() == subClass.getClassId()) {
                        availSubs.remove(availSub);
                    } else {
                        final ClassId subParent = ClassId.VALUES[subClass.getClassId()].getParent(player.getSex());
                        if (subParent == null || subParent.getId() != availSub.ordinal()) {
                            continue;
                        }
                        availSubs.remove(availSub);
                    }
                }
            }
        }
        return availSubs;
    }

    private String formatClassForDisplay(final PlayerClass className) {
        String classNameStr = className.toString();
        final char[] charArray = classNameStr.toCharArray();
        for (int i = 1; i < charArray.length; ++i) {
            if (Character.isUpperCase(charArray[i])) {
                classNameStr = classNameStr.substring(0, i) + " " + classNameStr.substring(i);
            }
        }
        return classNameStr;
    }

    private enum Commands {
        admin_edit_character,
        admin_character_actions,
        admin_current_player,
        admin_nokarma,
        admin_setkarma,
        admin_character_list,
        admin_show_characters,
        admin_show_characters_by_ip,
        admin_find_character,
        admin_save_modifications,
        admin_rec,
        admin_settitle,
        admin_setclass,
        admin_setname,
        admin_setsex,
        admin_setcolor,
        admin_add_exp_sp_to_character,
        admin_add_exp_sp,
        admin_sethero,
        admin_setnoble,
        admin_trans,
        admin_setsubclass,
        admin_setfame,
        admin_setbday,
        admin_give_item,
        admin_remove_item,
        admin_add_bang,
        admin_set_bang
    }
}
