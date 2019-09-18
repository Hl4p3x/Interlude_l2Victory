package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;

import java.util.Collection;

public class RequestBlock extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestBlock.class);
    private static final int BLOCK = 0;
    private static final int UNBLOCK = 1;
    private static final int BLOCKLIST = 2;
    private static final int ALLBLOCK = 3;
    private static final int ALLUNBLOCK = 4;

    private Integer _type;
    private String targetName;

    public RequestBlock() {
        targetName = null;
    }

    @Override
    protected void readImpl() {
        _type = readD();
        if (_type == 0 || _type == 1) {
            targetName = readS(16);
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        switch (_type) {
            case 0: {
                activeChar.addToBlockList(targetName);
                break;
            }
            case 1: {
                activeChar.removeFromBlockList(targetName);
                break;
            }
            case 2: {
                final Collection<String> blockList = activeChar.getBlockList();
                if (blockList != null) {
                    activeChar.sendPacket(Msg._IGNORE_LIST_);
                    for (final String name : blockList) {
                        activeChar.sendMessage(name);
                    }
                    activeChar.sendPacket(Msg.__EQUALS__);
                    break;
                }
                break;
            }
            case 3: {
                activeChar.setBlockAll(true);
                activeChar.sendPacket(Msg.YOU_ARE_NOW_BLOCKING_EVERYTHING);
                activeChar.sendEtcStatusUpdate();
                break;
            }
            case 4: {
                activeChar.setBlockAll(false);
                activeChar.sendPacket(Msg.YOU_ARE_NO_LONGER_BLOCKING_EVERYTHING);
                activeChar.sendEtcStatusUpdate();
                break;
            }
            default: {
                LOGGER.info("Unknown 0x0a block type: " + _type);
                break;
            }
        }
    }
}
