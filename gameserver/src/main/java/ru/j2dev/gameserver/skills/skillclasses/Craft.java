package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RecipeBookItemList;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Craft extends Skill {
    private final boolean _dwarven;

    public Craft(final StatsSet set) {
        super(set);
        _dwarven = set.getBool("isDwarven");
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        final Player p = (Player) activeChar;
        return !p.isInStoreMode() && !p.isProcessingRequest() && super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        activeChar.sendPacket(new RecipeBookItemList((Player) activeChar, _dwarven));
    }
}
