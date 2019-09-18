package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder.MultiSellListContainer;
import ru.j2dev.gameserver.model.base.MultiSellEntry;
import ru.j2dev.gameserver.model.base.MultiSellIngredient;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.util.ArrayList;
import java.util.List;

public class MultiSellList extends L2GameServerPacket {
    private final int _page;
    private final int _finished;
    private final int _listId;
    private final List<MultiSellEntry> _list;

    public MultiSellList(final MultiSellListContainer list, final int page, final int finished) {
        _list = list.getEntries();
        _listId = list.getListId();
        _page = page;
        _finished = finished;
    }

    private static List<MultiSellIngredient> fixIngredients(final List<MultiSellIngredient> ingredients) {
        int needFix = (int) ingredients.stream().filter(ingredient -> ingredient.getItemCount() > 2147483647L).count();
        if (needFix == 0) {
            return ingredients;
        }
        final List<MultiSellIngredient> result = new ArrayList<>(ingredients.size() + needFix);
        ingredients.stream().map(MultiSellIngredient::clone).forEach(ingredient2 -> {
            while (ingredient2.getItemCount() > 2147483647L) {
                final MultiSellIngredient temp = ingredient2.clone();
                temp.setItemCount(2000000000L);
                result.add(temp);
                ingredient2.setItemCount(ingredient2.getItemCount() - 2000000000L);
            }
            if (ingredient2.getItemCount() > 0L) {
                result.add(ingredient2);
            }
        });
        return result;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xd0);
        writeD(_listId);
        writeD(_page);
        writeD(_finished);
        writeD(Config.MULTISELL_SIZE);
        writeD(_list.size());
        _list.forEach(ent -> {
            final List<MultiSellIngredient> ingredients = fixIngredients(ent.getIngredients());
            writeD(ent.getEntryId());
            writeD(0);
            writeD(0);
            writeC(1);
            writeH(ent.getProduction().size());
            writeH(ingredients.size());
            ent.getProduction().forEach(prod -> {
                final int itemId = prod.getItemId();
                final ItemTemplate template = (itemId > 0) ? ItemTemplateHolder.getInstance().getTemplate(prod.getItemId()) : null;
                writeH(itemId);
                writeD((itemId > 0) ? template.getBodyPart() : 0);
                writeH((itemId > 0) ? template.getType2ForPackets() : 0);
                writeD((int) prod.getItemCount());
                writeH(prod.getItemEnchant());
                writeD(0);
                writeD(0);
            });
            ingredients.forEach(i -> {
                final int itemId = i.getItemId();
                final ItemTemplate item = (itemId > 0) ? ItemTemplateHolder.getInstance().getTemplate(i.getItemId()) : null;
                writeH(itemId);
                writeH((itemId > 0) ? item.getType2() : 65535);
                writeD((int) i.getItemCount());
                writeH(i.getItemEnchant());
                writeD(0);
                writeD(0);
            });
        });
    }
}
