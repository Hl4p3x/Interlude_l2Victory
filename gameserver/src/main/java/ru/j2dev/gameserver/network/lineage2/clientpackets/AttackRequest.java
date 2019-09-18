package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;

public class AttackRequest extends L2GameClientPacket {
    private int _objectId;
    private int _originX;
    private int _originY;
    private int _originZ;
    private int _attackId;

    @Override
    protected void readImpl() {
        _objectId = readD();
        _originX = readD();
        _originY = readD();
        _originZ = readD();
        _attackId = readC();
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        final Player activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.setActive();
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if (!activeChar.getPlayerAccess().CanAttack) {
            activeChar.sendActionFailed();
            return;
        }
        final GameObject target = activeChar.getVisibleObject(_objectId);
        if (target == null) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.getAggressionTarget() != null && activeChar.getAggressionTarget() != target && !activeChar.getAggressionTarget().isDead()) {
            activeChar.sendActionFailed();
            return;
        }
        if (target.isPlayer() && (activeChar.isInBoat() || target.isInBoat())) {
            activeChar.sendPacket(Msg.THIS_IS_NOT_ALLOWED_WHILE_USING_A_FERRY, ActionFail.STATIC);
            return;
        }
        if (target.isPlayable()) {
            if (activeChar.isInZonePeace()) {
                activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_IN_A_PEACEFUL_ZONE, ActionFail.STATIC);
                return;
            }
            if (((Playable) target).isInZonePeace()) {
                activeChar.sendPacket(Msg.YOU_MAY_NOT_ATTACK_THIS_TARGET_IN_A_PEACEFUL_ZONE, ActionFail.STATIC);
                return;
            }
        }
        final long now = System.currentTimeMillis();
        if (now - client.getLastIncomePacketTimeStamp(AttackRequest.class) < Config.ATTACK_PACKET_DELAY) {
            activeChar.sendActionFailed();
            return;
        }
        client.setLastIncomePacketTimeStamp(AttackRequest.class, now);
        if (activeChar.getTarget() != target) {
            target.onAction(activeChar, _attackId == 1);
            return;
        }
        if (target.getObjectId() != activeChar.getObjectId() && !activeChar.isInStoreMode() && !activeChar.isProcessingRequest()) {
            target.onForcedAttack(activeChar, _attackId == 1);
        }
    }
}
