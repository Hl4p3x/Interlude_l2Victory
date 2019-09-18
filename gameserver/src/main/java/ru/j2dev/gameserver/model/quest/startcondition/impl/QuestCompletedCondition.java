package ru.j2dev.gameserver.model.quest.startcondition.impl;


import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.startcondition.ConditionList;
import ru.j2dev.gameserver.model.quest.startcondition.ICheckStartCondition;

public final class QuestCompletedCondition implements ICheckStartCondition {
    private final String questId;

    public QuestCompletedCondition(final String questId) {
        this.questId = questId;
    }

    @Override
    public final ConditionList checkCondition(final Player player) {
        if (player.isQuestCompleted(questId))
            return ConditionList.NONE;
        return ConditionList.QUEST;
    }
}