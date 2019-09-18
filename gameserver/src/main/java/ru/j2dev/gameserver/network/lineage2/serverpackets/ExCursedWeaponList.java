package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.manager.CursedWeaponsManager;

public class ExCursedWeaponList extends L2GameServerPacket {
    private final int[] cursedWeapon_ids;

    public ExCursedWeaponList() {
        cursedWeapon_ids = CursedWeaponsManager.getInstance().getCursedWeaponsIds();
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x45);
        writeDD(cursedWeapon_ids, true);
    }
}
