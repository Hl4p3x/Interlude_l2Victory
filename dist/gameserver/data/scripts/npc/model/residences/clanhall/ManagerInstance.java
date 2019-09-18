package npc.model.residences.clanhall;

import npc.model.residences.ResidenceManager;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.network.lineage2.serverpackets.AgitDecoInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class ManagerInstance extends ResidenceManager {
    public ManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    protected Residence getResidence() {
        return getClanHall();
    }

    @Override
    public L2GameServerPacket decoPacket() {
        final ClanHall clanHall = getClanHall();
        if (clanHall != null) {
            return new AgitDecoInfo(clanHall);
        }
        return null;
    }

    @Override
    protected int getPrivUseFunctions() {
        return 2048;
    }

    @Override
    protected int getPrivSetFunctions() {
        return 16384;
    }

    @Override
    protected int getPrivDismiss() {
        return 8192;
    }

    @Override
    protected int getPrivDoors() {
        return 1024;
    }
}
