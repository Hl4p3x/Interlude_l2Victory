package ai.isle_of_prayer;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class IsleOfPrayerFighter extends Fighter {
    private static final int[] PENALTY_MOBS = {18364, 18365, 18366};
    private static final int YELLOW_CRYSTAL = 9593;
    private static final int GREEN_CRYSTAL = 9594;

    private boolean _penaltyMobsNotSpawned;

    public IsleOfPrayerFighter(final NpcInstance actor) {
        super(actor);
        _penaltyMobsNotSpawned = true;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (_penaltyMobsNotSpawned && attacker.isPlayable() && attacker.getPlayer() != null) {
            final Party party = attacker.getPlayer().getParty();
            if (party != null && party.getMemberCount() > 2) {
                _penaltyMobsNotSpawned = false;
                for (int i = 0; i < 2; ++i) {
                    final MonsterInstance npc = new MonsterInstance(IdFactory.getInstance().getNextId(), NpcTemplateHolder.getInstance().getTemplate(IsleOfPrayerFighter.PENALTY_MOBS[Rnd.get(IsleOfPrayerFighter.PENALTY_MOBS.length)]));
                    npc.setSpawnedLoc(((MonsterInstance) actor).getMinionPosition());
                    npc.setReflection(actor.getReflection());
                    npc.setCurrentHpMp((double) npc.getMaxHp(), (double) npc.getMaxMp(), true);
                    npc.spawnMe(npc.getSpawnedLoc());
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(1, 100));
                }
            }
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        _penaltyMobsNotSpawned = true;
        if (killer != null) {
            final Player player = killer.getPlayer();
            if (player != null) {
                final NpcInstance actor = getActor();
                switch (actor.getNpcId()) {
                    case 22259: {
                        if (Rnd.chance(26)) {
                            actor.dropItem(player, 9593, 1L);
                            break;
                        }
                        break;
                    }
                    case 22263: {
                        if (Rnd.chance(14)) {
                            actor.dropItem(player, 9594, 1L);
                            break;
                        }
                        break;
                    }
                }
            }
        }
        super.onEvtDead(killer);
    }
}
