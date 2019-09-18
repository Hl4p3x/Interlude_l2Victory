package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.VariationGroupHolder;
import ru.j2dev.gameserver.templates.item.support.VariationGroupData;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

public class VariationGroupParser extends AbstractFileParser<VariationGroupHolder> {

    private VariationGroupParser() {
        super(VariationGroupHolder.getInstance());
    }

    public static VariationGroupParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/variation/variation_group.xml");
    }

    @Override
    protected void readData(final VariationGroupHolder holder, final Element rootElement) {
        for (final Element vge : rootElement.getChildren()) {
            final String groupName = vge.getAttributeValue("name");
            final List<Integer> itemsList = new ArrayList<>();
            final List<VariationGroupData> variationGroupDataList = new ArrayList<>();
            vge.getChildren().forEach(vge2 -> {
                final String vge2Name = vge2.getName();
                if ("items".equalsIgnoreCase(vge2Name)) {
                    final StringTokenizer stItems = new StringTokenizer(vge2.getValue());
                    while (stItems.hasMoreTokens()) {
                        itemsList.add(Integer.parseInt(stItems.nextToken()));
                    }
                } else {
                    if (!"fee".equalsIgnoreCase(vge2Name)) {
                        return;
                    }
                    final long cancelPrice = Long.parseLong(vge2.getAttributeValue("cancelPrice"));
                    vge2.getChildren().stream().filter(vge3 -> "mineral".equalsIgnoreCase(vge3.getName())).forEach(vge3 -> {
                        final int mineralItemId = Integer.parseInt(vge3.getAttributeValue("itemId"));
                        final int gemstoneItemId = Integer.parseInt(vge3.getAttributeValue("gemstoneItemId"));
                        final long gemstoneItemCnt = Long.parseLong(vge3.getAttributeValue("gemstoneItemCnt"));
                        variationGroupDataList.add(new VariationGroupData(mineralItemId, gemstoneItemId, gemstoneItemCnt, cancelPrice));
                    });
                }
            });
            if (variationGroupDataList.isEmpty()) {
                throw new RuntimeException("Undefined fee for group " + groupName);
            }
            final int[] itemIds = new int[itemsList.size()];
            for (int i = 0; i < itemsList.size(); ++i) {
                itemIds[i] = itemsList.get(i);
            }
            Arrays.sort(itemIds);
            variationGroupDataList.forEach(variationGroupData -> holder.addSorted(itemIds, variationGroupData));
        }
    }

    private static class LazyHolder {
        protected static final VariationGroupParser INSTANCE = new VariationGroupParser();
    }
}
