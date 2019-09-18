package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class EvasGiftBox extends Fighter {
    private static final int[] KISS_OF_EVA = {1073, 3141, 3252};
    private static final int Red_Coral = 9692;
    private static final int Crystal_Fragment = 9693;

    public EvasGiftBox(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        if (killer != null) {
            final Player player = killer.getPlayer();
            if (player != null && player.getEffectList().containEffectFromSkills(EvasGiftBox.KISS_OF_EVA)) {
                actor.dropItem(player, Rnd.chance(50) ? 9692 : 9693, 1L);
            }
        }
        super.onEvtDead(killer);
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}
