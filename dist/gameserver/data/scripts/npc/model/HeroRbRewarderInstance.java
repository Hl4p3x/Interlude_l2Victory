package npc.model;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import services.GlobalServices;

/**
 * Solution
 * 01.09.2018
 * 17:41
 */

public class HeroRbRewarderInstance extends RaidBossInstance {
    public HeroRbRewarderInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onDeath(Creature killer) {
        super.onDeath(killer);
        if (killer != null && killer.isPlayable()) {
            Player player = killer.getPlayer();
            if (player.isDead()) {
                return;
            }
            Party partyPlayer = player.getParty();
            if (partyPlayer == null) {
                GlobalServices.makeCustomHero(player, 24*60*60);
            }
            if (partyPlayer != null) {
                for (Player playerParty : partyPlayer) {
                    if (partyPlayer != null && !playerParty.isDead()) {
                        if (getDistance3D(playerParty) > Config.ALT_PARTY_DISTRIBUTION_RANGE) {
                            continue;
                        }
                        GlobalServices.makeCustomHero(playerParty, 4*60*60);
                    }
                }
            }
        }
    }
}
