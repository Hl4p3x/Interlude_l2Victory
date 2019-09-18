package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;

public class RequestTutorialQuestionMarkPressed extends L2GameClientPacket {
    int _number;

    public RequestTutorialQuestionMarkPressed() {
        _number = 0;
    }

    @Override
    protected void readImpl() {
        _number = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Quest q = QuestManager.getQuest(255);
        if (q != null) {
            player.processQuestEvent(q.getName(), "QM" + _number, null);
        }
    }
}
