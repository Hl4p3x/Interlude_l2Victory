package ai;

import npc.model.OrfenInstance;
import ru.j2dev.commons.text.PrintfFormat;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

public class Orfen extends Fighter {
    public static final PrintfFormat[] MsgOnRecall = {new PrintfFormat("%s. Stop kidding yourself about your own powerlessness!"), new PrintfFormat("%s. I'll make you feel what true fear is!"), new PrintfFormat("You're really stupid to have challenged me. %s! Get ready!"), new PrintfFormat("%s. Do you think that's going to work?!")};

    public final Skill[] _paralyze;

    public Orfen(final NpcInstance actor) {
        super(actor);
        _paralyze = getActor().getTemplate().getDebuffSkills();
    }

    @Override
    protected boolean thinkActive() {
        if (super.thinkActive()) {
            return true;
        }
        final OrfenInstance actor = getActor();
        if (actor.isTeleported() && actor.getCurrentHpPercents() > 95.0) {
            actor.setTeleported(false);
            return true;
        }
        return false;
    }

    @Override
    protected boolean createNewTask() {
        return defaultNewTask();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        super.onEvtAttacked(attacker, damage);
        final OrfenInstance actor = getActor();
        if (actor.isCastingNow()) {
            return;
        }
        final double distance = actor.getDistance(attacker);
        if (distance > 300.0 && distance < 1000.0 && _damSkills.length > 0 && Rnd.chance(10)) {
            Functions.npcSay(actor, Orfen.MsgOnRecall[Rnd.get(Orfen.MsgOnRecall.length - 1)].sprintf(attacker.getName()));
            teleToLocation(attacker, Location.findFrontPosition(actor, attacker, 0, 50));
            final Skill r_skill = _damSkills[Rnd.get(_damSkills.length)];
            if (canUseSkill(r_skill, attacker, -1.0)) {
                addTaskAttack(attacker, r_skill, 1000000);
            }
        } else if (_paralyze.length > 0 && Rnd.chance(20)) {
            final Skill r_skill = _paralyze[Rnd.get(_paralyze.length)];
            if (canUseSkill(r_skill, attacker, -1.0)) {
                addTaskAttack(attacker, r_skill, 1000000);
            }
        }
    }

    @Override
    protected void onEvtSeeSpell(final Skill skill, final Creature caster) {
        super.onEvtSeeSpell(skill, caster);
        final OrfenInstance actor = getActor();
        if (actor.isCastingNow()) {
            return;
        }
        final double distance = actor.getDistance(caster);
        if (_damSkills.length > 0 && skill.getEffectPoint() > 0 && distance < 1000.0 && Rnd.chance(20)) {
            Functions.npcSay(actor, Orfen.MsgOnRecall[Rnd.get(Orfen.MsgOnRecall.length)].sprintf(caster.getName()));
            teleToLocation(caster, Location.findFrontPosition(actor, caster, 0, 50));
            final Skill r_skill = _damSkills[Rnd.get(_damSkills.length)];
            if (canUseSkill(r_skill, caster, -1.0)) {
                addTaskAttack(caster, r_skill, 1000000);
            }
        }
    }

    @Override
    public OrfenInstance getActor() {
        return (OrfenInstance) super.getActor();
    }

    private void teleToLocation(final Creature attacker, final Location loc) {
        attacker.teleToLocation(loc);
    }
}
