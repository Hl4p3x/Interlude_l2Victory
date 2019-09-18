package ru.j2dev.gameserver.model.entity.events.actions;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.events.EventAction;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.SysString;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;

public class SayAction implements EventAction {
    private final int _range;
    private final ChatType _chatType;
    private String _how;
    private String _text;
    private SysString _sysString;
    private SystemMsg _systemMsg;

    protected SayAction(final int range, final ChatType type) {
        _range = range;
        _chatType = type;
    }

    public SayAction(final int range, final ChatType type, final SysString sysString, final SystemMsg systemMsg) {
        this(range, type);
        _sysString = sysString;
        _systemMsg = systemMsg;
    }

    public SayAction(final int range, final ChatType type, final String how, final String string) {
        this(range, type);
        _text = string;
        _how = how;
    }

    @Override
    public void call(final GlobalEvent event) {
        event.broadcastPlayers(_range).forEach(this::packet);
    }

    private void packet(final Player player) {
        if (player == null) {
            return;
        }
        L2GameServerPacket packet;
        if (_sysString != null) {
            packet = new Say2(0, _chatType, _sysString, _systemMsg);
        } else {

            //TODO check this SHIT!!! and replace to FString
            packet = new Say2(0, _chatType, _how, _text);
        }
        player.sendPacket(packet);
    }
}
