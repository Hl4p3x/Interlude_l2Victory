package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.Quest;

public class RequestTutorialPassCmdToServer extends L2GameClientPacket {
    String _bypass;

    public RequestTutorialPassCmdToServer() {
        _bypass = null;
    }

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
        final Quest tutorial = QuestManager.getQuest(255);
        if (tutorial != null) {
            player.processQuestEvent(tutorial.getName(), _bypass, null);
        }
    }
}
