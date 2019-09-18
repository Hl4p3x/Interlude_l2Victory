package npc.model.residences.clanhall;

import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallMiniGameEvent;
import ru.j2dev.gameserver.model.entity.events.objects.CMGSiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.SpawnExObject;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class RainbowCoordinatorInstance extends NpcInstance {
    public RainbowCoordinatorInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final ClanHall clanHall = getClanHall();
        final ClanHallMiniGameEvent miniGameEvent = clanHall.getSiegeEvent();
        if (miniGameEvent == null) {
            return;
        }
        if (miniGameEvent.isArenaClosed()) {
            showChatWindow(player, "residence2/clanhall/game_manager003.htm");
            return;
        }
        final List<CMGSiegeClanObject> siegeClans = miniGameEvent.getObjects("attackers");
        final CMGSiegeClanObject siegeClan = miniGameEvent.getSiegeClan("attackers", player.getClan());
        if (siegeClan == null) {
            showChatWindow(player, "residence2/clanhall/game_manager014.htm");
            return;
        }
        if (siegeClan.getPlayers().isEmpty()) {
            final Party party = player.getParty();
            if (party == null) {
                showChatWindow(player, player.isClanLeader() ? "residence2/clanhall/game_manager005.htm" : "residence2/clanhall/game_manager002.htm");
                return;
            }
            if (!player.isClanLeader()) {
                showChatWindow(player, "residence2/clanhall/game_manager004.htm");
                return;
            }
            if (party.getMemberCount() < 5) {
                showChatWindow(player, "residence2/clanhall/game_manager003.htm");
                return;
            }
            if (party.getPartyLeader() != player) {
                showChatWindow(player, "residence2/clanhall/game_manager006.htm");
                return;
            }
            for (final Player member : party.getPartyMembers()) {
                if (member.getClan() != player.getClan()) {
                    showChatWindow(player, "residence2/clanhall/game_manager007.htm");
                    return;
                }
            }
            final int index = siegeClans.indexOf(siegeClan);
            final SpawnExObject spawnEx = miniGameEvent.getFirstObject("arena_" + index);
            final Location loc = (Location) spawnEx.getSpawns().get(0).getCurrentSpawnRange();
            for (final Player member2 : party.getPartyMembers()) {
                siegeClan.addPlayer(member2.getObjectId());
                member2.teleToLocation(Location.coordsRandomize(loc, 100, 200));
            }
        } else {
            showChatWindow(player, "residence2/clanhall/game_manager013.htm");
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        showChatWindow(player, "residence2/clanhall/game_manager001.htm");
    }
}
