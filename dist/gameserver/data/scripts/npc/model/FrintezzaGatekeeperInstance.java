package npc.model;

import bosses.FrintezzaManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.CommandChannel;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.List;

public final class FrintezzaGatekeeperInstance extends NpcInstance {
    public FrintezzaGatekeeperInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        if ("request_frintezza".equalsIgnoreCase(command)) {
            try {
                if (!FrintezzaManager.getInstance().isInUse()) {
                    if (FrintezzaManager.getInstance().canEnter()) {
                        if (player.getParty() == null || player.getParty().getCommandChannel() == null) {
                            player.sendMessage("Only a party room leader can request to enter.");
                            return;
                        }
                        final CommandChannel cc = player.getParty().getCommandChannel();
                        final List<Party> partyList = cc.getParties();
                        if (partyList.size() < 4 || partyList.size() > 5) {
                            player.sendMessage("Party room is too small or too big to enter.");
                            return;
                        }
                        if (cc.getChannelLeader() != player) {
                            player.sendMessage("Only a party room leader can request to enter.");
                            return;
                        }
                        for (final Party party : cc.getParties()) {
                            for (final Player member : party.getPartyMembers()) {
                                if (member.getLevel() < 74) {
                                    player.sendMessage("Level of " + member.getName() + " to low to enter.");
                                    return;
                                }
                                if (getDistance(member) > 300.0) {
                                    player.sendMessage(member.getName() + " to far.");
                                    return;
                                }
                            }
                        }
                        if (ItemFunctions.removeItem(player, 8073, 1L, true) < 1L) {
                            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                            return;
                        }
                        FrintezzaManager.getInstance().tryEnter(partyList);
                    } else {
                        player.sendMessage("Frintezza is still reborning. You cannot enter now.");
                    }
                } else {
                    player.sendMessage("Frintezza is under attack. You cannot enter now.");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
