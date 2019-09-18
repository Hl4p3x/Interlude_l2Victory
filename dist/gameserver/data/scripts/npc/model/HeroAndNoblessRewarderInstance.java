package npc.model;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillList;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import services.GlobalServices;

public class HeroAndNoblessRewarderInstance extends RaidBossInstance {
    public HeroAndNoblessRewarderInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected void onDeath(final Creature killer) {
        super.onDeath(killer);
        if (killer != null && killer.isPlayable()) {
            final Player player = killer.getPlayer();
            if (player.isDead() || player.getParty() == null) {
                return;
            }
            final Party playerParty = player.getParty();
            for (final Player partyPlayer : playerParty) {
                if (partyPlayer != null && !partyPlayer.isDead() && !partyPlayer.isNoble() && !partyPlayer.isSubClassActive() && partyPlayer.getLevel() >= 76) {
                    if (getDistance3D(partyPlayer) > Config.ALT_PARTY_DISTRIBUTION_RANGE) {
                        continue;
                    }
                    NoblessManager.getInstance().addNoble(partyPlayer.getPlayer());
                    partyPlayer.setNoble(true);
                    partyPlayer.updatePledgeClass();
                    partyPlayer.updateNobleSkills();
                    partyPlayer.getPlayer().sendPacket(new SkillList(partyPlayer.getPlayer()));
                    partyPlayer.getPlayer().broadcastUserInfo(true);
                }
            }
            if (Config.ALT_ALLOW_CUSTOM_HERO) {
                for (final Player partyPlayer : playerParty) {
                    if (partyPlayer != null && !partyPlayer.isDead() && !partyPlayer.isSubClassActive() && partyPlayer.getLevel() >= 76) {
                        if (getDistance3D(partyPlayer) > Config.ALT_PARTY_DISTRIBUTION_RANGE) {
                            continue;
                        }
                        GlobalServices.makeCustomHero(partyPlayer, Config.CUSTOM_HERO_STATUS_TIME);
                    }
                }
            }
        }
    }
}
