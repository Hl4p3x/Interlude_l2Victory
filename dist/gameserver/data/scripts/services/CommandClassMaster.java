package services;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.actor.player.OnGainExpSpListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.PlayerListenerList;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

import java.util.*;

public class CommandClassMaster extends Functions implements OnInitScriptListener, IVoicedCommandHandler {
    private static final CommandClassMaster INSTANCE = new CommandClassMaster();
    private static final String SCRIPT_BYPASS_CLASS = "scripts_" + CommandClassMaster.class.getName();
    private static final String SHOW_COUNT_VAR_NAME = "cmd_class_master_show_cnt";
    private static Map<ClassId, ClassMasterPath> CLASS_INFOS = Collections.emptyMap();

    private String[] _voiceCommands;

    public CommandClassMaster() {
        _voiceCommands = ArrayUtils.EMPTY_STRING_ARRAY;
    }

    protected static CommandClassMaster getInstance() {
        return CommandClassMaster.INSTANCE;
    }

    private static ClassMasterPath getClassInfoForClassId(final ClassId classId) {
        return CommandClassMaster.CLASS_INFOS.get(classId);
    }

    private static int getMinPlayerLevelForClassId(final int classLevel) {
        switch (classLevel) {
            case 1: {
                return 1;
            }
            case 2: {
                return 20;
            }
            case 3: {
                return 40;
            }
            case 4: {
                return 76;
            }
            default: {
                return -1;
            }
        }
    }

    private static String getMinPlayerLevelForClassIdMessageAddress(final int classLevel) {
        switch (classLevel) {
            case 2: {
                return "ClassMaster.Need20Level";
            }
            case 3: {
                return "ClassMaster.Need40Level";
            }
            case 4: {
                return "ClassMaster.Need76Level";
            }
            default: {
                return "ClassMaster.NothingToUp";
            }
        }
    }

    private static List<Pair<Integer, Long>> parseItems(final String itemsStr) {
        final StringTokenizer itemListCfgTok = new StringTokenizer(itemsStr, ",");
        final List<Pair<Integer, Long>> result = new LinkedList<>();
        while (itemListCfgTok.hasMoreTokens()) {
            final String itemEntryCfg = itemListCfgTok.nextToken().trim();
            if (itemEntryCfg.isEmpty()) {
                continue;
            }
            final int itemCountDelim = itemEntryCfg.indexOf(":");
            if (itemCountDelim < 0) {
                throw new RuntimeException("Can't parse items \"" + itemsStr + "\"");
            }
            final int itemId = Integer.parseInt(itemEntryCfg.substring(0, itemCountDelim).trim());
            final long itemCount = Long.parseLong(itemEntryCfg.substring(itemCountDelim + 1).trim());
            result.add(Pair.of(itemId, itemCount));
        }
        return Collections.unmodifiableList(result);
    }

    private static Map<ClassId, ClassMasterPath> parseConfig(final String cfgStr) {
        final Map<ClassId, ClassMasterPath> result = new TreeMap<>();
        final StringTokenizer classLvlCfgTok = new StringTokenizer(cfgStr.trim(), ";");
        while (classLvlCfgTok.hasMoreTokens()) {
            final String classLvlCfg = classLvlCfgTok.nextToken().trim();
            final int priceDelimIdx = classLvlCfg.indexOf("-");
            List<Pair<Integer, Long>> price = Collections.emptyList();
            List<Pair<Integer, Long>> reward = Collections.emptyList();
            int classLevelOrd;
            if (priceDelimIdx < 0) {
                classLevelOrd = Integer.parseInt(classLvlCfg.trim());
            } else {
                classLevelOrd = Integer.parseInt(classLvlCfg.substring(0, priceDelimIdx).trim());
                final String priceAndRewardStr = classLvlCfg.substring(priceDelimIdx + 1).trim();
                final int rewardDelimIdx = priceAndRewardStr.indexOf("/");
                if (rewardDelimIdx < 0) {
                    price = parseItems(priceAndRewardStr);
                } else {
                    price = parseItems(priceAndRewardStr.substring(0, rewardDelimIdx).trim());
                    reward = parseItems(priceAndRewardStr.substring(rewardDelimIdx + 1).trim());
                }
            }
            for (final ClassId fromClassId : ClassId.VALUES) {
                if (fromClassId.getLevel() == classLevelOrd) {
                    final List<ClassId> availableClassIds = new LinkedList<>();
                    for (final ClassId availableClassId : ClassId.VALUES) {
                        if (availableClassId.childOf(fromClassId) && fromClassId.getLevel() + 1 == availableClassId.getLevel()) {
                            availableClassIds.add(availableClassId);
                        }
                    }
                    result.put(fromClassId, new ClassMasterPath(availableClassIds, fromClassId, price, reward));
                }
            }
        }
        return Collections.unmodifiableMap(result);
    }

    private boolean canProcess(final Player player, final boolean notify) {
        if (player == null || player.isLogoutStarted() || player.isOutOfControl() || player.isDead() || player.isInDuel() || player.isAlikeDead() || player.isOlyParticipant() || player.isFlying() || player.isSitting() || player.getTeam() != TeamType.NONE || player.isInStoreMode() || player.entering) {
            if (notify && player != null && !player.entering) {
                player.sendMessage(new CustomMessage("common.TryLater", player));
            }
            return false;
        }
        return true;
    }

    private void showClassMasterPath(final Player player, final ClassMasterPath classMasterPath) {
        if (!canProcess(player, false) || classMasterPath == null || classMasterPath.getAvailableClassIds().isEmpty()) {
            return;
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, null);
        html.setFile("scripts/services/command_class_master.htm");
        final StringBuilder classMasterListHtml = new StringBuilder();
        for (final ClassId classId : classMasterPath.getAvailableClassIds()) {
            classMasterListHtml.append("<button width=140 height=21 back=l2ui_ch3.msnbutton fore=l2ui_ch3.msnbutton ").append("action=\"bypass -h ").append(CommandClassMaster.SCRIPT_BYPASS_CLASS).append(":classMaster ").append(classId.getId()).append("\" ").append("value=\"").append(new CustomMessage(String.format("ClassName.%d", classId.getId()), player)).append("\">").append("<br1>");
        }
        html.replace("%class_master_list%", classMasterListHtml.toString());
        final StringBuilder requiredListHtml = new StringBuilder();
        for (final Pair<Integer, Long> requiredItem : classMasterPath.getPrice()) {
            final ItemTemplate itemTemplate = ItemTemplateHolder.getInstance().getTemplate(requiredItem.getKey());
            String reqItemHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.CommandClassMaster.requiredItem");
            reqItemHtml = reqItemHtml.replace("%item_id%", String.valueOf(requiredItem.getKey()));
            reqItemHtml = reqItemHtml.replace("%item_name%", itemTemplate.getName());
            reqItemHtml = reqItemHtml.replace("%item_count%", String.valueOf(requiredItem.getValue()));
            requiredListHtml.append(reqItemHtml);
        }
        html.replace("%required_items_list%", requiredListHtml.toString());
        final StringBuilder rewardListHtml = new StringBuilder();
        for (final Pair<Integer, Long> rewardItem : classMasterPath.getReward()) {
            final ItemTemplate itemTemplate2 = ItemTemplateHolder.getInstance().getTemplate(rewardItem.getKey());
            String rewItemHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.CommandClassMaster.rewardItem");
            rewItemHtml = rewItemHtml.replace("%item_id%", String.valueOf(rewardItem.getKey()));
            rewItemHtml = rewItemHtml.replace("%item_name%", itemTemplate2.getName());
            rewItemHtml = rewItemHtml.replace("%item_count%", String.valueOf(rewardItem.getValue()));
            rewardListHtml.append(rewItemHtml);
        }
        html.replace("%reward_items_list%", rewardListHtml.toString());
        player.sendPacket(html);
    }

    private void showClassMasterPath(final Player player) {
        final ClassMasterPath classMasterPath = CommandClassMaster.CLASS_INFOS.get(player.getClassId());
        if (classMasterPath == null || classMasterPath.getAvailableClassIds().isEmpty() || classMasterPath.getMinPlayerLevel() > player.getLevel()) {
            return;
        }
        showClassMasterPath(player, classMasterPath);
    }

    public void classMaster() {
        classMaster(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public void classMaster(final String... args) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.COMMAND_CLASS_MASTER_ENABLED) {
            player.sendMessage(new CustomMessage("common.Disabled", player));
            return;
        }
        if (!canProcess(player, true)) {
            return;
        }
        final ClassId currentClassId = player.getClassId();
        final ClassMasterPath classMasterPath = CommandClassMaster.CLASS_INFOS.get(currentClassId);
        if (classMasterPath == null) {
            show(new CustomMessage("ClassMaster.NothingToUp", player), player);
            return;
        }
        if (classMasterPath.getMinPlayerLevel() > player.getLevel()) {
            player.sendMessage(new CustomMessage(getMinPlayerLevelForClassIdMessageAddress(currentClassId.getLevel() + 1), player));
            return;
        }
        if (args.length > 0) {
            final int requiredClassIdOrd = Integer.parseInt(args[0]);
            ClassId requiredClassId = null;
            for (final ClassId classId : classMasterPath.getAvailableClassIds()) {
                if (classId.getId() == requiredClassIdOrd) {
                    requiredClassId = classId;
                    break;
                }
            }
            if (requiredClassId == null) {
                return;
            }
            for (final Pair<Integer, Long> requiredItem : classMasterPath.getPrice()) {
                if (ItemFunctions.getItemCount(player, requiredItem.getKey()) < requiredItem.getValue()) {
                    if (requiredItem.getKey() == 57) {
                        player.sendPacket(SystemMsg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                    } else {
                        player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                    }
                    return;
                }
            }
            long weight = 0L;
            long slots = 0L;
            for (final Pair<Integer, Long> rewardItem : classMasterPath.getReward()) {
                final ItemTemplate rewardItemTemplate = ItemTemplateHolder.getInstance().getTemplate(rewardItem.getKey());
                weight += rewardItemTemplate.getWeight() * rewardItem.getValue();
                slots += rewardItemTemplate.isStackable() ? 1L : rewardItem.getValue();
            }
            if (!player.getInventory().validateWeight(weight)) {
                player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!player.getInventory().validateCapacity(slots)) {
                player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            for (final Pair<Integer, Long> requiredItem2 : classMasterPath.getPrice()) {
                if (ItemFunctions.removeItem(player, requiredItem2.getKey(), requiredItem2.getValue(), true) < requiredItem2.getValue()) {
                    return;
                }
            }
            changeClass(player, requiredClassId);
            for (final Pair<Integer, Long> rewardItem : classMasterPath.getReward()) {
                ItemFunctions.addItem(player, rewardItem.getKey(), rewardItem.getValue(), true);
            }
        }
        showClassMasterPath(player);
    }

    private void changeClass(final Player player, final ClassId classId) {
        if (player.getClassId().getLevel() == 3) {
            player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS);
        } else {
            player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
        }
        player.setClassId(classId.getId(), false, false);
        player.broadcastCharInfo();
        player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 0, 0L));
    }

    private class OnGainExpSpListenerImpl implements OnGainExpSpListener {

        @Override
        public void onGainExpSp(final Player player, final long exp, final long sp) {
            if (!Config.COMMAND_CLASS_MASTER_ENABLED || Config.COMMAND_CLASS_POPUP_LIMIT == 0) {
                return;
            }
            if (!canProcess(player, false)) {
                return;
            }
            final ClassMasterPath classMasterPath = getClassInfoForClassId(player.getClassId());
            if (classMasterPath == null || classMasterPath.getMinPlayerLevel() > player.getLevel()) {
                return;
            }
            if (Config.COMMAND_CLASS_POPUP_LIMIT == -1) {
                showClassMasterPath(player);
            } else if (Config.COMMAND_CLASS_POPUP_LIMIT > 0) {
                final int cntVarVal = player.getVarInt("cmd_class_master_show_cnt", 0);
                final int cntClassId = cntVarVal & 0xFF;
                final int cntVal = cntVarVal >> 8;
                if (cntClassId != player.getActiveClassId() || cntVal < Config.COMMAND_CLASS_POPUP_LIMIT) {
                    player.setVar("cmd_class_master_show_cnt", cntVal + 1 << 8 | (player.getActiveClassId() & 0xFF), -1L);
                    showClassMasterPath(player);
                }
            }
        }
    }

    @Override
    public void onInit() {
        if (Config.COMMAND_CLASS_MASTER_ENABLED) {
            CommandClassMaster.CLASS_INFOS = parseConfig(Config.COMMAND_CLASS_MASTER_CLASSES);
            if (Config.COMMAND_CLASS_POPUP_LIMIT != 0) {
                PlayerListenerList.addGlobal(new OnGainExpSpListenerImpl());
            }
            if (Config.COMMAND_CLASS_MASTER_VOICE_COMMANDS.length > 0) {
                CommandClassMaster.INSTANCE._voiceCommands = Config.COMMAND_CLASS_MASTER_VOICE_COMMANDS;
                VoicedCommandHandler.getInstance().registerVoicedCommandHandler(CommandClassMaster.INSTANCE);
            }
        }
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
        for (final String cmd : _voiceCommands) {
            if (cmd != null && !cmd.isEmpty() && cmd.equalsIgnoreCase(command)) {
                Scripts.getInstance().callScripts(activeChar, CommandClassMaster.class.getName(), "classMaster");
                return true;
            }
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        if (!Config.COMMAND_CLASS_MASTER_ENABLED) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return Config.COMMAND_CLASS_MASTER_VOICE_COMMANDS;
    }

    private static class ClassMasterPath {
        private final List<ClassId> _availableClassIds;
        private final ClassId _fromClassId;
        private final List<Pair<Integer, Long>> _price;
        private final List<Pair<Integer, Long>> _reward;

        private ClassMasterPath(final List<ClassId> availableClassIds, final ClassId fromClassId, final List<Pair<Integer, Long>> price, final List<Pair<Integer, Long>> reward) {
            _availableClassIds = availableClassIds;
            _fromClassId = fromClassId;
            _price = price;
            _reward = reward;
        }

        public int getMinPlayerLevel() {
            return getMinPlayerLevelForClassId(_fromClassId.getLevel() + 1);
        }

        public List<ClassId> getAvailableClassIds() {
            return _availableClassIds;
        }

        public ClassId getFromClassId() {
            return _fromClassId;
        }

        public List<Pair<Integer, Long>> getPrice() {
            return _price;
        }

        public List<Pair<Integer, Long>> getReward() {
            return _reward;
        }
    }
}
