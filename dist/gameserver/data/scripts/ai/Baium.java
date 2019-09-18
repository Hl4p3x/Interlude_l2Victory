package ai;

import bosses.BaiumManager;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Earthquake;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;
import ru.j2dev.gameserver.utils.ReflectionUtils;

import java.util.HashMap;
import java.util.Map;

public class Baium extends DefaultAI {
    private final Skill baium_normal_attack;
    private final Skill energy_wave;
    private final Skill earth_quake;
    private final Skill thunderbolt;
    private final Skill group_hold;
    private boolean _firstTimeAttacked;
    private final Zone _zoneBroadCast = ReflectionUtils.getZone("[baium_no_restart]");

    public Baium(final NpcInstance actor) {
        super(actor);
        _firstTimeAttacked = true;
        final TIntObjectHashMap<Skill> skills = getActor().getTemplate().getSkills();
        baium_normal_attack = skills.get(4127);
        energy_wave = skills.get(4128);
        earth_quake = skills.get(4129);
        thunderbolt = skills.get(4130);
        group_hold = skills.get(4131);
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        BaiumManager.setLastAttackTime();
        startAITask();
        if (_firstTimeAttacked) {
            _firstTimeAttacked = false;
            final NpcInstance actor = getActor();
            if (attacker == null) {
                return;
            }
            if (attacker.isPlayer() && attacker.getPet() != null) {
                attacker.getPet().doDie(actor);
            } else if ((attacker.isSummon() || attacker.isPet()) && attacker.getPlayer() != null) {
                attacker.getPlayer().doDie(actor);
            }
            attacker.doDie(actor);
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected boolean createNewTask() {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return true;
        }
        if (!BaiumManager.getZone().checkIfInZone(actor)) {
            teleportHome();
            return false;
        }
        clearTasks();
        Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }
        if (!BaiumManager.getZone().checkIfInZone(target)) {
            actor.getAggroList().remove(target, false);
            return false;
        }
        final int s_energy_wave = 20;
        final int s_earth_quake = 20;
        final int s_group_hold = (actor.getCurrentHpPercents() > 50.0) ? 0 : 20;
        final int s_thunderbolt = (actor.getCurrentHpPercents() > 25.0) ? 0 : 20;
        Skill r_skill = null;
        if (actor.isMovementDisabled()) {
            r_skill = thunderbolt;
        } else if (!Rnd.chance(100 - s_thunderbolt - s_group_hold - s_energy_wave - s_earth_quake)) {
            final Map<Skill, Integer> d_skill = new HashMap<>();
            final double distance = actor.getDistance(target);
            addDesiredSkill(d_skill, target, distance, energy_wave);
            addDesiredSkill(d_skill, target, distance, earth_quake);
            if (s_group_hold > 0) {
                addDesiredSkill(d_skill, target, distance, group_hold);
            }
            if (s_thunderbolt > 0) {
                addDesiredSkill(d_skill, target, distance, thunderbolt);
            }
            r_skill = selectTopSkill(d_skill);
        }
        if (r_skill == null) {
            r_skill = baium_normal_attack;
        } else if (r_skill.getTargetType() == SkillTargetType.TARGET_SELF) {
            target = actor;
        }
        addTaskCast(target, r_skill);
        return true;
    }

    @Override
    protected boolean maybeMoveToHome() {
        final NpcInstance actor = getActor();
        if (actor != null && !BaiumManager.getZone().checkIfInZone(actor)) {
            teleportHome();
        }
        return false;
    }

    @Override
    protected void onEvtFinishCasting() {
        final NpcInstance actor = getActor();
        Skill skill = actor.getCastingSkill();
        if (skill == energy_wave || skill == earth_quake) {
            _zoneBroadCast.broadcastPacket(new Earthquake(actor.getLoc(), 40, 10), false);
            _zoneBroadCast.broadcastPacket(new PlaySound("BS02_A"), false);

        }
        super.onEvtFinishCasting();
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        _firstTimeAttacked = true;

        GameObjectsStorage.getNpcs(npcInstance -> npcInstance.getNpcId() == 29021).forEach(NpcInstance::deleteMe);

        NpcUtils.spawnSingle(31842, new Location(115017, 15549, 10090, 0), 300000);
        super.onEvtDead(killer);
    }
}
