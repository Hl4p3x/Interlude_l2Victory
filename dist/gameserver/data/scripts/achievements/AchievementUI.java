package achievements;

import achievements.AchievementInfo.AchievementInfoCategory;
import achievements.AchievementInfo.AchievementInfoLevel;
import achievements.AchievementUI.HtmlProgressBarUI.ProgressBarStyle;
import achievements.AchievementUI.HtmlTabUI.TabStyle;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.gameserver.cache.HtmCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Util;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class AchievementUI extends Functions {
    static final String SCRIPT_BYPASS_CLASS = "scripts_" + AchievementUI.class.getName();
    private static final int ACHIEVEMENT_LEVELS_PER_PAGE = 5;
    private static final NumberFormat pf = NumberFormat.getPercentInstance(Locale.ENGLISH);

    public void achievements() {
        achievements(ArrayUtils.EMPTY_STRING_ARRAY);
    }

    public void achievements(final String... args) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (!Achievements.getInstance().isEnabled()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        String htmlContent = HtmCache.getInstance().getNotNull("scripts/achievements/achievements.htm", player);
        final HtmlTabUI categoriesTabsUI = new HtmlTabUI(TabStyle.inventory);
        final List<AchievementInfoCategory> categories = Achievements.getInstance().getCategories();
        final int activeCategoryIdx = Math.min(Math.max(0, (args.length > 0) ? Integer.parseInt(args[0]) : 0), categories.size() - 1);
        AchievementInfoCategory activeCategory = null;
        for (int categoryIdx = 0; categoryIdx < categories.size(); ++categoryIdx) {
            final AchievementInfoCategory category = categories.get(categoryIdx);
            if (activeCategoryIdx == categoryIdx) {
                activeCategory = category;
                categoriesTabsUI.addTab(category.getTitle(player), String.format("%s:achievements %d", AchievementUI.SCRIPT_BYPASS_CLASS, categoryIdx), true);
            } else {
                categoriesTabsUI.addTab(category.getTitle(player), String.format("%s:achievements %d", AchievementUI.SCRIPT_BYPASS_CLASS, categoryIdx), false);
            }
        }
        htmlContent = htmlContent.replace("%categories_tabs%", categoriesTabsUI.toHtml());
        if (activeCategory != null) {
            htmlContent = htmlContent.replace("%active_category_title%", activeCategory.getTitle(player));
            final int activePageIdx = (args.length > 1) ? Integer.parseInt(args[1]) : 0;
            final int activeAchId = (args.length > 2) ? Integer.parseInt(args[2]) : -1;
            final int activeAchLvl = (args.length > 3) ? Integer.parseInt(args[3]) : -1;
            final Paginator<AchievementInfo> paginator = new Paginator<AchievementInfo>(5, activePageIdx) {
                @Override
                protected String getBypassForPageOrdinal(final int pageIdx, final int pageOrd) {
                    return String.format("%s:achievements %d %d", AchievementUI.SCRIPT_BYPASS_CLASS, activeCategoryIdx, pageIdx);
                }
            };
            final List<AchievementInfo> achievementInfos = Achievements.getInstance().getAchievementInfosByCategory(activeCategory);
            final StringBuilder achievementsHtml = new StringBuilder();
            if (achievementInfos != null && !achievementInfos.isEmpty()) {
                achievementInfos.forEach(achievementInfo -> paginator.addItem(achievementInfo, achievementInfo.getLevels().size()));
                final List<Pair<AchievementInfo, Pair<Integer, Integer>>> achievementInfoLevelsIdxLimList = paginator.getItems();
                boolean invColor = true;
                for (final Pair<AchievementInfo, Pair<Integer, Integer>> achievementInfoLevelsIdxLim : achievementInfoLevelsIdxLimList) {
                    final AchievementInfo achievementInfo2 = achievementInfoLevelsIdxLim.getLeft();
                    achievementsHtml.append(buildAchievementHtml(activeCategoryIdx, activePageIdx, activeAchId, activeAchLvl, player, achievementInfo2, achievementInfoLevelsIdxLim.getRight(), invColor = !invColor));
                }
            }
            htmlContent = htmlContent.replace("%achievements_list%", achievementsHtml.toString());
            htmlContent = htmlContent.replace("%pagination%", paginator.toHtml());
        }
        final NpcHtmlMessage htmlPacket = new NpcHtmlMessage(player, null);
        htmlPacket.setHtml(truncateHtmlTagsSpaces(htmlContent));
        player.sendPacket(htmlPacket);
    }

    private String buildAchievementHtml(final int catIdx, final int pageIdx, final int activeAchId, final int activeAchLvl, final Player player, final AchievementInfo achievementInfo, final Pair<Integer, Integer> levelOffAndLimit, final boolean invColor) {
        final Achievement achievement = new Achievement(achievementInfo, player);
        final AchievementInfoLevel nextLevel = achievement.getNextLevel();
        String achievementHtml;
        if (achievement.isCompleted()) {
            achievementHtml = HtmCache.getInstance().getNotNull("scripts/achievements/entry.completed.htm", player);
        } else {
            achievementHtml = HtmCache.getInstance().getNotNull("scripts/achievements/entry.htm", player);
        }
        achievementHtml = achievementHtml.replace("%template_bg_color%", invColor ? " bgcolor=000000" : "");
        achievementHtml = achievementHtml.replace("%achievement_icon%", achievementInfo.getIcon());
        achievementHtml = achievementHtml.replace("%achievement_name%", HtmlUtils.bbParse(achievementInfo.getName(player)));
        achievementHtml = achievementHtml.replace("%achievement_current_level%", String.valueOf(achievement.isCompleted() ? achievementInfo.getMaxLevel() : nextLevel.getLevel()));
        achievementHtml = achievementHtml.replace("%achievement_max_level%", String.valueOf(achievementInfo.getMaxLevel()));
        final List<AchievementInfoLevel> levels = achievementInfo.getLevels();
        final StringBuilder levelsListHtml = new StringBuilder();
        for (int levelIdx = levelOffAndLimit.getLeft(), levelIdxLim = levelOffAndLimit.getRight(); levelIdx < levelIdxLim; ++levelIdx) {
            final AchievementInfoLevel level = levels.get(levelIdx);
            AchFaceLevelDisplayType levelDisplayType = AchFaceLevelDisplayType.DISPLAY_DEFAULT;
            if (level == nextLevel) {
                levelDisplayType = AchFaceLevelDisplayType.DISPLAY_PROGRESSING;
            } else if (achievement.isCompleted() || (nextLevel != null && level.getLevel() < nextLevel.getLevel())) {
                if (achievement.isLevelRewarded(level)) {
                    levelDisplayType = AchFaceLevelDisplayType.DISPLAY_REWARDED;
                } else {
                    levelDisplayType = AchFaceLevelDisplayType.DISPLAY_COMPLETED;
                }
            }
            levelsListHtml.append(buildAchivementLevelHtml(catIdx, pageIdx, activeAchId, activeAchLvl, player, levelDisplayType, achievement, level));
        }
        achievementHtml = achievementHtml.replace("%achievement_levels_list%", levelsListHtml.toString());
        return achievementHtml.trim();
    }

    private String buildAchivementLevelHtml(final int catIdx, final int pageIdx, final int activeAchId, final int activeAchLvl, final Player player, final AchFaceLevelDisplayType displayType, final Achievement achievement, final AchievementInfoLevel level) {
        String levelHtml = HtmCache.getInstance().getNotNull("scripts/achievements/" + displayType.templateFileName, player);
        levelHtml = levelHtml.replaceAll("%achievement_id%", String.valueOf(achievement.getAchInfo().getId()));
        levelHtml = levelHtml.replaceAll("%achievement_level_ordinal%", String.valueOf(level.getLevel()));
        levelHtml = levelHtml.replace("%achievement_level_description%", level.getDesc(player).replace("\\n", "<br1>"));
        if (displayType.haveRewardList) {
            if (activeAchId == achievement.getAchInfo().getId() && activeAchLvl == level.getLevel()) {
                String collapseButton = StringHolder.getInstance().getNotNull(player, "achievements.rewardList.collapseButton");
                collapseButton = collapseButton.replace("%collapse_bypass%", String.format("%s:achievements %d %d", AchievementUI.SCRIPT_BYPASS_CLASS, catIdx, pageIdx));
                final List<RewardData> rewardDatas = level.getRewardDataList();
                levelHtml = levelHtml.replace("%reward_list%", buildAchivementLevelRewardHtml(player, rewardDatas));
                levelHtml = levelHtml.replace("%reward_switch%", collapseButton);
            } else {
                String expandButton = StringHolder.getInstance().getNotNull(player, "achievements.rewardList.expandButton");
                expandButton = expandButton.replace("%expand_bypass%", String.format("%s:achievements %d %d %d %d", AchievementUI.SCRIPT_BYPASS_CLASS, catIdx, pageIdx, achievement.getAchInfo().getId(), level.getLevel()));
                levelHtml = levelHtml.replace("%reward_list%", StringHolder.getInstance().getNotNull(player, "achievements.rewardList.collapsedText"));
                levelHtml = levelHtml.replace("%reward_switch%", expandButton);
            }
        }
        if (displayType.haveProgressBar) {
            levelHtml = levelHtml.replace("%achievement_level_progress_bar%", new HtmlProgressBarUI(ProgressBarStyle.flax_light).setBarWidth(64).setFull(level.getValue()).setValue(achievement.getCounter().getVal()).toHtml());
        }
        if (displayType.haveRewardBypass) {
            if (activeAchId >= 0 && activeAchLvl >= 0) {
                levelHtml = levelHtml.replace("%achievement_level_reward_bypass%", String.format("%s:achieveReward %d %d %d %d %d %d", AchievementUI.SCRIPT_BYPASS_CLASS, achievement.getAchInfo().getId(), level.getLevel(), catIdx, pageIdx, activeAchId, activeAchLvl));
            } else {
                levelHtml = levelHtml.replace("%achievement_level_reward_bypass%", String.format("%s:achieveReward %d %d %d %d", AchievementUI.SCRIPT_BYPASS_CLASS, achievement.getAchInfo().getId(), level.getLevel(), catIdx, pageIdx));
            }
        }
        return levelHtml.trim();
    }

    private String buildAchivementLevelRewardHtml(final Player player, final List<RewardData> rewardDatas) {
        final StringBuilder rewardListHtml = new StringBuilder();
        for (final RewardData rewardData : rewardDatas) {
            String rewardHtml = HtmCache.getInstance().getNotNull("scripts/achievements/reward.htm", player);
            final ItemTemplate itemTemplate = rewardData.getItem();
            rewardHtml = rewardHtml.replace("%item_icon%", itemTemplate.getIcon());
            rewardHtml = rewardHtml.replace("%item_id%", String.valueOf(itemTemplate.getItemId()));
            rewardHtml = rewardHtml.replace("%item_name%", itemTemplate.getName());
            rewardHtml = rewardHtml.replace("%min_amount%", String.valueOf(rewardData.getMinDrop()));
            rewardHtml = rewardHtml.replace("%max_amount%", String.valueOf(rewardData.getMaxDrop()));
            rewardHtml = rewardHtml.replace("%chance%", AchievementUI.pf.format(rewardData.getChance() / 1000000.0));
            rewardListHtml.append(rewardHtml);
        }
        return rewardListHtml.toString().trim();
    }

    public void achieveReward(final String... args) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final NpcInstance npc = getNpc();
        if (!Achievements.getInstance().isEnabled()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (args.length < 4) {
            return;
        }
        final int achId = Integer.parseInt(args[0]);
        final int achLvl = Integer.parseInt(args[1]);
        final AchievementInfo achievementInfo = Achievements.getInstance().getAchievementInfoById(achId);
        if (achievementInfo == null) {
            return;
        }
        final AchievementInfoLevel level = achievementInfo.getLevel(achLvl);
        if (level == null) {
            return;
        }
        final Achievement achievement = new Achievement(achievementInfo, player);
        if (achievement.isRewardableLevel(level)) {
            final List<RewardData> rewardDataList = level.getRewardDataList();
            long weight = 0L;
            long slots = 0L;
            for (final RewardData rewardData : rewardDataList) {
                weight += rewardData.getItem().getWeight() * rewardData.getMaxDrop();
                slots += (rewardData.getItem().isStackable() ? 1L : rewardData.getMaxDrop());
            }
            if (!player.getInventory().validateWeight(weight)) {
                player.sendPacket(Msg.YOU_HAVE_EXCEEDED_THE_WEIGHT_LIMIT);
                return;
            }
            if (!player.getInventory().validateCapacity(slots)) {
                player.sendPacket(Msg.YOUR_INVENTORY_IS_FULL);
                return;
            }
            achievement.setLevelRewarded(level, true);
            for (final RewardData rewardData : rewardDataList) {
                final long roll = Util.rollDrop(rewardData.getMinDrop(), rewardData.getMaxDrop(), rewardData.getChance(), false);
                if (roll > 0L) {
                    ItemFunctions.addItem(player, rewardData.getItemId(), roll, true);
                }
            }
            player.sendMessage(new CustomMessage("achievements.rewardedS1LevelS2", player, achievementInfo.getName(player), level.getLevel()));
        }
        achievements(args[2], args[3]);
    }

    public void adminReload() {
        final Player self = getSelf();
        if (self == null) {
            return;
        }
        if (!self.isGM()) {
            return;
        }
        Achievements.getInstance().parse();
    }

    

    private enum AchFaceLevelDisplayType {
        DISPLAY_DEFAULT("level.htm", false, false, true),
        DISPLAY_PROGRESSING("level.progressing.htm", true, false, true),
        DISPLAY_COMPLETED("level.completed.htm", false, true, true),
        DISPLAY_REWARDED("level.rewarded.htm", false, false, false);

        final String templateFileName;
        final boolean haveProgressBar;
        final boolean haveRewardBypass;
        final boolean haveRewardList;

        AchFaceLevelDisplayType(final String templateFileName, final boolean haveProgressBar, final boolean haveRewardBypass, final boolean haveRewardList) {
            this.templateFileName = templateFileName;
            this.haveProgressBar = haveProgressBar;
            this.haveRewardBypass = haveRewardBypass;
            this.haveRewardList = haveRewardList;
        }
    }

    public static class HtmlTabUI {
        private final TabStyle style;
        private final List<TabRecord> records;
        private TabRecord active;
        private int tabsPerRow;

        public HtmlTabUI(final TabStyle style) {
            this(style, 296 / style.width);
        }

        public HtmlTabUI(final TabStyle style, final int tabsPerRow) {
            records = new ArrayList<>();
            this.tabsPerRow = -1;
            this.style = style;
            this.tabsPerRow = tabsPerRow;
        }

        public TabRecord addTab(final String title, final String bypass, final boolean active) {
            final TabRecord tabRecord = new TabRecord(title, bypass);
            if (active) {
                setActive(tabRecord);
            }
            records.add(tabRecord);
            return tabRecord;
        }

        public TabRecord addTab(final String title, final String bypass) {
            return addTab(title, bypass, false);
        }

        public void setActive(final TabRecord active) {
            this.active = active;
        }

        public void setTabsPerRow(final int tabsPerRow) {
            this.tabsPerRow = tabsPerRow;
        }

        public String toHtml() {
            final StringBuilder html = new StringBuilder();
            html.append("<table width=").append(tabsPerRow * style.width).append(" border=0 cellspacing=0 cellpadding=0>");
            for (int row = 0; row * tabsPerRow < records.size(); ++row) {
                html.append("<tr>");
                for (int col = 0; col < tabsPerRow; ++col) {
                    html.append("<td width=").append(style.width).append(">");
                    final int idx = col + row * tabsPerRow;
                    if (idx < records.size()) {
                        final TabRecord tabRecord = records.get(idx);
                        html.append("<button").append(" width=").append(style.width).append(" height=").append(style.height);
                        if (tabRecord.title != null) {
                            html.append(" value=\"").append(tabRecord.title).append("\"");
                        }
                        if (tabRecord.bypass != null) {
                            html.append(" action=\"bypass -h ").append(tabRecord.bypass).append("\"");
                        }
                        if (tabRecord == active) {
                            html.append(" fore=").append(style.active).append(" back=").append(style.active);
                        } else {
                            html.append(" fore=").append(style.fore).append(" back=").append(style.back);
                        }
                        html.append(">");
                    } else {
                        html.append("&nbsp;");
                    }
                    html.append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</table>");
            return html.toString();
        }

        public enum TabStyle {
            board(74, 22, "L2UI_CH3.board_tab1", "L2UI_CH3.board_tab2", "L2UI_CH3.board_tab2"),
            chatting(true, 64, 22, "L2UI_CH3.chatting_tab1", "L2UI_CH3.chatting_tab2", "L2UI_CH3.chatting_tab2"),
            inventory(94, 22, "L2UI_CH3.inventory_tab1", "L2UI_CH3.inventory_tab2", "L2UI_CH3.inventory_tab2"),
            msn(114, 22, "L2UI_CH3.msn_tab1", "L2UI_CH3.msn_tab2", "L2UI_CH3.msn_tab2"),
            normal(74, 22, "L2UI_CH3.normal_tab", "L2UI_CH3.normal_tab_on", "L2UI_CH3.normal_tab_on");

            final boolean isButtom;
            final int width;
            final int height;
            final String active;
            final String fore;
            final String back;

            TabStyle(final boolean isButtom, final int width, final int height, final String active, final String fore, final String back) {
                this.isButtom = isButtom;
                this.width = width;
                this.height = height;
                this.active = active;
                this.fore = fore;
                this.back = back;
            }

            TabStyle(final int width, final int height, final String active, final String fore, final String back) {
                this(false, width, height, active, fore, back);
            }
        }

        public class TabRecord {
            final String title;
            final String bypass;

            private TabRecord(final String title, final String bypass) {
                this.title = title;
                this.bypass = bypass;
            }
        }
    }

    public static class HtmlProgressBarUI {
        private final ProgressBarStyle style;
        private int barWidth;
        private int percent;
        private int full;
        private int value;

        public HtmlProgressBarUI(final ProgressBarStyle style) {
            percent = -1;
            full = -1;
            value = -1;
            this.style = style;
            barWidth = ((style.maxWidth >= 0) ? style.maxWidth : 100);
        }

        public int getBarWidth() {
            return barWidth;
        }

        public HtmlProgressBarUI setBarWidth(final int barWidth) {
            this.barWidth = barWidth;
            return this;
        }

        public int getValue() {
            return value;
        }

        public HtmlProgressBarUI setValue(final int value) {
            this.value = value;
            if (value > full) {
                full = value;
            }
            return this;
        }

        public int getFull() {
            return full;
        }

        public HtmlProgressBarUI setFull(final int full) {
            this.full = full;
            return this;
        }

        public int getPercent() {
            return percent;
        }

        public void setPercent(final int percent) {
            this.percent = percent;
        }

        public String toHtml() {
            final StringBuilder html = new StringBuilder();
            final int width = (style.maxWidth >= 0) ? Math.min(barWidth, style.maxWidth) : barWidth;
            html.append("<table width=").append(width).append(" border=0 cellspacing=0 cellpadding=0><tr>");
            if (value >= 0 && full >= 0) {
                if (value < full) {
                    final int valWidth = width / full * value;
                    html.append("<td><img src=\"").append(style.valueTexture).append("\" width=").append(valWidth).append(" height=").append(style.height).append("></td>");
                    html.append("<td><img src=\"").append(style.backTexture).append("\" width=").append(width - valWidth).append(" height=").append(style.height).append("></td>");
                } else {
                    html.append("<td><img src=\"").append((full == 0) ? style.backTexture : style.valueTexture).append("\" width=").append(width).append(" height=").append(style.height).append("></td>");
                }
            } else if (percent >= 0) {
                final int valWidth = (int) (percent / 100.0f * value);
                if (percent < 100) {
                    html.append("<td><img src=\"").append(style.valueTexture).append("\" width=").append(valWidth).append(" height=").append(style.height).append("></td>");
                    html.append("<td><img src=\"").append(style.backTexture).append("\" width=").append(width - valWidth).append(" height=").append(style.height).append("></td>");
                } else {
                    html.append("<td><img src=\"").append((percent == 0) ? style.backTexture : style.valueTexture).append("\" width=").append(width).append(" height=").append(style.height).append("></td>");
                }
            }
            html.append("</tr></table>");
            return html.toString();
        }

        public enum ProgressBarStyle {
            classic_red(3, 96, "sek.cbui62", "sek.cbui64"),
            classic_blue(3, 96, "sek.cbui63", "sek.cbui64"),
            yellow(12, -1, "L2UI_CH3.br_bar1_cp", "L2UI_CH3.br_bar1back_cp"),
            flax(12, -1, "L2UI_CH3.br_bar1_cp1", "L2UI_CH3.br_bar1back_cp"),
            flax_light(8, -1, "L2UI_CH3.br_bar1_cp1", "L2UI_CH3.br_bar1back_cp");

            final int height;
            final int maxWidth;
            final String valueTexture;
            final String backTexture;

            ProgressBarStyle(final int height, final int maxWidth, final String valueTexture, final String backTexture) {
                this.height = height;
                this.maxWidth = maxWidth;
                this.valueTexture = valueTexture;
                this.backTexture = backTexture;
            }
        }
    }

    public abstract static class Paginator<ItemType> {
        private final int pageSize;
        private final List<Pair<ItemType, Integer>> items;
        private int pageIdx;

        public Paginator(final int pageSize, final int pageIdx) {
            items = new LinkedList<>();
            this.pageSize = pageSize;
            this.pageIdx = pageIdx;
        }

        public Paginator(final int pageLength) {
            this(pageLength, 0);
        }

        public int getPageIdx() {
            return pageIdx;
        }

        public Paginator<ItemType> setPageIdx(final int pageIdx) {
            this.pageIdx = pageIdx;
            return this;
        }

        public Paginator<ItemType> addItem(final ItemType item, final int length) {
            items.add(Pair.of(item, length));
            return this;
        }

        private int itemsLength() {
            int lengthSum = 0;
            for (final Pair<ItemType, Integer> itemAndLength : items) {
                lengthSum += itemAndLength.getRight();
            }
            return lengthSum;
        }

        public List<Pair<ItemType, Pair<Integer, Integer>>> getItems(final int pageNum) {
            final int begin = pageNum * pageSize;
            final int end = begin + pageSize;
            int offset = 0;
            final List<Pair<ItemType, Pair<Integer, Integer>>> result = new LinkedList<>();
            for (final Pair<ItemType, Integer> itemAndLength : items) {
                final int length = itemAndLength.getRight();
                if (offset < end && offset + length > begin) {
                    result.add(Pair.of(itemAndLength.getLeft(), Pair.of(Math.max(begin - offset, 0), Math.min(length, end - offset))));
                }
                offset += length;
            }
            return result;
        }

        public List<Pair<ItemType, Pair<Integer, Integer>>> getItems() {
            return getItems(pageIdx);
        }

        protected abstract String getBypassForPageOrdinal(final int p0, final int p1);

        public String toHtml() {
            final int itemsLength = itemsLength();
            final int pages = (itemsLength + pageSize - 1) / pageSize;
            final StringBuilder paginationHtml = new StringBuilder();
            paginationHtml.append("<table border=0 cellspacing=0 cellpadding=0><tr>");
            for (int pageIdx = 0; pageIdx < pages; ++pageIdx) {
                final int pageOrd = pageIdx + 1;
                paginationHtml.append("<td>");
                if (pageIdx == this.pageIdx) {
                    paginationHtml.append(pageOrd);
                } else {
                    paginationHtml.append("<a action=\"bypass -h ").append(getBypassForPageOrdinal(pageIdx, pageOrd)).append("\">").append(pageOrd).append("</a>");
                }
                paginationHtml.append("</td>");
            }
            paginationHtml.append("</tr></table>");
            return paginationHtml.toString();
        }
    }
}
