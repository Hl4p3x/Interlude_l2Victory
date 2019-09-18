package ru.j2dev.gameserver.phantoms.data.parser;

import org.jdom2.Element;
import ru.j2dev.gameserver.phantoms.data.holder.PhantomPhraseHolder;
import ru.j2dev.gameserver.phantoms.template.PhantomPhraseTemplate;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.utils.Language;

import java.io.File;

public class PhantomPhraseParser extends AbstractFileParser<PhantomPhraseHolder> {
    public static final PhantomPhraseParser instance = new PhantomPhraseParser();

    public PhantomPhraseParser() {
        super(PhantomPhraseHolder.getInstance());
    }

    public static PhantomPhraseParser getInstance() {
        return PhantomPhraseParser.instance;
    }

    @Override
    public File getXMLFile() {
        if (Language.ENGLISH.getShortName().equalsIgnoreCase(Config.DEFAULT_LANG)) {
            return new File(Config.DATAPACK_ROOT, "data/xml/phantoms/phrases_en.xml");
        }
        return new File(Config.DATAPACK_ROOT, "data/xml/phantoms/phrases_ru.xml");
    }


    @Override
    protected void readData(final PhantomPhraseHolder holder, final Element rootElement) {
        rootElement.getChildren().stream().filter(element -> "phrase".equals(element.getName())).forEach(element -> {
            final PhantomPhraseTemplate template = new PhantomPhraseTemplate();
            template.setPhrase(element.getAttributeValue("text"));
            template.setType(ChatType.valueOf(element.getAttributeValue("type")));
            final String chance = element.getAttributeValue("chance");
            if (chance != null) {
                template.setChance(Integer.parseInt(chance));
            }
            holder.addPhrase(template.getType(), template);
        });
    }
}
