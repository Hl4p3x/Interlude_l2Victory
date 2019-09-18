package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.SoulCrystalHolder;
import ru.j2dev.gameserver.templates.SoulCrystal;

import java.io.File;

public final class SoulCrystalParser extends AbstractFileParser<SoulCrystalHolder> {

    private SoulCrystalParser() {
        super(SoulCrystalHolder.getInstance());
    }

    public static SoulCrystalParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/soul_crystals.xml");
    }

    @Override
    protected void readData(final SoulCrystalHolder holder, final Element rootElement) {
        rootElement.getChildren("crystal").forEach(element -> {
            final int itemId = Integer.parseInt(element.getAttributeValue("item_id"));
            final int level = Integer.parseInt(element.getAttributeValue("level"));
            final int nextItemId = Integer.parseInt(element.getAttributeValue("next_item_id"));
            final int cursedNextItemId = (element.getAttributeValue("cursed_next_item_id") == null) ? 0 : Integer.parseInt(element.getAttributeValue("cursed_next_item_id"));
            holder.addCrystal(new SoulCrystal(itemId, level, nextItemId, cursedNextItemId));
        });
    }

    private static class LazyHolder {
        protected static final SoulCrystalParser INSTANCE = new SoulCrystalParser();
    }
}
