package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.Macro;
import ru.j2dev.gameserver.model.actor.instances.player.Macro.L2MacroCmd;

public class RequestMakeMacro extends L2GameClientPacket {
    private Macro _macro;

    @Override
    protected void readImpl() {
        final int _id = readD();
        final String _name = readS(32);
        final String _desc = readS(64);
        final String _acronym = readS(4);
        final int _icon = readC();
        int _count = readC();
        if (_count > 12) {
            _count = 12;
        }
        final L2MacroCmd[] commands = new L2MacroCmd[_count];
        for (int i = 0; i < _count; ++i) {
            final int entry = readC();
            final int type = readC();
            final int d1 = readD();
            final int d2 = readC();
            final String command = readS().replace(";", "").replace(",", "");
            commands[i] = new L2MacroCmd(entry, type, d1, d2, command);
        }
        _macro = new Macro(_id, _icon, _name, _desc, _acronym, commands);
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (activeChar.getMacroses().getAllMacroses().size() > 48) {
            activeChar.sendPacket(Msg.YOU_MAY_CREATE_UP_TO_48_MACROS);
            return;
        }
        if (_macro.name.length() == 0) {
            activeChar.sendPacket(Msg.ENTER_THE_NAME_OF_THE_MACRO);
            return;
        }
        if (_macro.descr.length() > 32) {
            activeChar.sendPacket(Msg.MACRO_DESCRIPTIONS_MAY_CONTAIN_UP_TO_32_CHARACTERS);
            return;
        }
        activeChar.registerMacro(_macro);
    }
}
