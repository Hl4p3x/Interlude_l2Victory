package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ManufactureItem;

import java.util.List;

public class RecipeShopSellList extends L2GameServerPacket {
    private final int objId;
    private final int curMp;
    private final int maxMp;
    private final long adena;
    private final List<ManufactureItem> createList;

    public RecipeShopSellList(final Player buyer, final Player manufacturer) {
        objId = manufacturer.getObjectId();
        curMp = (int) manufacturer.getCurrentMp();
        maxMp = manufacturer.getMaxMp();
        adena = buyer.getAdena();
        createList = manufacturer.getCreateList();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd9);
        writeD(objId);
        writeD(curMp);
        writeD(maxMp);
        writeD((int) adena);
        writeD(createList.size());
        createList.forEach(mi -> {
            writeD(mi.getRecipeId());
            writeD(0);
            writeD((int) mi.getCost());
        });
    }
}
