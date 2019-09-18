package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import ru.j2dev.gameserver.templates.StatsSet;

public class SSQStatus extends L2GameServerPacket {
    private final int _page;
    private final int period;
    private Player _player;

    public SSQStatus(final Player player, final int recordPage) {
        _player = player;
        _page = recordPage;
        period = SevenSigns.getInstance().getCurrentPeriod();
    }

    @Override
    protected final void writeImpl() {
        writeC(0xf5);
        writeC(_page);
        writeC(period);
        switch (_page) {
            case 1: {
                writeD(SevenSigns.getInstance().getCurrentCycle());
                switch (period) {
                    case 0: {
                        writeD(1183);
                        break;
                    }
                    case 1: {
                        writeD(1176);
                        break;
                    }
                    case 2: {
                        writeD(1184);
                        break;
                    }
                    case 3: {
                        writeD(1177);
                        break;
                    }
                }
                switch (period) {
                    case 0:
                    case 2: {
                        writeD(1287);
                        break;
                    }
                    case 1:
                    case 3: {
                        writeD(1286);
                        break;
                    }
                }
                writeC(SevenSigns.getInstance().getPlayerCabal(_player));
                writeC(SevenSigns.getInstance().getPlayerSeal(_player));
                writeD((int) SevenSigns.getInstance().getPlayerStoneContrib(_player));
                writeD((int) SevenSigns.getInstance().getPlayerAdenaCollect(_player));
                final long dawnStoneScore = SevenSigns.getInstance().getCurrentStoneScore(2);
                final long dawnFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(2);
                final long dawnTotalScore = SevenSigns.getInstance().getCurrentScore(2);
                final long duskStoneScore = SevenSigns.getInstance().getCurrentStoneScore(1);
                final long duskFestivalScore = SevenSigns.getInstance().getCurrentFestivalScore(1);
                final long duskTotalScore = SevenSigns.getInstance().getCurrentScore(1);
                long totalStoneScore = duskStoneScore + dawnStoneScore;
                totalStoneScore = ((totalStoneScore == 0L) ? 1L : totalStoneScore);
                final long duskStoneScoreProp = Math.round(duskStoneScore * 500.0 / totalStoneScore);
                final long dawnStoneScoreProp = Math.round(dawnStoneScore * 500.0 / totalStoneScore);
                long totalOverallScore = duskTotalScore + dawnTotalScore;
                totalOverallScore = ((totalOverallScore == 0L) ? 1L : totalOverallScore);
                final long dawnPercent = Math.round(dawnTotalScore * 110.0 / totalOverallScore);
                final long duskPercent = Math.round(duskTotalScore * 110.0 / totalOverallScore);
                writeD((int) duskStoneScoreProp);
                writeD((int) duskFestivalScore);
                writeD((int) duskTotalScore);
                writeC((int) duskPercent);
                writeD((int) dawnStoneScoreProp);
                writeD((int) dawnFestivalScore);
                writeD((int) dawnTotalScore);
                writeC((int) dawnPercent);
                break;
            }
            case 2: {
                writeH(1);
                writeC(5);
                for (int i = 0; i < 5; ++i) {
                    writeC(i + 1);
                    writeD(SevenSignsFestival.FESTIVAL_LEVEL_SCORES[i]);
                    final long duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
                    final long dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
                    writeQ(duskScore);
                    StatsSet highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(1, i);
                    if (duskScore > 0L) {
                        final String[] partyMembers = highScoreData.getString("names").split(",");
                        writeC(partyMembers.length);
                        for (final String partyMember : partyMembers) {
                            writeS(partyMember);
                        }
                    } else {
                        writeC(0);
                    }
                    writeQ(dawnScore);
                    highScoreData = SevenSignsFestival.getInstance().getHighestScoreData(2, i);
                    if (dawnScore > 0L) {
                        final String[] partyMembers = highScoreData.getString("names").split(",");
                        writeC(partyMembers.length);
                        for (final String partyMember : partyMembers) {
                            writeS(partyMember);
                        }
                    } else {
                        writeC(0);
                    }
                }
                break;
            }
            case 3: {
                writeC(10);
                writeC(35);
                writeC(3);
                int totalDawnProportion = 1;
                int totalDuskProportion = 1;
                for (int j = 1; j <= 3; ++j) {
                    totalDawnProportion += SevenSigns.getInstance().getSealProportion(j, 2);
                    totalDuskProportion += SevenSigns.getInstance().getSealProportion(j, 1);
                }
                totalDawnProportion = Math.max(1, totalDawnProportion);
                totalDuskProportion = Math.max(1, totalDuskProportion);
                for (int j = 1; j <= 3; ++j) {
                    final int dawnProportion = SevenSigns.getInstance().getSealProportion(j, 2);
                    final int duskProportion = SevenSigns.getInstance().getSealProportion(j, 1);
                    writeC(j);
                    writeC(SevenSigns.getInstance().getSealOwner(j));
                    writeC(duskProportion * 100 / totalDuskProportion);
                    writeC(dawnProportion * 100 / totalDawnProportion);
                }
                break;
            }
            case 4: {
                final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
                writeC(winningCabal);
                writeC(3);
                final int dawnTotalPlayers = SevenSigns.getInstance().getTotalMembers(2);
                final int duskTotalPlayers = SevenSigns.getInstance().getTotalMembers(1);
                for (int k = 1; k < 4; ++k) {
                    writeC(k);
                    final int dawnSealPlayers = SevenSigns.getInstance().getSealProportion(k, 2);
                    final int duskSealPlayers = SevenSigns.getInstance().getSealProportion(k, 1);
                    final int dawnProp = (dawnTotalPlayers > 0) ? (dawnSealPlayers * 100 / dawnTotalPlayers) : 0;
                    final int duskProp = (duskTotalPlayers > 0) ? (duskSealPlayers * 100 / duskTotalPlayers) : 0;
                    final int curSealOwner = SevenSigns.getInstance().getSealOwner(k);
                    if (Math.max(dawnProp, duskProp) < 10) {
                        writeC(0);
                        if (curSealOwner == 0) {
                            writeD(1292);
                        } else {
                            writeD(1291);
                        }
                    } else if (Math.max(dawnProp, duskProp) < 35) {
                        writeC(curSealOwner);
                        if (curSealOwner == 0) {
                            writeD(1292);
                        } else {
                            writeD(1289);
                        }
                    } else if (dawnProp == duskProp) {
                        writeC(0);
                        writeD(1293);
                    } else {
                        final int sealWinning = (dawnProp > duskProp) ? 2 : 1;
                        writeC(sealWinning);
                        if (sealWinning == curSealOwner) {
                            writeD(1289);
                        } else {
                            writeD(1290);
                        }
                    }
                }
                break;
            }
        }
        _player = null;
    }
}
