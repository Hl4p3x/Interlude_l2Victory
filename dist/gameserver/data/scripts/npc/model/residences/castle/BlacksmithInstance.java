package npc.model.residences.castle;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.manager.CastleManorManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MyTargetSelected;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ValidateLocation;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class BlacksmithInstance extends NpcInstance {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    protected static final int COND_OWNER = 2;

    public BlacksmithInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (this != player.getTarget()) {
            player.setTarget(this);
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            player.sendPacket(new ValidateLocation(this));
        } else {
            player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel() - getLevel()));
            if (!isInActingRange(player)) {
                player.getAI().setIntention(CtrlIntention.AI_INTENTION_INTERACT, this);
                player.sendActionFailed();
            } else {
                if (CastleManorManager.getInstance().isDisabled()) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setFile("npcdefault.htm");
                    player.sendPacket(html);
                } else {
                    showMessageWindow(player, 0);
                }
                player.sendActionFailed();
            }
        }
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if (CastleManorManager.getInstance().isDisabled()) {
            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
            html.setFile("npcdefault.htm");
            player.sendPacket(html);
            return;
        }
        final int condition = validateCondition(player);
        if (condition <= 0) {
            return;
        }
        if (condition == 1) {
            return;
        }
        if (condition == 2) {
            if (command.startsWith("Chat")) {
                int val = 0;
                try {
                    val = Integer.parseInt(command.substring(5));
                } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
                }
                showMessageWindow(player, val);
            } else {
                super.onBypassFeedback(player, command);
            }
        }
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    private void showMessageWindow(final Player player, final int val) {
        player.sendActionFailed();
        String filename = "castle/blacksmith/castleblacksmith-no.htm";
        final int condition = validateCondition(player);
        if (condition > 0) {
            if (condition == 1) {
                filename = "castle/blacksmith/castleblacksmith-busy.htm";
            } else if (condition == 2) {
                if (val == 0) {
                    filename = "castle/blacksmith/castleblacksmith.htm";
                } else {
                    filename = "castle/blacksmith/castleblacksmith-" + val + ".htm";
                }
            }
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(filename);
        html.replace("%castleid%", Integer.toString(getCastle().getId()));
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
            if (getCastle().getOwnerId() == player.getClanId() && (player.getClanPrivileges() & 0x10000) == 0x10000) {
                return 2;
            }
        }
        return 0;
    }
}
