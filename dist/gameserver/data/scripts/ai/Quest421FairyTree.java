package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.tables.SkillTable;

import java.util.List;

public class Quest421FairyTree extends Fighter {
    private static final Skill s_quest_vicious_poison = SkillTable.getInstance().getInfo(4243, 1);

    public Quest421FairyTree(final NpcInstance actor) {
        super(actor);
        actor.startImmobilized();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker != null) {
            if (attacker.isPlayer() && Rnd.chance(29)) {
                s_quest_vicious_poison.getEffects(actor, attacker, false, false);
            } else if (attacker.isPet()) {
                final Player player = attacker.getPlayer();
                if (player != null) {
                    final List<QuestState> quests = player.getQuestsForEvent(actor, QuestEventType.ATTACKED_WITH_QUEST);
                    if (quests != null) {
                        quests.forEach(qs -> qs.getQuest().notifyAttack(actor, qs));
                    }
                }
            }
        }
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

}
