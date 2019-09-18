package npc.model.residences.castle;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.skills.skillclasses.Call;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class CourtInstance extends NpcInstance {
    protected static final int COND_ALL_FALSE = 0;
    protected static final int COND_BUSY_BECAUSE_OF_SIEGE = 1;
    protected static final int COND_OWNER = 2;

    public CourtInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final int condition = validateCondition(player);
        if (condition <= 0) {
            return;
        }
        if (condition == 1) {
            return;
        }
        if ((player.getClanPrivileges() & 0x40000) != 0x40000) {
            player.sendPacket(SystemMsg.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
            return;
        }
        if (condition == 2) {
            if (command.startsWith("Chat")) {
                int val = 0;
                try {
                    val = Integer.parseInt(command.substring(5));
                } catch (IndexOutOfBoundsException | NumberFormatException ignored) {
                }
                showChatWindow(player, val);
                return;
            }
            if (command.startsWith("gotoleader")) {
                if (player.getClan() != null) {
                    final Player clanLeader = player.getClan().getLeader().getPlayer();
                    if (clanLeader == null) {
                        return;
                    }
                    if (clanLeader.getEffectList().getEffectsBySkillId(3632) != null) {
                        if (Call.canSummonHere(clanLeader) != null) {
                            return;
                        }
                        if (Call.canBeSummoned(player) == null) {
                            player.teleToLocation(Location.findAroundPosition(clanLeader, 100));
                        }
                    } else {
                        showChatWindow(player, "castle/CourtMagician/CourtMagician-nogate.htm");
                    }
                }
            } else {
                super.onBypassFeedback(player, command);
            }
        }
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        player.sendActionFailed();
        String filename = "castle/CourtMagician/CourtMagician-no.htm";
        final int condition = validateCondition(player);
        if (condition > 0) {
            if (condition == 1) {
                filename = "castle/CourtMagician/CourtMagician-busy.htm";
            } else if (condition == 2) {
                if (val == 0) {
                    filename = "castle/CourtMagician/CourtMagician.htm";
                } else {
                    filename = "castle/CourtMagician/CourtMagician-" + val + ".htm";
                }
            }
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(filename);
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%npcname%", getName());
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
