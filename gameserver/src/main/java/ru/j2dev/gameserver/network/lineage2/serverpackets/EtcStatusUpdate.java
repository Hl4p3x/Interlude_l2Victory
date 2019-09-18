package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;

public class EtcStatusUpdate extends L2GameServerPacket {
    private final int increasedForce;
    private final int weightPenalty;
    private final int messageRefusal;
    private final int dangerArea;
    private final int gradeExpertisePenalty;
    private final int charmOfCourage;
    private final int deathPenaltyLevel;

    public EtcStatusUpdate(final Player player) {
        increasedForce = player.getIncreasedForce();
        weightPenalty = player.getWeightPenalty();
        messageRefusal = ((player.getMessageRefusal() || player.getNoChannel() != 0L || player.isBlockAll()) ? 1 : 0);
        dangerArea = (player.isInDangerArea() ? 1 : 0);
        gradeExpertisePenalty = player.getGradePenalty();
        charmOfCourage = (player.isCharmOfCourage() ? 1 : 0);
        deathPenaltyLevel = ((player.getDeathPenalty() == null) ? 0 : player.getDeathPenalty().getLevel());
    }

    @Override
    protected final void writeImpl() {
        writeC(0xf3);
        writeD(increasedForce);
        writeD(weightPenalty);
        writeD(messageRefusal);
        writeD(dangerArea);
        writeD(gradeExpertisePenalty);
        writeD(charmOfCourage);
        writeD(deathPenaltyLevel);
    }
}
