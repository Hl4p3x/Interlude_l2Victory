package ru.j2dev.gameserver.data.xml.holder;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.utils.Language;

import java.util.*;

public class StringHolder extends AbstractHolder {

    private final Map<Language, Map<String, String>> _strings = new HashMap<>();

    public static StringHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public String getNotNull(final Language lang, final String name) {
        return getString(name, lang);
    }

    public String getNotNull(final String name, final Player player) {
        final Language lang = player == null ? Language.ENGLISH : player.getLanguage();
        boolean checked = false;
        String text = getString(name, lang);
        if (text == null && lang.equals(Language.RUSSIAN)) {
            text = getString(name, Language.ENGLISH);
            checked = true;
        }
        if (text == null && player != null && checked) {
            text = "Not find string: " + name + "; for lang: " + lang;
            _strings.get(lang).put(name, text);
        }

        return text;
    }

    public String getNotNull(final Player player, final String name) {
        final Language lang = player == null ? Language.ENGLISH : player.getLanguage();
        boolean checked = false;
        String text = getString(name, lang);
        if (text == null && lang.equals(Language.RUSSIAN)) {
            text = getString(name, Language.ENGLISH);
            checked = true;
        }
        if (text == null && player != null && checked) {
            text = "Not find string: " + name + "; for lang: " + lang;
            _strings.get(lang).put(name, text);
        }

        return text;
    }

    public String getString(final String address, final Language lang) {
        final Map<String, String> strings = _strings.get(lang);
        return strings.get(address);
    }

    @Override
    public void log() {
        _strings.forEach((key, value) -> info("load strings: " + value.size() + " for lang: " + key));
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public void clear() {
        _strings.clear();
    }

    public void addString(final String key, final String lang, final String text) {
        Language language = EnumSet.of(Language.ENGLISH, Language.VALUES).stream().filter(value -> value.getShortName().equals(lang)).findFirst().orElse(null);

        if (language == null) {
            LOGGER.error("Unknown language: {}. Key: {}", lang, key);
            return;
        }

        Map<String, String> map = _strings.computeIfAbsent(language, k -> new TreeMap<>());
        map.put(key, text);
    }

    private static class LazyHolder {
        protected static final StringHolder INSTANCE = new StringHolder();
    }
}
