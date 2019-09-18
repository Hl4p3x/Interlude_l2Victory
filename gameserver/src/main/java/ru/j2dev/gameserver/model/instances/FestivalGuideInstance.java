package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.SevenSignsFestival;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Calendar;

public final class FestivalGuideInstance extends NpcInstance {
    protected int _festivalType;
    protected int _festivalOracle;

    public FestivalGuideInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        switch (getNpcId()) {
            case 31127:
            case 31132: {
                _festivalType = 0;
                _festivalOracle = 2;
                break;
            }
            case 31128:
            case 31133: {
                _festivalType = 1;
                _festivalOracle = 2;
                break;
            }
            case 31129:
            case 31134: {
                _festivalType = 2;
                _festivalOracle = 2;
                break;
            }
            case 31130:
            case 31135: {
                _festivalType = 3;
                _festivalOracle = 2;
                break;
            }
            case 31131:
            case 31136: {
                _festivalType = 4;
                _festivalOracle = 2;
                break;
            }
            case 31137:
            case 31142: {
                _festivalType = 0;
                _festivalOracle = 1;
                break;
            }
            case 31138:
            case 31143: {
                _festivalType = 1;
                _festivalOracle = 1;
                break;
            }
            case 31139:
            case 31144: {
                _festivalType = 2;
                _festivalOracle = 1;
                break;
            }
            case 31140:
            case 31145: {
                _festivalType = 3;
                _festivalOracle = 1;
                break;
            }
            case 31141:
            case 31146: {
                _festivalType = 4;
                _festivalOracle = 1;
                break;
            }
        }
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (SevenSigns.getInstance().getPlayerCabal(player) == 0) {
            player.sendMessage("You must be Seven Signs participant.");
            return;
        }
        if (command.startsWith("FestivalDesc")) {
            final int val = Integer.parseInt(command.substring(13));
            showChatWindow(player, val, null, true);
        } else if (command.startsWith("Festival")) {
            final Party playerParty = player.getParty();
            final int val2 = Integer.parseInt(command.substring(9, 10));
            switch (val2) {
                case 1: {
                    showChatWindow(player, 1, null, false);
                    break;
                }
                case 2: {
                    if (SevenSigns.getInstance().getCurrentPeriod() != 1) {
                        showChatWindow(player, 2, "a", false);
                        return;
                    }
                    if (SevenSignsFestival.getInstance().isFestivalInitialized()) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.InProgress", player));
                        return;
                    }
                    if (playerParty == null || playerParty.getMemberCount() < Config.FESTIVAL_MIN_PARTY_SIZE) {
                        showChatWindow(player, 2, "b", false);
                        return;
                    }
                    if (!playerParty.isLeader(player)) {
                        showChatWindow(player, 2, "c", false);
                        return;
                    }
                    final int maxlevel = SevenSignsFestival.getMaxLevelForFestival(_festivalType);
                    for (final Player p : playerParty.getPartyMembers()) {
                        if (p.getLevel() > maxlevel) {
                            showChatWindow(player, 2, "d", false);
                            return;
                        }
                        if (SevenSigns.getInstance().getPlayerCabal(p) == 0) {
                            showChatWindow(player, 2, "g", false);
                            return;
                        }
                    }
                    if (player.isFestivalParticipant()) {
                        showChatWindow(player, 2, "f", false);
                        return;
                    }
                    final int stoneType = Integer.parseInt(command.substring(11));
                    final long stonesNeeded = (long) Math.floor(SevenSignsFestival.getStoneCount(_festivalType, stoneType) * Config.FESTIVAL_RATE_PRICE);
                    if (!player.getInventory().destroyItemByItemId(stoneType, stonesNeeded)) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2FestivalGuideInstance.NotEnoughSSType", player));
                        return;
                    }
                    player.sendPacket(SystemMessage2.removeItems(stoneType, stonesNeeded));
                    SevenSignsFestival.getInstance().addAccumulatedBonus(_festivalType, stoneType, stonesNeeded);
                    new DarknessFestival(player.getParty(), SevenSigns.getInstance().getPlayerCabal(player), _festivalType);
                    showChatWindow(player, 2, "e", false);
                    break;
                }
                case 4: {
                    final StringBuilder strBuffer = new StringBuilder("<html><body>Festival Guide:<br>These are the top scores of the week, for the ");
                    final StatsSet dawnData = SevenSignsFestival.getInstance().getHighestScoreData(2, _festivalType);
                    final StatsSet duskData = SevenSignsFestival.getInstance().getHighestScoreData(1, _festivalType);
                    final StatsSet overallData = SevenSignsFestival.getInstance().getOverallHighestScoreData(_festivalType);
                    final int dawnScore = dawnData.getInteger("score");
                    final int duskScore = duskData.getInteger("score");
                    int overallScore = 0;
                    if (overallData != null) {
                        overallScore = overallData.getInteger("score");
                    }
                    strBuffer.append(SevenSignsFestival.getFestivalName(_festivalType)).append(" festival.<br>");
                    if (dawnScore > 0) {
                        strBuffer.append("Dawn: ").append(calculateDate(dawnData.getString("date"))).append(". Score ").append(dawnScore).append("<br>").append(dawnData.getString("names").replaceAll(",", ", ")).append("<br>");
                    } else {
                        strBuffer.append("Dawn: No record exists. Score 0<br>");
                    }
                    if (duskScore > 0) {
                        strBuffer.append("Dusk: ").append(calculateDate(duskData.getString("date"))).append(". Score ").append(duskScore).append("<br>").append(duskData.getString("names").replaceAll(",", ", ")).append("<br>");
                    } else {
                        strBuffer.append("Dusk: No record exists. Score 0<br>");
                    }
                    if (overallScore > 0 && overallData != null) {
                        String cabalStr = "Children of Dusk";
                        if (overallData.getInteger("cabal") == 2) {
                            cabalStr = "Children of Dawn";
                        }
                        strBuffer.append("Consecutive top scores: ").append(calculateDate(overallData.getString("date"))).append(". Score ").append(overallScore).append("<br>Affilated side: ").append(cabalStr).append("<br>").append(overallData.getString("names").replaceAll(",", ", ")).append("<br>");
                    } else {
                        strBuffer.append("Consecutive top scores: No record exists. Score 0<br>");
                    }
                    strBuffer.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_Chat 0\">Go back.</a></body></html>");
                    final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                    html.setHtml(strBuffer.toString());
                    player.sendPacket(html);
                    break;
                }
                case 8: {
                    if (playerParty == null) {
                        return;
                    }
                    if (!playerParty.isLeader(player)) {
                        showChatWindow(player, 8, "a", false);
                        break;
                    }
                    final Reflection r = getReflection();
                    if (!(r instanceof DarknessFestival)) {
                        break;
                    }
                    if (((DarknessFestival) r).increaseChallenge()) {
                        showChatWindow(player, 8, "b", false);
                        break;
                    }
                    showChatWindow(player, 8, "c", false);
                    break;
                }
                case 9: {
                    if (playerParty == null) {
                        return;
                    }
                    final Reflection r = getReflection();
                    if (!(r instanceof DarknessFestival)) {
                        return;
                    }
                    if (playerParty.isLeader(player)) {
                        r.collapse();
                        break;
                    }
                    if (playerParty.getMemberCount() > Config.FESTIVAL_MIN_PARTY_SIZE) {
                        player.leaveParty();
                        break;
                    }
                    player.sendMessage("Only party leader can leave festival, if minmum party member is reached.");
                    break;
                }
                default: {
                    showChatWindow(player, val2, null, false);
                    break;
                }
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private void showChatWindow(final Player player, final int val, final String suffix, final boolean isDescription) {
        String filename = "seven_signs/festival/";
        filename += (isDescription ? "desc_" : "festival_");
        filename += ((suffix != null) ? (val + suffix + ".htm") : (val + ".htm"));
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        html.setFile(filename);
        html.replace("%festivalType%", SevenSignsFestival.getFestivalName(_festivalType));
        html.replace("%min%", String.valueOf(Config.FESTIVAL_MIN_PARTY_SIZE));
        if (val == 1) {
            html.replace("%price1%", String.valueOf((long) Math.floor(SevenSignsFestival.getStoneCount(_festivalType, 6362) * Config.FESTIVAL_RATE_PRICE)));
            html.replace("%price2%", String.valueOf((long) Math.floor(SevenSignsFestival.getStoneCount(_festivalType, 6361) * Config.FESTIVAL_RATE_PRICE)));
            html.replace("%price3%", String.valueOf((long) Math.floor(SevenSignsFestival.getStoneCount(_festivalType, 6360) * Config.FESTIVAL_RATE_PRICE)));
        }
        if (val == 5) {
            html.replace("%statsTable%", getStatsTable());
        }
        if (val == 6) {
            html.replace("%bonusTable%", getBonusTable());
        }
        player.sendPacket(html);
        player.sendActionFailed();
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        String filename = "seven_signs/";
        switch (getNpcId()) {
            case 31127:
            case 31128:
            case 31129:
            case 31130:
            case 31131: {
                filename += "festival/dawn_guide.htm";
                break;
            }
            case 31137:
            case 31138:
            case 31139:
            case 31140:
            case 31141: {
                filename += "festival/dusk_guide.htm";
                break;
            }
            case 31132:
            case 31133:
            case 31134:
            case 31135:
            case 31136:
            case 31142:
            case 31143:
            case 31144:
            case 31145:
            case 31146: {
                filename += "festival/festival_witch.htm";
                break;
            }
            default: {
                filename = getHtmlPath(getNpcId(), val, player);
                break;
            }
        }
        player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
    }

    private String getStatsTable() {
        final StringBuilder tableHtml = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            final long dawnScore = SevenSignsFestival.getInstance().getHighestScore(2, i);
            final long duskScore = SevenSignsFestival.getInstance().getHighestScore(1, i);
            final String festivalName = SevenSignsFestival.getFestivalName(i);
            String winningCabal = "Children of Dusk";
            if (dawnScore > duskScore) {
                winningCabal = "Children of Dawn";
            } else if (dawnScore == duskScore) {
                winningCabal = "None";
            }
            tableHtml.append("<tr><td width=\"100\" align=\"center\">").append(festivalName).append("</td><td align=\"center\" width=\"35\">").append(duskScore).append("</td><td align=\"center\" width=\"35\">").append(dawnScore).append("</td><td align=\"center\" width=\"130\">").append(winningCabal).append("</td></tr>");
        }
        return tableHtml.toString();
    }

    private String getBonusTable() {
        final StringBuilder tableHtml = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            final long accumScore = SevenSignsFestival.getInstance().getAccumulatedBonus(i);
            final String festivalName = SevenSignsFestival.getFestivalName(i);
            tableHtml.append("<tr><td align=\"center\" width=\"150\">").append(festivalName).append("</td><td align=\"center\" width=\"150\">").append(accumScore).append("</td></tr>");
        }
        return tableHtml.toString();
    }

    private String calculateDate(final String milliFromEpoch) {
        final long numMillis = Long.parseLong(milliFromEpoch);
        final Calendar calCalc = Calendar.getInstance();
        calCalc.setTimeInMillis(numMillis);
        return calCalc.get(Calendar.YEAR) + "/" + calCalc.get(Calendar.MONTH) + "/" + calCalc.get(Calendar.DATE);
    }
}
