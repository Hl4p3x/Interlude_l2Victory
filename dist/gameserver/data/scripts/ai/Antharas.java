package ai;

import bosses.AntharasManager;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Antharas extends DefaultAI {
    private static long _minionsSpawnDelay;

    final Skill s_fear;
    final Skill s_fear2;
    final Skill s_curse;
    final Skill s_paralyze;
    final Skill s_shock;
    final Skill s_shock2;
    final Skill s_antharas_ordinary_attack;
    final Skill s_antharas_ordinary_attack2;
    final Skill s_meteor;
    final Skill s_breath;
    final Skill s_regen1;
    final Skill s_regen2;
    final Skill s_regen3;
    private final List<NpcInstance> minions;
    private int _hpStage;

    public Antharas(final NpcInstance actor) {
        super(actor);
        s_fear = getSkillInfo(4108, 1);
        s_fear2 = getSkillInfo(5092, 1);
        s_curse = getSkillInfo(4109, 1);
        s_paralyze = getSkillInfo(4111, 1);
        s_shock = getSkillInfo(4106, 1);
        s_shock2 = getSkillInfo(4107, 1);
        s_antharas_ordinary_attack = getSkillInfo(4112, 1);
        s_antharas_ordinary_attack2 = getSkillInfo(4113, 1);
        s_meteor = getSkillInfo(5093, 1);
        s_breath = getSkillInfo(4110, 1);
        s_regen1 = getSkillInfo(4239, 1);
        s_regen2 = getSkillInfo(4240, 1);
        s_regen3 = getSkillInfo(4241, 1);
        _hpStage = 0;
        minions = new ArrayList<>();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        AntharasManager.setLastAttackTime();
        for (final Playable p : AntharasManager.getZone().getInsidePlayables()) {
            notifyEvent(CtrlEvent.EVT_AGGRESSION, p, 1);
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtSpawn() {
        super.onEvtSpawn();
        _minionsSpawnDelay = System.currentTimeMillis() + 120000L;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

    @Override
    protected boolean createNewTask() {
        clearTasks();
        Creature target;
        if ((target = prepareTarget()) == null) {
            return false;
        }
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return false;
        }
        final double distance = actor.getDistance(target);
        final double chp = actor.getCurrentHpPercents();
        if (_hpStage == 0) {
            actor.altOnMagicUseTimer(actor, s_regen1);
            _hpStage = 1;
        } else if (chp < 75.0 && _hpStage == 1) {
            actor.altOnMagicUseTimer(actor, s_regen2);
            _hpStage = 2;
        } else if (chp < 50.0 && _hpStage == 2) {
            actor.altOnMagicUseTimer(actor, s_regen3);
            _hpStage = 3;
        } else if (chp < 30.0 && _hpStage == 3) {
            actor.altOnMagicUseTimer(actor, s_regen3);
            _hpStage = 4;
        }
        if (_minionsSpawnDelay < System.currentTimeMillis() && getAliveMinionsCount() < 30 && Rnd.chance(5)) {
            final NpcInstance minion = Functions.spawn(Location.findPointToStay(actor.getLoc(), 400, 700, actor.getGeoIndex()), Rnd.chance(50) ? 29070 : 29069);
            minions.add(minion);
            AntharasManager.addSpawnedMinion(minion);
        }
        if (Rnd.chance(50)) {
            return chooseTaskAndTargets(Rnd.chance(50) ? s_antharas_ordinary_attack : s_antharas_ordinary_attack2, target, distance);
        }
        final Map<Skill, Integer> d_skill = new HashMap<>();
        switch (_hpStage) {
            case 1: {
                addDesiredSkill(d_skill, target, distance, s_curse);
                addDesiredSkill(d_skill, target, distance, s_paralyze);
                addDesiredSkill(d_skill, target, distance, s_meteor);
                break;
            }
            case 2: {
                addDesiredSkill(d_skill, target, distance, s_curse);
                addDesiredSkill(d_skill, target, distance, s_paralyze);
                addDesiredSkill(d_skill, target, distance, s_meteor);
                addDesiredSkill(d_skill, target, distance, s_fear2);
                break;
            }
            case 3: {
                addDesiredSkill(d_skill, target, distance, s_curse);
                addDesiredSkill(d_skill, target, distance, s_paralyze);
                addDesiredSkill(d_skill, target, distance, s_meteor);
                addDesiredSkill(d_skill, target, distance, s_fear2);
                addDesiredSkill(d_skill, target, distance, s_shock2);
                addDesiredSkill(d_skill, target, distance, s_breath);
                break;
            }
            case 4: {
                addDesiredSkill(d_skill, target, distance, s_curse);
                addDesiredSkill(d_skill, target, distance, s_paralyze);
                addDesiredSkill(d_skill, target, distance, s_meteor);
                addDesiredSkill(d_skill, target, distance, s_fear2);
                addDesiredSkill(d_skill, target, distance, s_shock2);
                addDesiredSkill(d_skill, target, distance, s_fear);
                addDesiredSkill(d_skill, target, distance, s_shock);
                addDesiredSkill(d_skill, target, distance, s_breath);
                break;
            }
        }
        final Skill r_skill = selectTopSkill(d_skill);
        if (r_skill != null && !r_skill.isOffensive()) {
            target = actor;
        }
        return chooseTaskAndTargets(r_skill, target, distance);
    }

    private int getAliveMinionsCount() {
        int i = 0;
        for (final NpcInstance n : minions) {
            if (n != null && !n.isDead()) {
                ++i;
            }
        }
        return i;
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        if (minions != null && !minions.isEmpty()) {
            for (final NpcInstance n : minions) {
                n.deleteMe();
            }
        }
        super.onEvtDead(killer);
    }
}
