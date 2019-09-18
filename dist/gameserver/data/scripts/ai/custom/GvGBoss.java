package ai.custom;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class GvGBoss extends Fighter {
    boolean phrase1;
    boolean phrase2;
    boolean phrase3;

    public GvGBoss(final NpcInstance actor) {
        super(actor);
        phrase1 = false;
        phrase2 = false;
        phrase3 = false;
        actor.startImmobilized();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (actor.getCurrentHpPercents() < 50.0 && !phrase1) {
            phrase1 = true;
            Functions.npcSay(actor, "\u0412\u0430\u043c \u043d\u0435 \u0443\u0434\u0430\u0441\u0442\u0441\u044f \u043f\u043e\u0445\u0438\u0442\u0438\u0442\u044c \u0441\u043e\u043a\u0440\u043e\u0432\u0438\u0449\u0430 \u0413\u0435\u0440\u0430\u043b\u044c\u0434\u0430!");
        } else if (actor.getCurrentHpPercents() < 30.0 && !phrase2) {
            phrase2 = true;
            Functions.npcSay(actor, "\u042f \u0442\u0435\u0431\u0435 \u0447\u0435\u0440\u0435\u043f \u043f\u0440\u043e\u043b\u043e\u043c\u043b\u044e!");
        } else if (actor.getCurrentHpPercents() < 5.0 && !phrase3) {
            phrase3 = true;
            Functions.npcSay(actor, "\u0412\u044b \u0432\u0441\u0435 \u043f\u043e\u0433\u0438\u0431\u043d\u0435\u0442\u0435 \u0432 \u0441\u0442\u0440\u0430\u0448\u043d\u044b\u0445 \u043c\u0443\u043a\u0430\u0445! \u0423\u043d\u0438\u0447\u0442\u043e\u0436\u0443!");
        }
        super.onEvtAttacked(attacker, damage);
    }
}
