package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.StringTokenizer;

public final class MercManagerInstance extends MerchantInstance {
    private static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    private static final int COND_OWNER = 2;
    private static int COND_ALL_FALSE;

    public MercManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        final int condition = validateCondition(player);
        if (condition <= COND_ALL_FALSE || condition == COND_BUSY_BECAUSE_OF_SIEGE) {
            return;
        }
        if (condition == COND_OWNER) {
            final StringTokenizer st = new StringTokenizer(command, " ");
            final String actualCommand = st.nextToken();
            String val = "";
            if (st.countTokens() >= 1) {
                val = st.nextToken();
            }
            if ("hire".equalsIgnoreCase(actualCommand)) {
                if ("".equals(val)) {
                    return;
                }
                showShopWindow(player, Integer.parseInt(val), false);
            } else {
                super.onBypassFeedback(player, command);
            }
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        String filename = "castle/mercmanager/mercmanager-no.htm";
        final int condition = validateCondition(player);
        if (condition == COND_BUSY_BECAUSE_OF_SIEGE) {
            filename = "castle/mercmanager/mercmanager-busy.htm";
        } else if (condition == COND_OWNER) {
            if (SevenSigns.getInstance().getCurrentPeriod() == 3) {
                switch (SevenSigns.getInstance().getSealOwner(3)) {
                    case 2:
                        filename = "castle/mercmanager/mercmanager_dawn.htm";
                        break;
                    case 1:
                        filename = "castle/mercmanager/mercmanager_dusk.htm";
                        break;
                    default:
                        filename = "castle/mercmanager/mercmanager.htm";
                        break;
                }
            } else {
                filename = "castle/mercmanager/mercmanager_nohire.htm";
            }
        }
        player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
    }

    private int validateCondition(final Player player) {
        if (player.isGM()) {
            return COND_OWNER;
        }
        if (getCastle() != null && getCastle().getId() > 0 && player.getClan() != null) {
            if (getCastle().getSiegeEvent().isInProgress()) {
                return COND_BUSY_BECAUSE_OF_SIEGE;
            }
            if (getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x200000) == 0x200000) {
                return COND_OWNER;
            }
        }
        return COND_ALL_FALSE;
    }
}
