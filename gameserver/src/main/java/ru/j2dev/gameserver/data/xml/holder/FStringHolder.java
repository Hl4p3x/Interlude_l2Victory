package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.templates.FStringTemplate;

/**
 * @author PaInKiLlEr
 */
public final class FStringHolder extends AbstractHolder {

    private final TIntObjectMap<FStringTemplate> _template = new TIntObjectHashMap<>();

    public static FStringHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addTemplate(final FStringTemplate template) {
        _template.put(template.getId(), template);
    }

    public FStringTemplate getTemplate(final int id) {
        return _template.get(id);
    }

    @Override
    public int size() {
        return _template.size();
    }

    @Override
    public void clear() {
        _template.clear();
    }

    private static class LazyHolder {
        protected static final FStringHolder INSTANCE = new FStringHolder();
    }
}
