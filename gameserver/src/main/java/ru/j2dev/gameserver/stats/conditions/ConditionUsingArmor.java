package ru.j2dev.gameserver.stats.conditions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.templates.item.ArmorTemplate.ArmorType;

public class ConditionUsingArmor extends Condition {
    private final ArmorType _armor;

    public ConditionUsingArmor(final ArmorType armor) {
        _armor = armor;
    }

    @Override
    protected boolean testImpl(final Env env) {
        return env.character.isPlayer() && ((Player) env.character).isWearingArmor(_armor);
    }
}
