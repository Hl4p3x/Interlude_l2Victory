package services;

import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.MapUtils;

import java.util.ArrayList;
import java.util.List;

public class Misc extends Functions {
    private static final Location SUMMON_LOCATION = new Location(11416, -23432, -3640);

    public void doSummon() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null || !NpcInstance.canBypassCheck(player, player.getLastNpc())) {
            return;
        }
        final Party party = player.getParty();
        final List<Player> membersToRecall = new ArrayList<>();
        if (party != null) {
            for (final Player member : party.getPartyMembers()) {
                if (member.isDead() && MapUtils.regionX(member) == 20 && MapUtils.regionY(member) == 17) {
                    membersToRecall.add(member);
                }
            }
        }
        if (membersToRecall.isEmpty() || !player.reduceAdena(200000L, true)) {
            show("default/32104-fail.htm", player);
            return;
        }
        for (final Player member : membersToRecall) {
            member.teleToLocation(Misc.SUMMON_LOCATION);
        }
        show("default/32104-success.htm", player);
    }
}
