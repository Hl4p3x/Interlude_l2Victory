package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.AgathionInstance;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class Agathion extends Skill {
    public Agathion(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        final Player player = activeChar.getPlayer();
        if (player == null) {
            return false;
        }

        if (player.isOlyParticipant()) {
            player.sendPacket(Msg.THIS_ITEM_IS_NOT_AVAILABLE_FOR_THE_OLYMPIAD_EVENT);
            return false;
        }
        if (player.isCursedWeaponEquipped()) {
            player.sendPacket(Msg.YOU_MAY_NOT_USE_MULTIPLE_PETS_OR_SERVITORS_AT_THE_SAME_TIME);
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature caster, final List<Creature> targets) {
        final Player activeChar = caster.getPlayer();
        final NpcTemplate summonTemplate = NpcTemplateHolder.getInstance().getTemplate(getNpcId());
        final AgathionInstance summon = new AgathionInstance(IdFactory.getInstance().getNextId(), summonTemplate);

        summon.setTargetable(false);
        summon.setShowName(false);
        summon.setHeading(activeChar.getHeading());
        summon.setReflection(activeChar.getReflection());
        summon.spawnMe(Location.findAroundPosition(activeChar, 50, 70));
        summon.setRunning();
        summon.setFollowMode(true);
        summon.setRemainingTime(convertedTime());
        summon.setBuffInterval(get_buffinterval());
        summon.set_restoreCpId(getRestoreCP());
        summon.set_restoreHpId(getRestoreHP());
        summon.set_restoreMpId(getRestoreMP());
        summon.set_restoreCpPercent(getRestoreCPPercent());
        summon.set_restoreMpPercent(getRestoreMPPercent());
        summon.set_restoreHpPercent(getRestoreHPercent());
        summon.setOwner(activeChar);
        summon.addUnsummon(activeChar, getUnsumonSkill());
    }

    private int convertedTime()
    {
        int minute = 60000;
        return minute * getRemainingTime();
    }
}
