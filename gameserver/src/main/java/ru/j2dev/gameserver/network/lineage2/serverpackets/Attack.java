package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;

import java.util.stream.IntStream;

public class Attack extends L2GameServerPacket {
    public final int _attackerId;
    public final boolean _soulshot;
    private final int _grade;
    private final int _x;
    private final int _y;
    private final int _z;
    private final int _tx;
    private final int _ty;
    private final int _tz;
    private Hit[] hits;

    public Attack(final Creature attacker, final Creature target, final boolean ss, final int grade) {
        _attackerId = attacker.getObjectId();
        _soulshot = ss;
        _grade = grade;
        _x = attacker.getX();
        _y = attacker.getY();
        _z = attacker.getZ();
        _tx = target.getX();
        _ty = target.getY();
        _tz = target.getZ();
        hits = new Hit[0];
    }

    public void addHit(final GameObject target, final int damage, final boolean miss, final boolean crit, final boolean shld) {
        final int pos = hits.length;
        final Hit[] tmp = new Hit[pos + 1];
        System.arraycopy(hits, 0, tmp, 0, hits.length);
        tmp[pos] = new Hit(target, damage, miss, crit, shld);
        hits = tmp;
    }

    public boolean hasHits() {
        return hits.length > 0;
    }

    @Override
    protected final void writeImpl() {
        writeC(5);
        writeD(_attackerId);
        writeD(hits[0]._targetId);
        writeD(hits[0]._damage);
        writeC(hits[0]._flags);
        writeD(_x);
        writeD(_y);
        writeD(_z);
        writeH(hits.length - 1);
        IntStream.range(1, hits.length).forEach(i -> {
            writeD(hits[i]._targetId);
            writeD(hits[i]._damage);
            writeC(hits[i]._flags);
        });
    }

    private class Hit {
        final int _targetId;
        final int _damage;
        int _flags;

        Hit(final GameObject target, final int damage, final boolean miss, final boolean crit, final boolean shld) {
            _targetId = target.getObjectId();
            _damage = damage;
            if (_soulshot) {
                _flags |= (0x10 | _grade);
            }
            if (crit) {
                _flags |= 0x20;
            }
            if (shld) {
                _flags |= 0x40;
            }
            if (miss) {
                _flags |= 0x80;
            }
        }
    }
}
