package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.actor.instances.player.Macro;
import ru.j2dev.gameserver.model.actor.instances.player.Macro.L2MacroCmd;

import java.util.stream.IntStream;

public class SendMacroList extends L2GameServerPacket {
    private final int _rev;
    private final int _count;
    private final Macro _macro;

    public SendMacroList(final int rev, final int count, final Macro macro) {
        _rev = rev;
        _count = count;
        _macro = macro;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xe7);
        writeD(_rev);
        writeC(0);
        writeC(_count);
        writeC((_macro != null) ? 1 : 0);
        if (_macro != null) {
            writeD(_macro.id);
            writeS(_macro.name);
            writeS(_macro.descr);
            writeS(_macro.acronym);
            writeC(_macro.icon);
            writeC(_macro.commands.length);
            IntStream.range(0, _macro.commands.length).forEach(i -> {
                final L2MacroCmd cmd = _macro.commands[i];
                writeC(i + 1);
                writeC(cmd.type);
                writeD(cmd.d1);
                writeC(cmd.d2);
                writeS(cmd.cmd);
            });
        }
    }
}
