package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Summon;

import java.util.ArrayList;
import java.util.List;

public class ExEventMatchTeamInfo extends L2GameServerPacket {
    private final int leader_id;
    private final int loot;
    private final List<EventMatchTeamInfo> members;

    public ExEventMatchTeamInfo(final List<Player> party, final Player exclude) {
        members = new ArrayList<>();
        leader_id = party.get(0).getObjectId();
        loot = party.get(0).getParty().getLootDistribution();
        party.stream().filter(member -> !member.equals(exclude)).map(EventMatchTeamInfo::new).forEach(members::add);
    }

    @Override
    protected void writeImpl() {
        writeEx(0x1c);
    }

    public static class EventMatchTeamInfo {
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
        public final int pet_id;
        public String pet_Name;
        public int pet_NpcId;
        public int pet_curHp;
        public int pet_maxHp;
        public int pet_curMp;
        public int pet_maxMp;
        public int pet_level;

        public EventMatchTeamInfo(final Player member) {
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
            final Summon pet = member.getPet();
            if (pet != null) {
                pet_id = pet.getObjectId();
                pet_NpcId = pet.getNpcId() + 1000000;
                pet_Name = pet.getName();
                pet_curHp = (int) pet.getCurrentHp();
                pet_maxHp = pet.getMaxHp();
                pet_curMp = (int) pet.getCurrentMp();
                pet_maxMp = pet.getMaxMp();
                pet_level = pet.getLevel();
            } else {
                pet_id = 0;
            }
        }
    }
}
