package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class RequestDestroyQuest extends L2GameClientPacket {
    private int _questID;

    @Override
    protected void readImpl() {
        _questID = readD();
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        final Quest quest = QuestManager.getQuest(_questID);
        if (activeChar == null || quest == null) {
            return;
        }
        if (!quest.canAbortByPacket()) {
            return;
        }
        final QuestState qs = activeChar.getQuestState(quest.getClass());
        if (qs != null && !qs.isCompleted()) {
            qs.abortQuest();
        }
    }
}
