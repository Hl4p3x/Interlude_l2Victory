package events.DropEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class DropEvent extends Functions implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropEvent.class);
    private static final String EVENT_NAME = "DropEvent";
    private static final DropEvent INSTANCE = new DropEvent();
    private static final int MAX_LEVEL = 99;
    private static boolean ACTIVE;
    private static DropEventItem[][] ITEMS_BY_NPC_LVL;
    private static Map<Integer, List<DropEventItem>> ITEMS_BY_NPC_ID;

    private OnDeathListenerImpl deathListener = new OnDeathListenerImpl();

    private static boolean isActive() {
        return IsActive(EVENT_NAME);
    }

    private static DropEventItem parseItemInfo(final String itemInfo) {
        final Pattern p = Pattern.compile("(\\d+)[:-](\\d+)\\((\\d+)\\)(<(\\d+)-(\\d+)>)?(\\[([\\d,]+)\\])?");
        final Matcher m = p.matcher(itemInfo);
        if (m.matches()) {
            final int itemId = Integer.parseInt(m.group(1));
            final long itemCount = Long.parseLong(m.group(2));
            final double chance = Double.parseDouble(m.group(3));
            int minLvl = (m.group(5) != null) ? Integer.parseInt(m.group(5)) : 1;
            int maxLvl = (m.group(5) != null) ? Integer.parseInt(m.group(6)) : 99;
            final List<Integer> npcIdsList = new ArrayList<>();
            if (m.group(8) != null) {
                minLvl = 0;
                maxLvl = -1;
                final String[] npcIdTexts = m.group(8).split(",");
                Stream.of(npcIdTexts).map(Integer::parseInt).forEach(npcIdsList::add);
            }
            final int[] npcIds = new int[npcIdsList.size()];
            for (int npcIdIdx = 0; npcIdIdx < npcIds.length; ++npcIdIdx) {
                npcIds[npcIdIdx] = npcIdsList.get(npcIdIdx);
            }
            return new DropEventItem(itemId, itemCount, chance, npcIds, minLvl, maxLvl);
        }
        throw new RuntimeException("Can't parse drop event item \"" + itemInfo + "\"");
    }

    private static List<DropEventItem> parseDropEventItemsInfos(final String itemsInfos) {
        final List<DropEventItem> dropEventItems = new ArrayList<>();
        final StringTokenizer itemsListTokenizer = new StringTokenizer(itemsInfos, ";");
        while (itemsListTokenizer.hasMoreTokens()) {
            final String itemInfoTextTok = itemsListTokenizer.nextToken();
            dropEventItems.add(parseItemInfo(itemInfoTextTok));
        }
        return dropEventItems;
    }

    private static void loadConfig() {
        ITEMS_BY_NPC_ID = new HashMap<>();
        ITEMS_BY_NPC_LVL = new DropEventItem[99][];
        if (isActive()) {
            final List<DropEventItem> dropEventItems = parseDropEventItemsInfos(Config.EVENT_DropEvent_Items);
            for (final DropEventItem dropEventItem : dropEventItems) {
                for (int lvl = dropEventItem.getMinLvl(); lvl <= dropEventItem.getMaxLvl(); ++lvl) {
                    if (lvl > 0) {
                        final DropEventItem[] byLvl = ITEMS_BY_NPC_LVL[lvl];
                        ITEMS_BY_NPC_LVL[lvl] = ArrayUtils.add(byLvl, dropEventItem);
                    }
                }
                for (final Integer npcId : dropEventItem.getNpcIds()) {
                    List<DropEventItem> items = ITEMS_BY_NPC_ID.computeIfAbsent(npcId, k -> new ArrayList<>());
                    items.add(dropEventItem);
                }
            }
        }
    }

    public void startEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("DropEvent", true)) {
            if (!ACTIVE) {
                CharListenerList.addGlobal(deathListener);
            }
            player.sendMessage("Event 'DropEvent' started.");
        } else {
            player.sendMessage("Event 'DropEvent' already started.");
        }
        ACTIVE = true;
        show("admin/events/events.htm", player);
    }

    public void stopEvent() {
        final Player player = getSelf();
        if (!player.getPlayerAccess().IsEventGm) {
            return;
        }
        if (SetActive("DropEvent", false)) {
            if (ACTIVE) {
                CharListenerList.removeGlobal(deathListener);
            }
            LOGGER.info("Event: 'DropEvent' stopped.");
        } else {
            player.sendMessage("Event: 'DropEvent' not started.");
        }
        ACTIVE = false;
        show("admin/events/events.htm", player);
    }

    @Override
    public void onInit() {
        loadConfig();
        if (isActive()) {
            CharListenerList.addGlobal(deathListener);
            ACTIVE = true;
            LOGGER.info("Loaded Event: Drop Event [state: activated]");
        } else {
            LOGGER.info("Loaded Event: Drop Event [state: deactivated]");
        }
    }

    private class OnDeathListenerImpl implements OnDeathListener {


        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            try {
                if (!Functions.SimpleCheckDrop(actor, killer)) {
                    return;
                }
                final NpcInstance npc = (NpcInstance) actor;
                final DropEventItem[] byLvl = ITEMS_BY_NPC_LVL[npc.getLevel()];
                final List<DropEventItem> byId = ITEMS_BY_NPC_ID.get(npc.getNpcId());
                final Set<DropEventItem> availableDropEventItems = new HashSet<>();
                if (byLvl != null) {
                    availableDropEventItems.addAll(Arrays.asList(byLvl));
                }
                if (byId != null) {
                    availableDropEventItems.addAll(byId);
                }
                availableDropEventItems.stream().filter(dropEventItem -> Rnd.chance(dropEventItem.getChance())).forEach(dropEventItem -> npc.dropItem(killer.getPlayer(), dropEventItem.getItemId(), Config.EVENT_DropEvent_Rate ? ((long) (dropEventItem.getItemCount() * killer.getRateItems())) : dropEventItem.getItemCount()));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    private static class DropEventItem {
        private final int _itemId;
        private final long _itemCount;
        private final double _chance;
        private final int[] _npcIds;
        private final int _minLvl;
        private final int _maxLvl;

        private DropEventItem(final int itemId, final long itemCount, final double chance, final int[] npcIds, final int minLvl, final int maxLvl) {
            Arrays.sort(npcIds);
            _itemId = itemId;
            _itemCount = itemCount;
            _chance = chance;
            _npcIds = npcIds;
            _minLvl = minLvl;
            _maxLvl = maxLvl;
        }

        public double getChance() {
            return _chance;
        }

        public long getItemCount() {
            return _itemCount;
        }

        public int getItemId() {
            return _itemId;
        }

        public int getMaxLvl() {
            return _maxLvl;
        }

        public int getMinLvl() {
            return _minLvl;
        }

        public int[] getNpcIds() {
            return _npcIds;
        }
    }
}
