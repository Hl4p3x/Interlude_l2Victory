package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.quest.QuestNpcLogInfo;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ExQuestNpcLogList extends L2GameServerPacket {
    private final int _questId;
    private List<int[]> _logList;

    public ExQuestNpcLogList(final QuestState state) {
        _logList = Collections.emptyList();
        _questId = state.getQuest().getQuestIntId();
        final int cond = state.getCond();
        final List<QuestNpcLogInfo> vars = state.getQuest().getNpcLogList(cond);
        if (vars == null) {
            return;
        }
        _logList = new ArrayList<>(vars.size());
        vars.stream().map(entry -> new int[]{entry.getNpcIds()[0] + 1000000, state.getInt(entry.getVarName())}).forEach(i -> _logList.add(i));
    }

    @Override
    protected void writeImpl() {
        writeEx(0xc5);
        writeD(_questId);
        writeC(_logList.size());
        _logList.forEach(values -> {
            writeD(values[0]);
            writeC(0);
            writeD(values[1]);
        });
    }
}
