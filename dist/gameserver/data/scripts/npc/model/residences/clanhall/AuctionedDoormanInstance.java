package npc.model.residences.clanhall;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.Privilege;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class AuctionedDoormanInstance extends NpcInstance {
    private final int[] _doors;
    private final boolean _elite;

    public AuctionedDoormanInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _doors = template.getAIParams().getIntegerArray("doors", ArrayUtils.EMPTY_INT_ARRAY);
        _elite = template.getAIParams().getBool("elite", false);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final ClanHall clanHall = getClanHall();
        if (command.equalsIgnoreCase("openDoors")) {
            if (player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId()) {
                for (final int d : _doors) {
                    ReflectionUtils.getDoor(d).openMe();
                }
                showChatWindow(player, "residence2/clanhall/agitafterdooropen.htm");
            } else {
                showChatWindow(player, "residence2/clanhall/noAuthority.htm");
            }
        } else if (command.equalsIgnoreCase("closeDoors")) {
            if (player.hasPrivilege(Privilege.CH_ENTER_EXIT) && player.getClan().getHasHideout() == clanHall.getId()) {
                for (final int d : _doors) {
                    ReflectionUtils.getDoor(d).closeMe(player, true);
                }
                showChatWindow(player, "residence2/clanhall/agitafterdoorclose.htm");
            } else {
                showChatWindow(player, "residence2/clanhall/noAuthority.htm");
            }
        } else if (command.equalsIgnoreCase("banish")) {
            if (player.hasPrivilege(Privilege.CH_DISMISS)) {
                clanHall.banishForeigner();
                showChatWindow(player, "residence2/clanhall/agitafterbanish.htm");
            } else {
                showChatWindow(player, "residence2/clanhall/noAuthority.htm");
            }
        } else if (command.equalsIgnoreCase("Clan_Hall_RideWyvern") && player.isClanLeader() && _elite) {
            if (!player.isRiding() || !PetDataTable.isStrider(player.getMountNpcId())) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("residence2/clanhall/WyvernAgit_not_ready.htm");
                player.sendPacket(html);
            } else if (player.getInventory().getItemByItemId(1460) == null || player.getInventory().getItemByItemId(1460).getCount() < 10L) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.replace("%npcname%", getName());
                player.sendPacket(html);
            } else if (SevenSigns.getInstance().getCurrentPeriod() == 3 && SevenSigns.getInstance().getCabalHighestScore() == 3) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("residence2/clanhall/WyvernAgit_no_ride_dusk.htm");
                player.sendPacket(html);
            } else if (player.getInventory().destroyItemByItemId(1460, 10L)) {
                player.setMount(12621, player.getMountObjId(), player.getMountLevel());
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("residence2/clanhall/WyvernAgit_after_ride.htm");
                player.sendPacket(html);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final ClanHall clanHall = getClanHall();
        if (clanHall != null) {
            final Clan ownerClan = clanHall.getOwner();
            if (ownerClan != null) {
                final Clan playerClan = player.getClan();
                if (playerClan != null && playerClan == ownerClan) {
                    showChatWindow(player, _elite ? "residence2/clanhall/WyvernAgitJanitorHi.htm" : "residence2/clanhall/AgitJanitorHi.htm", "%owner%", playerClan.getName());
                } else {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("residence2/clanhall/defaultAgitInfo.htm");
                    html.replace("<?my_owner_name?>", ownerClan.getLeaderName());
                    html.replace("<?my_pledge_name?>", ownerClan.getName());
                    player.sendPacket(html);
                }
            } else {
                showChatWindow(player, "residence2/clanhall/noAgitInfo.htm");
            }
        }
    }

    @Override
    protected boolean canInteractWithKarmaPlayer() {
        return true;
    }
}
