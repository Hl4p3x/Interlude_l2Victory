package ai;

import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.List;

public class QuestNotAggroMob extends DefaultAI {
    public QuestNotAggroMob(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public boolean thinkActive() {
        return false;
    }

    @Override
    public void onEvtAttacked(final Creature attacker, final int dam) {
        final NpcInstance actor = getActor();
        final Player player = attacker.getPlayer();
        if (player != null) {
            final List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
            if (quests != null) {
                for (final QuestState qs : quests) {
                    qs.getQuest().notifyAttack(actor, qs);
                }
            }
        }
    }

    @Override
    public void onEvtAggression(final Creature attacker, final int d) {
    }
}
