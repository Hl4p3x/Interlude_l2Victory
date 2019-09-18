package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;

public class EvilNpc extends DefaultAI {
    private static final String[] _txt = {"\u043e\u0442\u0441\u0442\u0430\u043d\u044c!", "\u0443\u0439\u043c\u0438\u0441\u044c!", "\u044f \u0442\u0435\u0431\u0435 \u043e\u0442\u043e\u043c\u0449\u0443, \u043f\u043e\u0442\u043e\u043c \u0431\u0443\u0434\u0435\u0448\u044c \u043f\u0440\u043e\u0449\u0435\u043d\u0438\u044f \u043f\u0440\u043e\u0441\u0438\u0442\u044c!", "\u0443 \u0442\u0435\u0431\u044f \u0431\u0443\u0434\u0443\u0442 \u043d\u0435\u043f\u0440\u0438\u044f\u0442\u043d\u043e\u0441\u0442\u0438!", "\u044f \u043d\u0430 \u0442\u0435\u0431\u044f \u043f\u043e\u0436\u0430\u043b\u0443\u044e\u0441\u044c, \u0442\u0435\u0431\u044f \u0430\u0440\u0435\u0441\u0442\u0443\u044e\u0442!"};

    private long _lastAction;

    public EvilNpc(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null || attacker.getPlayer() == null) {
            return;
        }
        if (System.currentTimeMillis() - _lastAction > 3000L) {
            final int chance = Rnd.get(0, 100);
            if (chance < 2) {
                attacker.getPlayer().setKarma(attacker.getPlayer().getKarma() + 5);
            } else if (chance < 4) {
                actor.doCast(SkillTable.getInstance().getInfo(4578, 1), attacker, true);
            } else {
                actor.doCast(SkillTable.getInstance().getInfo(4185, 7), attacker, true);
            }
            Functions.npcSay(actor, attacker.getName() + ", " + EvilNpc._txt[Rnd.get(EvilNpc._txt.length)]);
            _lastAction = System.currentTimeMillis();
        }
    }
}
