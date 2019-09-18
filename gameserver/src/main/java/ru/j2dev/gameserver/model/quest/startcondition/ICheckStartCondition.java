package ru.j2dev.gameserver.model.quest.startcondition;


import ru.j2dev.gameserver.model.Player;

@FunctionalInterface
public interface ICheckStartCondition {
    ConditionList checkCondition(Player player);
}