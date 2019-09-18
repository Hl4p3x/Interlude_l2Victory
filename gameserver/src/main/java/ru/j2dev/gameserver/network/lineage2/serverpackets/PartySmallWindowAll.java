package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;

import java.util.ArrayList;
import java.util.List;

public class PartySmallWindowAll extends L2GameServerPacket {
    private final int leaderId;
    private final int loot;
    private final List<PartySmallWindowMemberInfo> members;

    public PartySmallWindowAll(final Party party, final Player exclude) {
        members = new ArrayList<>();
        leaderId = party.getPartyLeader().getObjectId();
        loot = party.getLootDistribution();
        party.getPartyMembers().stream().filter(member -> member != exclude).map(PartySmallWindowMemberInfo::new).forEach(members::add);
    }

    @Override
    protected final void writeImpl() {
        writeC(0x4e);
        writeD(leaderId);
        writeD(loot);
        writeD(members.size());
        members.forEach(member -> {
            writeD(member._id);
            writeS(member._name);
            writeD(member.curCp);
            writeD(member.maxCp);
            writeD(member.curHp);
            writeD(member.maxHp);
            writeD(member.curMp);
            writeD(member.maxMp);
            writeD(member.level);
            writeD(member.class_id);
            writeD(0);
            writeD(member.race_id);
        });
    }

    public static class PartySmallWindowMemberInfo {
        public final String _name;
        public final int _id;
        public final int curCp;
        public final int maxCp;
        public final int curHp;
        public final int maxHp;
        public final int curMp;
        public final int maxMp;
        public final int level;
        public final int class_id;
        public final int race_id;

        public PartySmallWindowMemberInfo(final Player member) {
            _name = member.getName();
            _id = member.getObjectId();
            curCp = (int) member.getCurrentCp();
            maxCp = member.getMaxCp();
            curHp = (int) member.getCurrentHp();
            maxHp = member.getMaxHp();
            curMp = (int) member.getCurrentMp();
            maxMp = member.getMaxMp();
            level = member.getLevel();
            class_id = member.getClassId().getId();
            race_id = member.getRace().ordinal();
        }
    }
}
