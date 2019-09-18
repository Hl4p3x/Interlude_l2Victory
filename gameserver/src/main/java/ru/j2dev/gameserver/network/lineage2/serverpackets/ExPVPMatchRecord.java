package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.event.PvpEventPlayerInfo;
import ru.j2dev.gameserver.model.event.PvpEventTeam;
import ru.j2dev.gameserver.model.event.PvpEventUtils;

import java.util.Arrays;

public class ExPVPMatchRecord extends L2GameServerPacket {
    private final PvpEventPlayerInfo[][] _teams;
    private final boolean _blue_team_win;
    private final boolean _red_team_win;
    private final int[] _points;

    /**
     * @param blue_team Team 1's player records
     * @param red_team  Team 2's player records
     */
    public ExPVPMatchRecord(final PvpEventTeam blue_team, final PvpEventTeam red_team) {
        _teams = new PvpEventPlayerInfo[2][];
        _teams[0] = PvpEventUtils.sortAndTrimPlayerInfos(blue_team.getPlayerInfos().toArray(new PvpEventPlayerInfo[0]));
        _teams[1] = PvpEventUtils.sortAndTrimPlayerInfos(red_team.getPlayerInfos().toArray(new PvpEventPlayerInfo[0]));
        _blue_team_win = blue_team.getTeamPoints() > red_team.getTeamPoints();
        _red_team_win = red_team.getTeamPoints() > blue_team.getTeamPoints();

        _points = new int[2];
        _points[0] = blue_team.getTeamPoints();
        _points[1] = red_team.getTeamPoints();
    }

    @Override
    protected void writeImpl() {
        writeEx(0x7E);
        writeD(0x02); // state, 2 = end,
        // ничья?
        if (!_blue_team_win && !_red_team_win) {
            writeD(0x00);
            writeD(0x00);
        } else {
            writeD(_blue_team_win ? 0x01 : 0x02); // winner team no?
            writeD(_red_team_win ? 0x01 : 0x02); // looser team no?
        }

        writeD(_points[0]); // team1 kills
        writeD(_points[1]); // team2 kills

        Arrays.stream(_teams).forEach(team -> {
            writeD(team.length);
            Arrays.stream(team).forEach(aTeam -> {
                if (aTeam != null) {
                    final Player p = aTeam.getPlayer();
                    if (p != null) {
                        writeS(p.getName()); // player name
                    } else {
                        writeS("Disconnected");
                    }

                    writeD(aTeam.getKillsCount()); // kills
                    writeD(aTeam.getDeathsCount()); // deaths
                } else {
                    writeS("Disconnected");
                    writeD(0x00); // kills
                    writeD(0x00); // deaths
                }
            });
        });
    }
}
