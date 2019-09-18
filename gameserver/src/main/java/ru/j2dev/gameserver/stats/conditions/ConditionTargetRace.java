package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class ConditionTargetRace extends Condition {
    private final int _race;

    public ConditionTargetRace(final String race) {
        if ("Undead".equalsIgnoreCase(race)) {
            _race = 1;
        } else if ("MagicCreatures".equalsIgnoreCase(race)) {
            _race = 2;
        } else if ("Beasts".equalsIgnoreCase(race)) {
            _race = 3;
        } else if ("Animals".equalsIgnoreCase(race)) {
            _race = 4;
        } else if ("Plants".equalsIgnoreCase(race)) {
            _race = 5;
        } else if ("Humanoids".equalsIgnoreCase(race)) {
            _race = 6;
        } else if ("Spirits".equalsIgnoreCase(race)) {
            _race = 7;
        } else if ("Angels".equalsIgnoreCase(race)) {
            _race = 8;
        } else if ("Demons".equalsIgnoreCase(race)) {
            _race = 9;
        } else if ("Dragons".equalsIgnoreCase(race)) {
            _race = 10;
        } else if ("Giants".equalsIgnoreCase(race)) {
            _race = 11;
        } else if ("Bugs".equalsIgnoreCase(race)) {
            _race = 12;
        } else if ("Fairies".equalsIgnoreCase(race)) {
            _race = 13;
        } else if ("Humans".equalsIgnoreCase(race)) {
            _race = 14;
        } else if ("Elves".equalsIgnoreCase(race)) {
            _race = 15;
        } else if ("DarkElves".equalsIgnoreCase(race)) {
            _race = 16;
        } else if ("Orcs".equalsIgnoreCase(race)) {
            _race = 17;
        } else if ("Dwarves".equalsIgnoreCase(race)) {
            _race = 18;
        } else if ("Others".equalsIgnoreCase(race)) {
            _race = 19;
        } else if ("NonLivingBeings".equalsIgnoreCase(race)) {
            _race = 20;
        } else if ("SiegeWeapons".equalsIgnoreCase(race)) {
            _race = 21;
        } else if ("DefendingArmy".equalsIgnoreCase(race)) {
            _race = 22;
        } else if ("Mercenaries".equalsIgnoreCase(race)) {
            _race = 23;
        } else {
            if (!"UnknownCreature".equalsIgnoreCase(race)) {
                throw new IllegalArgumentException("ConditionTargetRace: Invalid race name: " + race);
            }
            _race = 24;
        }
    }

    @Override
    protected boolean testImpl(final Env env) {
        final Creature target = env.target;
        return target != null && target.getTemplate() != null && (target.isSummon() || target.isNpc()) && _race == ((NpcTemplate) target.getTemplate()).getRace();
    }
}
