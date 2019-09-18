package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.games.LotteryManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;

import java.text.DateFormat;

public class LotteryManagerInstance extends NpcInstance {
    public LotteryManagerInstance(final int objectID, final NpcTemplate template) {
        super(objectID, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if (command.startsWith("Loto")) {
            try {
                final int val = Integer.parseInt(command.substring(5));
                showLotoWindow(player, val);
            } catch (NumberFormatException e) {
                Log.debug("L2LotteryManagerInstance: bypass: " + command + "; player: " + player, e);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "LotteryManager";
        } else {
            pom = "LotteryManager-" + val;
        }
        return "lottery/" + pom + ".htm";
    }

    public void showLotoWindow(final Player player, final int val) {
        final int npcId = getTemplate().npcId;
        final NpcHtmlMessage html = new NpcHtmlMessage(player, this);
        switch (val) {
            case 0: {
                final String filename = getHtmlPath(npcId, 1, player);
                html.setFile(filename);
                break;
            }
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
            case 8:
            case 9:
            case 10:
            case 11:
            case 12:
            case 13:
            case 14:
            case 15:
            case 16:
            case 17:
            case 18:
            case 19:
            case 20:
            case 21: {
                if (!LotteryManager.getInstance().isStarted()) {
                    player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
                    return;
                }
                if (!LotteryManager.getInstance().isSellableTickets()) {
                    player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
                    return;
                }
                final String filename = getHtmlPath(npcId, 5, player);
                html.setFile(filename);
                int count = 0;
                int found = 0;
                for (int i = 0; i < 5; ++i) {
                    if (player.getLoto(i) == val) {
                        player.setLoto(i, 0);
                        found = 1;
                    } else if (player.getLoto(i) > 0) {
                        ++count;
                    }
                }
                if (count < 5 && found == 0 && val <= 20) {
                    for (int i = 0; i < 5; ++i) {
                        if (player.getLoto(i) == 0) {
                            player.setLoto(i, val);
                            break;
                        }
                    }
                }
                count = 0;
                for (int i = 0; i < 5; ++i) {
                    if (player.getLoto(i) > 0) {
                        ++count;
                        String button = String.valueOf(player.getLoto(i));
                        if (player.getLoto(i) < 10) {
                            button = "0" + button;
                        }
                        final String search = "fore=\"L2UI.lottoNum" + button + "\" back=\"L2UI.lottoNum" + button + "a_check\"";
                        final String replace = "fore=\"L2UI.lottoNum" + button + "a_check\" back=\"L2UI.lottoNum" + button + "\"";
                        html.replace(search, replace);
                    }
                }
                if (count == 5) {
                    String search2;
                    String replace2;
                    if (!player.isLangRus()) {
                        search2 = "0\">Return";
                        replace2 = "22\">The winner selected the numbers above.";
                    } else {
                        search2 = "0\">\u041d\u0430\u0437\u0430\u0434";
                        replace2 = "22\">\u0412\u044b\u0438\u0433\u0440\u044b\u0448\u043d\u044b\u0435 \u043d\u043e\u043c\u0435\u0440\u0430 \u0432\u044b\u0431\u0440\u0430\u043d\u043d\u044b\u0435 \u0432\u044b\u0448\u0435.";
                    }
                    html.replace(search2, replace2);
                }
                break;
            }
            case 22: {
                if (!LotteryManager.getInstance().isStarted()) {
                    player.sendPacket(Msg.LOTTERY_TICKETS_ARE_NOT_CURRENTLY_BEING_SOLD);
                    return;
                }
                if (!LotteryManager.getInstance().isSellableTickets()) {
                    player.sendPacket(Msg.TICKETS_FOR_THE_CURRENT_LOTTERY_ARE_NO_LONGER_AVAILABLE);
                    return;
                }
                final int price = Config.SERVICES_ALT_LOTTERY_PRICE;
                final int lotonumber = LotteryManager.getInstance().getId();
                int enchant = 0;
                int type2 = 0;
                for (int j = 0; j < 5; ++j) {
                    if (player.getLoto(j) == 0) {
                        return;
                    }
                    if (player.getLoto(j) < 17) {
                        enchant += (int) Math.pow(2.0, player.getLoto(j) - 1);
                    } else {
                        type2 += (int) Math.pow(2.0, player.getLoto(j) - 17);
                    }
                }
                if (player.getAdena() < price) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
                player.reduceAdena(price, true);
                final SystemMessage sm = new SystemMessage(371);
                sm.addNumber(lotonumber);
                sm.addItemName(4442);
                player.sendPacket(sm);
                final ItemInstance item = ItemFunctions.createItem(4442);
                item.setBlessed(lotonumber);
                item.setEnchantLevel(enchant);
                item.setDamaged(type2);
                player.getInventory().addItem(item);
                final String filename = getHtmlPath(npcId, 3, player);
                html.setFile(filename);
                break;
            }
            case 23: {
                final String filename = getHtmlPath(npcId, 3, player);
                html.setFile(filename);
                break;
            }
            case 24: {
                final String filename = getHtmlPath(npcId, 4, player);
                html.setFile(filename);
                final int lotonumber2 = LotteryManager.getInstance().getId();
                StringBuilder message = new StringBuilder();
                for (final ItemInstance item2 : player.getInventory().getItems()) {
                    if (item2 != null) {
                        if (item2.getItemId() == 4442 && item2.getBlessed() < lotonumber2) {
                            message.append("<a action=\"bypass -h npc_%objectId%_Loto ").append(item2.getObjectId()).append("\">").append(item2.getBlessed());
                            message.append(" ").append(new CustomMessage("LotteryManagerInstance.NpcString.EVENT_NUMBER", player)).append(" ");
                            final int[] numbers = LotteryManager.getInstance().decodeNumbers(item2.getEnchantLevel(), item2.getDamaged());
                            for (int k = 0; k < 5; ++k) {
                                message.append(numbers[k]).append(" ");
                            }
                            final int[] check = LotteryManager.getInstance().checkTicket(item2);
                            if (check[0] > 0) {
                                message.append("- ");
                                switch (check[0]) {
                                    case 1: {
                                        message.append(new CustomMessage("LotteryManagerInstance.NpcString.FIRST_PRIZE", player));
                                        break;
                                    }
                                    case 2: {
                                        message.append(new CustomMessage("LotteryManagerInstance.NpcString.SECOND_PRIZE", player));
                                        break;
                                    }
                                    case 3: {
                                        message.append(new CustomMessage("LotteryManagerInstance.NpcString.THIRD_PRIZE", player));
                                        break;
                                    }
                                    case 4: {
                                        message.append(new CustomMessage("LotteryManagerInstance.NpcString.FOURTH_PRIZE", player));
                                        break;
                                    }
                                }
                                message.append(" ").append(check[1]).append("a.");
                            }
                            message.append("</a>");
                        }
                    }
                }
                if (message.length() == 0) {
                    message.append(new CustomMessage("LotteryManagerInstance.NpcString.THERE_HAS_BEEN_NO_WINNING_LOTTERY_TICKET", player));
                }
                html.replace("%result%", message.toString());
                break;
            }
            case 25: {
                final String filename = getHtmlPath(npcId, 2, player);
                html.setFile(filename);
                break;
            }
            default: {
                if (val <= 25) {
                    break;
                }
                final int lotonumber2 = LotteryManager.getInstance().getId();
                final ItemInstance item3 = player.getInventory().getItemByObjectId(val);
                if (item3 == null || item3.getItemId() != 4442 || item3.getBlessed() >= lotonumber2) {
                    return;
                }
                final int[] check2 = LotteryManager.getInstance().checkTicket(item3);
                if (player.getInventory().destroyItem(item3, 1L)) {
                    player.sendPacket(SystemMessage2.removeItems(4442, 1L));
                    final int adena = check2[1];
                    if (adena > 0) {
                        player.addAdena(adena);
                    }
                }
                return;
            }
        }
        html.replace("%objectId%", String.valueOf(getObjectId()));
        html.replace("%race%", "" + LotteryManager.getInstance().getId());
        html.replace("%adena%", "" + LotteryManager.getInstance().getPrize());
        html.replace("%ticket_price%", "" + Config.SERVICES_LOTTERY_TICKET_PRICE);
        html.replace("%prize5%", "" + Config.SERVICES_LOTTERY_5_NUMBER_RATE * 100.0);
        html.replace("%prize4%", "" + Config.SERVICES_LOTTERY_4_NUMBER_RATE * 100.0);
        html.replace("%prize3%", "" + Config.SERVICES_LOTTERY_3_NUMBER_RATE * 100.0);
        html.replace("%prize2%", "" + Config.SERVICES_LOTTERY_2_AND_1_NUMBER_PRIZE);
        html.replace("%enddate%", "" + DateFormat.getDateInstance().format(LotteryManager.getInstance().getEndDate()));
        player.sendPacket(html);
        player.sendActionFailed();
    }
}
