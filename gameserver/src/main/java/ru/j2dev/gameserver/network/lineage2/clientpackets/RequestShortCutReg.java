package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShortCutRegister;

public class RequestShortCutReg extends L2GameClientPacket {
    private int _type;
    private int _id;
    private int _slot;
    private int _page;
    private int _lvl;
    private int _characterType;

    @Override
    protected void readImpl() {
        _type = readD();
        final int slot = readD();
        _id = readD();
        _characterType = readD();
        _slot = slot % 12;
        _page = slot / 12;
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_page < 0 || _page > 11) {
            activeChar.sendActionFailed();
            return;
        }
        switch (_type) {
            case 2: {
                _lvl = activeChar.getSkillLevel(_id);
                break;
            }
            default: {
                _lvl = 0;
                break;
            }
        }
        final ShortCut shortCut = new ShortCut(_slot, _page, _type, _id, _lvl, _characterType);
        activeChar.sendPacket(new ShortCutRegister(activeChar, shortCut));
        activeChar.registerShortCut(shortCut);
    }
}
