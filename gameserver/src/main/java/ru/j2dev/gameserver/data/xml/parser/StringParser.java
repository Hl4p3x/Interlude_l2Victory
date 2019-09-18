package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;

import java.io.File;

public class StringParser extends AbstractFileParser<StringHolder> {

    private StringParser() {
        super(StringHolder.getInstance());
    }

    public static StringParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/others/strings.xml");
    }

    @Override
    protected void readData(final StringHolder holder, final Element rootElement) {
        rootElement.getChildren().
                forEach(element -> element.getChildren().
                        forEach(childElement -> holder.addString(element.getAttributeValue("key"), childElement.getName(), childElement.getTextTrim())));
    }

    private static class LazyHolder {
        protected static final StringParser INSTANCE = new StringParser();
    }
}