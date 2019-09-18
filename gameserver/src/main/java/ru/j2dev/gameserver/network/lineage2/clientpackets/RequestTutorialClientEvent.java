package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;

public class RequestTutorialClientEvent extends L2GameClientPacket {
    int event;

    public RequestTutorialClientEvent() {
        event = 0;
    }

    @Override
    protected void readImpl() {
        event = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = getClient().getActiveChar();
        if (player == null) {
            return;
        }
        final Quest tutorial = QuestManager.getQuest(255);
        if (tutorial != null) {
            player.processQuestEvent(tutorial.getName(), "CE" + event, null);
        }
    }
}
