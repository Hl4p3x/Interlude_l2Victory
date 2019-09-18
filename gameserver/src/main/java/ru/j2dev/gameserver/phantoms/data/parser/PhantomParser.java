package ru.j2dev.gameserver.phantoms.data.parser;

import org.jdom2.Element;
import ru.j2dev.gameserver.phantoms.ai.PhantomAiType;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomHolder;
import ru.j2dev.gameserver.phantoms.template.PhantomTemplate;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.templates.item.ItemGrade;

import java.io.File;

public class PhantomParser extends AbstractFileParser<PhantomHolder> {
    private static final PhantomParser instance = new PhantomParser();

    private PhantomParser() {
        super(PhantomHolder.getInstance());
    }

    public static PhantomParser getInstance() {
        return PhantomParser.instance;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/phantoms/phantoms.xml");
    }

    @Override
    protected void readData(final PhantomHolder holder, final Element rootElement) {
        for (final Element phantomElement : rootElement.getChildren()) {
            if ("phantom".equals(phantomElement.getName())) {
                final PhantomTemplate template = new PhantomTemplate();
                template.setType(PhantomAiType.valueOf(phantomElement.getAttributeValue("type")));
                template.setName(phantomElement.getAttributeValue("name"));
                template.setTitle(phantomElement.getAttributeValue("title"));
                template.setItemGrade(ItemGrade.valueOf(phantomElement.getAttributeValue("grade")));
                for (final Element setElement : phantomElement.getChildren()) {
                    if ("set".equals(setElement.getName())) {
                        final String setName = setElement.getAttributeValue("name");
                        switch (setName) {
                            case "race":
                                template.setRace(getInteger(setElement));
                                break;
                            case "classId":
                                template.setClassId(getInteger(setElement));
                                break;
                            case "sex":
                                template.setSex(getInteger(setElement));
                                break;
                            case "face":
                                template.setFace(getInteger(setElement));
                                break;
                            case "hair":
                                template.setHair(getInteger(setElement));
                                break;
                            case "nameColor":
                                template.setNameColor(getIntegerDecode(setElement));
                                break;
                            default:
                                if (!"titleColor".equals(setName)) {
                                    continue;
                                }
                                template.setTitleColor(getIntegerDecode(setElement));
                                break;
                        }
                    }
                }
                holder.addPhantomTemplate(template.getItemGrade(), template);
            }
        }
    }

    private int getInteger(final Element set) {
        return Integer.parseInt(set.getAttributeValue("value"));
    }

    private int getIntegerDecode(final Element set) {
        return Integer.decode(set.getAttributeValue("value"));
    }
}
