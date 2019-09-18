package ai;

import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.List;

public class AttackMobNotPlayerFighter extends Fighter {
    public AttackMobNotPlayerFighter(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null) {
            return;
        }
        final Player player = attacker.getPlayer();
        if (player != null) {
            final List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
            if (quests != null) {
                for (final QuestState qs : quests) {
                    qs.getQuest().notifyAttack(actor, qs);
                }
            }
        }
        onEvtAggression(attacker, damage);
    }

    @Override
    protected void onEvtAggression(final Creature attacker, final int aggro) {
        final NpcInstance actor = getActor();
        if (attacker == null) {
            return;
        }
        if (!actor.isRunning()) {
            startRunningTask(AI_TASK_ATTACK_DELAY);
        }
        if (getIntention() != CtrlIntention.AI_INTENTION_ATTACK) {
            setIntention(CtrlIntention.AI_INTENTION_ATTACK, attacker);
        }
    }
}
