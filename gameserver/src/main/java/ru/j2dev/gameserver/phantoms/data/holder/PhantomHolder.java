package ru.j2dev.gameserver.phantoms.data.holder;

import ru.j2dev.gameserver.phantoms.template.PhantomTemplate;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.templates.item.ItemGrade;

import java.util.*;

public class PhantomHolder extends AbstractHolder {
    private static final PhantomHolder instance = new PhantomHolder();
    private final Map<ItemGrade, List<PhantomTemplate>> phantomTemplates;

    public PhantomHolder() {
        phantomTemplates = new HashMap<>();
        for (final ItemGrade itemGrade : ItemGrade.values()) {
            phantomTemplates.put(itemGrade, new ArrayList<>());
        }
    }

    public static PhantomHolder getInstance() {
        return PhantomHolder.instance;
    }

    public void addPhantomTemplate(final ItemGrade itemGrade, final PhantomTemplate phantom) {
        phantomTemplates.get(itemGrade).add(phantom);
        Config.CNAME_FORBIDDEN_NAMES = ArrayUtils.add(Config.CNAME_FORBIDDEN_NAMES, phantom.getName());
    }

    public Map<ItemGrade, List<PhantomTemplate>> getPhantomTemplateMap() {
        return phantomTemplates;
    }

    public PhantomTemplate getRandomPhantomTemplate(final ItemGrade minItemGrade, final ItemGrade maxItemGrade) {
        final ItemGrade rndItemGrade = ItemGrade.values()[Rnd.get(minItemGrade.ordinal(), maxItemGrade.ordinal())];
        final List<PhantomTemplate> gradeList = phantomTemplates.get(rndItemGrade);
        if (gradeList.size() == 0) {
            warn("Can't find template for grade: " + rndItemGrade);
            return null;
        }
        return gradeList.get(Rnd.get(gradeList.size()));
    }

    public boolean isNameExists(final String name) {
        return phantomTemplates.values().stream().flatMap(Collection::stream).anyMatch(template -> template.getName().equalsIgnoreCase(name));
    }

    @Override
    public int size() {
        return phantomTemplates.size();
    }

    @Override
    public void clear() {
        phantomTemplates.clear();
    }

}
