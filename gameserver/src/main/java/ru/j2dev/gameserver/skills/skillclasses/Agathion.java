package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.dao.SummonEffectsDAO;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.instances.AgathionInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.stats.funcs.FuncAdd;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class Agathion extends Skill {
    protected Agathion(StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature caster, final List<Creature> targets) {
        final Player activeChar = caster.getPlayer();

        Location loc = null;

        if (activeChar.getPet() != null || activeChar.isMounted()) {
            return;
        }
        final NpcTemplate summonTemplate = NpcTemplateHolder.getInstance().getTemplate(getNpcId());
        final AgathionInstance summon = new AgathionInstance(IdFactory.getInstance().getNextId(), summonTemplate);

        summon.setOwner(activeChar);
        summon.setHeading(activeChar.getHeading());
        summon.setReflection(activeChar.getReflection());
        summon.spawnMe((loc == null) ? Location.findAroundPosition(activeChar, 50, 70) : loc);
        summon.setRunning();

    }
}
