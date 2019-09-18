package actions;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.instances.ChestInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.model.reward.RewardGroup;
import ru.j2dev.gameserver.model.reward.RewardList;
import ru.j2dev.gameserver.model.reward.RewardType;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.utils.HtmlUtils;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class RewardListInfo extends Functions {
    private static final int ITEMS_PER_PAGE = 15;
    private static final RewardType[] ITEMS_REWARD_ORDER = {RewardType.RATED_GROUPED, RewardType.SWEEP, RewardType.NOT_RATED_GROUPED, RewardType.NOT_RATED_NOT_GROUPED};
    private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);
    private static final NumberFormat df = NumberFormat.getInstance(Locale.ENGLISH);

    static {
        pf.setMaximumFractionDigits(4);
        df.setMinimumFractionDigits(2);
    }

    private static boolean canBypassCheck(final Player player, final NpcInstance npc) {
        if (npc == null) {
            player.sendPacket(Msg.THAT_IS_THE_INCORRECT_TARGET);
            player.sendActionFailed();
            return false;
        }
        if (!Config.ALLOW_TALK_WHILE_SITTING && player.isSitting()) {
            player.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
            player.sendActionFailed();
            return false;
        }
        if (!npc.isInRange(player, 2500L)) {
            player.sendPacket(SystemMsg.POSITIONING_CANNOT_BE_DONE_HERE_BECAUSE_THE_DISTANCE_BETWEEN_MERCENARIES_IS_TOO_SHORT, ActionFail.STATIC);
            player.sendActionFailed();
            return false;
        }
        return true;
    }

    public static void showRewardHtml(final Player player, final NpcInstance npc) {
        showRewardHtml(player, npc, 0);
    }

    public static void showRewardHtml(final Player player, final NpcInstance npc, int pageNum) {
        if (!canBypassCheck(player, npc)) {
            return;
        }
        final int diff = npc.calculateLevelDiffForDrop(player.isInParty() ? player.getParty().getLevel() : player.getLevel());
        double mod = npc.calcStat(Stats.ITEM_REWARD_MULTIPLIER, 1.0, player, null);
        mod *= Experience.penaltyModifier((long) diff, 9.0);
        final NpcHtmlMessage htmlMessage = new NpcHtmlMessage(player, npc);
        htmlMessage.replace("%npc_name%", HtmlUtils.htmlNpcName(npc.getNpcId()));
        if (mod <= 0.0 && Config.NO_SHOW_REWARD_ON_DIFF) {
            htmlMessage.setFile("actions/rewardlist_to_weak.htm");
            player.sendPacket(htmlMessage);
            return;
        }
        if (npc instanceof ChestInstance) {
            player.sendMessage("You can't view drop in the chest");
            player.sendActionFailed();
            return;
        }
        if (npc.getTemplate().getRewards().isEmpty()) {
            htmlMessage.setFile("actions/rewardlist_empty.htm");
            player.sendPacket(htmlMessage);
            return;
        }
        htmlMessage.setFile("actions/rewardlist_info.htm");
        final Map<RewardType, RewardList> rewards = npc.getTemplate().getRewards();
        final List<String> tmp = new ArrayList<>();
        for (final RewardType rewardType : ITEMS_REWARD_ORDER) {
            final RewardList rewardList = rewards.get(rewardType);
            if (rewardList != null) {
                if (!rewardList.isEmpty()) {
                    switch (rewardType) {
                        case RATED_GROUPED: {
                            tmp.add("<font color=\"aaccff\">RATED GROUP:</font>");
                            ratedGroupedRewardList(tmp, npc, rewardList, player, mod);
                            break;
                        }
                        case NOT_RATED_GROUPED: {
                            tmp.add("<font color=\"aaccff\">NOT RATED GROUP:</font>");
                            notRatedGroupedRewardList(tmp, rewardList, mod);
                            break;
                        }
                        case NOT_RATED_NOT_GROUPED: {
                            tmp.add("<font color=\"aaccff\">NOT RATED GROUP NOT GROUPED:</font>");
                            notGroupedRewardList(tmp, rewardList, 1.0, mod);
                            break;
                        }
                        case SWEEP: {
                            tmp.add("<font color=\"aaccff\">SWEEP:</font>");
                            notGroupedRewardList(tmp, rewardList, Config.RATE_DROP_SPOIL * player.getRateSpoil(), mod);
                            break;
                        }
                        case EVENT: {
                            tmp.add("<font color=\"aaccff\">EVENT:</font>");
                            notRatedGroupedRewardList(tmp, rewardList, mod);
                            break;
                        }
                    }
                }
            }
        }
        final StringBuilder builder = new StringBuilder();
        final int pages = tmp.size() / ITEMS_PER_PAGE;
        pageNum = Math.min(pageNum, pages);
        final int firstIdx = pageNum * ITEMS_PER_PAGE;
        for (int lastIdx = Math.max(0, Math.min((pageNum + 1) * ITEMS_PER_PAGE - 1, tmp.size() - 1)), idx = firstIdx; idx <= lastIdx; ++idx) {
            builder.append(tmp.get(idx));
        }
        htmlMessage.replace("%info%", builder.toString());
        builder.setLength(0);
        builder.append("<table><tr>");
        for (int p = 0; p <= pages; ++p) {
            builder.append("<td>");
            if (p == pageNum) {
                builder.append(p + 1);
            } else {
                builder.append("<a action=\"bypass -h scripts_actions.RewardListInfo:showReward ").append(p).append("\">").append(p + 1).append("</a>");
            }
            builder.append("</td>");
        }
        builder.append("</tr></table>");
        htmlMessage.replace("%paging%", builder.toString());
        player.sendPacket(htmlMessage);
    }

    public static void ratedGroupedRewardList(final List<String> tmp, final NpcInstance npc, final RewardList list, final Player player, final double mod) {
        for (final RewardGroup g : list) {
            final List<RewardData> items = g.getItems();
            final double gchance = g.getChance();
            double gmod = mod;
            final double rateDrop = (npc instanceof RaidBossInstance) ? (Config.RATE_DROP_RAIDBOSS * player.getBonus().getDropRaidItems()) : (npc.isSiegeGuard() ? Config.RATE_DROP_SIEGE_GUARD : (Config.RATE_DROP_ITEMS * player.getRateItems()));
            final double rateAdena = Config.RATE_DROP_ADENA * player.getRateAdena();
            final double rateSealStone = Config.RATE_DROP_SEAL_STONES * player.getRateItems();
            double grate;
            if (g.isAdena()) {
                if (rateAdena == 0.0) {
                    continue;
                }
                grate = rateAdena;
                if (gmod > 10.0) {
                    gmod *= gchance / 1000000.0;
                }
                grate *= gmod;
            } else if (g.isSealStone()) {
                if (rateSealStone == 0.0) {
                    continue;
                }
                grate = rateSealStone;
                if (g.notRate()) {
                    grate = Math.min(gmod, 1.0);
                } else {
                    grate *= gmod;
                }
            } else {
                if (rateDrop == 0.0) {
                    continue;
                }
                grate = rateDrop;
                if (g.notRate()) {
                    grate = Math.min(gmod, 1.0);
                } else {
                    grate *= gmod;
                }
            }
            final double gmult = Math.ceil(grate);
            tmp.add(formatRewardGroupHtml(g));
            for (final RewardData d : items) {
                tmp.add(formatRewardDataHtml(d, gmult));
            }
        }
    }

    public static void notRatedGroupedRewardList(final List<String> tmp, final RewardList list, final double mod) {
        for (final RewardGroup g : list) {
            final List<RewardData> items = g.getItems();
            tmp.add(formatRewardGroupHtml(g));
            for (final RewardData d : items) {
                tmp.add(formatRewardDataHtml(d, 1.0));
            }
        }
    }

    public static void notGroupedRewardList(final List<String> tmp, final RewardList list, final double rate, final double mod) {
        for (final RewardGroup g : list) {
            final List<RewardData> items = g.getItems();
            if (rate == 0.0) {
                continue;
            }
            double grate;
            if (g.notRate()) {
                grate = Math.min(mod, 1.0);
            } else {
                grate = rate * mod;
            }
            final double gmult = Math.ceil(grate);
            for (final RewardData d : items) {
                tmp.add(formatRewardDataHtml(d, gmult));
            }
        }
    }

    private static String formatRewardGroupHtml(final RewardGroup g) {
        return String.format("<table width=270 border=0 bgcolor=333333><tr><td width=170><font color=\"a2a0a2\">Group Chance:</font><font color=\"b09979\">%s</font></td><td width=100 align=right></td></tr></table>", pf.format(g.getChance() / 1000000.0));
    }

    private static String formatRewardDataHtml(final RewardData d, final double gmult) {
        String icon = d.getItem().getIcon();
        if (icon == null || "".equals(icon)) {
            icon = "icon.etc_question_mark_i00";
        }
        return String.format("<table width=270 border=0><tr><td width=32><img src=%s width=32 height=32></td><td width=238>%s<br1><font color=\"b09979\">[%d..%d]&nbsp;%s</font></td></tr></table>", icon, HtmlUtils.htmlItemName(d.getItemId()), d.getMinDrop(), Math.round(d.getMaxDrop() * (d.notRate() ? 1.0 : gmult)), pf.format(d.getChance() / 1000000.0));
    }

    public void showReward(final String[] param) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        int pageNum = 0;
        if (param.length > 0) {
            try {
                pageNum = Integer.parseInt(param[0]);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        showRewardHtml(player, npc, pageNum);
    }
}
