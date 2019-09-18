package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillList;

public final class RequestSkillList extends L2GameClientPacket {
    private static final String _C__50_REQUESTSKILLLIST = "[C] 50 RequestSkillList";

    @Override
    protected void readImpl() {
    }

    @Override
    protected void runImpl() {
        final Player cha = getClient().getActiveChar();
        if (cha != null) {
            cha.sendPacket(new SkillList(cha));
        }
    }

    @Override
    public String getType() {
        return "[C] 50 RequestSkillList";
    }
}
