package ru.j2dev.gameserver.templates.moveroute;

import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.utils.Location;

public class MoveNode extends Location {
    private static final long serialVersionUID = 8291528118019681063L;
    private final String _msgAddr;
    private final ChatType _chatType;
    private final long _delay;
    private final int _socialId;

    public MoveNode(final int x, final int y, final int z, final String msgAddr, final int socialId, final long delay, final ChatType chatType) {
        super(x, y, z);
        _msgAddr = msgAddr;
        _socialId = socialId;
        _delay = delay;
        _chatType = chatType;
    }

    public String getNpcMsgAddress() {
        return _msgAddr;
    }

    public long getDelay() {
        return _delay;
    }

    public int getSocialId() {
        return _socialId;
    }

    public ChatType getChatType() {
        return _chatType;
    }
}
