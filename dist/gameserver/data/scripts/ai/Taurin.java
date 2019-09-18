package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

public class Taurin extends DefaultAI {
    static final Location[] points = {new Location(80752, 146400, -3533), new Location(80250, 146988, -3559), new Location(80070, 146942, -3559), new Location(80048, 146705, -3547), new Location(79784, 146561, -3546), new Location(79476, 146800, -3547), new Location(79490, 147480, -3559), new Location(79812, 148310, -3559), new Location(79692, 148564, -3559), new Location(77569, 148495, -3623), new Location(77495, 148191, -3622), new Location(77569, 148495, -3623), new Location(79819, 148740, -3559), new Location(79773, 149110, -3559), new Location(79291, 149523, -3559), new Location(79569, 150214, -3548), new Location(79679, 150717, -3543), new Location(80106, 150630, -3547), new Location(81207, 150276, -3559), new Location(81820, 150666, -3559), new Location(82038, 150589, -3559), new Location(82394, 149943, -3559), new Location(82038, 150589, -3559), new Location(81820, 150666, -3559), new Location(81582, 150590, -3559), new Location(81535, 149653, -3495), new Location(83814, 148630, -3420), new Location(87001, 148637, -3428), new Location(83814, 148630, -3420), new Location(82921, 148467, -3495), new Location(82060, 148070, -3495), new Location(82060, 148070, -3495), new Location(82060, 148070, -3495), new Location(82060, 148070, -3495), new Location(81544, 147514, -3491), new Location(81691, 146578, -3559), new Location(83190, 146687, -3491), new Location(81691, 146578, -3559), new Location(81331, 146915, -3559), new Location(81067, 146925, -3559), new Location(80752, 146400, -3533)};

    private int current_point;
    private long wait_timeout;
    private boolean wait;

    public Taurin(final NpcInstance actor) {
        super(actor);
        current_point = -1;
        wait_timeout = 0L;
        wait = false;
        AI_TASK_ATTACK_DELAY = 250L;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return true;
        }
        if (_def_think) {
            doTask();
            return true;
        }
        if (System.currentTimeMillis() > wait_timeout && (current_point > -1 || Rnd.chance(5))) {
            if (!wait) {
                switch (current_point) {
                    case 4: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 10: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 14: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 16: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 21: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 27: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 30: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        Functions.npcSay(actor, "\u0412\u0441\u0435\u043c \u043b\u0435\u0436\u0430\u0442\u044c, \u0443 \u043c\u0435\u043d\u044f \u0431\u043e\u043c\u0431\u0430!");
                        return wait = true;
                    }
                    case 31: {
                        wait_timeout = System.currentTimeMillis() + 15000L;
                        Functions.npcSay(actor, "\u042f \u0431\u043e\u043b\u044c\u043d\u043e\u0439, \u0437\u0430 \u0441\u0435\u0431\u044f \u043d\u0435 \u0440\u0443\u0447\u0430\u044e\u0441\u044c!!!!");
                        return wait = true;
                    }
                    case 32: {
                        wait_timeout = System.currentTimeMillis() + 15000L;
                        Functions.npcSay(actor, "\u0412\u044b \u0432\u0441\u0435 \u0435\u0449\u0435 \u0442\u0443\u0442? \u042f \u0432\u0430\u0441 \u043f\u0440\u0435\u0434\u0443\u043f\u0440\u0435\u0436\u0434\u0430\u043b!!!!!");
                        return wait = true;
                    }
                    case 33: {
                        actor.broadcastPacket(new MagicSkillUse(actor, actor, 2025, 1, 500, 0L));
                        wait_timeout = System.currentTimeMillis() + 1000L;
                        return wait = true;
                    }
                    case 35: {
                        wait_timeout = System.currentTimeMillis() + 10000L;
                        return wait = true;
                    }
                    case 37: {
                        wait_timeout = System.currentTimeMillis() + 30000000L;
                        return wait = true;
                    }
                }
            }
            wait_timeout = 0L;
            wait = false;
            ++current_point;
            if (current_point >= Taurin.points.length) {
                current_point = 0;
            }
            addTaskMove(Taurin.points[current_point], true);
            doTask();
            return true;
        }
        return randomAnimation();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
    }
}
