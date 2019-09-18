package ru.j2dev.gameserver.listener.actor.player;

import ru.j2dev.gameserver.listener.PlayerListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.quest.QuestState;

public interface OnQuestStateChangeListener extends PlayerListener {
    void onQuestStateChange(final Player p0, final QuestState p1);
}
