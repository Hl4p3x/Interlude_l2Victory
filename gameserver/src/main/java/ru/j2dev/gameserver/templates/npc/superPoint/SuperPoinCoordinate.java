package ru.j2dev.gameserver.templates.npc.superPoint;


import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.utils.Location;

@SuppressWarnings("serial")
public class SuperPoinCoordinate extends Location {
    private int msgId;
    private int msgRadius;
    private ChatType msgChatType;

    private int delay;

    private int socialId;

    public SuperPoinCoordinate(int locX, int locY, int locZ) {
        super(locX, locY, locZ);
    }

    public int getMsgId() {
        return msgId;
    }

    public void setMsgId(int id) {
        msgId = id;
    }

    public int getDelayInSec() {
        return delay;
    }

    public void setDelayInSec(int sec) {
        delay = sec;
    }

    public int getMsgRadius() {
        return msgRadius;
    }

    public void setMsgRadius(int radius) {
        msgRadius = radius;
    }

    public ChatType getMsgChatType() {
        return msgChatType;
    }

    public void setMsgChatType(ChatType type) {
        msgChatType = type;
    }

    public int getSocialId() {
        return socialId;
    }

    public void setSocialId(int id) {
        socialId = id;
    }
}