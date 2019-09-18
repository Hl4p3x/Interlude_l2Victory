package ru.j2dev.gameserver.model.quest.startcondition.impl;


import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.startcondition.ConditionList;
import ru.j2dev.gameserver.model.quest.startcondition.ICheckStartCondition;

public final class ClassLevelCondition implements ICheckStartCondition {
    private final int classLevels;

    public ClassLevelCondition(final int classLevels) {
        this.classLevels = classLevels;
    }

    @Override
    public final ConditionList checkCondition(final Player player) {
        return player.getClassId().getLevel() >= classLevels ? ConditionList.NONE : ConditionList.CLASS_LEVEL;
    }
}