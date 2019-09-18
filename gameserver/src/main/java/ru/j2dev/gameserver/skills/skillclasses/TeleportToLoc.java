package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by JunkyFunky
 * on 04.03.2018 14:14
 * group j2dev
 */
public class TeleportToLoc extends Skill {
    private final List<Location> _teleportsLoc;

    public TeleportToLoc(final StatsSet set) {
        super(set);
        _teleportsLoc = new ArrayList<>();
        for (String loc : set.getString("teleports", null).split(";")) {
            _teleportsLoc.add(Location.parseLoc(loc));
        }
    }

    @Override
    public void useSkill(Creature activeChar, List<Creature> targets) {
        for (final Creature target : targets) {
            if (target != null && !target.isDead()) {
                getEffects(activeChar, target, getActivateRate() > 0, false);
                target.abortAttack(true, true);
                target.abortCast(true, true);
                target.stopMove();
                Location location = Rnd.get(_teleportsLoc);
                target.teleToLocation(location);
                target.setLoc(location);
                target.validateLocation(1);
            }
        }
    }
}
