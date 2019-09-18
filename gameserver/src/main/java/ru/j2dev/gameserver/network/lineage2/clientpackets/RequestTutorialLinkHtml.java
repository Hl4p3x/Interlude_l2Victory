package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;

public class RequestTutorialLinkHtml extends L2GameClientPacket {
    String _bypass;

    @Override
    protected void readImpl() {
        _bypass = readS();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Quest q = QuestManager.getQuest(255);
        final Quest q1 = QuestManager.getQuest(777);
        if (q != null) {
            player.processQuestEvent(q.getName(), _bypass, null);
        }
        if (q1 != null) {
            player.processQuestEvent(q1.getName(), _bypass, null);
        }
    }
}
