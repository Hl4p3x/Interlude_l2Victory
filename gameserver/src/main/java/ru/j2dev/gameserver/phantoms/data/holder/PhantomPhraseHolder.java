package ru.j2dev.gameserver.phantoms.data.holder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.phantoms.template.PhantomPhraseTemplate;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;

import java.util.*;

public class PhantomPhraseHolder extends AbstractHolder {
    private static final PhantomPhraseHolder instance = new PhantomPhraseHolder();
    private static final Logger log = LoggerFactory.getLogger(PhantomPhraseHolder.class);

    private Map<ChatType, List<PhantomPhraseTemplate>> phrases;

    public PhantomPhraseHolder() {
        phrases = new HashMap<>();
        Arrays.stream(ChatType.values()).forEach(type -> phrases.put(type, new ArrayList<>(0)));
    }

    public static PhantomPhraseHolder getInstance() {
        return instance;
    }

    public void addPhrase(final ChatType type, final PhantomPhraseTemplate phrase) {
        phrases.get(type).add(phrase);
    }

    public String getRandomPhrase(final ChatType type) {
        final List<PhantomPhraseTemplate> phList = phrases.get(type);
        if (phList.size() == 0) {
            log.warn("Can't find phrases for chat type: " + type);
            return null;
        }
        for (int i = 0; i < 20; ++i) {
            final PhantomPhraseTemplate phrase = phList.get(Rnd.get(phList.size()));
            if (Rnd.chance(phrase.getChance())) {
                return phrase.getPhrase();
            }
        }
        log.warn("Can't find phrase for chat type: " + type + "! Please add more phrases or correct chance.");
        return null;
    }

    @Override
    public int size() {
        return phrases.size();
    }

    @Override
    public void clear() {
        phrases.clear();
    }
}
