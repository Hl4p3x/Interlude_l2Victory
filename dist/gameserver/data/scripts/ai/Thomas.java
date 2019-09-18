package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class Thomas extends Fighter {
    private static final String[] _stay = {"\u0425\u0430...\u0425\u0430... \u0412\u044b \u043f\u0440\u0438\u0448\u043b\u0438 \u0441\u043f\u0430\u0441\u0442\u0438 \u0441\u043d\u0435\u0433\u043e\u0432\u0438\u043a\u0430?", "\u0422\u0430\u043a \u043f\u0440\u043e\u0441\u0442\u043e \u044f \u0432\u0430\u043c \u0435\u0433\u043e \u043d\u0435 \u043e\u0442\u0434\u0430\u043c!", "\u0427\u0442\u043e\u0431\u044b \u0441\u043f\u0430\u0441\u0442\u0438 \u0432\u0430\u0448\u0435\u0433\u043e \u0441\u043d\u0435\u0433\u043e\u0432\u0438\u043a\u0430, \u0432\u0430\u043c \u043f\u0440\u0438\u0434\u0435\u0442\u0441\u044f \u0443\u0431\u0438\u0442\u044c \u043c\u0435\u043d\u044f!", "\u0425\u0430...\u0425\u0430... \u0412\u044b \u0434\u0443\u043c\u0430\u0435\u0442\u0435 \u044d\u0442\u043e \u0442\u0430\u043a \u043f\u0440\u043e\u0441\u0442\u043e?"};
    private static final String[] _attacked = {"\u0412\u044b \u0434\u043e\u043b\u0436\u043d\u044b \u0432\u0441\u0435 \u0443\u043c\u0435\u0440\u0435\u0442\u044c!", "\u0421\u043d\u0435\u0433\u043e\u0432\u0438\u043a \u043c\u043e\u0439 \u0438 \u043d\u0435 \u0431\u0443\u0434\u0435\u0442 \u0443 \u0432\u0430\u0441 \u041d\u043e\u0432\u043e\u0433\u043e \u0413\u043e\u0434\u0430!", "\u042f \u0432\u0430\u0441 \u0432\u0441\u0435\u0445 \u0443\u0431\u044c\u044e!", "\u0427\u0442\u043e \u0442\u0430\u043a \u0441\u043b\u0430\u0431\u043e \u0431\u044c\u0435\u0442\u0435? \u041c\u0430\u043b\u043e \u043a\u0430\u0448\u0438 \u0435\u043b\u0438? \u0425\u0430... \u0425\u0430...", "\u0418 \u044d\u0442\u043e \u043d\u0430\u0437\u044b\u0432\u0430\u0435\u0442\u0441\u044f \u0433\u0435\u0440\u043e\u0438?", "\u041d\u0435 \u0432\u0438\u0434\u0430\u0442\u044c \u0432\u0430\u043c \u0441\u043d\u0435\u0433\u043e\u0432\u0438\u043a\u0430!", "\u0422\u043e\u043b\u044c\u043a\u043e \u0434\u0440\u0435\u0432\u043d\u0435\u0435 \u043e\u0440\u0443\u0436\u0438\u0435 \u0441\u043f\u043e\u0441\u043e\u0431\u043d\u043e \u043f\u043e\u0431\u0435\u0434\u0438\u0442\u044c \u043c\u0435\u043d\u044f!"};

    private long _lastSay;

    public Thomas(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }
        if (!actor.isInCombat() && System.currentTimeMillis() - _lastSay > 10000L) {
            Functions.npcSay(actor, Thomas._stay[Rnd.get(Thomas._stay.length)]);
            _lastSay = System.currentTimeMillis();
        }
        return super.thinkActive();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null || attacker.getPlayer() == null) {
            return;
        }
        if (System.currentTimeMillis() - _lastSay > 5000L) {
            Functions.npcSay(actor, Thomas._attacked[Rnd.get(Thomas._attacked.length)]);
            _lastSay = System.currentTimeMillis();
        }
        super.onEvtAttacked(attacker, damage);
    }
}
