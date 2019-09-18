package services.community.custom;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.BuyList;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.StringTokenizer;

public class CommunityServices implements ICommunityBoardHandler, OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityServices.class);

    @Override
    public void onInit() {
        if (Config.COMMUNITYBOARD_ENABLED) {
            CommunityServices.LOGGER.info("CommunityBoard: CommunityServices loaded.");
            CommunityBoardManager.getInstance().registerHandler(this);
        }
    }

    @Override
    public String[] getBypassCommands() {
        return new String[]{"_cbbsservicesdelvl", "_cbbsservicesbuynoble", "_cbbsserviceschangesex", "_cbbsserviceschangename", "_bbssell", "_bbsclanup", "_bbsclanexpire"};
    }

    @Override
    public void onBypassCommand(final Player player, final String bypass) {
        if (!CommunityTools.checkConditions(player)) {
            String html = HtmCache.getInstance().getNotNull("scripts/services/community/pages/locked.htm", player);
            html = html.replace("%name%", player.getName());
            ShowBoard.separateAndSend(html, player);
            return;
        }
        final StringTokenizer st = new StringTokenizer(bypass, "_");
        final String cmd = st.nextToken();
        final String html2 = "";
        if ("_bbssell".equals(cmd)) {
            if (!player.getPlayerAccess().UseShop) {
                player.sendMessage("\u0412\u0430\u043c \u0437\u0430\u043f\u0440\u0435\u0449\u0435\u043d\u043e \u0438\u0441\u043f\u043e\u043b\u044c\u0437\u043e\u0432\u0430\u0442\u044c \u043c\u0430\u0433\u0430\u0437\u0438\u043d!");
                return;
            }
            player.sendPacket(new BuyList(null, player, 0.0));
        } else if (bypass.startsWith("_cbbsservicesdelvl")) {
            if (Functions.getItemCount(player, ACbConfigManager.ALT_CB_DELVLV_ITEM_ID) < ACbConfigManager.ALT_CB_DELVL_ITEM_COUNT) {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            if (player.getLevel() < 3 || player.getLevel() > player.getMaxExp()) {
                return;
            }
            Functions.removeItem(player, ACbConfigManager.ALT_CB_DELVLV_ITEM_ID, ACbConfigManager.ALT_CB_DELVL_ITEM_COUNT);
            player.addExpAndSp(Experience.LEVEL[player.getLevel() - 2] - player.getExp(), 0L, false, false);
        } else if (bypass.startsWith("_cbbsservicesbuynoble")) {
            if (Functions.getItemCount(player, ACbConfigManager.ALT_CB_NOBLES_ITEM_ID) < ACbConfigManager.ALT_CB_NOBLES_ITEM_COUNT) {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            if (player.isNoble()) {
                player.sendMessage("You already have a noble status.");
                return;
            }
            if (player.getLevel() < 76) {
                player.sendMessage("You must be 76 lvl or greater.");
                return;
            }
            Functions.removeItem(player, ACbConfigManager.ALT_CB_NOBLES_ITEM_ID, ACbConfigManager.ALT_CB_NOBLES_ITEM_COUNT);
            NoblessManager.getInstance().addNoble(player);
            player.setNoble(true);
            player.updatePledgeClass();
            player.updateNobleSkills();
            player.sendSkillList();
            player.broadcastUserInfo(true);
            player.sendMessage("Congratulation! You become a nobles.");
        } else if (bypass.startsWith("_cbbsserviceschangesex")) {
            if (Functions.getItemCount(player, ACbConfigManager.ALT_CB_CHANGESEX_ITEM_ID) < ACbConfigManager.ALT_CB_CHANGESEX_ITEM_COUNT) {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            Functions.removeItem(player, ACbConfigManager.ALT_CB_CHANGESEX_ITEM_ID, ACbConfigManager.ALT_CB_CHANGESEX_ITEM_COUNT);
            Connection con = null;
            PreparedStatement offline = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                offline = con.prepareStatement("UPDATE characters SET sex = ? WHERE obj_Id = ?");
                offline.setInt(1, (player.getSex() != 1) ? 1 : 0);
                offline.setInt(2, player.getObjectId());
                offline.executeUpdate();
            } catch (Exception e) {
                e.printStackTrace();
                return;
            } finally {
                DbUtils.closeQuietly(con, offline);
            }
            player.setHairColor(0);
            player.setHairStyle(0);
            player.setFace(0);
            player.logout();
        } else if (bypass.startsWith("_cbbsserviceschangename")) {
            if (player.isHero()) {
                player.sendMessage("Rename is unavailable for hero character.");
                return;
            }
            if (player.getEvent(SiegeEvent.class) != null) {
                player.sendMessage("Rename is unavailable in siege period.");
                return;
            }
            String[] param = null;
            try {
                param = bypass.split(" ");
            } catch (Exception ignored) {
            }
            if (param == null || param.length != 2 || param[1] == null) {
                player.sendMessage("Incorrect name.");
                return;
            }
            final String newname = param[1];
            if (!Util.isMatchingRegexp(newname, Config.CNAME_TEMPLATE) || newname.length() > Config.CNAME_MAXLEN) {
                player.sendMessage("Incorrect name.");
                return;
            }
            if (CharacterDAO.getInstance().getObjectIdByName(newname) > 0) {
                player.sendMessage("Name already used.");
                return;
            }
            if (Functions.getItemCount(player, ACbConfigManager.ALT_CB_CHANGENAME_ITEM_ID) < ACbConfigManager.ALT_CB_CHANGENAME_ITEM_COUNT) {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            Functions.removeItem(player, ACbConfigManager.ALT_CB_CHANGENAME_ITEM_ID, ACbConfigManager.ALT_CB_CHANGENAME_ITEM_COUNT);
            player.reName(newname, true);
            player.logout();
        } else if (bypass.startsWith("_bbsclanup")) {
            final Clan clan = player.getClan();
            if (clan == null) {
                player.sendMessage("Get clan first.");
                return;
            }
            if (clan.getLeaderId() != player.getObjectId()) {
                player.sendMessage("Only clan leader can do that.");
                return;
            }
            if (clan.getLevel() < 1 || clan.getLevel() > 7) {
                player.sendMessage("Clan level to high.");
                return;
            }
            if (Functions.getItemCount(player, ACbConfigManager.ALT_CB_CLANUP_ITEM_ID) < ACbConfigManager.ALT_CB_CLANUP_ITEM_COUNT) {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            Functions.removeItem(player, ACbConfigManager.ALT_CB_CLANUP_ITEM_ID, ACbConfigManager.ALT_CB_CLANUP_ITEM_COUNT);
            clan.setLevel(clan.getLevel() + 1);
            clan.updateClanInDB();
            clan.broadcastClanStatus(true, true, true);
        } else if (bypass.startsWith("_bbsclanexpire")) {
            final Clan clan = player.getClan();
            if (clan == null) {
                player.sendMessage("Get clan first.");
                return;
            }
            if (clan.getLeaderId() != player.getObjectId()) {
                player.sendMessage("Only clan leader can do that.");
                return;
            }
            if (Functions.getItemCount(player, ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_ID) < ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_COUNT) {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                return;
            }
            Functions.removeItem(player, ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_ID, ACbConfigManager.ALT_CB_CLAN_PENALTY_ITEM_COUNT);
            player.getClan().setExpelledMemberTime(0L);
            player.sendMessage("The penalty for a clan has been lifted");
        }
        ShowBoard.separateAndSend(html2, player);
    }

    @Override
    public void onWriteCommand(final Player player, final String bypass, final String arg1, final String arg2, final String arg3, final String arg4, final String arg5) {
    }
}
