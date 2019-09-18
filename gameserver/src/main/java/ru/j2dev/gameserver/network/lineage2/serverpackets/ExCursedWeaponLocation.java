package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class ExCursedWeaponLocation extends L2GameServerPacket {
    private final List<CursedWeaponInfo> _cursedWeaponInfo;

    public ExCursedWeaponLocation(final List<CursedWeaponInfo> cursedWeaponInfo) {
        _cursedWeaponInfo = cursedWeaponInfo;
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x46);
        if (_cursedWeaponInfo.isEmpty()) {
            writeD(0);
        } else {
            writeD(_cursedWeaponInfo.size());
            _cursedWeaponInfo.forEach(w -> {
                writeD(w._id);
                writeD(w._status);
                writeD(w._pos.x);
                writeD(w._pos.y);
                writeD(w._pos.z);
            });
        }
    }

    public static class CursedWeaponInfo {
        public final Location _pos;
        public final int _id;
        public final int _status;

        public CursedWeaponInfo(final Location p, final int ID, final int status) {
            _pos = p;
            _id = ID;
            _status = status;
        }
    }
}
