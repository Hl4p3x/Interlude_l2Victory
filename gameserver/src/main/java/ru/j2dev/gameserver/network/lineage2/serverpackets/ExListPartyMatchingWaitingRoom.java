package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.manager.MatchingRoomManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Reflection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExListPartyMatchingWaitingRoom extends L2GameServerPacket {
    private final int _fullSize;
    private List<PartyMatchingWaitingInfo> _waitingList;

    public ExListPartyMatchingWaitingRoom(final Player searcher, final int minLevel, final int maxLevel, final int page, final int[] classes) {
        _waitingList = Collections.emptyList();
        final int first = (page - 1) * 64;
        final int firstNot = page * 64;
        int i = 0;
        final List<Player> temp = MatchingRoomManager.getInstance().getWaitingList(minLevel, maxLevel, classes);
        _fullSize = temp.size();
        _waitingList = new ArrayList<>(_fullSize);
        for (final Player pc : temp) {
            if (i >= first) {
                if (i >= firstNot) {
                    continue;
                }
                _waitingList.add(new PartyMatchingWaitingInfo(pc));
                ++i;
            }
        }
    }

    @Override
    protected void writeImpl() {
        writeEx(0x36);
        writeD(_fullSize);
        writeD(_waitingList.size());
        _waitingList.forEach(waiting_info -> {
            writeS(waiting_info.name);
            writeD(waiting_info.classId);
            writeD(waiting_info.level);
        });
    }

    static class PartyMatchingWaitingInfo {
        public final int classId;
        public final int level;
        public final int currentInstance;
        public final String name;
        public final int[] instanceReuses;

        public PartyMatchingWaitingInfo(final Player member) {
            name = member.getName();
            classId = member.getClassId().getId();
            level = member.getLevel();
            final Reflection ref = member.getReflection();
            currentInstance = ((ref == null) ? 0 : ref.getInstancedZoneId());
            instanceReuses = ArrayUtils.toArray(member.getInstanceReuses().keySet());
        }
    }
}
