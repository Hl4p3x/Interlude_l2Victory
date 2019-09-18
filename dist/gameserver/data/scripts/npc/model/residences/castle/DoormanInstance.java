package npc.model.residences.castle;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class DoormanInstance extends npc.model.residences.DoormanInstance {
    private final Location[] _locs;

    public DoormanInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _locs = new Location[2];
        for (int i = 0; i < _locs.length; ++i) {
            final String loc = template.getAIParams().getString("tele_loc" + i, null);
            if (loc != null) {
                _locs[i] = Location.parseLoc(loc);
            }
        }
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final int cond = getCond(player);
        switch (cond) {
            case 0: {
                if ("openDoors".equalsIgnoreCase(command)) {
                    for (final int i : _doors) {
                        ReflectionUtils.getDoor(i).openMe(player, true);
                    }
                    break;
                }
                if ("closeDoors".equalsIgnoreCase(command)) {
                    for (final int i : _doors) {
                        ReflectionUtils.getDoor(i).closeMe(player, true);
                    }
                    break;
                }
                if (command.startsWith("tele")) {
                    final int id = Integer.parseInt(command.substring(4, 5));
                    final Location loc = _locs[id];
                    if (loc != null) {
                        player.teleToLocation(loc);
                    }
                    break;
                }
                break;
            }
            case 1: {
                if (command.startsWith("tele")) {
                    final int id = Integer.parseInt(command.substring(4, 5));
                    final Location loc = _locs[id];
                    if (loc != null) {
                        player.teleToLocation(loc);
                    }
                    break;
                }
                player.sendPacket(new NpcHtmlMessage(player, this, _siegeDialog, 0));
                break;
            }
            case 2: {
                player.sendPacket(new NpcHtmlMessage(player, this, _failDialog, 0));
                break;
            }
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        String filename = null;
        final int cond = getCond(player);
        switch (cond) {
            case 0:
            case 1: {
                filename = _mainDialog;
                break;
            }
            case 2: {
                filename = _failDialog;
                break;
            }
        }
        player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
    }

    @Override
    protected int getCond(final Player player) {
        final Castle residence = getCastle();
        final Clan residenceOwner = residence.getOwner();
        if (residenceOwner == null || player.getClan() != residenceOwner || (player.getClanPrivileges() & getOpenPriv()) != getOpenPriv()) {
            return 2;
        }
        if (residence.getSiegeEvent().isInProgress()) {
            return 1;
        }
        return 0;
    }

    @Override
    public int getOpenPriv() {
        return 32768;
    }

    @Override
    public Residence getResidence() {
        return getCastle();
    }
}
