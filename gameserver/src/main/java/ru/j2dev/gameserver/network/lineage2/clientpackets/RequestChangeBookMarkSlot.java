package ru.j2dev.gameserver.network.lineage2.clientpackets;

public class RequestChangeBookMarkSlot extends L2GameClientPacket {
    private int slot_old;
    private int slot_new;

    @Override
    protected void readImpl() {
        slot_old = readD();
        slot_new = readD();
    }

    @Override
    protected void runImpl() {
    }
}
