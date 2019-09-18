package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Fishing;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.WeaponTemplate;

import java.util.List;

public class ReelingPumping extends Skill {
    public ReelingPumping(final StatsSet set) {
        super(set);
    }

    @Override
    public boolean checkCondition(final Creature activeChar, final Creature target, final boolean forceUse, final boolean dontMove, final boolean first) {
        if (!((Player) activeChar).isFishing()) {
            activeChar.sendPacket((getSkillType() == SkillType.PUMPING) ? Msg.PUMPING_SKILL_IS_AVAILABLE_ONLY_WHILE_FISHING : Msg.REELING_SKILL_IS_AVAILABLE_ONLY_WHILE_FISHING);
            activeChar.sendActionFailed();
            return false;
        }
        return super.checkCondition(activeChar, target, forceUse, dontMove, first);
    }

    @Override
    public void useSkill(final Creature caster, final List<Creature> targets) {
        if (caster == null || !caster.isPlayer()) {
            return;
        }
        final Player player = caster.getPlayer();
        final Fishing fishing = player.getFishing();
        if (fishing == null || !fishing.isInCombat()) {
            return;
        }
        final WeaponTemplate weaponItem = player.getActiveWeaponItem();
        final int SS = player.getChargedFishShot() ? 2 : 1;
        int pen = 0;
        final double gradebonus = 1.0 + weaponItem.getCrystalType().ordinal() * 0.1;
        int dmg = (int) (getPower() * gradebonus * SS);
        if (player.getSkillLevel(1315) < getLevel() - 2) {
            player.sendPacket(Msg.SINCE_THE_SKILL_LEVEL_OF_REELING_PUMPING_IS_HIGHER_THAN_THE_LEVEL_OF_YOUR_FISHING_MASTERY_A_PENALTY_OF_S1_WILL_BE_APPLIED);
            pen = 50;
            final int penatlydmg = dmg -= pen;
        }
        if (SS == 2) {
            player.unChargeFishShot();
        }
        fishing.useFishingSkill(dmg, pen, getSkillType());
    }
}
