package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExQuestNpcLogList;

public class RequestAddExpandQuestAlarm extends L2GameClientPacket {
    private int _questId;

    @Override
    protected void readImpl() {
        _questId = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Quest quest = QuestManager.getQuest(_questId);
        if (quest == null) {
            return;
        }
        final QuestState state = player.getQuestState(quest.getClass());
        if (state == null) {
            return;
        }
        player.sendPacket(new ExQuestNpcLogList(state));
    }
}
