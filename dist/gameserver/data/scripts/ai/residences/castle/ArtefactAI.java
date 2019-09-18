package ai.residences.castle;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.instances.NpcInstance;

public class ArtefactAI extends CharacterAI {
    public ArtefactAI(final NpcInstance actor) {
        super(actor);
    }

    @Override
    protected void onEvtAggression(final Creature attacker, final int aggro) {
        final Player player;
        final NpcInstance actor;
        if (attacker == null || (player = attacker.getPlayer()) == null || (actor = (NpcInstance) getActor()) == null) {
            return;
        }
        final SiegeEvent<?, ?> siegeEvent1 = (SiegeEvent<?, ?>) actor.getEvent(SiegeEvent.class);
        final SiegeEvent<?, ?> siegeEvent2 = (SiegeEvent<?, ?>) player.getEvent(SiegeEvent.class);
        final SiegeClanObject siegeClan = siegeEvent1.getSiegeClan("attackers", player.getClan());
        if (siegeEvent2 == null || (siegeEvent1 == siegeEvent2 && siegeClan != null)) {
            ThreadPoolManager.getInstance().schedule(new notifyGuard(player), 1000L);
        }
    }

    class notifyGuard extends RunnableImpl {
        private final HardReference<Player> _playerRef;

        public notifyGuard(final Player attacker) {
            _playerRef = attacker.getRef();
        }

        @Override
        public void runImpl() {
            final Player attacker = _playerRef.get();
            final NpcInstance actor;
            if (attacker == null || (actor = (NpcInstance) getActor()) == null) {
                return;
            }
            for (final NpcInstance npc : actor.getAroundNpc(1500, 200)) {
                if (npc.isSiegeGuard() && Rnd.chance(20)) {
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, 5000);
                }
            }
            if (attacker.getCastingSkill() != null && attacker.getCastingSkill().getTargetType() == SkillTargetType.TARGET_HOLY) {
                ThreadPoolManager.getInstance().schedule(this, 10000L);
            }
        }
    }
}
