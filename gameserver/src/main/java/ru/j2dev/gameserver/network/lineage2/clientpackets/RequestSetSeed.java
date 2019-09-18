package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.CastleManorManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.templates.manor.SeedProduction;

import java.util.ArrayList;
import java.util.List;

public class RequestSetSeed extends L2GameClientPacket {
    private int _count;
    private int _manorId;
    private long[] _items;

    @Override
    protected void readImpl() {
        _manorId = readD();
        _count = readD();
        if (_count * 12 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new long[_count * 3];
        for (int i = 0; i < _count; ++i) {
            _items[i * 3] = readD();
            _items[i * 3 + 1] = readD();
            _items[i * 3 + 2] = readD();
            if (_items[i * 3] < 1L || _items[i * 3 + 1] < 0L || _items[i * 3 + 2] < 0L) {
                _count = 0;
                return;
            }
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null || _count == 0) {
            return;
        }
        if (activeChar.getClan() == null) {
            activeChar.sendActionFailed();
            return;
        }
        final Castle caslte = ResidenceHolder.getInstance().getResidence(Castle.class, _manorId);
        if (caslte.getOwnerId() != activeChar.getClanId() || (activeChar.getClanPrivileges() & 0x10000) != 0x10000) {
            activeChar.sendActionFailed();
            return;
        }
        final List<SeedProduction> seeds = new ArrayList<>(_count);
        for (int i = 0; i < _count; ++i) {
            final int id = (int) _items[i * 3];
            final long sales = _items[i * 3 + 1];
            final long price = _items[i * 3 + 2];
            if (id > 0) {
                final SeedProduction s = CastleManorManager.getInstance().getNewSeedProduction(id, sales, price, sales);
                seeds.add(s);
            }
        }
        caslte.setSeedProduction(seeds, 1);
        caslte.saveSeedData(1);
    }
}
