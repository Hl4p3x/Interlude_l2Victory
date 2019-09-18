package ru.j2dev.gameserver.model.entity.events.objects;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;

import java.io.Serializable;
import java.util.Comparator;

public class SiegeClanObject implements Serializable {
    private final long _date;
    private final Clan _clan;
    private String _type;
    private NpcInstance _flag;

    public SiegeClanObject(final String type, final Clan clan, final long param) {
        this(type, clan, 0L, System.currentTimeMillis());
    }

    public SiegeClanObject(final String type, final Clan clan, final long param, final long date) {
        _type = type;
        _clan = clan;
        _date = date;
    }

    public int getObjectId() {
        return _clan.getClanId();
    }

    public Clan getClan() {
        return _clan;
    }

    public NpcInstance getFlag() {
        return _flag;
    }

    public void setFlag(final NpcInstance npc) {
        _flag = npc;
    }

    public void deleteFlag() {
        if (_flag != null) {
            _flag.deleteMe();
            _flag = null;
        }
    }

    public String getType() {
        return _type;
    }

    public void setType(final String type) {
        _type = type;
    }

    public void broadcast(final IStaticPacket... packet) {
        getClan().broadcastToOnlineMembers(packet);
    }

    public void broadcast(final L2GameServerPacket... packet) {
        getClan().broadcastToOnlineMembers(packet);
    }

    public void setEvent(final boolean start, final SiegeEvent event) {
        if (start) {
            _clan.getOnlineMembers(0).forEach(player -> {
                player.addEvent(event);
                player.broadcastCharInfo();
            });
        } else {
            _clan.getOnlineMembers(0).forEach(player -> {
                player.removeEvent(event);
                player.broadcastCharInfo();
            });
        }
    }

    public boolean isParticle(final Player player) {
        return true;
    }

    public long getParam() {
        return 0L;
    }

    public long getDate() {
        return _date;
    }

    public static class SiegeClanComparatorImpl implements Comparator<SiegeClanObject> {
        private static final SiegeClanComparatorImpl _instance = new SiegeClanComparatorImpl();

        public static SiegeClanComparatorImpl getInstance() {
            return _instance;
        }

        @Override
        public int compare(final SiegeClanObject o1, final SiegeClanObject o2) {
            return Long.compare(o2.getParam(), o1.getParam());
        }
    }
}
