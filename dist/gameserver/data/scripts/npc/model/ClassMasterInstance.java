package npc.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.instances.MerchantInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Util;

import java.util.StringTokenizer;

public final class ClassMasterInstance extends MerchantInstance {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassMasterInstance.class);
    public ClassMasterInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    private String makeMessage(final Player player) {
        final ClassId classId = player.getClassId();
        int jobLevel = classId.getLevel();
        final int level = player.getLevel();
        final StringBuilder html = new StringBuilder();
        if (Config.ALLOW_CLASS_MASTERS_LIST.isEmpty() || !Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel)) {
            jobLevel = 4;
        }
        if (((level >= 20 && jobLevel == 1) || (level >= 40 && jobLevel == 2) || (level >= 76 && jobLevel == 3)) && Config.ALLOW_CLASS_MASTERS_LIST.contains(jobLevel)) {
            final int jobLevelPriceIdx = jobLevel - 1;
            final ItemTemplate item = ItemTemplateHolder.getInstance().getTemplate(Config.CLASS_MASTERS_PRICE_ITEM[jobLevelPriceIdx]);
            if (Config.CLASS_MASTERS_PRICE_LIST[jobLevelPriceIdx] > 0L) {
                html.append(player.isLangRus() ? "Цена: " : "Price: ").append(Util.formatAdena(Config.CLASS_MASTERS_PRICE_LIST[jobLevelPriceIdx])).append(" ").append(item.getName()).append("<br1>");
            }
            for (final ClassId cid : ClassId.VALUES) {
                if (cid.childOf(classId) && cid.getLevel() == classId.getLevel() + 1) {
                    html.append("<button value=");
                    html.append(HtmlUtils.makeClassNameFString(player, cid.getId()));
                    html.append(" action=\"bypass -h npc_");
                    html.append(getObjectId());
                    html.append("_change_class ");
                    html.append(cid.getId());
                    html.append(" ");
                    html.append(jobLevelPriceIdx);
                    html.append("\" width=80 height=23 back=\"test.btn_1\" fore=\"test.btn_1\"");
                    html.append(">");
                    html.append("<br>");
                }
            }
            player.sendPacket(new NpcHtmlMessage(player, this).setHtml(html.toString()));
        } else {
            switch (jobLevel) {
                case 1: {
                    html.append(new CustomMessage("ClassMaster.Need20Level", player));
                    break;
                }
                case 2: {
                    html.append(new CustomMessage("ClassMaster.Need40Level", player));
                    break;
                }
                case 3: {
                    html.append(new CustomMessage("ClassMaster.Need76Level", player));
                    break;
                }
                case 4: {
                    html.append(new CustomMessage("ClassMaster.NothingToUp", player));
                    break;
                }
            }
        }
        return html.toString();
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
        final NpcHtmlMessage msg = new NpcHtmlMessage(player, this);
        if(getTemplate().getHtmRoot() != null) {
            msg.setFile(getTemplate().getHtmRoot()+ getNpcId() +".htm");
        } else {
            msg.setFile("custom/31860.htm");
        }
        msg.replace("%classmaster%", makeMessage(player));
        player.sendPacket(msg);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        final StringTokenizer st = new StringTokenizer(command);
        if ("change_class".equals(st.nextToken())) {
            final int val = Integer.parseInt(st.nextToken());
            final int idx = Integer.parseInt(st.nextToken());
            if (idx < Config.CLASS_MASTERS_PRICE_ITEM.length && idx < Config.CLASS_MASTERS_PRICE_LIST.length) {
                final int itemId = Config.CLASS_MASTERS_PRICE_ITEM[idx];
                final long itemCount = Config.CLASS_MASTERS_PRICE_LIST[idx];
                if (player.getInventory().destroyItemByItemId(itemId, itemCount)) {
                    changeClass(player, val);
                    if (Config.CLASS_MASTERS_REWARD_ITEM.length > idx && Config.CLASS_MASTERS_REWARD_ITEM[idx] > 0 && Config.CLASS_MASTERS_REWARD_AMOUNT.length > idx && Config.CLASS_MASTERS_REWARD_AMOUNT[idx] > 0L) {
                        ItemFunctions.addItem(player, Config.CLASS_MASTERS_REWARD_ITEM[idx], Config.CLASS_MASTERS_REWARD_AMOUNT[idx], true);
                    }
                } else if (itemId == 57) {
                    player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
                } else {
                    player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
                }
            } else {
                LOGGER.error("ClassMasterInstance: Incorect job index " + idx);
            }
        } else {
            super.onBypassFeedback(player, command);
        }
    }

    private void changeClass(final Player player, final int val) {
        if (player.getClassId().getLevel() == 3) {
            player.sendPacket(Msg.YOU_HAVE_COMPLETED_THE_QUEST_FOR_3RD_OCCUPATION_CHANGE_AND_MOVED_TO_ANOTHER_CLASS_CONGRATULATIONS);
        } else {
            player.sendPacket(Msg.CONGRATULATIONS_YOU_HAVE_TRANSFERRED_TO_A_NEW_CLASS);
        }
        player.setClassId(val, false, false);
        player.broadcastCharInfo();
        player.sendPacket(new SocialAction(player.getObjectId(), SocialAction.VICTORY));
        player.broadcastPacket(new MagicSkillUse(player, player, 4339, 1, 0, 0L));
    }
}
