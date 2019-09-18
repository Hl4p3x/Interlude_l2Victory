package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.attachment.FlagItemAttachment;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.SkillTable;

public class RequestMagicSkillUse extends L2GameClientPacket {
    private Integer _magicId;
    private boolean _ctrlPressed;
    private boolean _shiftPressed;

    @Override
    protected void readImpl() {
        _magicId = readD();
        _ctrlPressed = (readD() != 0);
        _shiftPressed = (readC() != 0);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.setActive();
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        final Skill skill = SkillTable.getInstance().getInfo(_magicId, activeChar.getSkillLevel(_magicId));
        if (skill != null) {
            if (!skill.isActive() && !skill.isToggle()) {
                return;
            }
            final FlagItemAttachment attachment = activeChar.getActiveWeaponFlagAttachment();
            if (attachment != null && !attachment.canCast(activeChar, skill)) {
                activeChar.sendActionFailed();
                return;
            }
            if (activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skill)) {
                return;
            }
            if (skill.isToggle() && activeChar.getEffectList().getEffectsBySkill(skill) != null) {
                activeChar.getEffectList().stopEffect(skill.getId());
                activeChar.sendPacket(new SystemMessage(335).addSkillName(skill.getId(), skill.getLevel()));
                activeChar.sendActionFailed();
                return;
            }
            final Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());
            activeChar.setGroundSkillLoc(null);
            activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
        } else {
            activeChar.sendActionFailed();
        }
    }
}
