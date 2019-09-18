package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Transformation extends Skill {
    public final boolean useSummon;
    public final boolean isDisguise;
    public final String transformationName;

    public Transformation(final StatsSet set) {
        super(set);
        useSummon = set.getBool("useSummon", false);
        isDisguise = set.getBool("isDisguise", false);
        transformationName = set.getString("transformationName", null);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        final Player player = target.getPlayer();
        if (player == null || player.getActiveWeaponFlagAttachment() != null) {
            return false;
        }
        if (player.getTransformation() != 0 && getId() != 619) {
            activeChar.sendPacket(Msg.YOU_ALREADY_POLYMORPHED_AND_CANNOT_POLYMORPH_AGAIN);
            return false;
        }
        if ((getId() == 840 || getId() == 841 || getId() == 842) && (player.getX() > -166168 || player.getZ() <= 0 || player.getZ() >= 6000 || player.getPet() != null || player.getReflection() != ReflectionManager.DEFAULT)) {
            activeChar.sendPacket(new SystemMessage(113).addSkillName(_id, _level));
            return false;
        }
        if (player.isInFlyingTransform() && getId() == 619 && Math.abs(player.getZ() - player.getLoc().correctGeoZ().z) > 333) {
            activeChar.sendPacket(new SystemMessage(113).addSkillName(_id, _level));
            return false;
        }
        if (player.isInWater()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_INTO_THE_DESIRED_FORM_IN_WATER);
            return false;
        }
        if (player.isRiding() || player.getMountType() == 2) {
            activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_PET);
            return false;
        }
        if (player.getEffectList().getEffectsBySkillId(1411) != null) {
            activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHILE_UNDER_THE_EFFECT_OF_A_SPECIAL_SKILL);
            return false;
        }
        if (player.isInBoat()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHILE_RIDING_A_BOAT);
            return false;
        }
        if (useSummon) {
            if (player.getPet() == null || !player.getPet().isSummon() || player.getPet().isDead()) {
                activeChar.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
                return false;
            }
        } else if (player.getPet() != null && player.getPet().isPet() && getId() != 619 && !isBaseTransformation()) {
            activeChar.sendPacket(Msg.YOU_CANNOT_POLYMORPH_WHEN_YOU_HAVE_SUMMONED_A_SERVITOR_PET);
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (useSummon) {
            if (activeChar.getPet() == null || !activeChar.getPet().isSummon() || activeChar.getPet().isDead()) {
                activeChar.sendPacket(Msg.PETS_AND_SERVITORS_ARE_NOT_AVAILABLE_AT_THIS_TIME);
                return;
            }
            activeChar.getPet().unSummon();
        }
        if (isSummonerTransformation() && activeChar.getPet() != null && activeChar.getPet().isSummon()) {
            activeChar.getPet().unSummon();
        }
        for (final Creature target : targets) {
            if (target != null && target.isPlayer()) {
                getEffects(activeChar, target, false, false);
            }
        }
        if (isSSPossible() && (!Config.SAVING_SPS || _skillType != SkillType.BUFF)) {
            activeChar.unChargeShots(isMagic());
        }
    }
}
