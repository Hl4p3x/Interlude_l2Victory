package ru.j2dev.gameserver.scripts;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.model.mail.Mail.SenderType;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExNoticePostArrived;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcSay;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.*;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ScheduledFuture;

public class Functions {
    private static final String ITEM_ID_AMOUNT_LIST_DELIMITERS = ",;/";
    private static final String ITEM_ID_AMOUNT_ITEM_DELIMITERS = "-:_";
    public HardReference<Player> self = HardReferences.emptyRef();
    public HardReference<NpcInstance> npc = HardReferences.emptyRef();

    public static ScheduledFuture<?> executeTask(final Player caller, final String className, final String methodName, final Object[] args, final Map<String, Object> variables, final long delay) {
        return ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                callScripts(caller, className, methodName, args, variables);
            }
        }, delay);
    }

    public static ScheduledFuture<?> executeTask(final String className, final String methodName, final Object[] args, final Map<String, Object> variables, final long delay) {
        return executeTask(null, className, methodName, args, variables, delay);
    }

    public static ScheduledFuture<?> executeTask(final Player player, final String className, final String methodName, final Object[] args, final long delay) {
        return executeTask(player, className, methodName, args, null, delay);
    }

    public static ScheduledFuture<?> executeTask(final String className, final String methodName, final Object[] args, final long delay) {
        return executeTask(className, methodName, args, null, delay);
    }

    public static Object callScripts(final String className, final String methodName, final Object[] args) {
        return callScripts(className, methodName, args, null);
    }

    public static Object callScripts(final String className, final String methodName, final Object[] args, final Map<String, Object> variables) {
        return callScripts(null, className, methodName, args, variables);
    }

    public static Object callScripts(final Player player, final String className, final String methodName, final Object[] args, final Map<String, Object> variables) {
        return Scripts.getInstance().callScripts(player, className, methodName, args, variables);
    }

    public static List<Pair<ItemTemplate, Long>> parseItemIdAmountList(final String itemIdAmountListText) {
        final List<Pair<ItemTemplate, Long>> result = new ArrayList<>();
        final StringTokenizer itemsListTokenizer = new StringTokenizer(itemIdAmountListText, ",;/");
        while (itemsListTokenizer.hasMoreTokens()) {
            final String consumeItemTextTok = itemsListTokenizer.nextToken();
            final StringTokenizer itemIdAmountTokenizer = new StringTokenizer(consumeItemTextTok, "-:_");
            final int itemId = Integer.parseInt(itemIdAmountTokenizer.nextToken());
            final ItemTemplate itemTemplate = ItemTemplateHolder.getInstance().getTemplate(itemId);
            final long itemCount = Long.parseLong(itemIdAmountTokenizer.nextToken());
            result.add(Pair.of(itemTemplate, itemCount));
        }
        return Collections.unmodifiableList(result);
    }

    public static void show(final String text, final Player self, final NpcInstance npc, final Object... arg) {
        if (text == null || self == null) {
            return;
        }
        final NpcHtmlMessage msg = new NpcHtmlMessage(self, npc);
        if (text.endsWith(".html") || text.endsWith(".htm")) {
            msg.setFile(text);
        } else {
            msg.setHtml(HtmlUtils.bbParse(text));
        }
        if (arg != null && arg.length % 2 == 0) {
            for (int i = 0; i < arg.length; i = 2) {
                msg.replace(String.valueOf(arg[i]), String.valueOf(arg[i + 1]));
            }
        }
        self.sendPacket(msg);
    }

    public static void show(final CustomMessage message, final Player self) {
        show(message.toString(), self, null);
    }

    public static void sendMessage(final String text, final Player self) {
        self.sendMessage(text);
    }

    public static void sendMessage(final CustomMessage message, final Player self) {
        self.sendMessage(message);
    }

    public static void npcSayInRange(final NpcInstance npc, final String text, final int range) {
        npcSayInRange(npc, range, NpcString.NONE, text);
    }

    public static void npcSayInRange(final NpcInstance npc, final int range, final NpcString fStringId, final String... params) {
        if (npc == null) {
            return;
        }
        final NpcSay cs = new NpcSay(npc, ChatType.NPC_NORMAL, fStringId, params);
        World.getAroundPlayers(npc, range, Math.max(range / 2, 200)).stream().filter(player -> npc.getReflection() == player.getReflection()).forEach(player -> player.sendPacket(cs));
    }

    public static void npcSay(final NpcInstance npc, final String text) {
        npcSayInRange(npc, text, 1500);
    }

    public static void npcSay(final NpcInstance npc, final NpcString npcString, final String... params) {
        npcSayInRange(npc, 1500, npcString, params);
    }

    public static void npcSayInRangeCustomMessage(final NpcInstance npc, final int range, final String address, final Object... replacements) {
        if (npc == null) {
            return;
        }
        World.getAroundPlayers(npc, range, Math.max(range / 2, 200)).stream().filter(player -> npc.getReflection() == player.getReflection()).forEach(player -> player.sendPacket(new NpcSay(npc, ChatType.NPC_NORMAL, new CustomMessage(address, player, replacements).toString())));
    }

    public static void npcSayInRangeCustomMessage(final NpcInstance npc, final ChatType chatType, final int range, final String address, final Object... replacements) {
        if (npc == null) {
            return;
        }
        World.getAroundPlayers(npc, range, Math.max(range / 2, 200)).stream().filter(player -> npc.getReflection() == player.getReflection()).forEach(player -> player.sendPacket(new NpcSay(npc, chatType, new CustomMessage(address, player, replacements).toString())));
    }

    public static void npcSayCustomMessage(final NpcInstance npc, final ChatType chatType, final String address, final Object... replacements) {
        npcSayInRangeCustomMessage(npc, chatType, 1500, address, replacements);
    }

    public static void npcSayCustomMessage(final NpcInstance npc, final String address, final Object... replacements) {
        npcSayCustomMessage(npc, ChatType.NPC_NORMAL, address, replacements);
    }

    public static void npcSayToPlayer(final NpcInstance npc, final Player player, final String text) {
        npcSayToPlayer(npc, player, NpcString.NONE, text);
    }

    public static void npcSayToPlayer(final NpcInstance npc, final Player player, final NpcString npcString, final String... params) {
        if (npc == null) {
            return;
        }
        player.sendPacket(new NpcSay(npc, ChatType.TELL, npcString, params));
    }

    public static void npcShout(final NpcInstance npc, final String text) {
        npcShout(npc, NpcString.NONE, text);
    }

    public static void npcShout(final NpcInstance npc, final NpcString npcString, final String... params) {
        if (npc == null) {
            return;
        }
        final NpcSay cs = new NpcSay(npc, ChatType.SHOUT, npcString, params);
        final int rx = MapUtils.regionX(npc);
        final int ry = MapUtils.regionY(npc);
        final int offset = Config.SHOUT_OFFSET;
        GameObjectsStorage.getPlayers().stream().filter(player -> player.getReflection() == npc.getReflection()).forEach(player -> {
            final int tx = MapUtils.regionX(player);
            final int ty = MapUtils.regionY(player);
            if (tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) {
                return;
            }
            player.sendPacket(cs);
        });
    }

    public static void npcShoutCustomMessage(final NpcInstance npc, final String address, final Object... replacements) {
        if (npc == null) {
            return;
        }
        final int rx = MapUtils.regionX(npc);
        final int ry = MapUtils.regionY(npc);
        final int offset = Config.SHOUT_OFFSET;
        GameObjectsStorage.getPlayers().stream().filter(player -> player.getReflection() == npc.getReflection()).forEach(player -> {
            final int tx = MapUtils.regionX(player);
            final int ty = MapUtils.regionY(player);
            if ((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !npc.isInRange(player, Config.CHAT_RANGE)) {
                return;
            }
            player.sendPacket(new NpcSay(npc, ChatType.SHOUT, new CustomMessage(address, player, replacements).toString()));
        });
    }

    public static void npcSay(final NpcInstance npc, final NpcString address, final ChatType type, final int range, final String... replacements) {
        if (npc == null) {
            return;
        }
        World.getAroundPlayers(npc, range, Math.max(range / 2, 200)).stream().filter(player -> player.getReflection() == npc.getReflection()).forEach(player -> player.sendPacket(new NpcSay(npc, type, address, replacements)));
    }

    public static void addItem(final Playable playable, final int itemId, final long count) {
        ItemFunctions.addItem(playable, itemId, count, true);
    }

    public static long getItemCount(final Playable playable, final int itemId) {
        return ItemFunctions.getItemCount(playable, itemId);
    }

    public static long removeItem(final Playable playable, final int itemId, final long count) {
        return ItemFunctions.removeItem(playable, itemId, count, true);
    }

    public static boolean getPay(final Player player, final int itemid, final long count) {
        if (player.getInventory().getItemByItemId(itemid) == null) {
            player.sendMessage("Цена: " + count + ' ' + getItemName(itemid));
            player.sendMessage("У вас вообще нету " + getItemName(itemid));
            return false;
        }
        if (getItemCount(player, itemid) < count) {
            player.sendMessage("Цена: " + count + ' ' + getItemName(itemid));
            player.sendMessage("Вам не хватает " + (count - getItemCount(player, itemid)) + ' ' + getItemName(itemid));
            return false;
        }
        player.getInventory().destroyItemByItemId(itemid, count);
        return true;
    }

    public static void addItem(final Player player, final int itemId, final long count) {
        player.getInventory().addItem(itemId, count);
        final String sb = "Вы получили " + count + ' ' + getItemName(itemId);
        player.sendMessage(sb);
    }

    public static String getItemName(final int itemId) {
        return ItemTemplateHolder.getInstance().getTemplate(itemId).getName();
    }

    public static long getItemCount(final Player player, final int itemId) {
        return player.getInventory().getCountOf(itemId);
    }

    public static boolean ride(final Player player, final int pet) {
        if (player.isMounted()) {
            player.setMount(0, 0, 0);
        }
        if (player.getPet() != null) {
            player.sendPacket(Msg.YOU_ALREADY_HAVE_A_PET);
            return false;
        }
        player.setMount(pet, 0, 0);
        return true;
    }

    public static void unRide(final Player player) {
        if (player.isMounted()) {
            player.setMount(0, 0, 0);
        }
    }

    public static void unSummonPet(final Player player, final boolean onlyPets) {
        final Summon pet = player.getPet();
        if (pet == null) {
            return;
        }
        if (pet.isPet() || !onlyPets) {
            pet.unSummon();
        }
    }

    @Deprecated
    public static NpcInstance spawn(final Location loc, final int npcId) {
        return spawn(loc, npcId, ReflectionManager.DEFAULT);
    }

    @Deprecated
    public static NpcInstance spawn(final Location loc, final int npcId, final Reflection reflection) {
        return NpcUtils.spawnSingle(npcId, loc, reflection, 0L);
    }

    @Deprecated
    public static void SpawnNPCs(final int npcId, final int[][] locations, final List<SimpleSpawner> list) {
        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(npcId);
        if (template == null) {
            System.out.println("WARNING! Functions.SpawnNPCs template is null for npc: " + npcId);
            Thread.dumpStack();
            return;
        }
        for (final int[] location : locations) {
            final SimpleSpawner sp = new SimpleSpawner(template);
            final Location loc = new Location(location[0], location[1], location[2]);
            if (location.length > 3) {
                loc.setH(location[3]);
            }
            sp.setLoc(loc);
            sp.setAmount(1);
            sp.setRespawnDelay(0);
            sp.init();
            if (list != null) {
                list.add(sp);
            }
        }
    }

    public static void deSpawnNPCs(final List<SimpleSpawner> list) {
        for (final SimpleSpawner sp : list) {
            sp.deleteAll();
        }
        list.clear();
    }

    public static void teleportParty(final Party party, final Location loc, final int radius) {
        for (final Player partyMember : party.getPartyMembers()) {
            partyMember.teleToLocation(Location.findPointToStay(loc, radius, partyMember.getGeoIndex()));
        }
    }

    public static boolean IsActive(final String name) {
        return "on".equalsIgnoreCase(ServerVariables.getString(name, "off"));
    }

    public static boolean SetActive(final String name, final boolean active) {
        if (active == IsActive(name)) {
            return false;
        }
        if (active) {
            ServerVariables.set(name, "on");
        } else {
            ServerVariables.unset(name);
        }
        return true;
    }

    public static boolean SimpleCheckDrop(final Creature mob, final Creature killer) {
        return mob != null && mob.isMonster() && !mob.isRaid() && killer != null && killer.getPlayer() != null && killer.getLevel() - mob.getLevel() < 9;
    }

    public static boolean isPvPEventStarted() {
        return (boolean) callScripts("events.tvt.TvT", "isRunned", new Object[0])
                || (boolean) callScripts("events.lasthero.LastHero", "isRunned", new Object[0])
                || (boolean) callScripts("events.ctf.CtF", "isRunned", new Object[0])
                || (boolean) callScripts("events.deathmatch.DeathMatch", "isRunned", new Object[0])
                || (boolean) callScripts("events.koreanstyle.KoreanStyle", "isRunned", new Object[0]);
    }

    public static MultiValueSet<String> parseParams(final String mapText) {
        final MultiValueSet<String> result = new MultiValueSet<>();
        final char[] chs = mapText.toCharArray();
        final StringBuilder sb = new StringBuilder();
        String key = null;
        String val;
        for (final char ch : chs) {
            if (ch == '=' && key == null) {
                key = sb.toString();
                sb.setLength(0);
            } else if (ch == '&') {
                val = sb.toString();
                result.put(key, val);
                sb.setLength(0);
                key = null;
            } else {
                sb.append(ch);
            }
        }
        if (key != null) {
            val = sb.toString();
            result.put(key, val);
        }
        return result;
    }

    public static void sendDebugMessage(final Player player, final String message) {
        if (!player.isGM()) {
            return;
        }
        player.sendAdminMessage(message);
    }

    public static void sendSystemMail(final Player receiver, final String title, final String body, final Map<Integer, Long> items) {
        if (receiver == null || !receiver.isOnline()) {
            return;
        }
        if (title == null) {
            return;
        }
        if (items.keySet().size() > 8) {
            return;
        }
        final Mail mail = new Mail();
        mail.setSenderId(1);
        mail.setSenderName("Admin");
        mail.setReceiverId(receiver.getObjectId());
        mail.setReceiverName(receiver.getName());
        mail.setTopic(title);
        mail.setBody(body);
        items.forEach((key, value) -> {
            final ItemInstance item = ItemFunctions.createItem(key);
            item.setLocation(ItemLocation.MAIL);
            item.setCount(value);
            item.setOwnerId(receiver.getObjectId());
            item.save();
            mail.addAttachment(item);
        });
        mail.setType(SenderType.NEWS_INFORMER);
        mail.setUnread(true);
        mail.setExpireTime(2592000 + (int) (System.currentTimeMillis() / 1000L));
        mail.save();
        receiver.sendPacket(ExNoticePostArrived.STATIC_TRUE);
        receiver.sendPacket(Msg.THE_MAIL_HAS_ARRIVED);
    }

    public static String truncateHtmlTagsSpaces(final String srcHtml) {
        final StringBuilder dstHtml = new StringBuilder(srcHtml.length());
        final StringBuilder buff = new StringBuilder();
        boolean doBuff = false;
        for (int srcIdx = 0, srcLen = srcHtml.length(); srcIdx < srcLen; ++srcIdx) {
            final char srcCh = srcHtml.charAt(srcIdx);
            if (srcCh == '<') {
                doBuff = false;
                if (buff.length() > 0) {
                    dstHtml.append(StringUtils.trim(buff.toString()));
                    buff.setLength(0);
                }
            }
            if (!doBuff) {
                dstHtml.append(srcCh);
            } else {
                buff.append(srcCh);
            }
            if (srcCh == '>') {
                doBuff = true;
            }
        }
        if (buff.length() > 0) {
            dstHtml.append(StringUtils.trim(buff.toString()));
            buff.setLength(0);
        }
        return dstHtml.toString();
    }

    public static Map<String, ScheduledFuture<?>> ScheduleTimeStarts(final Runnable r, final String[] times) {
        final Map<String, ScheduledFuture<?>> result = new HashMap<>();
        if (r == null || times == null || times.length == 0) {
            return result;
        }
        final Calendar currentTime = Calendar.getInstance();
        final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
        for (final String str_time : times) {
            final String[] spl_time = str_time.trim().split(":");
            final int hour = Integer.parseInt(spl_time[0].trim());
            final int minute = Integer.parseInt(spl_time[1].trim());
            final Calendar nextStartTime = Calendar.getInstance();
            nextStartTime.set(Calendar.HOUR_OF_DAY, hour);
            nextStartTime.set(Calendar.MINUTE, minute);
            if (nextStartTime.getTimeInMillis() < currentTime.getTimeInMillis()) {
                nextStartTime.add(Calendar.DATE, 1);
            }
            final long millsLeft = nextStartTime.getTimeInMillis() - currentTime.getTimeInMillis();
            if (millsLeft > 0L) {
                result.put(sdf.format(nextStartTime.getTime()), ThreadPoolManager.getInstance().schedule(r, millsLeft));
            }
        }
        return result;
    }

    public void show(final String text, final Player self) {
        show(text, self, getNpc());
    }

    public Player getSelf() {
        return self.get();
    }

    public NpcInstance getNpc() {
        return npc.get();
    }
}
