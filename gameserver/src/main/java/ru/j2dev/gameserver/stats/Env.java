package ru.j2dev.gameserver.stats;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;

public final class Env {
    public Creature character;
    public Creature target;
    public ItemInstance item;
    public Skill skill;
    public double value;

    public Env() {
    }

    public Env(final Creature cha, final Creature tar, final Skill sk) {
        character = cha;
        target = tar;
        skill = sk;
    }
}
