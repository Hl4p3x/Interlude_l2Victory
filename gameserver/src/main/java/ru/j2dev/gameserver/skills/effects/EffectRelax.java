package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Env;

public class EffectRelax extends Effect {
    private boolean _isWereSitting;

    public EffectRelax(final Env env, final EffectTemplate template) {
        super(env, template);
    }

    @Override
    public boolean checkCondition() {
        final Player player = _effected.getPlayer();
        if (player == null) {
            return false;
        }
        if (player.isMounted()) {
            player.sendPacket(new SystemMessage(113).addSkillName(_skill.getId(), _skill.getLevel()));
            return false;
        }
        return super.checkCondition();
    }

    @Override
    public void onStart() {
        super.onStart();
        final Player player = _effected.getPlayer();
        if (player.isMoving()) {
            player.stopMove();
        }
        _isWereSitting = player.isSitting();
        player.sitDown(null);
    }

    @Override
    public void onExit() {
        super.onExit();
        final Skill skill = getSkill();
        if (skill != null) {
            for (final Effect other : _effected.getEffectList().getEffectsBySkill(skill)) {
                if (other != this) {
                    other.exit();
                }
            }
        }
        if (!_isWereSitting) {
            _effected.getPlayer().standUp();
        }
    }

    @Override
    public boolean onActionTime() {
        final Player player = _effected.getPlayer();
        if (player.isAlikeDead()) {
            return false;
        }
        if (!player.isSitting()) {
            return false;
        }
        if (player.isCurrentHpFull() && getSkill().isToggle()) {
            getEffected().sendPacket(Msg.HP_WAS_FULLY_RECOVERED_AND_SKILL_WAS_REMOVED);
            return false;
        }
        final double manaDam = calc();
        if (manaDam > _effected.getCurrentMp() && getSkill().isToggle()) {
            player.sendPacket(Msg.NOT_ENOUGH_MP, new SystemMessage(749).addSkillName(getSkill().getId(), getSkill().getDisplayLevel()));
            return false;
        }
        _effected.reduceCurrentMp(manaDam, null);
        return true;
    }
}
