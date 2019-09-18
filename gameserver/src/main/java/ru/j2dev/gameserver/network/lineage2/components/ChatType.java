package ru.j2dev.gameserver.network.lineage2.components;

public enum ChatType {
    ALL,
    SHOUT,
    TELL,
    PARTY,
    CLAN,
    GM,
    PETITION_PLAYER,
    PETITION_GM,
    TRADE,
    ALLIANCE,
    ANNOUNCEMENT,
    SYSTEM_MESSAGE,
    L2FRIEND,
    MSNCHAT,
    PARTY_ROOM,
    COMMANDCHANNEL_ALL,
    COMMANDCHANNEL_COMMANDER,
    HERO_VOICE,
    CRITICAL_ANNOUNCE,
    SCREEN_ANNOUNCE,
    BATTLEFIELD,
    MPCC_ROOM,
    NPC_NORMAL;

    public static final ChatType[] VALUES = values();

}
