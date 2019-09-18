package services;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.QuestManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.*;
import java.util.Map.Entry;

public class QuestSell extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger((Class) QuestSell.class);
    private static final String HTML_BASE = "scripts/services";
    private static final String BYPASS_PREFIX = "-h scripts_services.QuestSell:";
    private static final int ITEMS_PER_PAGE = 5;

    private static Map<Set<Quest>, List<Pair<Integer, Long>>> parseQuestSellList(final String qsText) {
        return parseQuestSellList(qsText, false);
    }

    private static boolean isAllowedNpc(final Player player, final NpcInstance npc) {
        return player != null && npc != null && !player.isActionsDisabled() && (Config.ALLOW_TALK_WHILE_SITTING || !player.isSitting()) && npc.isInActingRange(player);
    }

    private static Map<Set<Quest>, List<Pair<Integer, Long>>> parseQuestSellList(final String qsText, final boolean inform) {
        final Map<Set<Quest>, List<Pair<Integer, Long>>> result = new LinkedHashMap<>();
        final StringTokenizer questListEntryTextTok = new StringTokenizer(qsText, ";");
        loop:
        while (questListEntryTextTok.hasMoreTokens()) {
            final String questListEntryText = questListEntryTextTok.nextToken().trim();
            if (questListEntryText.isEmpty()) {
                continue;
            }
            final int priceDelimIdx = questListEntryText.indexOf(58);
            if (priceDelimIdx <= 0) {
                if (!inform) {
                    continue;
                }
                LOGGER.warn("QuestSellService: Can't process quest sell list entry \"" + questListEntryText + "\"");
            } else {
                final String questListText = questListEntryText.substring(0, priceDelimIdx).trim();
                final String questListPricesText = questListEntryText.substring(priceDelimIdx + 1).trim();
                final StringTokenizer questNameOrIdTextTok = new StringTokenizer(questListText, ",");
                Set<Quest> quests = new LinkedHashSet<>();
                while (questNameOrIdTextTok.hasMoreTokens()) {
                    final String questNameOrId = questNameOrIdTextTok.nextToken();
                    final Quest quest = QuestManager.getQuest2(questNameOrId);
                    if (quest == null) {
                        if (inform) {
                            LOGGER.warn("QuestSellService: Can't get quest \"" + questNameOrId + "\" in \"" + questListEntryText + "\"");
                            continue loop;
                        }
                        continue loop;
                    } else {
                        quests.add(quest);
                    }
                }
                quests = Collections.unmodifiableSet(quests);
                final StringTokenizer priceTextTok = new StringTokenizer(questListPricesText, ",");
                final List<Pair<Integer, Long>> price = new ArrayList<>();
                while (priceTextTok.hasMoreTokens()) {
                    final String priceText = priceTextTok.nextToken().trim();
                    final int idAmountDelimIdx = priceText.indexOf(45);
                    if (idAmountDelimIdx <= 0) {
                        if (inform) {
                            LOGGER.warn("QuestSellService: Can't get price of \"" + questListEntryText + "\"");
                            continue loop;
                        }
                        continue loop;
                    } else {
                        final Integer itemId = Integer.parseInt(priceText.substring(0, idAmountDelimIdx).trim());
                        final Long itemAmount = Long.parseLong(priceText.substring(idAmountDelimIdx + 1).trim());
                        if (ItemTemplateHolder.getInstance().getTemplate(itemId) == null) {
                            if (inform) {
                                LOGGER.warn("QuestSellService: Can't get item \"" + itemId + "\" of \"" + questListEntryText + "\"");
                                continue loop;
                            }
                            continue loop;
                        } else {
                            price.add(Pair.of(itemId, itemAmount));
                        }
                    }
                }
                if (result.containsKey(quests)) {
                    if (!inform) {
                        continue;
                    }
                    LOGGER.warn("QuestSellService: Quests already defined \"" + questListText + "\"");
                } else {
                    result.put(quests, Collections.unmodifiableList(price));
                }
            }
        }
        return result;
    }

    private static List<String> formatQuestList(final Player player, final Collection<Quest> quests) {
        final List<String> result = new ArrayList<>();
        for (final Quest quest : quests) {
            String questInfo;
            final QuestState questState = player.getQuestState(quest);
            if (questState != null) {
                switch (questState.getState()) {
                    case 1:
                    case 2: {
                        questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfoHave");
                        break;
                    }
                    case 3: {
                        questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfoDone");
                        break;
                    }
                    default: {
                        questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfo");
                        break;
                    }
                }
            } else {
                questInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questInfo");
            }
            questInfo = questInfo.replace("%quest_name%", quest.getDescr(player));
            questInfo = questInfo.replace("%quest_id%", String.valueOf(quest.getQuestIntId()));
            result.add(questInfo);
        }
        return result;
    }

    private static String formatRequiredItemPrice(final Player player, final Pair<Integer, Long> priceItemPair) {
        final int itemId = priceItemPair.getKey();
        final long itemAmount = priceItemPair.getValue();
        final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(itemId);
        String priceInfo = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.requiredPriceItemInfo");
        priceInfo = priceInfo.replace("%item_id%", String.valueOf(item.getItemId()));
        priceInfo = priceInfo.replace("%item_name%", item.getName());
        priceInfo = priceInfo.replace("%item_amount%", String.valueOf(itemAmount));
        return priceInfo;
    }

    private static List<String> formatPriceList(final Player player, final Collection<Pair<Integer, Long>> requiredItemPairs) {
        final List<String> result = new ArrayList<>();
        for (final Pair<Integer, Long> ip : requiredItemPairs) {
            result.add(formatRequiredItemPrice(player, ip));
        }
        return result;
    }

    private static String formatQuestSellInfo(final Player player, final int idx, final Pair<Set<Quest>, List<Pair<Integer, Long>>> questSellInfo) {
        String questSellInfoText = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.questSellInfo");
        final Set<Quest> quests = questSellInfo.getKey();
        final List<Pair<Integer, Long>> price = questSellInfo.getValue();
        questSellInfoText = questSellInfoText.replace("%quests_list%", String.join("<br1>", formatQuestList(player, quests)));
        questSellInfoText = questSellInfoText.replace("%price_list%", String.join("<br1>", formatPriceList(player, price)));
        questSellInfoText = questSellInfoText.replace("%bypass%", "-h scripts_services.QuestSell:buyQuestsListByIdx " + idx);
        return questSellInfoText;
    }

    private static String pagingHtml(final Player player, final String prevBypass, final int currPage, final String nextBypass) {
        String html = StringHolder.getInstance().getNotNull(player, "scripts.services.QuestSell.paging");
        html = html.replace("%prev_button%", (prevBypass != null) ? "<button value=\"&$1037;\" action=\"bypass %prev_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%prev_bypass%", prevBypass) : "");
        html = html.replace("%curr_page%", Integer.toString(currPage + 1));
        html = html.replace("%next_button%", (nextBypass != null) ? "<button value=\"&$1038;\" action=\"bypass %next_bypass%\" width=65 height=20 back=\"l2ui_ch3.smallbutton2_down\" fore=\"l2ui_ch3.smallbutton2\">".replace("%next_bypass%", nextBypass) : "");
        return html;
    }

    private static boolean hasNextPage(final int length, final int page) {
        return (page + 1) * 5 < length;
    }

    private static String pagingHtml(final Player player, final int page, final Object[] items, final String method) {
        final String bypassFmt = "-h scripts_services.QuestSell:" + method + " %d";
        return pagingHtml(player, (page > 0) ? String.format(bypassFmt, page - 1).trim() : null, page, hasNextPage(items.length, page) ? String.format(bypassFmt, page + 1).trim() : null);
    }

    private static boolean isMayTakeQuests(final Player player, final Collection<Quest> quests) {
        for (final Quest quest : quests) {
            if (!quest.isVisible()) {
                continue;
            }
            final QuestState qs = player.getQuestState(quest);
            if (qs == null || qs.getState() != 3) {
                return true;
            }
        }
        return false;
    }

    private static Pair[] filterAvailableQuests(final Player player, final Map<Set<Quest>, List<Pair<Integer, Long>>> questSellMap) {
        final List<Pair<Set<Quest>, List<Pair<Integer, Long>>>> result = new ArrayList<>();
        for (final Entry<Set<Quest>, List<Pair<Integer, Long>>> e : questSellMap.entrySet()) {
            if (isMayTakeQuests(player, e.getKey())) {
                result.add(Pair.of(e.getKey(), e.getValue()));
            }
        }
        return result.toArray(new Pair[result.size()]);
    }

    private static Pair<Set<Quest>, List<Pair<Integer, Long>>>[] getAvailableQuests(final Player player) {
        return filterAvailableQuests(player, parseQuestSellList(Config.QUEST_SELL_QUEST_PRICES));
    }

    private static void doListAvailableQuestsForSell(final Player player, final NpcInstance npc, final int page) {
        final Pair<Set<Quest>, List<Pair<Integer, Long>>>[] questSell = getAvailableQuests(player);
        final StringBuilder questsHtmlBuilder = new StringBuilder();
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile("scripts/services/quests_sell_list.htm");
        for (int qIdx = 5 * page, qLastIdx = qIdx + 5; qIdx < qLastIdx && qIdx < questSell.length; ++qIdx) {
            final Pair<Set<Quest>, List<Pair<Integer, Long>>> qsItem = questSell[qIdx];
            questsHtmlBuilder.append(formatQuestSellInfo(player, qIdx, qsItem));
        }
        html.replace("%list%", questsHtmlBuilder.toString());
        html.replace("%paging%", pagingHtml(player, page, questSell, "listAvailableQuestsForSell"));
        player.sendPacket(html);
    }

    private static void buyQuests(final Player player, final Collection<Quest> quests, final Collection<Pair<Integer, Long>> price) {
        for (final Pair<Integer, Long> requiredItem : price) {
            if (ItemFunctions.getItemCount(player, requiredItem.getKey()) < requiredItem.getValue()) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
        }
        for (final Pair<Integer, Long> requiredItem : price) {
            if (ItemFunctions.removeItem(player, requiredItem.getKey(), requiredItem.getValue(), true) < requiredItem.getValue()) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                return;
            }
        }
        for (final Quest quest : quests) {
            player.setQuestState(quest.newQuestState(player, 3));
        }
    }

    private void listAvailableQuestsForSell() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.QUEST_SELL_ENABLE || !isAllowedNpc(player, npc)) {
            player.sendMessage(new CustomMessage("common.Disabled", player));
            return;
        }
        doListAvailableQuestsForSell(player, npc, 0);
    }

    public void listAvailableQuestsForSell(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.QUEST_SELL_ENABLE || !isAllowedNpc(player, npc)) {
            player.sendMessage(new CustomMessage("common.Disabled", player));
            return;
        }
        doListAvailableQuestsForSell(player, npc, Integer.parseInt(args[0]));
    }

    public void buyQuestsListByIdx(final String[] args) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (!Config.QUEST_SELL_ENABLE || !isAllowedNpc(player, npc)) {
            player.sendMessage(new CustomMessage("common.Disabled", player));
            return;
        }
        final Pair<Set<Quest>, List<Pair<Integer, Long>>>[] questSell = getAvailableQuests(player);
        final int questsListIdx = Integer.parseInt(args[0]);
        final Pair<Set<Quest>, List<Pair<Integer, Long>>> item = questSell[questsListIdx];
        buyQuests(player, item.getLeft(), item.getRight());
        listAvailableQuestsForSell();
    }

    @Override
    public void onInit() {
        LOGGER.info("QuestSellService: Loading ... ["+Config.QUEST_SELL_ENABLE+"]");
        if(Config.QUEST_SELL_ENABLE) {
            parseQuestSellList(Config.QUEST_SELL_QUEST_PRICES, true);
        }
    }
}
