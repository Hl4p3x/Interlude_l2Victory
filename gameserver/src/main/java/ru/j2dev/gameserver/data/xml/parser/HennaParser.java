package ru.j2dev.gameserver.data.xml.parser;

import gnu.trove.list.array.TIntArrayList;
import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.HennaHolder;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.templates.Henna;

import java.io.File;

public final class HennaParser extends AbstractFileParser<HennaHolder> {

    protected HennaParser() {
        super(HennaHolder.getInstance());
    }

    public static HennaParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/hennas.xml");
    }

    @Override
    protected void readData(final HennaHolder holder, final Element rootElement) {
        rootElement.getChildren().forEach(hennaElement -> {
            final int symbolId = Integer.parseInt(hennaElement.getAttributeValue("symbol_id"));
            final int dyeId = Integer.parseInt(hennaElement.getAttributeValue("dye_id"));
            final long price = Integer.parseInt(hennaElement.getAttributeValue("price"));
            final long drawCount = (hennaElement.getAttributeValue("draw_count") == null) ? 10L : Integer.parseInt(hennaElement.getAttributeValue("draw_count"));
            final int wit = Integer.parseInt(hennaElement.getAttributeValue("wit"));
            final int str = Integer.parseInt(hennaElement.getAttributeValue("str"));
            final int _int = Integer.parseInt(hennaElement.getAttributeValue("int"));
            final int con = Integer.parseInt(hennaElement.getAttributeValue("con"));
            final int dex = Integer.parseInt(hennaElement.getAttributeValue("dex"));
            final int men = Integer.parseInt(hennaElement.getAttributeValue("men"));
            final TIntArrayList list = new TIntArrayList();
            for (Element classElement : hennaElement.getChildren("class")) {
                if(classElement.getAttributeValue("id") != null) {
                    int id = Integer.parseInt(classElement.getAttributeValue("id"));
                    list.add(id);
                }
                if(Boolean.parseBoolean(classElement.getAttributeValue("first"))) {
                    for(ClassId classId : ClassId.values()) {
                        if(classId.getLevel() == 1) {
                            list.add(classId.getId());
                        }
                    }
                }
                if(Boolean.parseBoolean(classElement.getAttributeValue("second"))) {
                    for(ClassId classId : ClassId.values()) {
                        if(classId.getLevel() == 2) {
                            list.add(classId.getId());
                        }
                    }
                }
                if(Boolean.parseBoolean(classElement.getAttributeValue("third"))) {
                    for(ClassId classId : ClassId.values()) {
                        if(classId.getLevel() == 3) {
                            list.add(classId.getId());
                        }
                    }
                }
                if(Boolean.parseBoolean(classElement.getAttributeValue("fourth"))) {
                    for(ClassId classId : ClassId.values()) {
                        if(classId.getLevel() == 4) {
                            list.add(classId.getId());
                        }
                    }
                }
            }
            final Henna henna = new Henna(symbolId, dyeId, price, drawCount, wit, _int, con, str, dex, men, list);
            holder.addHenna(henna);
        });
    }

    private static class LazyHolder {
        protected static final HennaParser INSTANCE = new HennaParser();
    }
}
