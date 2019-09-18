package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.pledge.Clan;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PledgeReceiveWarList extends L2GameServerPacket {
    private final List<WarInfo> infos;
    private final int _updateType;
    private final int _page;

    public PledgeReceiveWarList(final Clan clan, final int type, final int page) {
        infos = new ArrayList<>();
        _updateType = type;
        _page = page;
        final List<Clan> clans = (_updateType == 1) ? clan.getAttackerClans() : clan.getEnemyClans();
        clans.stream().filter(Objects::nonNull).map(_clan -> new WarInfo(_clan.getName(), _updateType, 0)).forEach(infos::add);
    }

    @Override
    protected final void writeImpl() {
        writeEx(0x3e);
        writeD(_updateType);
        writeD(0);
        writeD(infos.size());
        infos.forEach(_info -> {
            writeS(_info.clan_name);
            writeD(_info.unk1);
            writeD(_info.unk2);
        });
    }

    static class WarInfo {
        public final String clan_name;
        public final int unk1;
        public final int unk2;

        public WarInfo(final String _clan_name, final int _unk1, final int _unk2) {
            clan_name = _clan_name;
            unk1 = _unk1;
            unk2 = _unk2;
        }
    }
}
