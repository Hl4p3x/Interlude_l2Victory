package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.manager.CastleManorManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.templates.manor.CropProcure;

import java.util.ArrayList;
import java.util.List;

public class RequestSetCrop extends L2GameClientPacket {
    private int _count;
    private int _manorId;
    private long[] _items;

    @Override
    protected void readImpl() {
        _manorId = readD();
        _count = readD();
        if (_count * 13 > _buf.remaining() || _count > 32767 || _count < 1) {
            _count = 0;
            return;
        }
        _items = new long[_count * 4];
        for (int i = 0; i < _count; ++i) {
            _items[i * 4] = readD();
            _items[i * 4 + 1] = readD();
            _items[i * 4 + 2] = readD();
            _items[i * 4 + 3] = readC();
            if (_items[i * 4] < 1L || _items[i * 4 + 1] < 0L || _items[i * 4 + 2] < 0L) {
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
        final List<CropProcure> crops = new ArrayList<>(_count);
        for (int i = 0; i < _count; ++i) {
            final int id = (int) _items[i * 4];
            final long sales = _items[i * 4 + 1];
            final long price = _items[i * 4 + 2];
            final int type = (int) _items[i * 4 + 3];
            if (id > 0) {
                final CropProcure s = CastleManorManager.getInstance().getNewCropProcure(id, sales, type, price, sales);
                crops.add(s);
            }
        }
        caslte.setCropProcure(crops, 1);
        caslte.saveCropData(1);
    }
}
