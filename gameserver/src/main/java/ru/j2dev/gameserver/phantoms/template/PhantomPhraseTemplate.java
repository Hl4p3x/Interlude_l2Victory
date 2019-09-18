package ru.j2dev.gameserver.phantoms.template;

import ru.j2dev.gameserver.network.lineage2.components.ChatType;

public class PhantomPhraseTemplate {
    private String phrase;
    private int chance;
    private ChatType type;

    public PhantomPhraseTemplate() {
        chance = 100;
    }

    public ChatType getType() {
        return type;
    }

    public void setType(final ChatType type) {
        this.type = type;
    }

    public String getPhrase() {
        return phrase;
    }

    public void setPhrase(final String phrase) {
        this.phrase = phrase;
    }

    public int getChance() {
        return chance;
    }

    public void setChance(final int chance) {
        this.chance = chance;
    }
}
