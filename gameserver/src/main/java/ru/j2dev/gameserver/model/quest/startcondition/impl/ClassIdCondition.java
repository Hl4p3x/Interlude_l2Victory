package ru.j2dev.gameserver.model.quest.startcondition.impl;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.quest.startcondition.ConditionList;
import ru.j2dev.gameserver.model.quest.startcondition.ICheckStartCondition;

/**
 * @author KilRoy
 */
public final class ClassIdCondition implements ICheckStartCondition {
    private final ClassId[] classId;

    public ClassIdCondition(final ClassId... classId) {
        this.classId = classId;
    }

    @Override
    public final ConditionList checkCondition(final Player player) {
        if (ArrayUtils.contains(classId, player.getClassId()))
            return ConditionList.NONE;
        return ConditionList.CLASS_ID;
    }
}