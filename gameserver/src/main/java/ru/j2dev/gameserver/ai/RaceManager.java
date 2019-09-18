package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.RaceManagerInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MonRaceInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class RaceManager extends DefaultAI {
    private boolean thinking;
    private List<Player> _knownPlayers;

    public RaceManager(final NpcInstance actor) {
        super(actor);
        thinking = false;
        _knownPlayers = new ArrayList<>();
        AI_TASK_ATTACK_DELAY = 5000L;
    }

    @Override
    public void runImpl() {
        onEvtThink();
    }

    @Override
    protected void onEvtThink() {
        final RaceManagerInstance actor = getActor();
        if (actor == null) {
            return;
        }
        final MonRaceInfo packet = actor.getPacket();
        if (packet == null) {
            return;
        }
        synchronized (this) {
            if (thinking) {
                return;
            }
            thinking = true;
        }
        try {
            final List<Player> newPlayers = new ArrayList<>();
            World.getAroundPlayers(actor, 1200, 200).stream().filter(Objects::nonNull).forEach(player -> {
                newPlayers.add(player);
                if (!_knownPlayers.contains(player)) {
                    player.sendPacket(packet);
                }
                _knownPlayers.remove(player);
            });
            _knownPlayers.forEach(actor::removeKnownPlayer);
            _knownPlayers = newPlayers;
        } finally {
            thinking = false;
        }
    }

    @Override
    public RaceManagerInstance getActor() {
        return (RaceManagerInstance) super.getActor();
    }
}
