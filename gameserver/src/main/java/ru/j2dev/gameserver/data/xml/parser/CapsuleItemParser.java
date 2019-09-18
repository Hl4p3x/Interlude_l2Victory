package ru.j2dev.gameserver.data.xml.parser;

import org.apache.commons.lang3.tuple.Pair;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.CapsuleItemHolder;
import ru.j2dev.gameserver.data.xml.holder.CapsuleItemHolder.CapsuledItem;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

public class CapsuleItemParser extends AbstractFileParser<CapsuleItemHolder> {

    protected CapsuleItemParser() {
        super(CapsuleItemHolder.getInstance());
    }

    public static CapsuleItemParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/capsule_items.xml");
    }

    @Override
    protected void readData(final CapsuleItemHolder holder, final Element rootElement) {
        rootElement.getChildren().stream().filter(capsuleItemEntryElement -> "capsule".equalsIgnoreCase(capsuleItemEntryElement.getName())).forEach(capsuleItemEntryElement -> {
            final int capsuleItemId = Integer.parseInt(capsuleItemEntryElement.getAttributeValue("itemId"));
            final int requiredItemId = Integer.parseInt(capsuleItemEntryElement.getAttributeValue("requiredItemId", "0"));
            final long requiredItemAmount = Long.parseLong(capsuleItemEntryElement.getAttributeValue("requiredItemAmount", "0"));
            final List<CapsuledItem> capsuledItems = new LinkedList<>();
            capsuleItemEntryElement.getChildren("item").forEach(itemEleemnt -> {
                final int itemId = Integer.parseInt(itemEleemnt.getAttributeValue("id"));
                final long minCnt = Long.parseLong(itemEleemnt.getAttributeValue("min"));
                final long maxCnt = Long.parseLong(itemEleemnt.getAttributeValue("max"));
                final double chance = Double.parseDouble(itemEleemnt.getAttributeValue("chance", "100."));
                final int minEnchant = Integer.parseInt(itemEleemnt.getAttributeValue("enchant_min", "0"));
                final int maxEnchant = Integer.parseInt(itemEleemnt.getAttributeValue("enchant_max", "0"));
                if (minCnt > maxCnt) {
                    error("Capsuled item " + itemId + " min > max in capsule " + capsuleItemId);
                } else {
                    final CapsuledItem capsuledItem = new CapsuledItem(itemId, minCnt, maxCnt, chance, minEnchant, maxEnchant);
                    capsuledItems.add(capsuledItem);
                }
            });
            if (requiredItemId > 0 && requiredItemAmount > 0L) {
                holder.add(capsuleItemId, Pair.of(requiredItemId, requiredItemAmount), capsuledItems);
            } else {
                holder.add(capsuleItemId, capsuledItems);
            }
        });
    }

    private static class LazyHolder {
        private static final CapsuleItemParser INSTANCE = new CapsuleItemParser();
    }
}
