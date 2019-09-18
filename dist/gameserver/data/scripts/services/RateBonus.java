package services;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.Config.RateBonusInfo;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.AccountBonusDAO;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.Locale;

public class RateBonus extends Functions implements OnInitScriptListener, IVoicedCommandHandler {
    private final String[] _commandList;

    public RateBonus() {
        _commandList = new String[]{"premium", "pa", "delete_premium", "dp"};
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
        if (!Config.SERVICES_RATE_COMMAND_ENABLED) {
            return false;
        }
        if (_commandList[0].equalsIgnoreCase(command) || _commandList[1].equalsIgnoreCase(command)) {
            listMsg(activeChar);
            return true;
        }
        if  (_commandList[2].equalsIgnoreCase(command) || _commandList[3].equalsIgnoreCase(command)) {
            activeChar.deleteBonusPrem();
            activeChar.broadcastUserInfo(true);
            activeChar.sendMessage("Премиум удален");
            return true;
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        if (!Config.SERVICES_RATE_COMMAND_ENABLED) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return _commandList;
    }

    public void list() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Config.SERVICES_RATE_ENABLED) {
            show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
            return;
        }
        String html = "";
        final long expireTime = player.getBonus().getBonusExpire();
        html = getHtmString(player, expireTime);
        show(html, player);
    }

    private String getHtmString(Player player, long expireTime) {
        String html;
        if (expireTime > System.currentTimeMillis() / 1000L) {
            html = HtmCache.getInstance().getNotNull("scripts/services/RateBonusAlready.htm", player).replaceFirst("endtime", TimeUtils.toSimpleFormat(expireTime * 1000L));
        } else {
            html = HtmCache.getInstance().getNotNull("scripts/services/RateBonus.htm", player);
            final StringBuilder sb = new StringBuilder();
            for (final RateBonusInfo rateBonusInfo : Config.SERVICES_RATE_BONUS_INFO) {
                String rbHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.RateBonus.BonusHtml");
                rbHtml = rbHtml.replace("%bonus_idx%", String.valueOf(rateBonusInfo.id));
                rbHtml = rbHtml.replace("%exp_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.rateXp));
                rbHtml = rbHtml.replace("%sp_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.rateSp));
                rbHtml = rbHtml.replace("%quest_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.questRewardRate));
                rbHtml = rbHtml.replace("%quest_drop_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.questDropRate));
                rbHtml = rbHtml.replace("%adena_drop_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.dropAdena));
                rbHtml = rbHtml.replace("%items_drop_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.dropItems));
                rbHtml = rbHtml.replace("%spoil_drop_rate%", String.format(Locale.US, "%.1f", rateBonusInfo.dropSpoil));
                rbHtml = rbHtml.replace("%enchant_item_bonus%", String.valueOf((int) (100.0f * (rateBonusInfo.enchantItemMul - 1.0f))));
                rbHtml = rbHtml.replace("%period_days%", String.valueOf(rateBonusInfo.bonusTimeSeconds / 86400L));
                rbHtml = rbHtml.replace("%price%", String.valueOf(rateBonusInfo.consumeItemAmount));
                rbHtml = rbHtml.replace("%price_item_id%", String.valueOf(rateBonusInfo.consumeItemId));
                rbHtml = rbHtml.replace("%price_item_name%", ItemTemplateHolder.getInstance().getTemplate(rateBonusInfo.consumeItemId).getName());
                sb.append(rbHtml);
            }
            html = html.replaceFirst("%toreplace%", sb.toString());
        }
        return html;
    }

    public void listMsg(final Player player) {
        if (!Config.SERVICES_RATE_ENABLED) {
            show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
            return;
        }
        final long expireTime = player.getBonus().getBonusExpire();
        String html = getHtmString(player, expireTime);
        show(html, player);
    }

    public void get(final String[] param) {
        final Player player = getSelf();
        if (!Config.SERVICES_RATE_ENABLED) {
            show(HtmCache.getInstance().getNotNull("npcdefault.htm", player), player);
            return;
        }
        final int id = Integer.parseInt(param[0]);
        RateBonusInfo rateBonusInfo = null;
        for (final RateBonusInfo rbi : Config.SERVICES_RATE_BONUS_INFO) {
            if (rbi.id == id) {
                rateBonusInfo = rbi;
            }
        }
        if (rateBonusInfo == null) {
            player.sendMessage(player.isLangRus() ? "Неверный Id бонуса, обратитесь к администрации!" : "Invalid Id of the bonus, contact the administration!");
            return;
        }
        if (!player.getInventory().destroyItemByItemId(rateBonusInfo.consumeItemId, rateBonusInfo.consumeItemAmount)) {
            if (rateBonusInfo.consumeItemId == 57) {
                player.sendPacket(Msg.YOU_DO_NOT_HAVE_ENOUGH_ADENA);
            } else {
                player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            }
            return;
        }
        Log.add(player.getName() + "|" + player.getObjectId() + "|rate bonus|" + rateBonusInfo.id + "|" + rateBonusInfo.bonusTimeSeconds + "|", "services");
        AccountBonusDAO.getInstance().store(player.getAccountName(), rateBonusInfo.makeBonus());
        player.stopBonusTask();
        player.startBonusTask();
        rateBonusInfo.rewardItem.forEach(rewardPair -> ItemFunctions.addItem(player, rewardPair.getLeft(), rewardPair.getRight(), true));
        if (rateBonusInfo.nameColor != null) {
            player.setNameColor(rateBonusInfo.nameColor);
        }
        if (player.getParty() != null) {
            player.getParty().recalculatePartyData();
        }
        player.broadcastUserInfo(true);
        show(HtmCache.getInstance().getNotNull("scripts/services/RateBonusGet.htm", player), player);
    }

    @Override
    public void onInit() {
        if (Config.SERVICES_RATE_COMMAND_ENABLED) {
            VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
        }
    }
}
