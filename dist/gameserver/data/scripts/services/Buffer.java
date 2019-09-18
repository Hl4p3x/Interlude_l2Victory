package services;

import npc.model.NpcBufferInstance;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.handler.items.IItemHandler;
import ru.j2dev.gameserver.handler.items.ItemHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.ServerVariables;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShowBoard;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillCoolTime;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.skills.TimeStamp;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Buffer extends Functions implements OnInitScriptListener {
    private static final Logger LOG = LoggerFactory.getLogger(Buffer.class);
    private static final String DISPLAY_NAME = "Buffer";
    private static final String LIST_DELIMITERS = ";,";
    private static final String BUFF_PROFILE_REC_VAR = "BuffProfRec";
    private static final String BUFF_PROFILE_VAR_PREFIX = "BuffProf-";
    private static final ConcurrentMap<Integer, BuffTemplate> BUFF_TEMPLATES = new ConcurrentHashMap<>();
    private static final int CANCEL_MENU_ID = -2;
    private static final long CANCEL_ADENA_PRICE = 100L;
    private static final int RESTORE_CP_MP_HP_MENU_ID = -3;
    private static final long RESTORE_ADENA_PRICE = 50L;
    private static final int SHARED_REUSE_GROUP = -99999;
    private static final int PROFILE_MAX_PROFILES = 5;
    private static final int PROFILE_REC_MENU_ID = -100;
    private static final int PROFILE_BASE_MENU_ID = -1000;
    private static final int PROFILE_SAVE_MENU_ID = -2000;
    private static final SAXBuilder READER = new SAXBuilder();
    private static final File BUFF_TEMPLATES_FILE = new File(Config.DATAPACK_ROOT, "data/buff_templates.xml");
    private static BuffItemHandler BUFF_ITEMS_HANDLER;
    private static int[] BUFF_ITEMS_IDS;

    private static boolean checkReuse(final Player player) {
        if (player.isSharedGroupDisabled(-99999)) {
            player.sendPacket(new SystemMessage(48).addString("Buffer"));
            return false;
        }
        final TimeStamp timeStamp = new TimeStamp(-99999, System.currentTimeMillis() + Config.ALT_NPC_BUFFER_REUSE_DELAY, Config.ALT_NPC_BUFFER_REUSE_DELAY);
        player.addSharedGroupReuse(-99999, timeStamp);
        return true;
    }

    private static void applyMenuItem(final int menuId, final Player player) {
        switch (menuId) {
            case PROFILE_REC_MENU_ID: {
                player.setVar("BuffProfRec", "", -1L);
                break;
            }
            case CANCEL_MENU_ID: {
                if (player.isInCombat()) {
                    player.sendPacket(new SystemMessage(113).addString("Buffer"));
                    return;
                }
                if (!player.reduceAdena(100L, true)) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
                if (player.getPet() != null) {
                    player.getPet().getEffectList().stopAllEffects();
                }
                player.getEffectList().stopAllEffects();
                player.sendPacket(new SkillCoolTime(player));
                break;
            }
            case RESTORE_CP_MP_HP_MENU_ID: {
                if (player.isInCombat()) {
                    player.sendPacket(new SystemMessage(113).addString("Buffer"));
                    return;
                }
                if (!player.reduceAdena(50L, true)) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    return;
                }
                if (!checkReuse(player)) {
                    break;
                }
                player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.sendStatusUpdate(true, false, 9, 11, 33, 10, 12, 34);
                if (player.getPet() != null) {
                    final Summon summon = player.getPet();
                    summon.setCurrentHpMp((double) summon.getMaxHp(), (double) summon.getMaxMp(), false);
                    break;
                }
                break;
            }
            default: {
                if (menuId > 0) {
                    if (checkReuse(player)) {
                        final BuffTemplate buffTemplate = BUFF_TEMPLATES.get(menuId);
                        if (buffTemplate != null) {
                            buffTemplate.apply(player);
                            AddProfileRec(player, menuId);
                        } else {
                            LOG.warn("Buffer: Unknown menuId \"" + menuId + "\" used.");
                        }
                        break;
                    }
                    break;
                } else {
                    if (menuId <= -2000 && menuId > -2005) {
                        final int profileNum = Math.abs(-2000 - menuId);
                        SaveProfile(player, profileNum);
                        break;
                    }
                    if (menuId > -1000 || menuId <= -1005) {
                        break;
                    }
                    final int profileNum = Math.abs(-1000 - menuId);
                    if (checkReuse(player)) {
                        ApplyProfile(player, profileNum);
                        break;
                    }
                    break;
                }
            }
        }
    }

    private static void ApplyProfile(final Player player, final int profileNum) {
        if (profileNum > 5) {
            return;
        }
        final String profileStr = player.getVar(String.format("%s%d", "BuffProf-", profileNum));
        if (profileStr == null || profileStr.trim().isEmpty()) {
            player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.EmptyProfile", player));
            return;
        }
        final StringTokenizer st = new StringTokenizer(profileStr.trim(), ";,", false);
        final List<BuffTemplate> profileBuffTemplates = new LinkedList<>();
        while (st.hasMoreTokens()) {
            final String menuIdStr = st.nextToken();
            if (menuIdStr.isEmpty()) {
                continue;
            }
            try {
                final int menuId = Integer.parseInt(menuIdStr);
                final BuffTemplate buffTemplate = BUFF_TEMPLATES.get(menuId);
                if (buffTemplate == null) {
                    continue;
                }
                profileBuffTemplates.add(buffTemplate);
            } catch (NumberFormatException nfe) {
                LOG.error("Buffer: Can't apply profile \"" + profileNum + "\"", nfe);
            }
        }
        ThreadPoolManager.getInstance().execute(() -> {
                for (final BuffTemplate buffTemplate : profileBuffTemplates) {
                    if (!buffTemplate.applySync(player)) {
                        return;
                    }
            }
        });
    }

    private static void SaveProfile(final Player player, final int profileNum) {
        final String currRec = player.getVar("BuffProfRec");
        if (profileNum > 5 || currRec == null) {
            player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.CantSaveProfile", player));
            return;
        }
        player.setVar(String.format("%s%d", "BuffProf-", profileNum), currRec, -1L);
        player.unsetVar("BuffProfRec");
        player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.ProfileSaved", player));
    }

    private static void AddProfileRec(final Player player, final int menuId) {
        String currRec = player.getVar("BuffProfRec");
        if (currRec == null) {
            return;
        }
        if (currRec.trim().isEmpty()) {
            player.setVar("BuffProfRec", String.format("%d", menuId), -1L);
        } else {
            currRec += String.format(";%d", menuId);
            if (currRec.length() > 250) {
                player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.LimitProfile", player));
                player.unsetVar("BuffProf-");
            } else {
                player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.BuffAdded", player));
                player.setVar("BuffProfRec", currRec.trim(), -1L);
            }
        }
    }

    private static boolean haveBuffItem(final Player player) {
        return Arrays.stream(BUFF_ITEMS_IDS).anyMatch(BUFF_ITEMS_ID -> Functions.getItemCount(player, BUFF_ITEMS_ID) > 0L);
    }

    private static boolean isAllowedNpc(final Player player, final NpcInstance npc) {
        return npc instanceof NpcBufferInstance && !player.isActionsDisabled() && (Config.ALLOW_TALK_WHILE_SITTING || !player.isSitting()) && npc.isInActingRange(player);
    }

    public static void showPage(final Player player, final String page, final NpcInstance npc) {
        if (page == null || page.contains("..")) {
            return;
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        if(npc.getTemplate().getHtmRoot() != null) {
            html.setFile(npc.getTemplate().getHtmRoot()+ page +".htm");
        } else {
            html.setFile("mods/buffer/" + page + ".htm");
        }
        player.sendPacket(html);
    }

    public static void showBBSPage(final Player player, final String page) {
        if (page == null || page.contains("..")) {
            return;
        }
        final String html = HtmCache.getInstance().getNotNull("mods/buffer/community_buffer/" + page + ".htm", player);
        ShowBoard.separateAndSend(html, player);
    }

    private static void LoadBuffItems() {
        final String buffItemIdsText = ServerVariables.getString("BuffItemIds", "3433");
        final StringTokenizer st = new StringTokenizer(buffItemIdsText, ";,", false);
        final List<Integer> buffItemIds = new ArrayList<>();
        while (st.hasMoreTokens()) {
            final int handleBuffItemId = Integer.parseInt(st.nextToken());
            buffItemIds.add(handleBuffItemId);
        }
        if (BUFF_ITEMS_HANDLER != null) {
            ItemHandler.getInstance().unregisterItemHandler(BUFF_ITEMS_HANDLER);
        }
        BUFF_ITEMS_IDS = new int[buffItemIds.size()];
        for (int buffItemIdx = 0; buffItemIdx < buffItemIds.size(); ++buffItemIdx) {
            BUFF_ITEMS_IDS[buffItemIdx] = buffItemIds.get(buffItemIdx);
        }
        BUFF_ITEMS_HANDLER = new BuffItemHandler();
        ItemHandler.getInstance().registerItemHandler(BUFF_ITEMS_HANDLER);
        LOG.info("Buffer: Loaded " + BUFF_ITEMS_IDS.length + " buff item(s).");
    }

    private static void loadBuffTemplates() {
        final ConcurrentMap<Integer, BuffTemplate> templates = new ConcurrentHashMap<>();
        try {
            final Document document = READER.build(BUFF_TEMPLATES_FILE);
            for (Element templatesElement : document.getRootElement().getChildren()) {
                if ("template".equalsIgnoreCase(templatesElement.getName())) {
                    final int menuId = Integer.parseInt(templatesElement.getAttributeValue("menuId"));
                    final BuffTarget buffTarget = BuffTarget.valueOf(templatesElement.getAttributeValue("target").toUpperCase());
                    final int minLevel = Integer.parseInt(templatesElement.getAttributeValue("minLevel", "0"));
                    final List<BuffTemplateConsume> consumes = new ArrayList<>();
                    boolean consumeAnyFirst = false;
                    final List<Skill> produces = new ArrayList<>();
                    for (Element templateElement : templatesElement.getChildren()) {
                        if ("consume".equalsIgnoreCase(templateElement.getName())) {
                            consumeAnyFirst = Boolean.parseBoolean(templateElement.getAttributeValue("anyFirst", "false"));
                            for (Element consumeElement : templateElement.getChildren()) {
                                if ("item".equalsIgnoreCase(consumeElement.getName())) {
                                    final int itemId = Integer.parseInt(consumeElement.getAttributeValue("id"));
                                    final long amount = Long.parseLong(consumeElement.getAttributeValue("amount"));
                                    final int fromLevel = Integer.parseInt(consumeElement.getAttributeValue("from_level", "0"));
                                    final ItemTemplate itemTemplate = ItemTemplateHolder.getInstance().getTemplate(itemId);
                                    consumes.add(new BuffTemplateConsume(itemTemplate, amount, fromLevel));
                                }
                            }
                        } else {
                            if (!"produce".equalsIgnoreCase(templateElement.getName())) {
                                continue;
                            }
                            for (Element produceElement : templateElement.getChildren()) {
                                if ("skill".equalsIgnoreCase(produceElement.getName())) {
                                    final int skillId = Integer.parseInt(produceElement.getAttributeValue("id"));
                                    final int skillLevel = Integer.parseInt(produceElement.getAttributeValue("level"));
                                    final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLevel);
                                    produces.add(skill);
                                }
                            }
                        }
                    }
                    final BuffTemplate buffTemplate = new BuffTemplate(produces, buffTarget, consumes, consumeAnyFirst, minLevel);
                    templates.put(menuId, buffTemplate);
                }
            }
            BUFF_TEMPLATES.clear();
            BUFF_TEMPLATES.putAll(templates);
            LOG.info("Buffer: Loaded " + templates.size() + " buff template(s).");
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    private static Map<String, String> ParseArgs(final String argsText) {
        final Map<String, String> result = new TreeMap<>();
        final char[] chs = argsText.toCharArray();
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

    private static List<Skill> ParseSkillListText(final String templateText) {
        final List<Skill> result = new LinkedList<>();
        final StringTokenizer st = new StringTokenizer(templateText, ";,", false);
        while (st.hasMoreTokens()) {
            final String skillDefStr = st.nextToken();
            final int skillDefSplIdx = skillDefStr.indexOf(45);
            if (skillDefSplIdx > 0) {
                try {
                    final String skillIdStr = skillDefStr.substring(0, skillDefSplIdx);
                    final String skillLvlStr = skillDefStr.substring(skillDefSplIdx + 1);
                    final int skillId = Integer.parseInt(skillIdStr);
                    final int skillLvl = Integer.parseInt(skillLvlStr);
                    final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                    if (skill == null) {
                        LOG.error("Buffer: Buff template \"" + skillDefStr + "\" skill " + skillId + "-" + skillLvl + " undefined!");
                    } else {
                        result.add(skill);
                    }
                } catch (NumberFormatException nfe) {
                    LOG.error("Buffer: Can't parse buff template \"" + skillDefStr + "\"", nfe);
                }
            } else {
                LOG.error("Buffer: Can't parse buff template \"" + skillDefStr + "\"");
            }
        }
        return result;
    }

    public void act(final String[] args_) {
        final Player player = getSelf();
        if (player == null || args_ == null || args_.length == 0) {
            return;
        }
        NpcInstance npc = null;
        if (!haveBuffItem(player)) {
            npc = getNpc();
            if (!isAllowedNpc(player, npc)) {
                player.sendActionFailed();
                return;
            }
        }
        if (player.isInCombat()) {
            player.sendActionFailed();
            return;
        }
        final Map<String, String> args = ParseArgs(args_[0]);
        final String menuIdStr = args.get("ask");
        final String backPage = args.get("reply");
        if (menuIdStr != null && !menuIdStr.isEmpty()) {
            try {
                final int menuId = Integer.parseInt(menuIdStr);
                applyMenuItem(menuId, player);
            } catch (Exception ex) {
                LOG.error("Buffer: Can't apply buff of menuId = \"" + menuIdStr + "\"", ex);
            }
        }
        if (backPage != null && !backPage.isEmpty()) {
            showPage(player, backPage, npc);
        } else {
            player.sendActionFailed();
        }
    }

    public void actBBS(final String[] args_) {
        final Player player = getSelf();
        if (player == null || args_ == null || args_.length == 0) {
            return;
        }
        if (player.isInCombat()) {
            player.sendActionFailed();
            return;
        }
        final Map<String, String> args = ParseArgs(args_[0]);
        final String menuIdStr = args.get("ask");
        final String backPage = args.get("reply");
        if (menuIdStr != null && !menuIdStr.isEmpty()) {
            try {
                final int menuId = Integer.parseInt(menuIdStr);
                applyMenuItem(menuId, player);
            } catch (Exception ex) {
                LOG.error("Buffer: Can't apply buff of menuId = \"" + menuIdStr + "\"", ex);
            }
        }
        if (backPage != null && !backPage.isEmpty()) {
            showBBSPage(player, backPage);
        } else {
            player.sendActionFailed();
        }
    }

    @Override
    public void onInit() {
        loadBuffTemplates();
        LoadBuffItems();
    }

    public enum BuffTarget {
        BUFF_PLAYER,
        BUFF_PET
    }

    private static class BuffTemplateConsume {
        private final ItemTemplate _item;
        private final long _amount;
        private final int _formLevel;

        public BuffTemplateConsume(final ItemTemplate item, final long amount, final int formLevel) {
            _item = item;
            _amount = amount;
            _formLevel = formLevel;
        }

        public ItemTemplate getItem() {
            return _item;
        }

        public long getAmount() {
            return _amount;
        }

        public int getFormLevel() {
            return _formLevel;
        }

        public boolean mayConsume(final Player player) {
            final long count = player.getInventory().getCountOf(getItem().getItemId());
            if (count < getAmount()) {
                if (getItem().isAdena()) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                } else {
                    player.sendMessage(new CustomMessage("scripts.npc.model.L2NpcBufferInstance.RequiresS1S2", player, getItem().getName(), getAmount()));
                }
                return false;
            }
            return true;
        }

        public boolean consume(final Player player) {
            if (getAmount() == 0L) {
                return player.getInventory().getCountOf(getItem().getItemId()) > 0L;
            }
            return ItemFunctions.removeItem(player, getItem().getItemId(), getAmount(), true) >= getAmount();
        }
    }

    private static class BuffItemHandler implements IItemHandler {
        @Override
        public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
            try {
                final Player player = playable.getPlayer();
                if (player != null) {
                    showPage(playable.getPlayer(), "item-" + item.getItemId(), null);
                    return true;
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            return false;
        }

        @Override
        public void dropItem(final Player player, final ItemInstance item, final long count, final Location loc) {
            if (!player.getInventory().destroyItem(item, count)) {
                player.sendActionFailed();
                return;
            }
            Log.LogItem(player, ItemLog.Delete, item);
            player.disableDrop(1000);
            player.sendChanges();
        }

        @Override
        public boolean pickupItem(final Playable playable, final ItemInstance item) {
            return false;
        }

        @Override
        public int[] getItemIds() {
            return BUFF_ITEMS_IDS;
        }
    }

    private static class BuffTemplate {
        private final List<Skill> _buffs;
        private final BuffTarget _target;
        private final List<BuffTemplateConsume> _consumes;
        private final boolean _consumeAnyFirst;
        private final int _minLevel;

        BuffTemplate(final List<Skill> buffs, final BuffTarget target, final List<BuffTemplateConsume> consumes, final boolean consumeAnyFirst, final int minLevel) {
            _buffs = buffs;
            _target = target;
            _consumes = consumes;
            _consumeAnyFirst = consumeAnyFirst;
            _minLevel = minLevel;
        }

        private void applyBuff(final Creature target) {
            if (target == null) {
                return;
            }
            boolean warmOffMsg = false;
            target.block();
            try {
                for (final Skill sk : _buffs) {
                    for (final Effect e : target.getEffectList().getAllEffects()) {
                        if (e.getSkill().getId() == sk.getId()) {
                            e.exit();
                            warmOffMsg = true;
                        }
                    }
                    if (warmOffMsg) {
                        target.sendPacket(new SystemMessage(92).addSkillName(sk.getDisplayId(), sk.getDisplayLevel()));
                    }
                    if (Config.ALT_NPC_BUFFER_EFFECT_TIME > 0L) {
                        sk.getEffects(target, target, false, false, Config.ALT_NPC_BUFFER_EFFECT_TIME, 1.0, false);
                    } else {
                        sk.getEffects(target, target, false, false);
                    }
                }
            } finally {
                target.unblock();
            }
        }

        private Creature aimingTarget(final Creature target) {
            switch (_target) {
                case BUFF_PLAYER: {
                    return target.getPlayer();
                }
                case BUFF_PET: {
                    return target.getPet();
                }
                default: {
                    return null;
                }
            }
        }

        private boolean consumeRequirements(final Player player) {
            if (!_consumeAnyFirst) {
                for (final BuffTemplateConsume buffTemplateConsume : _consumes) {
                    if (buffTemplateConsume.getFormLevel() > 0 && player.getLevel() < buffTemplateConsume.getFormLevel()) {
                        continue;
                    }
                    if (!buffTemplateConsume.mayConsume(player)) {
                        return false;
                    }
                }
            }
            for (final BuffTemplateConsume buffTemplateConsume : _consumes) {
                if (buffTemplateConsume.getFormLevel() > 0 && player.getLevel() < buffTemplateConsume.getFormLevel()) {
                    continue;
                }
                if (!_consumeAnyFirst) {
                    if (!buffTemplateConsume.consume(player)) {
                        return false;
                    }
                } else {
                    if (buffTemplateConsume.consume(player)) {
                        return true;
                    }
                }
            }
            return !_consumeAnyFirst;
        }

        public boolean canApply(final Player player) {
            return !player.isInZone(ZoneType.SIEGE) && !player.isInZone(ZoneType.water) && !player.isInDuel() && !player.isOlyParticipant() && player.getLevel() >= _minLevel;
        }

        public boolean apply(final Player player) {
            if (!canApply(player)) {
                player.sendPacket(new SystemMessage(113).addString("Buffer"));
                return false;
            }
            ThreadPoolManager.getInstance().execute(new RunnableImpl() {
                @Override
                public void runImpl() {
                    final Creature target = aimingTarget(player);
                    if (target == null) {
                        player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
                        return;
                    }
                    if (!consumeRequirements(player)) {
                        player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
                        return;
                    }
                    applyBuff(target);
                }
            });
            return true;
        }

        public boolean applySync(final Player player) {
            synchronized (player) {
                if (!canApply(player)) {
                    player.sendPacket(new SystemMessage(113).addString("Buffer"));
                    return false;
                }
                final Creature target = aimingTarget(player);
                if (target == null) {
                    player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
                    return false;
                }
                if (!consumeRequirements(player)) {
                    return false;
                }
                applyBuff(target);
            }
            return true;
        }
    }
}
