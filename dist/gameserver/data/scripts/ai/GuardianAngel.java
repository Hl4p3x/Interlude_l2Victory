package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class GuardianAngel extends DefaultAI {
    static final String[] flood = {"Waaaah! Step back from the confounded box! I will take it myself!", "Grr! Who are you and why have you stopped me?", "Grr. I've been hit..."};

    public GuardianAngel(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        Functions.npcSay(actor, GuardianAngel.flood[Rnd.get(2)]);
        return super.thinkActive();
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        if (actor != null) {
            Functions.npcSay(actor, GuardianAngel.flood[2]);
        }
        super.onEvtDead(killer);
    }
}
