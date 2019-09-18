package npc.model.residences.clanhall;

import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallAuctionEvent;
import ru.j2dev.gameserver.model.entity.events.objects.AuctionSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.Privilege;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.MapUtils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class AuctioneerInstance extends NpcInstance {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yy");
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.KOREA);
    private static final long WEEK = 604800000L;
    private static final int CH_PAGE_SIZE = 7;
    private static final String CH_IN_LIST = "\t<tr>\n\t\t<td width=50>\n\t\t\t<font color=\"aaaaff\">&^%id%;</font>\n\t\t</td>\n\t\t<td width=100>\n\t\t\t<a action=\"bypass -h npc_%objectId%_info %id%\"><font color=\"ffffaa\">&%%id%;[%size%]</font></a>\n\t\t</td>\n\t\t<td width=50>%date%</td>\n\t\t<td width=70 align=right>\n\t\t\t<font color=\"aaffff\">%min_bid%</font>\n\t\t</td>\n\t</tr>";
    private static final int BIDDER_PAGE_SIZE = 10;
    private static final String BIDDER_IN_LIST = "\t<tr>\n\t\t<td width=100><font color=\"aaaaff\">&%%id%;</font></td>\n\t\t<td width=100><font color=\"ffffaa\">%clan_name%</font></td>\n\t\t<td width=70>%date%</td>\n\t</tr>";

    public AuctioneerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final StringTokenizer tokenizer = new StringTokenizer(command.replace("\r\n", "<br1>"));
        final String actualCommand = tokenizer.nextToken();
        if ("map".equalsIgnoreCase(actualCommand)) {
            showChatWindow(player, getMapDialog());
        } else if ("list_all".equalsIgnoreCase(actualCommand)) {
            final int page = Integer.parseInt(tokenizer.nextToken());
            final List<ClanHallAuctionEvent> events = new ArrayList<>();
            for (final ClanHall ch : ResidenceHolder.getInstance().getResidenceList(ClanHall.class)) {
                if (ch.getSiegeEvent().getClass() == ClanHallAuctionEvent.class && ch.getSiegeEvent().isInProgress() && Config.CH_DISPLAY_IDS.contains(ch.getId())) {
                    events.add(ch.getSiegeEvent());
                }
            }
            if (events.isEmpty()) {
                player.sendPacket(SystemMsg.THERE_ARE_NO_CLAN_HALLS_UP_FOR_AUCTION);
                showChatWindow(player, 0);
                return;
            }
            int min = 7 * page;
            int max = min + 7;
            if (min > events.size()) {
                min = 0;
                max = min + 7;
            }
            if (max > events.size()) {
                max = events.size();
            }
            final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
            msg.setFile("residence2/clanhall/auction_list_clanhalls.htm");
            final StringBuilder b = new StringBuilder();
            for (int i = min; i < max; ++i) {
                final ClanHallAuctionEvent event = events.get(i);
                final List<AuctionSiegeClanObject> attackers = event.getObjects("attackers");
                final Calendar endDate = event.getEndSiegeDate();
                final String out = "\t<tr>\n\t\t<td width=50>\n\t\t\t<font color=\"aaaaff\">&^%id%;</font>\n\t\t</td>\n\t\t<td width=100>\n\t\t\t<a action=\"bypass -h npc_%objectId%_info %id%\"><font color=\"ffffaa\">&%%id%;[%size%]</font></a>\n\t\t</td>\n\t\t<td width=50>%date%</td>\n\t\t<td width=70 align=right>\n\t\t\t<font color=\"aaffff\">%min_bid%</font>\n\t\t</td>\n\t</tr>".replace("%id%", String.valueOf(event.getId())).replace("%min_bid%", String.valueOf(event.getResidence().getAuctionMinBid())).replace("%size%", String.valueOf(attackers.size())).replace("%date%", AuctioneerInstance.DATE_FORMAT.format(endDate.getTimeInMillis()));
                b.append(out);
            }
            msg.replace("%list%", b.toString());
            if (events.size() > max) {
                msg.replace("%next_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_next") + "</td>");
                msg.replace("%next_bypass%", "-h npc_%objectId%_list_all " + (page + 1));
            } else {
                msg.replace("%next_button%", "");
            }
            if (page != 0) {
                msg.replace("%prev_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_perv") + "</td>");
                msg.replace("%prev_bypass%", "-h npc_%objectId%_list_all " + (page - 1));
            } else {
                msg.replace("%prev_button%", "");
            }
            player.sendPacket(msg);
        } else if ("info".equalsIgnoreCase(actualCommand)) {
            String fileName;
            ClanHall clanHall;
            SiegeClanObject siegeClan = null;
            if (tokenizer.hasMoreTokens()) {
                final int id = Integer.parseInt(tokenizer.nextToken());
                clanHall = ResidenceHolder.getInstance().getResidence(id);
                fileName = "residence2/clanhall/auction_clanhall_info_main.htm";
            } else {
                clanHall = ((player.getClan() == null) ? null : ((player.getClan().getHasHideout() > 0) ? ((ClanHall) ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout())) : null));
                if (clanHall != null && clanHall.getSiegeEvent().getClass() == ClanHallAuctionEvent.class) {
                    if (clanHall.getSiegeEvent().isInProgress()) {
                        fileName = "residence2/clanhall/auction_clanhall_info_owner_sell.htm";
                    } else {
                        fileName = "residence2/clanhall/auction_clanhall_info_owner.htm";
                    }
                } else {
                    for (final ClanHall ch2 : ResidenceHolder.getInstance().getResidenceList(ClanHall.class)) {
                        if (ch2.getSiegeEvent().getClass() == ClanHallAuctionEvent.class && (siegeClan = ch2.getSiegeEvent().getSiegeClan("attackers", player.getClan())) != null) {
                            clanHall = ch2;
                            break;
                        }
                    }
                    if (siegeClan == null) {
                        player.sendPacket(SystemMsg.THERE_ARE_NO_OFFERINGS_I_OWN_OR_I_MADE_A_BID_FOR);
                        showChatWindow(player, 0);
                        return;
                    }
                    fileName = "residence2/clanhall/auction_clanhall_info_bidded.htm";
                }
            }
            final ClanHallAuctionEvent auctionEvent = clanHall.getSiegeEvent();
            final List<AuctionSiegeClanObject> attackers2 = auctionEvent.getObjects("attackers");
            final NpcHtmlMessage msg2 = new NpcHtmlMessage(player, this);
            msg2.setFile(fileName);
            msg2.replace("%id%", String.valueOf(clanHall.getId()));
            msg2.replace("%bigger_size%", String.valueOf(attackers2.size()));
            msg2.replace("%grade%", String.valueOf(clanHall.getGrade()));
            msg2.replace("%rental_fee%", String.valueOf(clanHall.getRentalFee()));
            msg2.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            final Clan owner = clanHall.getOwner();
            msg2.replace("%owner%", (owner == null) ? "" : owner.getName());
            msg2.replace("%owner_leader%", (owner == null) ? "" : owner.getLeaderName());
            msg2.replace("%description%", clanHall.getAuctionDescription());
            msg2.replace("%min_bid%", String.valueOf(clanHall.getAuctionMinBid()));
            final Calendar c = auctionEvent.getEndSiegeDate();
            msg2.replace("%date%", AuctioneerInstance.DATE_FORMAT.format(c.getTimeInMillis()));
            msg2.replace("%hour%", String.valueOf(c.get(Calendar.HOUR_OF_DAY)));
            final int remainingTime = (int) ((c.getTimeInMillis() - System.currentTimeMillis()) / 60000L);
            msg2.replace("%remaining_hour%", String.valueOf(remainingTime / 60));
            msg2.replace("%remaining_minutes%", String.valueOf(remainingTime % 60));
            if (siegeClan != null) {
                msg2.replace("%my_bid%", String.valueOf(siegeClan.getParam()));
            }
            player.sendPacket(msg2);
        } else if ("bidder_list".equalsIgnoreCase(actualCommand)) {
            final int id2 = Integer.parseInt(tokenizer.nextToken());
            final int page2 = Integer.parseInt(tokenizer.nextToken());
            final ClanHall clanHall2 = ResidenceHolder.getInstance().getResidence(id2);
            final ClanHallAuctionEvent auctionEvent = clanHall2.getSiegeEvent();
            final List<AuctionSiegeClanObject> attackers2 = auctionEvent.getObjects("attackers");
            if (!auctionEvent.isInProgress()) {
                return;
            }
            int min2 = 10 * page2;
            int max2 = min2 + 10;
            if (min2 > attackers2.size()) {
                min2 = 0;
                max2 = min2 + 10;
            }
            if (max2 > attackers2.size()) {
                max2 = attackers2.size();
            }
            final NpcHtmlMessage msg3 = new NpcHtmlMessage(player, this);
            msg3.setFile("residence2/clanhall/auction_bidder_list.htm");
            msg3.replace("%id%", String.valueOf(id2));
            final StringBuilder b2 = new StringBuilder();
            for (int j = min2; j < max2; ++j) {
                final AuctionSiegeClanObject siegeClan2 = attackers2.get(j);
                final String t = "\t<tr>\n\t\t<td width=100><font color=\"aaaaff\">&%%id%;</font></td>\n\t\t<td width=100><font color=\"ffffaa\">%clan_name%</font></td>\n\t\t<td width=70>%date%</td>\n\t</tr>".replace("%id%", String.valueOf(id2)).replace("%clan_name%", siegeClan2.getClan().getName()).replace("%date%", AuctioneerInstance.DATE_FORMAT.format(siegeClan2.getDate()));
                b2.append(t);
            }
            msg3.replace("%list%", b2.toString());
            if (attackers2.size() > max2) {
                msg3.replace("%next_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_next") + "</td>");
                msg3.replace("%next_bypass%", "-h npc_%objectId%_bidder_list " + id2 + " " + (page2 + 1));
            } else {
                msg3.replace("%next_button%", "");
            }
            if (page2 != 0) {
                msg3.replace("%prev_button%", "<td>" + StringHolder.getInstance().getNotNull(player, "html_util_button_prev") + "</td>");
                msg3.replace("%prev_bypass%", "-h npc_%objectId%_bidder_list " + id2 + " " + (page2 - 1));
            } else {
                msg3.replace("%prev_button%", "");
            }
            player.sendPacket(msg3);
        } else if ("bid_start".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final int id2 = Integer.parseInt(tokenizer.nextToken());
            final ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id2);
            final ClanHallAuctionEvent auctionEvent2 = clanHall.getSiegeEvent();
            if (!auctionEvent2.isInProgress()) {
                return;
            }
            long minBid = clanHall.getAuctionMinBid();
            final AuctionSiegeClanObject siegeClan3 = auctionEvent2.getSiegeClan("attackers", player.getClan());
            if (siegeClan3 != null) {
                minBid = siegeClan3.getParam();
            }
            final NpcHtmlMessage msg4 = new NpcHtmlMessage(player, this);
            msg4.setFile("residence2/clanhall/auction_bid_start.htm");
            msg4.replace("%id%", String.valueOf(id2));
            msg4.replace("%min_bid%", String.valueOf(minBid));
            msg4.replace("%clan_currency_amount%", String.valueOf(player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID)));
            msg4.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            player.sendPacket(msg4);
        } else if ("bid_next".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final int id2 = Integer.parseInt(tokenizer.nextToken());
            long bid = 0L;
            if (tokenizer.hasMoreTokens()) {
                try {
                    bid = AuctioneerInstance.NUMBER_FORMAT.parse(tokenizer.nextToken()).longValue();
                } catch (ParseException ignored) {
                }
            }
            final ClanHall clanHall3 = ResidenceHolder.getInstance().getResidence(id2);
            final ClanHallAuctionEvent auctionEvent3 = clanHall3.getSiegeEvent();
            if (!auctionEvent3.isInProgress()) {
                return;
            }
            if (!checkBid(player, auctionEvent3, bid)) {
                return;
            }
            long minBid2 = clanHall3.getAuctionMinBid();
            final AuctionSiegeClanObject siegeClan4 = auctionEvent3.getSiegeClan("attackers", player.getClan());
            if (siegeClan4 != null) {
                minBid2 = siegeClan4.getParam();
            }
            final NpcHtmlMessage msg5 = new NpcHtmlMessage(player, this);
            msg5.setFile("residence2/clanhall/auction_bid_confirm.htm");
            msg5.replace("%id%", String.valueOf(id2));
            msg5.replace("%bid%", String.valueOf(bid));
            msg5.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            msg5.replace("%min_bid%", String.valueOf(minBid2));
            final Calendar c2 = auctionEvent3.getEndSiegeDate();
            msg5.replace("%date%", AuctioneerInstance.DATE_FORMAT.format(c2.getTimeInMillis()));
            msg5.replace("%hour%", String.valueOf(c2.get(Calendar.HOUR_OF_DAY)));
            player.sendPacket(msg5);
        } else if ("bid_confirm".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final int id2 = Integer.parseInt(tokenizer.nextToken());
            final long bid = Long.parseLong(tokenizer.nextToken());
            final ClanHall clanHall3 = ResidenceHolder.getInstance().getResidence(id2);
            final ClanHallAuctionEvent auctionEvent3 = clanHall3.getSiegeEvent();
            if (!auctionEvent3.isInProgress()) {
                return;
            }
            for (final ClanHall ch3 : ResidenceHolder.getInstance().getResidenceList(ClanHall.class)) {
                if (clanHall3 != ch3 && ch3.getSiegeEvent().getClass() == ClanHallAuctionEvent.class && ch3.getSiegeEvent().isInProgress() && ch3.getSiegeEvent().getSiegeClan("attackers", player.getClan()) != null) {
                    player.sendPacket(SystemMsg.SINCE_YOU_HAVE_ALREADY_SUBMITTED_A_BID_YOU_ARE_NOT_ALLOWED_TO_PARTICIPATE_IN_ANOTHER_AUCTION_AT_THIS_TIME);
                    onBypassFeedback(player, "bid_start " + id2);
                    return;
                }
            }
            if (!checkBid(player, auctionEvent3, bid)) {
                return;
            }
            long consumeBid = bid;
            AuctionSiegeClanObject siegeClan4 = auctionEvent3.getSiegeClan("attackers", player.getClan());
            if (siegeClan4 != null) {
                consumeBid -= siegeClan4.getParam();
                if (bid <= siegeClan4.getParam()) {
                    player.sendPacket(SystemMsg.THE_BID_AMOUNT_MUST_BE_HIGHER_THAN_THE_PREVIOUS_BID);
                    onBypassFeedback(player, "bid_start " + auctionEvent3.getId());
                    return;
                }
            }
            player.getClan().getWarehouse().destroyItemByItemId(Config.CH_BID_CURRENCY_ITEM_ID, consumeBid);
            if (siegeClan4 != null) {
                siegeClan4.setParam(bid);
                SiegeClanDAO.getInstance().update(clanHall3, siegeClan4);
            } else {
                siegeClan4 = new AuctionSiegeClanObject("attackers", player.getClan(), bid);
                auctionEvent3.addObject("attackers", siegeClan4);
                SiegeClanDAO.getInstance().insert(clanHall3, siegeClan4);
            }
            player.sendPacket(SystemMsg.YOUR_BID_HAS_BEEN_SUCCESSFULLY_PLACED);
            onBypassFeedback(player, "info");
        } else if ("cancel_bid".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final int id2 = Integer.parseInt(tokenizer.nextToken());
            final ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id2);
            final ClanHallAuctionEvent auctionEvent2 = clanHall.getSiegeEvent();
            if (!auctionEvent2.isInProgress()) {
                return;
            }
            final AuctionSiegeClanObject siegeClan5 = auctionEvent2.getSiegeClan("attackers", player.getClan());
            if (siegeClan5 == null) {
                return;
            }
            final long returnVal = siegeClan5.getParam() - (long) (siegeClan5.getParam() * 0.1);
            final NpcHtmlMessage msg4 = new NpcHtmlMessage(player, this);
            msg4.setFile("residence2/clanhall/auction_bid_cancel.htm");
            msg4.replace("%id%", String.valueOf(id2));
            msg4.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            msg4.replace("%bid%", String.valueOf(siegeClan5.getParam()));
            msg4.replace("%return%", String.valueOf(returnVal));
            player.sendPacket(msg4);
        } else if ("cancel_bid_confirm".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final int id2 = Integer.parseInt(tokenizer.nextToken());
            final ClanHall clanHall = ResidenceHolder.getInstance().getResidence(id2);
            final ClanHallAuctionEvent auctionEvent2 = clanHall.getSiegeEvent();
            if (!auctionEvent2.isInProgress()) {
                return;
            }
            final AuctionSiegeClanObject siegeClan5 = auctionEvent2.getSiegeClan("attackers", player.getClan());
            if (siegeClan5 == null) {
                return;
            }
            final long returnVal = siegeClan5.getParam() - (long) (siegeClan5.getParam() * 0.1);
            player.getClan().getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, returnVal);
            auctionEvent2.removeObject("attackers", siegeClan5);
            SiegeClanDAO.getInstance().delete(clanHall, siegeClan5);
            player.sendPacket(SystemMsg.YOU_HAVE_CANCELED_YOUR_BID);
            showChatWindow(player, 0);
        } else if ("register_start".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final ClanHall clanHall4 = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
            if (clanHall4.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall4.getSiegeEvent().isInProgress()) {
                return;
            }
            if (clanHall4.getLastSiegeDate().getTimeInMillis() + 604800000L > System.currentTimeMillis()) {
                player.sendPacket(SystemMsg.IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION);
                onBypassFeedback(player, "info");
                return;
            }
            final NpcHtmlMessage msg6 = new NpcHtmlMessage(player, this);
            msg6.setFile("residence2/clanhall/auction_clanhall_register_start.htm");
            msg6.replace("%id%", String.valueOf(player.getClan().getHasHideout()));
            msg6.replace("%currency_amount%", String.valueOf(player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID)));
            msg6.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            msg6.replace("%deposit%", String.valueOf(clanHall4.getDeposit()));
            player.sendPacket(msg6);
        } else if ("register_next".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final ClanHall clanHall4 = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
            if (clanHall4.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall4.getSiegeEvent().isInProgress()) {
                showChatWindow(player, 0);
                return;
            }
            if (player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID) < clanHall4.getDeposit()) {
                player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
                onBypassFeedback(player, "register_start");
                return;
            }
            final NpcHtmlMessage msg6 = new NpcHtmlMessage(player, this);
            msg6.setFile("residence2/clanhall/auction_clanhall_register_next.htm");
            msg6.replace("%min_bid%", String.valueOf(clanHall4.getBaseMinBid()));
            msg6.replace("%last_bid%", String.valueOf(clanHall4.getBaseMinBid()));
            msg6.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            player.sendPacket(msg6);
        } else if ("register_next2".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final ClanHall clanHall4 = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
            if (clanHall4.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall4.getSiegeEvent().isInProgress()) {
                showChatWindow(player, 0);
                return;
            }
            final int day = Integer.parseInt(tokenizer.nextToken());
            long bid2 = -1L;
            StringBuilder comment = new StringBuilder();
            if (tokenizer.hasMoreTokens()) {
                try {
                    bid2 = Long.parseLong(tokenizer.nextToken());
                } catch (Exception ignored) {
                }
            }
            if (tokenizer.hasMoreTokens()) {
                comment = new StringBuilder(tokenizer.nextToken());
                while (tokenizer.hasMoreTokens()) {
                    comment.append(" ").append(tokenizer.nextToken());
                }
            }
            comment = new StringBuilder(comment.substring(0, Math.min(comment.length(), 127)));
            if (bid2 <= -1L) {
                onBypassFeedback(player, "register_next");
                return;
            }
            final Calendar cal = Calendar.getInstance();
            cal.add(Calendar.HOUR_OF_DAY, day);
            final NpcHtmlMessage msg4 = new NpcHtmlMessage(player, this);
            msg4.setFile("residence2/clanhall/auction_clanhall_register_confirm.htm");
            msg4.replace("%description%", comment.toString());
            msg4.replace("%day%", String.valueOf(day));
            msg4.replace("%bid%", String.valueOf(bid2));
            msg4.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            msg4.replace("%base_bid%", String.valueOf(clanHall4.getBaseMinBid()));
            msg4.replace("%hour%", String.valueOf(cal.get(Calendar.HOUR_OF_DAY)));
            msg4.replace("%date%", AuctioneerInstance.DATE_FORMAT.format(cal.getTimeInMillis()));
            player.sendPacket(msg4);
        } else if ("register_confirm".equalsIgnoreCase(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final ClanHall clanHall4 = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
            if (clanHall4.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || clanHall4.getSiegeEvent().isInProgress()) {
                showChatWindow(player, 0);
                return;
            }
            if (clanHall4.getLastSiegeDate().getTimeInMillis() + 604800000L > System.currentTimeMillis()) {
                player.sendPacket(SystemMsg.IT_HAS_NOT_YET_BEEN_SEVEN_DAYS_SINCE_CANCELING_AN_AUCTION);
                onBypassFeedback(player, "info");
                return;
            }
            final int day = Integer.parseInt(tokenizer.nextToken());
            final long bid2 = Long.parseLong(tokenizer.nextToken());
            StringBuilder comment = new StringBuilder();
            if (tokenizer.hasMoreTokens()) {
                comment = new StringBuilder(tokenizer.nextToken());
                while (tokenizer.hasMoreTokens()) {
                    comment.append(" ").append(tokenizer.nextToken());
                }
            }
            if (bid2 <= -1L) {
                onBypassFeedback(player, "register_next");
                return;
            }
            clanHall4.setAuctionMinBid(bid2);
            clanHall4.setAuctionDescription(comment.toString());
            clanHall4.setAuctionLength(day);
            clanHall4.getSiegeDate().setTimeInMillis(System.currentTimeMillis());
            clanHall4.setJdbcState(JdbcEntityState.UPDATED);
            clanHall4.update();
            clanHall4.getSiegeEvent().reCalcNextTime(false);
            onBypassFeedback(player, "info");
            player.sendPacket(SystemMsg.YOU_HAVE_REGISTERED_FOR_A_CLAN_HALL_AUCTION);
        } else if ("cancel_start".equals(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final ClanHall clanHall4 = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
            if (clanHall4.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || !clanHall4.getSiegeEvent().isInProgress()) {
                showChatWindow(player, 0);
                return;
            }
            final NpcHtmlMessage msg6 = new NpcHtmlMessage(player, this);
            msg6.setFile("residence2/clanhall/auction_clanhall_cancel_confirm.htm");
            msg6.replace("%deposit%", String.valueOf(clanHall4.getDeposit()));
            msg6.replace("%cha_currency%", HtmlUtils.htmlItemName(Config.CH_BID_CURRENCY_ITEM_ID));
            player.sendPacket(msg6);
        } else if ("cancel_confirm".equals(actualCommand)) {
            if (!firstChecks(player)) {
                showChatWindow(player, 0);
                return;
            }
            final ClanHall clanHall4 = ResidenceHolder.getInstance().getResidence(player.getClan().getHasHideout());
            if (clanHall4.getSiegeEvent().getClass() != ClanHallAuctionEvent.class || !clanHall4.getSiegeEvent().isInProgress()) {
                showChatWindow(player, 0);
                return;
            }
            clanHall4.getSiegeEvent().setInProgress(false);
            clanHall4.getSiegeDate().setTimeInMillis(0L);
            clanHall4.getLastSiegeDate().setTimeInMillis(System.currentTimeMillis());
            clanHall4.setAuctionDescription("");
            clanHall4.setAuctionLength(0);
            clanHall4.setAuctionMinBid(0L);
            clanHall4.setJdbcState(JdbcEntityState.UPDATED);
            clanHall4.update();
            final ClanHallAuctionEvent auctionEvent4 = clanHall4.getSiegeEvent();
            final List<AuctionSiegeClanObject> siegeClans = auctionEvent4.removeObjects("attackers");
            SiegeClanDAO.getInstance().delete(clanHall4);
            for (final AuctionSiegeClanObject $siegeClan : siegeClans) {
                final long returnBid = $siegeClan.getParam() - (long) ($siegeClan.getParam() * 0.1);
                $siegeClan.getClan().getWarehouse().addItem(Config.CH_BID_CURRENCY_ITEM_ID, returnBid);
            }
            clanHall4.getSiegeEvent().reCalcNextTime(false);
            onBypassFeedback(player, "info");
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        showChatWindow(player, "residence2/clanhall/auction_dealer001.htm");
    }

    private boolean firstChecks(final Player player) {
        if (player.getClan() == null || player.getClan().getLevel() < Config.CLAN_MIN_LEVEL_FOR_BID) {
            player.sendPacket(SystemMsg.ONLY_A_CLAN_LEADER_WHOSE_CLAN_IS_OF_LEVEL_2_OR_HIGHER_IS_ALLOWED_TO_PARTICIPATE_IN_A_CLAN_HALL_AUCTION);
            return false;
        }
        if (player.getClan().isPlacedForDisband()) {
            player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
            return false;
        }
        if (!player.hasPrivilege(Privilege.CH_AUCTION)) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return false;
        }
        return true;
    }

    private boolean checkBid(final Player player, final ClanHallAuctionEvent auctionEvent, final long bid) {
        long consumeBid = bid;
        final AuctionSiegeClanObject siegeClan = auctionEvent.getSiegeClan("attackers", player.getClan());
        if (siegeClan != null) {
            consumeBid -= siegeClan.getParam();
        }
        if (consumeBid > player.getClan().getWarehouse().getCountOf(Config.CH_BID_CURRENCY_ITEM_ID)) {
            player.sendPacket(SystemMsg.THERE_IS_NOT_ENOUGH_ADENA_IN_THE_CLAN_HALL_WAREHOUSE);
            onBypassFeedback(player, "bid_start " + auctionEvent.getId());
            return false;
        }
        final long minBid = (siegeClan == null) ? auctionEvent.getResidence().getAuctionMinBid() : siegeClan.getParam();
        if (bid < minBid) {
            player.sendPacket(SystemMsg.YOUR_BID_PRICE_MUST_BE_HIGHER_THAN_THE_MINIMUM_PRICE_CURRENTLY_BEING_BID);
            onBypassFeedback(player, "bid_start " + auctionEvent.getId());
            return false;
        }
        return true;
    }

    private String getMapDialog() {
        final int rx = MapUtils.regionX(this);
        final int ry = MapUtils.regionY(this);
        return String.format("residence2/clanhall/map_agit_%d_%d.htm", rx, ry);
    }
}
