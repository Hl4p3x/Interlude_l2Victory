package ru.j2dev.gameserver.phantoms.data.parser;

import org.jdom2.Element;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomArmorHolder;
import ru.j2dev.gameserver.phantoms.template.PhantomArmorTemplate;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;

import java.io.File;

public class PhantomArmorParser extends AbstractFileParser<PhantomArmorHolder> {
    private static PhantomArmorParser instance = new PhantomArmorParser();

    private PhantomArmorParser() {
        super(PhantomArmorHolder.getInstance());
    }

    public static PhantomArmorParser getInstance() {
        return instance;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/phantoms/equipment/armor.xml");
    }

    @Override
    protected void readData(final PhantomArmorHolder holder, final Element rootElement) {
        rootElement.getChildren().stream().filter(armorElement -> "set".equalsIgnoreCase(armorElement.getName())).forEach(armorElement -> {
            final int setId = Integer.parseInt(armorElement.getAttributeValue("set_id"));
            final PhantomArmorTemplate template = new PhantomArmorTemplate();
            armorElement.getChildren().stream().filter(itemElement -> "item".equalsIgnoreCase(itemElement.getName())).mapToInt(itemElement -> Integer.parseInt(itemElement.getAttributeValue("id"))).forEach(template::addId);
            holder.addSet(setId, template);
        });
    }
}
