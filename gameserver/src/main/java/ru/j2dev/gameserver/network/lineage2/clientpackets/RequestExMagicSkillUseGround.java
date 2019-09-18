package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Location;

public class RequestExMagicSkillUseGround extends L2GameClientPacket {
    private final Location _loc;
    private int _skillId;
    private boolean _ctrlPressed;
    private boolean _shiftPressed;

    public RequestExMagicSkillUseGround() {
        _loc = new Location();
    }

    @Override
    protected void readImpl() {
        _loc.x = readD();
        _loc.y = readD();
        _loc.z = readD();
        _skillId = readD();
        _ctrlPressed = (readD() != 0);
        _shiftPressed = (readC() != 0);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        final Skill skill = SkillTable.getInstance().getInfo(_skillId, activeChar.getSkillLevel(_skillId));
        if (skill != null) {
            if (skill.getAddedSkills().length == 0) {
                return;
            }
            if (activeChar.getTransformation() != 0 && !activeChar.getAllSkills().contains(skill)) {
                return;
            }
            if (!activeChar.isInRange(_loc, skill.getCastRange())) {
                activeChar.sendPacket(Msg.YOUR_TARGET_IS_OUT_OF_RANGE);
                activeChar.sendActionFailed();
                return;
            }
            final Creature target = skill.getAimingTarget(activeChar, activeChar.getTarget());
            if (skill.checkCondition(activeChar, target, _ctrlPressed, _shiftPressed, true)) {
                activeChar.setGroundSkillLoc(_loc);
                activeChar.getAI().Cast(skill, target, _ctrlPressed, _shiftPressed);
            } else {
                activeChar.sendActionFailed();
            }
        } else {
            activeChar.sendActionFailed();
        }
    }
}
