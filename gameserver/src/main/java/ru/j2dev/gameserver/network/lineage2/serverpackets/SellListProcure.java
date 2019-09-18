package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.manor.CropProcure;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SellListProcure extends L2GameServerPacket {
    private final long _money;
    private final Map<ItemInstance, Integer> _sellList;
    private final int _castle;
    private List<CropProcure> _procureList;

    public SellListProcure(final Player player, final int castleId) {
        _sellList = new HashMap<>();
        _procureList = new ArrayList<>();
        _money = player.getAdena();
        _castle = castleId;
        _procureList = ResidenceHolder.getInstance().getResidence(Castle.class, _castle).getCropProcure(0);
        _procureList.forEach(c -> {
            final ItemInstance item = player.getInventory().getItemByItemId(c.getId());
            if (item != null && c.getAmount() > 0L) {
                _sellList.put(item, (int) c.getAmount());
            }
        });
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe9);
        writeD((int) _money);
        writeD(0);
        writeH(_sellList.size());
        _sellList.forEach((key, value) -> {
            writeH(0);
            writeD(key.getObjectId());
            writeD(key.getItemId());
            writeQ(value);
            writeH(key.getTemplate().getType2ForPackets());
            writeH(0);
            writeD(0);
        });
    }
}
