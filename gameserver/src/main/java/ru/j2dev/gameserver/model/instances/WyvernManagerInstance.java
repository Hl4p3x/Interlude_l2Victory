package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.tables.PetDataTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public final class WyvernManagerInstance extends NpcInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(WyvernManagerInstance.class);

    public WyvernManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        final StringTokenizer st = new StringTokenizer(command, " ");
        final String actualCommand = st.nextToken();
        final boolean condition = validateCondition(player);
        if ("RideHelp".equalsIgnoreCase(actualCommand)) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("wyvern/help_ride.htm");
            html.replace("%npcname%", "Wyvern Manager " + getName());
            player.sendPacket(html);
            player.sendActionFailed();
        }
        if (condition) {
            if ("RideWyvern".equalsIgnoreCase(actualCommand) && player.isClanLeader()) {
                if (!player.isRiding() || !PetDataTable.isStrider(player.getMountNpcId())) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("wyvern/not_ready.htm");
                    html.replace("%npcname%", "Wyvern Manager " + getName());
                    player.sendPacket(html);
                } else if (player.getInventory().getItemByItemId(1460) == null || player.getInventory().getItemByItemId(1460).getCount() < 10L) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("wyvern/havenot_cry.htm");
                    html.replace("%npcname%", "Wyvern Manager " + getName());
                    player.sendPacket(html);
                } else if (SevenSigns.getInstance().getCurrentPeriod() == 3 && SevenSigns.getInstance().getCabalHighestScore() == 3) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("wyvern/no_ride_dusk.htm");
                    html.replace("%npcname%", "Wyvern Manager " + getName());
                    player.sendPacket(html);
                } else if (player.getInventory().destroyItemByItemId(1460, 10L)) {
                    player.setMount(12621, player.getMountObjId(), player.getMountLevel());
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("wyvern/after_ride.htm");
                    html.replace("%npcname%", "Wyvern Manager " + getName());
                    player.sendPacket(html);
                }
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        if (!validateCondition(player)) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("wyvern/lord_only.htm");
            html.replace("%npcname%", "Wyvern Manager " + getName());
            player.sendPacket(html);
            player.sendActionFailed();
            return;
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile("wyvern/lord_here.htm");
        html.replace("%Char_name%", String.valueOf(player.getName()));
        html.replace("%npcname%", "Wyvern Manager " + getName());
        player.sendPacket(html);
        player.sendActionFailed();
    }

    private boolean validateCondition(final Player player) {
        Residence residence = getCastle();
        if (residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader()) {
            return true;
        }
        residence = getClanHall();
        return residence != null && residence.getId() > 0 && player.getClan() != null && residence.getOwnerId() == player.getClanId() && player.isClanLeader();
    }
}
