package services.community.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.Location;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.StringTokenizer;

public class CommunityTeleport implements OnInitScriptListener, ICommunityBoardHandler {
    public static final ZoneType[] FORBIDDEN_ZONES = {ZoneType.RESIDENCE, ZoneType.ssq_zone, ZoneType.battle_zone, ZoneType.SIEGE, ZoneType.no_restart, ZoneType.no_summon};
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityTeleport.class);

    public static boolean checkFirstConditions(final Player player) {
        if (player == null) {
            return false;
        }
        if (player.getActiveWeaponFlagAttachment() != null) {
            player.sendPacket(Msg.YOU_CANNOT_TELEPORT_WHILE_IN_POSSESSION_OF_A_WARD);
            return false;
        }
        if (player.isOlyParticipant()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_IN_AN_OLYMPIAD_MATCH);
            return false;
        }
        if (player.getReflection() != ReflectionManager.DEFAULT) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_IN_AN_INSTANT_ZONE);
            return false;
        }
        if (player.isInDuel()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_DUEL);
            return false;
        }
        if (player.isInCombat() || player.getPvpFlag() != 0) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_DURING_A_BATTLE);
            return false;
        }
        if (player.isOnSiegeField() || player.isInZoneBattle()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_PARTICIPATING_A_LARGE_SCALE_BATTLE_SUCH_AS_A_CASTLE_SIEGE);
            return false;
        }
        if (player.isFlying()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_FLYING);
            return false;
        }
        if (player.isInWater() || player.isInBoat()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_UNDERWATER);
            return false;
        }
        return true;
    }

    public static boolean checkTeleportConditions(final Player player) {
        if (player == null) {
            return false;
        }
        if (player.isAlikeDead()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_DEAD);
            return false;
        }
        if (player.isInStoreMode() || player.isInTrade()) {
            player.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
            return false;
        }
        if (player.isInBoat() || player.isParalyzed() || player.isStunned() || player.isSleeping()) {
            player.sendPacket(Msg.YOU_CANNOT_USE_MY_TELEPORTS_WHILE_YOU_ARE_IN_A_FLINT_OR_PARALYZED_STATE);
            return false;
        }
        return true;
    }

    public static boolean checkTeleportLocation(final Player player, final Location loc) {
        return checkTeleportLocation(player, loc.x, loc.y, loc.z);
    }

    public static boolean checkTeleportLocation(final Player player, final int x, final int y, final int z) {
        if (player == null) {
            return false;
        }
        for (final ZoneType zoneType : CommunityTeleport.FORBIDDEN_ZONES) {
            final Zone zone = player.getZone(zoneType);
            if (zone != null) {
                player.sendPacket(Msg.YOU_DO_NOT_MEET_THE_REQUIRED_CONDITION_TO_EQUIP_THAT_ITEM);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED && ACbConfigManager.ALLOW_PVPCB_TELEPORT) {
            CommunityTeleport.LOGGER.info("CommunityBoard: CommunityTeleport loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_bbsteleport", "_bbsteleport_delete", "_bbsteleport_save", "_bbsteleport_teleport"};
    }

    @Override
    public void onBypassCommand(final Player player, final String command) {
        if (!CommunityTools.checkConditions(player)) {
            String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/locked.htm", player);
            html = html.replace("%name%", player.getName());
            ShowBoard.separateAndSend(html, player);
            return;
        }
        final StringTokenizer stcmd = new StringTokenizer(command, ";");
        final String cmd = stcmd.nextToken();
        switch (cmd) {
            case "_bbsteleport":
                showTp(player);
                break;
            case "_bbsteleport_delete":
                final int TpNameDell = Integer.parseInt(stcmd.nextToken());
                delTp(player, TpNameDell);
                showTp(player);
                break;
            case "_bbsteleport_save":
                final String TpNameAdd = stcmd.nextToken();
                AddTp(player, TpNameAdd);
                showTp(player);
                break;
            case "_bbsteleport_teleport":
                final StringTokenizer stGoTp = new StringTokenizer(stcmd.nextToken(), " ");
                final int xTp = Integer.parseInt(stGoTp.nextToken());
                final int yTp = Integer.parseInt(stGoTp.nextToken());
                final int zTp = Integer.parseInt(stGoTp.nextToken());
                final int priceTp = Integer.parseInt(stGoTp.nextToken());
                goTp(player, xTp, yTp, zTp, priceTp);
                showTp(player);
                break;
            default:
                ShowBoard.separateAndSend("<html><body><br><br><center>\u0424\u0443\u043d\u043a\u0446\u0438\u044f: " + command + " \u043f\u043e\u043a\u0430 \u043d\u0435 \u0440\u0435\u0430\u043b\u0438\u0437\u043e\u0432\u0430\u043d\u0430</center><br><br></body></html>", player);
                break;
        }
    }

    private void goTp(final Player player, final int xTp, final int yTp, final int zTp, final int priceTp) {
        if (!checkFirstConditions(player) || !checkTeleportConditions(player)) {
            return;
        }
        if (!checkTeleportLocation(player, xTp, yTp, zTp)) {
            return;
        }
        if (priceTp > 0 && player.getAdena() < priceTp) {
            player.sendPacket(new SystemMessage(279));
            return;
        }
        if (priceTp > 0) {
            player.reduceAdena((long) priceTp, true);
        }
        player.teleToLocation(xTp, yTp, zTp);
    }

    private void showTp(final Player player) {
        Connection con = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            final PreparedStatement st = con.prepareStatement("SELECT * FROM bbs_comteleport WHERE charId=?;");
            st.setLong(1, player.getObjectId());
            final ResultSet rs = st.executeQuery();
            final StringBuilder html = new StringBuilder();
            html.append("<table width=220>");
            while (rs.next()) {
                final CBteleport tp = new CBteleport();
                tp.TpId = rs.getInt("TpId");
                tp.TpName = rs.getString("name");
                tp.PlayerId = rs.getInt("charId");
                tp.xC = rs.getInt("xPos");
                tp.yC = rs.getInt("yPos");
                tp.zC = rs.getInt("zPos");
                html.append("<tr>");
                html.append("<td>");
                html.append("<button value=\"").append(tp.TpName).append("\" action=\"bypass _bbsteleport_teleport;").append(tp.xC).append(" ").append(tp.yC).append(" ").append(tp.zC).append(" ").append(ACbConfigManager.ALT_CB_TELE_POINT_PRICE).append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
                html.append("</td>");
                html.append("<td>");
                html.append("<button value=\"\u0423\u0434\u0430\u043b\u0438\u0442\u044c\" action=\"bypass _bbsteleport_delete;").append(tp.TpId).append("\" width=100 height=20 back=\"L2UI_ct1.button_df\" fore=\"L2UI_ct1.button_df\">");
                html.append("</td>");
                html.append("</tr>");
            }
            html.append("</table>");
            DbUtils.closeQuietly(st, rs);
            String content = HtmCache.getInstance().getNotNull("scripts/services/community/pages/teleport.htm", player);
            content = content.replace("%tp%", html.toString());
            ShowBoard.separateAndSend(content, player);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    private void delTp(final Player player, final int TpNameDell) {
        Connection conDel = null;
        try {
            conDel = DatabaseFactory.getInstance().getConnection();
            final PreparedStatement stDel = conDel.prepareStatement("DELETE FROM bbs_comteleport WHERE charId=? AND TpId=?;");
            stDel.setInt(1, player.getObjectId());
            stDel.setInt(2, TpNameDell);
            stDel.execute();
            DbUtils.closeQuietly(stDel);
        } catch (Exception e) {
            CommunityTeleport.LOGGER.error("data error on Delete Teleport: " + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(conDel);
        }
    }

    private void AddTp(final Player player, final String TpNameAdd) {
        if (!checkFirstConditions(player) || !checkTeleportConditions(player)) {
            return;
        }
        if (!checkTeleportLocation(player, player.getX(), player.getY(), player.getZ())) {
            return;
        }
        if ("".equals(TpNameAdd) || TpNameAdd == null) {
            player.sendMessage("\u0412\u044b \u043d\u0435 \u0432\u0432\u0435\u043b\u0438 \u0418\u043c\u044f \u0437\u0430\u043a\u043b\u0430\u0434\u043a\u0438");
            return;
        }
        Connection con = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            final PreparedStatement st = con.prepareStatement("SELECT COUNT(*) FROM bbs_comteleport WHERE charId=?;");
            st.setLong(1, player.getObjectId());
            final ResultSet rs = st.executeQuery();
            rs.next();
            if (rs.getInt(1) > ACbConfigManager.ALT_CB_TELE_POINT_MAX_COUNT - 1) {
                player.sendMessage("\u0412\u044b \u043d\u0435 \u043c\u043e\u0436\u0435\u0442\u0435 \u0441\u043e\u0445\u0440\u0430\u043d\u0438\u0442\u044c \u0431\u043e\u043b\u0435\u0435 " + ACbConfigManager.ALT_CB_TELE_POINT_MAX_COUNT + " \u0437\u0430\u043a\u043b\u0430\u0434\u043e\u043a");
                return;
            }
            final PreparedStatement st2 = con.prepareStatement("SELECT COUNT(*) FROM bbs_comteleport WHERE charId=? AND name=?;");
            st2.setLong(1, player.getObjectId());
            st2.setString(2, TpNameAdd);
            final ResultSet rs2 = st2.executeQuery();
            rs2.next();
            if (rs2.getInt(1) == 0) {
                final PreparedStatement stAdd = con.prepareStatement("INSERT INTO bbs_comteleport (charId,xPos,yPos,zPos,name) VALUES(?,?,?,?,?)");
                stAdd.setInt(1, player.getObjectId());
                stAdd.setInt(2, player.getX());
                stAdd.setInt(3, player.getY());
                stAdd.setInt(4, player.getZ());
                stAdd.setString(5, TpNameAdd);
                stAdd.execute();
                DbUtils.closeQuietly(stAdd);
            } else {
                final PreparedStatement stAdd = con.prepareStatement("UPDATE bbs_comteleport SET xPos=?, yPos=?, zPos=? WHERE charId=? AND name=?;");
                stAdd.setInt(1, player.getObjectId());
                stAdd.setInt(2, player.getX());
                stAdd.setInt(3, player.getY());
                stAdd.setInt(4, player.getZ());
                stAdd.setString(5, TpNameAdd);
                stAdd.execute();
                DbUtils.closeQuietly(stAdd);
            }
            DbUtils.closeQuietly(st, rs);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con);
        }
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }

    public class CBteleport {
        public int TpId;
        public String TpName;
        public int PlayerId;
        public int xC;
        public int yC;
        public int zC;

        public CBteleport() {
            TpId = 0;
            TpName = "";
            PlayerId = 0;
            xC = 0;
            yC = 0;
            zC = 0;
        }
    }
}
