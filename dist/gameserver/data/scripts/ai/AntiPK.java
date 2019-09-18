package ai;

import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.utils.Location;

/**
 * Created by JunkyFunky
 * on 06.07.2018 17:16
 * group j2dev
 */
public class AntiPK extends Fighter {

    public AntiPK(NpcInstance actor) {
        super(actor);
    }

    @Override
    public boolean checkAggression(final Creature target) {
        return target.isPlayable() && target.getKarma() > 0 && super.checkAggression(target);
    }

    @Override
    public void onEvtAggression(final Creature target, final int agro) {
        NpcInstance actor = getActor();
        actor.teleToLocation(Location.findAroundPosition(target, 500, 1000));
        actor.broadcastPacket(new MagicSkillUse(actor, target, 2036, 1, 0, 0L));
        super.onEvtAggression(target, agro);

    }

}
