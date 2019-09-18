package ru.j2dev.gameserver.handler.admincommands.impl;

import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ManufactureItem;
import ru.j2dev.gameserver.model.items.TradeItem;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.gs2as.ChangeAccessLevel;
import ru.j2dev.gameserver.network.lineage2.CGMHelper;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.AdminFunctions;
import ru.j2dev.gameserver.utils.AutoBan;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;

import java.util.Collection;
import java.util.StringTokenizer;

public class AdminBan implements IAdminCommandHandler {
    private static String tradeToString(final Player targ, final int trade) {
        switch (trade) {
            case 3: {
                final Collection<TradeItem> list = targ.getBuyList();
                if (list == null || list.isEmpty()) {
                    return "";
                }
                StringBuilder ret = new StringBuilder(":buy:");
                for (final TradeItem i : list) {
                    ret.append(i.getItemId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":");
                }
                return ret.toString();
            }
            case 1:
            case 8: {
                final Collection<TradeItem> list = targ.getSellList();
                if (list == null || list.isEmpty()) {
                    return "";
                }
                StringBuilder ret = new StringBuilder(":sell:");
                for (final TradeItem i : list) {
                    ret.append(i.getItemId()).append(";").append(i.getCount()).append(";").append(i.getOwnersPrice()).append(":");
                }
                return ret.toString();
            }
            case 5: {
                final Collection<ManufactureItem> list = targ.getCreateList();
                if (list == null || list.isEmpty()) {
                    return "";
                }
                StringBuilder ret = new StringBuilder(":mf:");
                for (final ManufactureItem j : list) {
                    ret.append(j.getRecipeId()).append(";").append(j.getCost()).append(":");
                }
                return ret.toString();
            }
            default: {
                return "";
            }
        }
    }

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        final StringTokenizer st = new StringTokenizer(fullString);
        if (activeChar.getPlayerAccess().CanTradeBanUnban) {
            switch (command) {
                case admin_trade_ban: {
                    return tradeBan(st, activeChar);
                }
                case admin_trade_unban: {
                    return tradeUnban(st, activeChar);
                }
            }
        }
        if (activeChar.getPlayerAccess().CanBan) {
            switch (command) {
                case admin_ban: {
                    ban(st, activeChar);
                    break;
                }
                case admin_accban: {
                    st.nextToken();
                    int level = 0;
                    int banExpire = 0;
                    final String account = st.nextToken();
                    if (st.hasMoreTokens()) {
                        banExpire = (int) (System.currentTimeMillis() / 1000L) + Integer.parseInt(st.nextToken()) * 60;
                    } else {
                        level = -100;
                    }
                    AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account, level, banExpire));
                    final GameClient client = AuthServerCommunication.getInstance().getAuthedClient(account);
                    if (client != null) {
                        final Player player = client.getActiveChar();
                        if (player != null) {
                            player.kick();
                            activeChar.sendMessage("Player " + player.getName() + " kicked.");
                        }
                        break;
                    }
                    break;
                }
                case admin_accunban: {
                    st.nextToken();
                    final String account2 = st.nextToken();
                    AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account2, 0, 0));
                    break;
                }
                case admin_trade_ban: {
                    return tradeBan(st, activeChar);
                }
                case admin_trade_unban: {
                    return tradeUnban(st, activeChar);
                }
                case admin_hwidban: {
                    try {
                        st.nextToken();
                        final String charNameOrHwid = st.nextToken();
                        final Player target = World.getPlayer(charNameOrHwid);
                        String hwid2ban = null;
                        String ip = null;
                        String account3 = null;
                        final String comment = st.nextToken();
                        if (target != null) {
                            if (target.getNetConnection() != null && target.getNetConnection().isConnected()) {
                                hwid2ban = target.getNetConnection().getHwid();
                                ip = target.getNetConnection().getIpAddr();
                                account3 = target.getAccountName();
                                AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(account3, -100, 0));
                                target.kick();
                                activeChar.sendMessage("Player " + target.getName() + " kicked.");
                            }
                        } else {
                            hwid2ban = charNameOrHwid;
                        }
                        if (hwid2ban != null && !hwid2ban.isEmpty()) {
                            CGMHelper.getInstance().addHWIDBan(hwid2ban, ip, account3, comment);
                            activeChar.sendMessage("You ban hwid " + hwid2ban + ".");
                        } else {
                            activeChar.sendMessage("Such HWID or player not found.");
                        }
                    } catch (Exception e) {
                        activeChar.sendMessage("Command syntax: //hwidban [char_name|hwid] comment");
                    }
                    break;
                }
                case admin_chatban: {
                    try {
                        st.nextToken();
                        final String player2 = st.nextToken();
                        final String period = st.nextToken();
                        final String bmsg = "admin_chatban " + player2 + " " + period + " ";
                        final String msg = fullString.substring(bmsg.length(), fullString.length());
                        if (AutoBan.ChatBan(player2, Integer.parseInt(period), msg, activeChar.getName())) {
                            activeChar.sendMessage("You ban chat for " + player2 + ".");
                        } else {
                            activeChar.sendMessage("Can't find char " + player2 + ".");
                        }
                    } catch (Exception e) {
                        activeChar.sendMessage("Command syntax: //chatban char_name period reason");
                    }
                    break;
                }
                case admin_chatunban: {
                    try {
                        st.nextToken();
                        final String player2 = st.nextToken();
                        if (AutoBan.ChatUnBan(player2, activeChar.getName())) {
                            activeChar.sendMessage("You unban chat for " + player2 + ".");
                        } else {
                            activeChar.sendMessage("Can't find char " + player2 + ".");
                        }
                    } catch (Exception e) {
                        activeChar.sendMessage("Command syntax: //chatunban char_name");
                    }
                    break;
                }
                case admin_jail: {
                    try {
                        st.nextToken();
                        final String player2 = st.nextToken();
                        final String period = st.nextToken();
                        final String reason = st.nextToken();
                        final Player target2 = World.getPlayer(player2);
                        if (target2 != null) {
                            target2.setVar("jailedFrom", target2.getX() + ";" + target2.getY() + ";" + target2.getZ() + ";" + target2.getReflectionId(), -1L);
                            target2.setVar("jailed", period, -1L);
                            target2.startUnjailTask(target2, Integer.parseInt(period));
                            target2.teleToLocation(Location.findPointToStay(target2, AdminFunctions.JAIL_SPAWN, 50, 200), ReflectionManager.JAIL);
                            if (activeChar.isInStoreMode()) {
                                activeChar.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
                            }
                            target2.sitDown(null);
                            target2.block();
                            target2.sendMessage("You moved to jail, time to escape - " + period + " minutes, reason - " + reason + " .");
                            activeChar.sendMessage("You jailed " + player2 + ".");
                        } else {
                            activeChar.sendMessage("Can't find char " + player2 + ".");
                        }
                    } catch (Exception e) {
                        activeChar.sendMessage("Command syntax: //jail char_name period reason");
                    }
                    break;
                }
                case admin_unjail: {
                    try {
                        st.nextToken();
                        final String player2 = st.nextToken();
                        final Player target = World.getPlayer(player2);
                        if (target != null && target.getVar("jailed") != null) {
                            final String[] re = target.getVar("jailedFrom").split(";");
                            target.teleToLocation(Integer.parseInt(re[0]), Integer.parseInt(re[1]), Integer.parseInt(re[2]));
                            target.setReflection((re.length > 3) ? Integer.parseInt(re[3]) : 0);
                            target.stopUnjailTask();
                            target.unsetVar("jailedFrom");
                            target.unsetVar("jailed");
                            target.unblock();
                            target.standUp();
                            activeChar.sendMessage("You unjailed " + player2 + ".");
                        } else {
                            activeChar.sendMessage("Can't find char " + player2 + ".");
                        }
                    } catch (Exception e) {
                        activeChar.sendMessage("Command syntax: //unjail char_name");
                    }
                    break;
                }
                case admin_cban: {
                    activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/cban.htm"));
                    break;
                }
                case admin_permaban: {
                    if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
                        Functions.sendDebugMessage(activeChar, "Target should be set and be a player instance");
                        return false;
                    }
                    final Player banned = activeChar.getTarget().getPlayer();
                    final String banaccount = banned.getAccountName();
                    AuthServerCommunication.getInstance().sendPacket(new ChangeAccessLevel(banaccount, -100, 0));
                    if (banned.isInOfflineMode()) {
                        banned.setOfflineMode(false);
                    }
                    banned.kick();
                    Functions.sendDebugMessage(activeChar, "Player account " + banaccount + " is banned, player " + banned.getName() + " kicked.");
                    break;
                }
            }
        }
        return true;
    }

    private boolean tradeBan(final StringTokenizer st, final Player activeChar) {
        if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
            return false;
        }
        st.nextToken();
        final Player targ = (Player) activeChar.getTarget();
        long days = -1L;
        long time = -1L;
        if (st.hasMoreTokens()) {
            days = Long.parseLong(st.nextToken());
            time = days * 24L * 60L * 60L * 1000L + System.currentTimeMillis();
        }
        targ.setVar("tradeBan", String.valueOf(time), -1L);
        final String msg = activeChar.getName() + " \u0437\u0430\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043b \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044e \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436\u0443 " + targ.getName() + ((days == -1L) ? " \u043d\u0430 \u0431\u0435\u0441\u0441\u0440\u043e\u0447\u043d\u044b\u0439 \u043f\u0435\u0440\u0438\u043e\u0434." : (" \u043d\u0430 " + days + " \u0434\u043d\u0435\u0439."));
        Log.add(targ.getName() + ":" + days + tradeToString(targ, targ.getPrivateStoreType()), "tradeBan", activeChar);
        if (targ.isInOfflineMode()) {
            targ.setOfflineMode(false);
            targ.kick();
        } else if (targ.isInStoreMode()) {
            targ.setPrivateStoreType(Player.STORE_PRIVATE_NONE);
            targ.standUp();
            targ.broadcastCharInfo();
            targ.getBuyList().clear();
        }
        if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD) {
            Announcements.getInstance().announceToAll(msg);
        } else {
            Announcements.shout(activeChar, msg, ChatType.CRITICAL_ANNOUNCE);
        }
        return true;
    }

    private boolean tradeUnban(final StringTokenizer st, final Player activeChar) {
        if (activeChar.getTarget() == null || !activeChar.getTarget().isPlayer()) {
            return false;
        }
        final Player targ = (Player) activeChar.getTarget();
        targ.unsetVar("tradeBan");
        if (Config.BANCHAT_ANNOUNCE_FOR_ALL_WORLD) {
            Announcements.getInstance().announceToAll(activeChar + " \u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043b \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044e \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436\u0443 " + targ + ".");
        } else {
            Announcements.shout(activeChar, activeChar + " \u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043b \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044e \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436\u0443 " + targ + ".", ChatType.CRITICAL_ANNOUNCE);
        }
        Log.add(activeChar + " \u0440\u0430\u0437\u0431\u043b\u043e\u043a\u0438\u0440\u043e\u0432\u0430\u043b \u0442\u043e\u0440\u0433\u043e\u0432\u043b\u044e \u043f\u0435\u0440\u0441\u043e\u043d\u0430\u0436\u0443 " + targ + ".", "tradeBan", activeChar);
        return true;
    }

    private boolean ban(final StringTokenizer st, final Player activeChar) {
        try {
            st.nextToken();
            final String player = st.nextToken();
            int time = 0;
            StringBuilder msg = new StringBuilder();
            if (st.hasMoreTokens()) {
                time = Integer.parseInt(st.nextToken());
            }
            if (st.hasMoreTokens()) {
                msg = new StringBuilder("admin_ban " + player + " " + time + " ");
                while (st.hasMoreTokens()) {
                    msg.append(st.nextToken()).append(" ");
                }
                msg.toString().trim();
            }
            final Player plyr = World.getPlayer(player);
            if (plyr != null) {
                plyr.sendMessage(new CustomMessage("admincommandhandlers.YoureBannedByGM", plyr));
                plyr.setAccessLevel(-100);
                AutoBan.banPlayer(plyr, time, msg.toString(), activeChar.getName());
                plyr.kick();
                activeChar.sendMessage("You banned " + plyr.getName());
            } else if (AutoBan.banOfflinePlayer(player, -100, time, msg.toString(), activeChar.getName())) {
                activeChar.sendMessage("You banned " + player);
            } else {
                activeChar.sendMessage("Can't find char: " + player);
            }
        } catch (Exception e) {
            activeChar.sendMessage("Command syntax: //ban char_name days reason");
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private enum Commands {
        admin_ban,
        admin_unban,
        admin_hwidban,
        admin_cban,
        admin_chatban,
        admin_chatunban,
        admin_accban,
        admin_accunban,
        admin_trade_ban,
        admin_trade_unban,
        admin_jail,
        admin_unjail,
        admin_permaban
    }
}
