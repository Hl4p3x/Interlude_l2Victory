package events.mobBuffs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.scripts.Functions;

/**
 * Solution
 * 23.08.2018
 * 11:12
 */

public class mobBuffs extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(mobBuffs.class);
    private static final String EVENT_NAME = "MOB_BUFFS";
    private static boolean ACTIVE;

    private OnDeathListenerImpl deathListener = new OnDeathListenerImpl();



    @Override
    public void onInit() {
        CharListenerList.addGlobal(deathListener);
        ACTIVE = true;
        LOGGER.info("Loaded Event: MOB_BUFFS [state: activated]");
    }

    private class OnDeathListenerImpl implements OnDeathListener {


        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            try {
                if (!Functions.SimpleCheckDrop(actor, killer)) {
                    return;
                }
                final MonsterInstance npc = (MonsterInstance) actor;
                if (Rnd.get(100) < 5 && !npc.isChampionRed() && !npc.isChampionRed()) {
                    npc.mobBuffs(killer.getPlayer(), 8300, 1, 15, 4350, 3);
                }
                if (npc.isChampionRed()) {
                    npc.mobBuffs(killer.getPlayer(), 8300, 1, 15 * 4, 4350, 3);
                }
                if (npc.isChampionBlue()) {
                    npc.mobBuffs(killer.getPlayer(), 8300, 1, 15 * 2, 4350, 3);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }
}
