package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharMoveToLocation;
import ru.j2dev.gameserver.utils.Location;

public class MoveBackwardToLocation extends L2GameClientPacket {
    private final Location _targetLoc;
    private final Location _originLoc;
    private int _moveMovement;

    public MoveBackwardToLocation() {
        _targetLoc = new Location();
        _originLoc = new Location();
    }

    @Override
    protected void readImpl() {
        _targetLoc.x = readD();
        _targetLoc.y = readD();
        _targetLoc.z = readD();
        _originLoc.x = readD();
        _originLoc.y = readD();
        _originLoc.z = readD();
        if (_buf.hasRemaining()) {
            _moveMovement = readD();
        }
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        if (client == null) {
            return;
        }
        final Player activeChar = client.getActiveChar();
        if (activeChar == null) {
            return;
        }
        activeChar.setActive();
        if (activeChar.isTeleporting()) {
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.isFrozen()) {
            activeChar.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
            return;
        }
        if (activeChar.isOlyObserver()) {
            if (activeChar.getOlyObservingStadium().getObservingLoc().distance(_targetLoc) < 8192.0) {
                activeChar.sendPacket(new CharMoveToLocation(activeChar.getObjectId(), _originLoc, _targetLoc));
            } else {
                activeChar.sendActionFailed();
            }
            return;
        }
        if (activeChar.isOutOfControl()) {
            activeChar.sendActionFailed();
            return;
        }
        if (Config.ALT_ALLOW_DELAY_NPC_TALK && !activeChar.canMoveAfterInteraction()) {
            activeChar.sendMessage(new CustomMessage("YOU_CANNOT_MOVE_WHILE_SPEAKING_TO_AN_NPC__ONE_MOMENT_PLEASE", activeChar));
            activeChar.sendActionFailed();
            return;
        }
        if (activeChar.getTeleMode() > 0) {
            if (activeChar.getTeleMode() == 1) {
                activeChar.setTeleMode(0);
            }
            activeChar.sendActionFailed();
            activeChar.teleToLocation(_targetLoc);
            return;
        }
        if (activeChar.isInFlyingTransform()) {
            _targetLoc.z = Math.min(5950, Math.max(50, _targetLoc.z));
        }
        activeChar.moveBackwardToLocationForPacket(_targetLoc, _moveMovement != 0 && !activeChar.getVarB("no_pf"));
    }
}
