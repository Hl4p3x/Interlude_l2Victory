package ai.residences.clanhall;

import ai.residences.SiegeGuardFighter;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.events.impl.ClanHallSiegeEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SpawnExObject;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.PositionUtils;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class AlfredVonHellmann extends SiegeGuardFighter {
    public static final Skill DAMAGE_SKILL = SkillTable.getInstance().getInfo(5000, 1);
    public static final Skill DRAIN_SKILL = SkillTable.getInstance().getInfo(5001, 1);
    private static final Zone ZONE_3 = ReflectionUtils.getZone("lidia_zone3");

    public AlfredVonHellmann(final NpcInstance actor) {
        super(actor);
    }

    @Override
    public void onEvtSpawn() {
        super.onEvtSpawn();
        AlfredVonHellmann.ZONE_3.setActive(true);
        Functions.npcShoutCustomMessage(getActor(), "clanhall.AlfredVonHellmann.HEH_HEH_I_SEE_THAT_THE_FEAST_HAS_BEGAN_BE_WARY_THE_CURSE_OF_THE_HELLMANN_FAMILY_HAS_POISONED_THIS_LAND");
    }

    @Override
    public void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        super.onEvtDead(killer);
        AlfredVonHellmann.ZONE_3.setActive(false);
        Functions.npcShoutCustomMessage(actor, "clanhall.AlfredVonHellmann.AARGH_IF_I_DIE_THEN_THE_MAGIC_FORCE_FIELD_OF_BLOOD_WILL");
        final ClanHallSiegeEvent siegeEvent = actor.getEvent(ClanHallSiegeEvent.class);
        if (siegeEvent == null) {
            return;
        }
        final SpawnExObject spawnExObject = siegeEvent.getFirstObject("boss");
        final NpcInstance lidiaNpc = spawnExObject.getFirstSpawned();
        if (lidiaNpc.getCurrentHpRatio() == 1.0) {
            lidiaNpc.setCurrentHp((double) (lidiaNpc.getMaxHp() / 2), true);
        }
    }

    @Override
    public void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        super.onEvtAttacked(attacker, damage);
        if (PositionUtils.calculateDistance(attacker, actor, false) > 300.0 && Rnd.chance(0.13)) {
            addTaskCast(attacker, AlfredVonHellmann.DRAIN_SKILL);
        }
        final Creature target = actor.getAggroList().getMostHated();
        if (target == attacker && Rnd.chance(0.3)) {
            addTaskCast(attacker, AlfredVonHellmann.DAMAGE_SKILL);
        }
    }
}
