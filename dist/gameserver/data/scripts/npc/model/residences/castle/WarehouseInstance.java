package npc.model.residences.castle;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.WarehouseFunctions;

public class WarehouseInstance extends NpcInstance {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    protected static final int COND_OWNER = 2;

    public WarehouseInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ((player.getClanPrivileges() & 0x40000) != 0x40000) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (player.getEnchantScroll() != null) {
            Log.add("Player " + player.getName() + " trying to use enchant exploit[CastleWarehouse], ban this player!", "illegal-actions");
            player.kick();
            return;
        }
        if (command.startsWith("WithdrawP")) {
            final int val = Integer.parseInt(command.substring(10));
            if (val == 99) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("warehouse/personal.htm");
                player.sendPacket(html);
            } else {
                WarehouseFunctions.showRetrieveWindow(player, val);
            }
        } else if ("DepositP".equals(command)) {
            WarehouseFunctions.showDepositWindow(player);
        } else if (command.startsWith("WithdrawC")) {
            final int val = Integer.parseInt(command.substring(10));
            if (val == 99) {
                final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                html.setFile("warehouse/clan.htm");
                player.sendPacket(html);
            } else {
                WarehouseFunctions.showWithdrawWindowClan(player, val);
            }
        } else if ("DepositC".equals(command)) {
            WarehouseFunctions.showDepositWindowClan(player);
        } else if (command.startsWith("Chat")) {
            int val = 0;
            try {
                val = Integer.parseInt(command.substring(5));
            } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
            }
            showChatWindow(player, val);
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        player.sendActionFailed();
        String filename = "castle/warehouse/castlewarehouse-no.htm";
        final int condition = validateCondition(player);
        if (condition > 0) {
            if (condition == 1) {
                filename = "castle/warehouse/castlewarehouse-busy.htm";
            } else if (condition == 2) {
                if (val == 0) {
                    filename = "castle/warehouse/castlewarehouse.htm";
                } else {
                    filename = "castle/warehouse/castlewarehouse-" + val + ".htm";
                }
            }
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(filename);
        player.sendPacket(html);
    }

    protected int validateCondition(final Player player) {
        if (player.isGM()) {
            return 2;
        }
        if (getCastle() != null && getCastle().getId() > 0 && player.getClan() != null) {
            if (getCastle().getSiegeEvent().isInProgress()) {
                return 1;
            }
            if (getCastle().getOwnerId() == player.getClanId()) {
                return 2;
            }
        }
        return 0;
    }
}
