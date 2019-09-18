package ai;

import quests._024_InhabitantsOfTheForestOfTheDead;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

public class Quest024Fighter extends Fighter {
    public Quest024Fighter(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final Quest q = QuestManager.getQuest(_024_InhabitantsOfTheForestOfTheDead.class);
        if (q != null) {
            for (final Player player : World.getAroundPlayers(getActor(), 300, 200)) {
                final QuestState questState = player.getQuestState(_024_InhabitantsOfTheForestOfTheDead.class);
                if (questState != null && questState.getCond() == 3) {
                    q.notifyEvent("see_creature", questState, getActor());
                }
            }
        }
        return super.thinkActive();
    }
}
