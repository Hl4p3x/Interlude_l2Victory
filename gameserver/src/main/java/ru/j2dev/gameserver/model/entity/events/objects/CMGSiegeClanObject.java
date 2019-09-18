package ru.j2dev.gameserver.model.entity.events.objects;

import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.pledge.Clan;

public class CMGSiegeClanObject extends SiegeClanObject {
    private final TIntSet _players;
    private long _param;

    public CMGSiegeClanObject(final String type, final Clan clan, final long param, final long date) {
        super(type, clan, param, date);
        _players = new TIntHashSet();
        _param = param;
    }

    public CMGSiegeClanObject(final String type, final Clan clan, final long param) {
        super(type, clan, param);
        _players = new TIntHashSet();
        _param = param;
    }

    public void addPlayer(final int objectId) {
        _players.add(objectId);
    }

    @Override
    public long getParam() {
        return _param;
    }

    public void setParam(final long param) {
        _param = param;
    }

    @Override
    public boolean isParticle(final Player player) {
        return _players.contains(player.getObjectId());
    }

    @Override
    public void setEvent(final boolean start, final SiegeEvent event) {
        for (final int i : _players.toArray()) {
            final Player player = GameObjectsStorage.getPlayer(i);
            if (player != null) {
                if (start) {
                    player.addEvent(event);
                } else {
                    player.removeEvent(event);
                }
                player.broadcastCharInfo();
            }
        }
    }

    public TIntSet getPlayers() {
        return _players;
    }
}
