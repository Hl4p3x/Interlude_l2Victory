package npc.model.residences.clanhall;

import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import ru.j2dev.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.TimeUtils;

public class RainbowMessengerInstance extends NpcInstance {
    public static final int ITEM_ID = 8034;

    public RainbowMessengerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final ClanHall clanHall = getClanHall();
        final ClanHallMiniGameEvent miniGameEvent = clanHall.getSiegeEvent();
        if ("register".equalsIgnoreCase(command)) {
            if (miniGameEvent.isRegistrationOver()) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti014.htm");
                return;
            }
            if (player.getClan().isPlacedForDisband()) {
                player.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
                return;
            }
            final Clan clan = player.getClan();
            if (clan == null || clan.getLevel() < 3 || clan.getAllSize() <= 5) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm");
                return;
            }
            if (clan.getLeaderId() != player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
                return;
            }
            if (clan.getHasHideout() > 0) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti012.htm");
                return;
            }
            if (miniGameEvent.getSiegeClan("attackers", clan) != null) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti013.htm");
                return;
            }
            final long count = player.getInventory().getCountOf(8034);
            if (count == 0L) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti008.htm");
            } else {
                if (!player.consumeItem(8034, count)) {
                    return;
                }
                final CMGSiegeClanObject siegeClanObject = new CMGSiegeClanObject("attackers", clan, count);
                miniGameEvent.addObject("attackers", siegeClanObject);
                SiegeClanDAO.getInstance().insert(clanHall, siegeClanObject);
                showChatWindow(player, "residence2/clanhall/messenger_yetti009.htm");
            }
        } else if ("cancel".equalsIgnoreCase(command)) {
            if (miniGameEvent.isRegistrationOver()) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti017.htm");
                return;
            }
            final Clan clan = player.getClan();
            if (clan == null || clan.getLevel() < 3) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm");
                return;
            }
            if (clan.getLeaderId() != player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
                return;
            }
            final SiegeClanObject siegeClanObject2 = miniGameEvent.getSiegeClan("attackers", clan);
            if (siegeClanObject2 == null) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti016.htm");
            } else {
                miniGameEvent.removeObject("attackers", siegeClanObject2);
                SiegeClanDAO.getInstance().delete(clanHall, siegeClanObject2);
                ItemFunctions.addItem(player, 8034, siegeClanObject2.getParam() / 2L, true);
                showChatWindow(player, "residence2/clanhall/messenger_yetti005.htm");
            }
        } else if ("refund".equalsIgnoreCase(command)) {
            if (miniGameEvent.isRegistrationOver()) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
                return;
            }
            final Clan clan = player.getClan();
            if (clan == null || clan.getLevel() < 3) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti011.htm");
                return;
            }
            if (clan.getLeaderId() != player.getObjectId()) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti010.htm");
                return;
            }
            final SiegeClanObject siegeClanObject2 = miniGameEvent.getSiegeClan("refund", clan);
            if (siegeClanObject2 == null) {
                showChatWindow(player, "residence2/clanhall/messenger_yetti020.htm");
            } else {
                miniGameEvent.removeObject("refund", siegeClanObject2);
                SiegeClanDAO.getInstance().delete(clanHall, siegeClanObject2);
                ItemFunctions.addItem(player, 8034, siegeClanObject2.getParam(), true);
                showChatWindow(player, "residence2/clanhall/messenger_yetti019.htm");
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final ClanHall clanHall = getClanHall();
        final Clan clan = clanHall.getOwner();
        final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
        if (clan != null) {
            msg.setFile("residence2/clanhall/messenger_yetti001.htm");
            msg.replace("%owner_name%", clan.getName());
        } else {
            msg.setFile("residence2/clanhall/messenger_yetti001a.htm");
        }
        msg.replace("%siege_date%", TimeUtils.toSimpleFormat(clanHall.getSiegeDate()));
        player.sendPacket(msg);
    }
}
