package ru.j2dev.gameserver.model.instances;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.Calendar;
import java.util.StringTokenizer;

public class SignsPriestInstance extends NpcInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(SignsPriestInstance.class);

    public SignsPriestInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    private void showChatWindow(final Player player, final int val, final String suffix, final boolean isDescription) {
        String filename = "seven_signs/";
        filename += (isDescription ? ("desc_" + val) : ("signs_" + val));
        filename += ((suffix != null) ? ("_" + suffix + ".htm") : ".htm");
        showChatWindow(player, filename);
    }

    private boolean getPlayerAllyHasCastle(final Player player) {
        final Clan playerClan = player.getClan();
        if (playerClan == null) {
            return false;
        }
        if (!Config.ALT_GAME_REQUIRE_CLAN_CASTLE) {
            final int allyId = playerClan.getAllyId();
            if (allyId != 0) {
                final Clan[] clanList = ClanTable.getInstance().getClans();
                for (final Clan clan : clanList) {
                    if (clan.getAllyId() == allyId && clan.getCastle() > 0) {
                        return true;
                    }
                }
            }
        }
        return playerClan.getCastle() > 0;
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if ((getNpcId() == 31113 || getNpcId() == 31126) && SevenSigns.getInstance().getPlayerCabal(player) == 0 && !player.isGM() && Config.ALT_MAMONS_CHECK_SEVEN_SING_STATUS) {
            return;
        }
        super.onBypassFeedback(player, command);
        if (command.startsWith("SevenSignsDesc")) {
            final int val = Integer.parseInt(command.substring(15));
            showChatWindow(player, val, null, true);
        } else if (command.startsWith("SevenSigns")) {
            int cabal = 0;
            int stoneType = 0;
            ItemInstance ancientAdena = player.getInventory().getItemByItemId(5575);
            final long ancientAdenaAmount = (ancientAdena == null) ? 0L : ancientAdena.getCount();
            int val2 = Integer.parseInt(command.substring(11, 12).trim());
            if (command.length() > 12) {
                val2 = Integer.parseInt(command.substring(11, 13).trim());
            }
            if (command.length() > 13) {
                try {
                    cabal = Integer.parseInt(command.substring(14, 15).trim());
                } catch (Exception e2) {
                    try {
                        cabal = Integer.parseInt(command.substring(13, 14).trim());
                    } catch (Exception ignored) {
                    }
                }
            }
            switch (val2) {
                case 2: {
                    if (!player.getInventory().validateCapacity(1L)) {
                        player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                        return;
                    }
                    if (500L > player.getAdena()) {
                        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                        return;
                    }
                    player.reduceAdena(500L, true);
                    player.getInventory().addItem(ItemFunctions.createItem(5707));
                    player.sendPacket(SystemMessage2.obtainItems(5707, 1L, 0));
                    break;
                }
                case 3:
                case 8: {
                    cabal = SevenSigns.getInstance().getPriestCabal(getNpcId());
                    showChatWindow(player, val2, SevenSigns.getCabalShortName(cabal), false);
                    break;
                }
                case 10: {
                    cabal = SevenSigns.getInstance().getPriestCabal(getNpcId());
                    if (SevenSigns.getInstance().isSealValidationPeriod()) {
                        showChatWindow(player, val2, "", false);
                        break;
                    }
                    showChatWindow(player, val2, getParameters().getString("town", "no"), false);
                    break;
                }
                case 4: {
                    final int newSeal = Integer.parseInt(command.substring(15));
                    final int oldCabal = SevenSigns.getInstance().getPlayerCabal(player);
                    if (oldCabal != 0) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.AlreadyMember", player).addString(SevenSigns.getCabalName(cabal)));
                        return;
                    }
                    if (player.getClassId().level() == 0) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.YouAreNewbie", player));
                        break;
                    }
                    if (player.getClassId().level() >= 2 && Config.ALT_GAME_REQUIRE_CASTLE_DAWN) {
                        if (getPlayerAllyHasCastle(player)) {
                            if (cabal == 1) {
                                player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.CastleOwning", player));
                                return;
                            }
                        } else if (cabal == 2) {
                            boolean allowJoinDawn = false;
                            if (Functions.getItemCount(player, 6388) > 0L) {
                                Functions.removeItem(player, 6388, 1L);
                                allowJoinDawn = true;
                            } else if (Config.ALT_GAME_ALLOW_ADENA_DAWN && player.getAdena() >= 50000L) {
                                player.reduceAdena(50000L, true);
                                allowJoinDawn = true;
                            }
                            if (!allowJoinDawn) {
                                if (Config.ALT_GAME_ALLOW_ADENA_DAWN) {
                                    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.CastleOwningCertificate", player));
                                } else {
                                    player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.CastleOwningCertificate2", player));
                                }
                                return;
                            }
                        }
                    }
                    SevenSigns.getInstance().setPlayerInfo(player.getObjectId(), cabal, newSeal);
                    if (cabal == 2) {
                        player.sendPacket(Msg.YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_LORDS_OF_DAWN);
                    } else {
                        player.sendPacket(Msg.YOU_WILL_PARTICIPATE_IN_THE_SEVEN_SIGNS_AS_A_MEMBER_OF_THE_REVOLUTIONARIES_OF_DUSK);
                    }
                    switch (newSeal) {
                        case 1: {
                            player.sendPacket(Msg.YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_AVARICE_DURING_THIS_QUEST_EVENT_PERIOD);
                            break;
                        }
                        case 2: {
                            player.sendPacket(Msg.YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_GNOSIS_DURING_THIS_QUEST_EVENT_PERIOD);
                            break;
                        }
                        case 3: {
                            player.sendPacket(Msg.YOUVE_CHOSEN_TO_FIGHT_FOR_THE_SEAL_OF_STRIFE_DURING_THIS_QUEST_EVENT_PERIOD);
                            break;
                        }
                    }
                    showChatWindow(player, 4, SevenSigns.getCabalShortName(cabal), false);
                    break;
                }
                case 6: {
                    stoneType = Integer.parseInt(command.substring(13));
                    final ItemInstance redStones = player.getInventory().getItemByItemId(6362);
                    final long redStoneCount = (redStones == null) ? 0L : redStones.getCount();
                    final ItemInstance greenStones = player.getInventory().getItemByItemId(6361);
                    final long greenStoneCount = (greenStones == null) ? 0L : greenStones.getCount();
                    final ItemInstance blueStones = player.getInventory().getItemByItemId(6360);
                    final long blueStoneCount = (blueStones == null) ? 0L : blueStones.getCount();
                    long contribScore = SevenSigns.getInstance().getPlayerContribScore(player);
                    boolean stonesFound = false;
                    if (contribScore == SevenSigns.MAXIMUM_PLAYER_CONTRIB) {
                        player.sendPacket(Msg.CONTRIBUTION_LEVEL_HAS_EXCEEDED_THE_LIMIT_YOU_MAY_NOT_CONTINUE);
                        break;
                    }
                    long redContribCount = 0L;
                    long greenContribCount = 0L;
                    long blueContribCount = 0L;
                    switch (stoneType) {
                        case 1: {
                            blueContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 3L;
                            if (blueContribCount > blueStoneCount) {
                                blueContribCount = blueStoneCount;
                                break;
                            }
                            break;
                        }
                        case 2: {
                            greenContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 5L;
                            if (greenContribCount > greenStoneCount) {
                                greenContribCount = greenStoneCount;
                                break;
                            }
                            break;
                        }
                        case 3: {
                            redContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - contribScore) / 10L;
                            if (redContribCount > redStoneCount) {
                                redContribCount = redStoneCount;
                                break;
                            }
                            break;
                        }
                        case 4: {
                            long tempContribScore = contribScore;
                            redContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 10L;
                            if (redContribCount > redStoneCount) {
                                redContribCount = redStoneCount;
                            }
                            tempContribScore += redContribCount * 10L;
                            greenContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 5L;
                            if (greenContribCount > greenStoneCount) {
                                greenContribCount = greenStoneCount;
                            }
                            tempContribScore += greenContribCount * 5L;
                            blueContribCount = (SevenSigns.MAXIMUM_PLAYER_CONTRIB - tempContribScore) / 3L;
                            if (blueContribCount > blueStoneCount) {
                                blueContribCount = blueStoneCount;
                                break;
                            }
                            break;
                        }
                    }
                    if (redContribCount > 0L && player.getInventory().destroyItemByItemId(6362, redContribCount)) {
                        stonesFound = true;
                    }
                    if (greenContribCount > 0L && player.getInventory().destroyItemByItemId(6361, greenContribCount)) {
                        stonesFound = true;
                    }
                    if (blueContribCount > 0L) {
                        final ItemInstance temp = player.getInventory().getItemByItemId(6360);
                        if (player.getInventory().destroyItemByItemId(6360, blueContribCount)) {
                            stonesFound = true;
                        }
                    }
                    if (!stonesFound) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveAnySSType", player));
                        return;
                    }
                    contribScore = SevenSigns.getInstance().addPlayerStoneContrib(player, blueContribCount, greenContribCount, redContribCount);
                    final SystemMessage sm = new SystemMessage(1267);
                    sm.addNumber(contribScore);
                    player.sendPacket(sm);
                    showChatWindow(player, 6, null, false);
                    break;
                }
                case 7: {
                    long ancientAdenaConvert = 0L;
                    try {
                        ancientAdenaConvert = Long.parseLong(command.substring(13).trim());
                    } catch (NumberFormatException | StringIndexOutOfBoundsException e3) {
                        player.sendMessage(new CustomMessage("common.IntegerAmount", player));
                        return;
                    }
                    if (ancientAdenaAmount < ancientAdenaConvert || ancientAdenaConvert < 1L) {
                        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                        return;
                    }
                    if (player.getInventory().destroyItemByItemId(5575, ancientAdenaConvert)) {
                        player.addAdena(ancientAdenaConvert);
                        player.sendPacket(SystemMessage2.removeItems(5575, ancientAdenaConvert));
                        player.sendPacket(SystemMessage2.obtainItems(57, ancientAdenaConvert, 0));
                        break;
                    }
                    break;
                }
                case 9: {
                    final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
                    final int winningCabal = SevenSigns.getInstance().getCabalHighestScore();
                    if (!SevenSigns.getInstance().isSealValidationPeriod() || playerCabal != winningCabal) {
                        break;
                    }
                    final int ancientAdenaReward = SevenSigns.getInstance().getAncientAdenaReward(player, true);
                    if (ancientAdenaReward < 3) {
                        showChatWindow(player, 9, "b", false);
                        return;
                    }
                    ancientAdena = ItemFunctions.createItem(5575);
                    ancientAdena.setCount(ancientAdenaReward);
                    player.getInventory().addItem(ancientAdena);
                    player.sendPacket(SystemMessage2.obtainItems(5575, ancientAdenaReward, 0));
                    showChatWindow(player, 9, "a", false);
                    break;
                }
                case 11: {
                    try {
                        final String portInfo = command.substring(14).trim();
                        final StringTokenizer st = new StringTokenizer(portInfo);
                        final int x = Integer.parseInt(st.nextToken());
                        final int y = Integer.parseInt(st.nextToken());
                        final int z = Integer.parseInt(st.nextToken());
                        final long ancientAdenaCost = Long.parseLong(st.nextToken());
                        if (ancientAdenaCost > 0L && !player.getInventory().destroyItemByItemId(5575, ancientAdenaCost)) {
                            player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                            return;
                        }
                        player.teleToLocation(x, y, z);
                    } catch (Exception e) {
                        LOGGER.warn("SevenSigns: Error occurred while teleporting player: " + e);
                    }
                    break;
                }
                case 17: {
                    stoneType = Integer.parseInt(command.substring(14));
                    int stoneId = 0;
                    long stoneCount = 0L;
                    int stoneValue = 0;
                    String stoneColor = null;
                    if (stoneType == 4) {
                        final ItemInstance BlueStoneInstance = player.getInventory().getItemByItemId(6360);
                        final long bcount = (BlueStoneInstance != null) ? BlueStoneInstance.getCount() : 0L;
                        final ItemInstance GreenStoneInstance = player.getInventory().getItemByItemId(6361);
                        final long gcount = (GreenStoneInstance != null) ? GreenStoneInstance.getCount() : 0L;
                        final ItemInstance RedStoneInstance = player.getInventory().getItemByItemId(6362);
                        final long rcount = (RedStoneInstance != null) ? RedStoneInstance.getCount() : 0L;
                        final long ancientAdenaReward2 = SevenSigns.calcAncientAdenaReward(bcount, gcount, rcount);
                        if (ancientAdenaReward2 > 0L) {
                            if (BlueStoneInstance != null) {
                                player.getInventory().destroyItem(BlueStoneInstance, bcount);
                                player.sendPacket(SystemMessage2.removeItems(6360, bcount));
                            }
                            if (GreenStoneInstance != null) {
                                player.getInventory().destroyItem(GreenStoneInstance, gcount);
                                player.sendPacket(SystemMessage2.removeItems(6361, gcount));
                            }
                            if (RedStoneInstance != null) {
                                player.getInventory().destroyItem(RedStoneInstance, rcount);
                                player.sendPacket(SystemMessage2.removeItems(6362, rcount));
                            }
                            ancientAdena = ItemFunctions.createItem(5575);
                            ancientAdena.setCount(ancientAdenaReward2);
                            player.getInventory().addItem(ancientAdena);
                            player.sendPacket(SystemMessage2.obtainItems(5575, ancientAdenaReward2, 0));
                            break;
                        }
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveAnySS", player));
                        break;
                    } else {
                        switch (stoneType) {
                            case 1: {
                                stoneColor = "blue";
                                stoneId = 6360;
                                stoneValue = 3;
                                break;
                            }
                            case 2: {
                                stoneColor = "green";
                                stoneId = 6361;
                                stoneValue = 5;
                                break;
                            }
                            case 3: {
                                stoneColor = "red";
                                stoneId = 6362;
                                stoneValue = 10;
                                break;
                            }
                        }
                        final ItemInstance stoneInstance = player.getInventory().getItemByItemId(stoneId);
                        if (stoneInstance != null) {
                            stoneCount = stoneInstance.getCount();
                        }
                        final String path = "seven_signs/signs_17.htm";
                        String content = HtmCache.getInstance().getNotNull(path, player);
                        if (content != null) {
                            content = content.replaceAll("%stoneColor%", stoneColor);
                            content = content.replaceAll("%stoneValue%", String.valueOf(stoneValue));
                            content = content.replaceAll("%stoneCount%", String.valueOf(stoneCount));
                            content = content.replaceAll("%stoneItemId%", String.valueOf(stoneId));
                            final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
                            html.setHtml(content);
                            player.sendPacket(html);
                            break;
                        }
                        LOGGER.warn("Problem with HTML text seven_signs/signs_17.htm: " + path);
                        break;
                    }
                }
                case 18: {
                    final int convertStoneId = Integer.parseInt(command.substring(14, 18));
                    long convertCount = 0L;
                    try {
                        convertCount = Long.parseLong(command.substring(19).trim());
                    } catch (Exception NumberFormatException) {
                        player.sendMessage(new CustomMessage("common.IntegerAmount", player));
                        break;
                    }
                    final ItemInstance convertItem = player.getInventory().getItemByItemId(convertStoneId);
                    if (convertItem == null) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveAnySSType", player));
                        break;
                    }
                    final long totalCount = convertItem.getCount();
                    long ancientAdenaReward3 = 0L;
                    if (convertCount > totalCount || convertCount <= 0L) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.instances.L2SignsPriestInstance.DontHaveSSAmount", player));
                        break;
                    }
                    switch (convertStoneId) {
                        case 6360: {
                            ancientAdenaReward3 = SevenSigns.calcAncientAdenaReward(convertCount, 0L, 0L);
                            break;
                        }
                        case 6361: {
                            ancientAdenaReward3 = SevenSigns.calcAncientAdenaReward(0L, convertCount, 0L);
                            break;
                        }
                        case 6362: {
                            ancientAdenaReward3 = SevenSigns.calcAncientAdenaReward(0L, 0L, convertCount);
                            break;
                        }
                    }
                    if (player.getInventory().destroyItemByItemId(convertStoneId, convertCount)) {
                        ancientAdena = ItemFunctions.createItem(5575);
                        ancientAdena.setCount(ancientAdenaReward3);
                        player.getInventory().addItem(ancientAdena);
                        player.sendPacket(SystemMessage2.removeItems(convertStoneId, convertCount), SystemMessage2.obtainItems(5575, ancientAdenaReward3, 0));
                        break;
                    }
                    break;
                }
                case 19: {
                    final int chosenSeal = Integer.parseInt(command.substring(16));
                    final String fileSuffix = SevenSigns.getSealName(chosenSeal, true) + "_" + SevenSigns.getCabalShortName(cabal);
                    showChatWindow(player, val2, fileSuffix, false);
                    break;
                }
                case 20: {
                    final StringBuilder contentBuffer = new StringBuilder("<html><body><font color=\"LEVEL\">[Seal Status]</font><br>");
                    for (int i = 1; i < 4; ++i) {
                        final int sealOwner = SevenSigns.getInstance().getSealOwner(i);
                        if (sealOwner != 0) {
                            contentBuffer.append("[").append(SevenSigns.getSealName(i, false)).append(": ").append(SevenSigns.getCabalName(sealOwner)).append("]<br>");
                        } else {
                            contentBuffer.append("[").append(SevenSigns.getSealName(i, false)).append(": Nothingness]<br>");
                        }
                    }
                    contentBuffer.append("<a action=\"bypass -h npc_").append(getObjectId()).append("_SevenSigns 3 ").append(cabal).append("\">Go back.</a></body></html>");
                    final NpcHtmlMessage html2 = new NpcHtmlMessage(player, this);
                    html2.setHtml(contentBuffer.toString());
                    player.sendPacket(html2);
                    break;
                }
                case 21: {
                    if (player.getLevel() < 60) {
                        showChatWindow(player, 20, null, false);
                        return;
                    }
                    if (player.getVarInt("bmarketadena", 0) >= 500000) {
                        showChatWindow(player, 21, null, false);
                        return;
                    }
                    final Calendar sh = Calendar.getInstance();
                    sh.set(Calendar.HOUR_OF_DAY, 20);
                    sh.set(Calendar.MINUTE, 0);
                    sh.set(Calendar.SECOND, 0);
                    final Calendar eh = Calendar.getInstance();
                    eh.set(Calendar.HOUR_OF_DAY, 23);
                    eh.set(Calendar.MINUTE, 59);
                    eh.set(Calendar.SECOND, 59);
                    if (System.currentTimeMillis() > sh.getTimeInMillis() && System.currentTimeMillis() < eh.getTimeInMillis()) {
                        showChatWindow(player, 23, null, false);
                        break;
                    }
                    showChatWindow(player, 22, null, false);
                    break;
                }
                default: {
                    showChatWindow(player, val2, null, false);
                    break;
                }
            }
        }
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final int npcId = getTemplate().npcId;
        String filename = "seven_signs/";
        final int sealAvariceOwner = SevenSigns.getInstance().getSealOwner(1);
        final int sealGnosisOwner = SevenSigns.getInstance().getSealOwner(2);
        final int playerCabal = SevenSigns.getInstance().getPlayerCabal(player);
        final boolean isSealValidationPeriod = SevenSigns.getInstance().isSealValidationPeriod();
        final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
        Label_1149:
        {
            switch (npcId) {
                case 31078:
                case 31079:
                case 31080:
                case 31081:
                case 31082:
                case 31083:
                case 31084:
                case 31168:
                case 31692:
                case 31694:
                case 31997: {
                    switch (playerCabal) {
                        case 2: {
                            if (!isSealValidationPeriod) {
                                filename += "dawn_priest_1b.htm";
                                break Label_1149;
                            }
                            if (compWinner != 2) {
                                filename += "dawn_priest_2b.htm";
                                break Label_1149;
                            }
                            if (compWinner != sealGnosisOwner) {
                                filename += "dawn_priest_2c.htm";
                                break Label_1149;
                            }
                            filename += "dawn_priest_2a.htm";
                            break Label_1149;
                        }
                        case 1: {
                            if (isSealValidationPeriod) {
                                filename += "dawn_priest_3b.htm";
                                break Label_1149;
                            }
                            filename += "dawn_priest_3a.htm";
                            break Label_1149;
                        }
                        default: {
                            if (!isSealValidationPeriod) {
                                filename += "dawn_priest_1a.htm";
                                break Label_1149;
                            }
                            if (compWinner == 2) {
                                filename += "dawn_priest_4.htm";
                                break Label_1149;
                            }
                            filename += "dawn_priest_2b.htm";
                            break Label_1149;
                        }
                    }
                }
                case 31085:
                case 31086:
                case 31087:
                case 31088:
                case 31089:
                case 31090:
                case 31091:
                case 31169:
                case 31693:
                case 31695:
                case 31998: {
                    switch (playerCabal) {
                        case 1: {
                            if (!isSealValidationPeriod) {
                                filename += "dusk_priest_1b.htm";
                                break Label_1149;
                            }
                            if (compWinner != 1) {
                                filename += "dusk_priest_2b.htm";
                                break Label_1149;
                            }
                            if (compWinner != sealGnosisOwner) {
                                filename += "dusk_priest_2c.htm";
                                break Label_1149;
                            }
                            filename += "dusk_priest_2a.htm";
                            break Label_1149;
                        }
                        case 2: {
                            if (isSealValidationPeriod) {
                                filename += "dusk_priest_3b.htm";
                                break Label_1149;
                            }
                            filename += "dusk_priest_3a.htm";
                            break Label_1149;
                        }
                        default: {
                            if (!isSealValidationPeriod) {
                                filename += "dusk_priest_1a.htm";
                                break Label_1149;
                            }
                            if (compWinner == 1) {
                                filename += "dusk_priest_4.htm";
                                break Label_1149;
                            }
                            filename += "dusk_priest_2b.htm";
                            break Label_1149;
                        }
                    }
                }
                case 31092: {
                    filename += "blkmrkt_1.htm";
                    break;
                }
                case 31113: {
                    if (!player.isGM()) {
                        switch (compWinner) {
                            case 2: {
                                if (playerCabal != compWinner || playerCabal != sealAvariceOwner) {
                                    filename += "mammmerch_2.htm";
                                    return;
                                }
                                break;
                            }
                            case 1: {
                                if (playerCabal != compWinner || playerCabal != sealAvariceOwner) {
                                    filename += "mammmerch_2.htm";
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    filename += "mammmerch_1.htm";
                    break;
                }
                case 31126: {
                    if (!player.isGM()) {
                        switch (compWinner) {
                            case 2: {
                                if (playerCabal != compWinner || playerCabal != sealGnosisOwner) {
                                    filename += "mammblack_2.htm";
                                    return;
                                }
                                break;
                            }
                            case 1: {
                                if (playerCabal != compWinner || playerCabal != sealGnosisOwner) {
                                    filename += "mammblack_2.htm";
                                    return;
                                }
                                break;
                            }
                        }
                    }
                    filename += "mammblack_1.htm";
                    break;
                }
                default: {
                    filename = getHtmlPath(npcId, val, player);
                    break;
                }
            }
        }
        player.sendPacket(new NpcHtmlMessage(player, this, filename, val));
    }
}
