package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.RestartType;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.pledge.Clan;

import java.util.HashMap;
import java.util.Map;

public class Die extends L2GameServerPacket {
    private final int _objectId;
    private final boolean _fake;
    private final Map<RestartType, Boolean> _types;
    private boolean _sweepable;

    public Die(final Creature cha) {
        _types = new HashMap<>(RestartType.VALUES.length);
        _objectId = cha.getObjectId();
        _fake = !cha.isDead();
        if (cha.isMonster()) {
            _sweepable = ((MonsterInstance) cha).isSweepActive();
        } else if (cha.isPlayer()) {
            final Player player = (Player) cha;
            if (!player.isOlyCompetitionStarted() && !player.isResurectProhibited()) {
                put(RestartType.FIXED, player.getPlayerAccess().ResurectFixed || (player.getInventory().getCountOf(9218) > 0L && !player.isOnSiegeField()));
                put(RestartType.TO_VILLAGE, true);
                Clan clan = null;
                if (get(RestartType.TO_VILLAGE)) {
                    clan = player.getClan();
                }
                if (clan != null) {
                    put(RestartType.TO_CLANHALL, clan.getHasHideout() > 0);
                    put(RestartType.TO_CASTLE, clan.getCastle() > 0);
                }
                for (final GlobalEvent e : cha.getEvents()) {
                    e.checkRestartLocs(player, _types);
                }
            }
        }
    }

    @Override
    protected final void writeImpl() {
        if (_fake) {
            return;
        }
        writeC(0x6);
        writeD(_objectId);
        writeD(get(RestartType.TO_VILLAGE));
        writeD(get(RestartType.TO_CLANHALL));
        writeD(get(RestartType.TO_CASTLE));
        writeD(get(RestartType.TO_FLAG));
        writeD(_sweepable ? 1 : 0);
        writeD(get(RestartType.FIXED));
    }

    private void put(final RestartType t, final boolean b) {
        _types.put(t, b);
    }

    private boolean get(final RestartType t) {
        final Boolean b = _types.get(t);
        return b != null && b;
    }
}
