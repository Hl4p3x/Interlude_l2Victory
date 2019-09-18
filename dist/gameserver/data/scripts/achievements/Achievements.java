package achievements;

import achievements.AchievementInfo.AchievementInfoCategory;
import achievements.AchievementInfo.AchievementInfoLevel;
import org.apache.commons.lang3.ArrayUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.reward.RewardData;
import ru.j2dev.gameserver.scripts.Scripts;

import java.io.File;
import java.util.*;

public class Achievements implements OnInitScriptListener {
    private static final Achievements INSTANCE = new Achievements();
    private static final Logger LOG = LoggerFactory.getLogger(Achievements.class);
    private static final SAXBuilder READER = new SAXBuilder();
    private static final AchVoicedCommandHandler VOICED_COMMAND_HANDLER = new AchVoicedCommandHandler();
    private static final File ACHIEVEMENTS_FILE = new File(Config.DATAPACK_ROOT, "data/xml/achievements/achievements.xml");

    private boolean _isEnabled;
    private String[] _voiceCommands;
    private Map<AchievementMetricType, List<AchievementInfo>> _achievementByMetric;
    private Map<AchievementInfoCategory, List<AchievementInfo>> _achievementByCategory;
    private List<AchievementInfoCategory> _categories;
    private List<AchievementInfo> _achievementInfos;

    public Achievements() {
        _isEnabled = false;
        _voiceCommands = ArrayUtils.EMPTY_STRING_ARRAY;
        _achievementByMetric = Collections.emptyMap();
        _achievementByCategory = Collections.emptyMap();
        _categories = Collections.emptyList();
        _achievementInfos = Collections.emptyList();
    }

    public static Achievements getInstance() {
        return INSTANCE;
    }

    public boolean isEnabled() {
        return _isEnabled;
    }

    private String[] getVoiceCommands() {
        return _voiceCommands;
    }

    public List<AchievementInfo> getAchievementInfosByMetric(final AchievementMetricType metricType) {
        return _achievementByMetric.get(metricType);
    }

    public List<AchievementInfo> getAchievementInfosByCategory(final AchievementInfoCategory category) {
        return _achievementByCategory.get(category);
    }

    public AchievementInfo getAchievementInfoById(final int achievementId) {
        return _achievementInfos.stream().filter(achievementInfo -> achievementInfo.getId() == achievementId).findFirst().orElse(null);
    }

    public List<AchievementInfoCategory> getCategories() {
        return _categories;
    }

    public void parse() {
        try {
            final Document document = READER.build(ACHIEVEMENTS_FILE);
            final Map<AchievementMetricType, List<AchievementInfo>> achievementsByMetric = new HashMap<>();
            final Map<AchievementInfoCategory, List<AchievementInfo>> achievementsByCategory = new HashMap<>();
            final Map<String, AchievementInfoCategory> categories = new TreeMap<>();
            final List<AchievementInfoCategory> categoryList = new LinkedList<>();
            final List<AchievementInfo> achievementList = new ArrayList<>();
            final Element rootElement = document.getRootElement();
            _isEnabled = Boolean.parseBoolean(rootElement.getAttributeValue("enabled", "false"));
            final String voiceCommandsText = rootElement.getAttributeValue("voice_commands");
            _voiceCommands = ((voiceCommandsText != null && !voiceCommandsText.trim().isEmpty()) ? voiceCommandsText.split("[^\\w\\d_]+") : ArrayUtils.EMPTY_STRING_ARRAY);
            if (_isEnabled) {
                for (Element achievementsElement : document.getRootElement().getChildren()) {
                    if ("category".equalsIgnoreCase(achievementsElement.getName())) {
                        final String name = achievementsElement.getAttributeValue("name");
                        final AchievementInfoCategory category = new AchievementInfoCategory(name, achievementsElement.getAttributeValue("title_address"));
                        categories.put(name, category);
                        categoryList.add(category);
                    } else {
                        if (!"achievement".equalsIgnoreCase(achievementsElement.getName())) {
                            continue;
                        }
                        final String name = achievementsElement.getAttributeValue("name_address");
                        final String category2 = achievementsElement.getAttributeValue("category");
                        final AchievementMetricType metricType = AchievementMetricType.valueOf(achievementsElement.getAttributeValue("type"));
                        final String expireCronPattern = achievementsElement.getAttributeValue("expire_cron");
                        final long metricNotifyDelay = Long.parseLong(achievementsElement.getAttributeValue("metric_stage_notify_delay", String.valueOf(0L)));
                        final AchievementInfo achInfo = new AchievementInfo(Integer.parseInt(achievementsElement.getAttributeValue("id")), metricType, metricNotifyDelay, name, (expireCronPattern != null) ? new SchedulingPattern(expireCronPattern) : null);
                        achInfo.setCategory(categories.get(category2));
                        final String icon = achievementsElement.getAttributeValue("icon", "Icon.NOIMAGE");
                        achInfo.setIcon(icon);
                        int lastStageLvl = 0;
                        for (Element achievementElement : achievementsElement.getChildren()) {
                            if ("conds".equalsIgnoreCase(achievementElement.getName())) {
                                for (Element condElement : achievementElement.getChildren()) {
                                    if ("cond".equalsIgnoreCase(condElement.getName())) {
                                        final AchievementCondition cond = AchievementCondition.makeCond(condElement.getAttributeValue("name"), condElement.getAttributeValue("value"));
                                        if (cond == null) {
                                            throw new RuntimeException("Unknown condition \"" + condElement.getName() + " of achievement " + name + "(" + achInfo.getId() + ")");
                                        }
                                        achInfo.addCond(cond);
                                    }
                                }
                            } else {
                                if (!"stage".equalsIgnoreCase(achievementElement.getName())) {
                                    continue;
                                }
                                final int level;
                                lastStageLvl = (level = Integer.parseInt(achievementElement.getAttributeValue("level", String.valueOf(lastStageLvl + 1))));
                                final String stageDescAddr = achievementElement.getAttributeValue("desc_address");
                                final int stageVal = Integer.parseInt(achievementElement.getAttributeValue("value"));
                                final boolean resetMetric = Boolean.parseBoolean(achievementElement.getAttributeValue("reset_metric", String.valueOf(Boolean.TRUE)));
                                if (level - achInfo.getMaxLevel() > 1) {
                                    LOG.warn("Inconsistent level \"" + level + "\" of achievement \"" + name + "\"(" + achInfo.getId() + ")");
                                }
                                final AchievementInfoLevel achInfoLevel = achInfo.addLevel(level, stageVal, stageDescAddr, resetMetric);
                                achievementElement.getChildren().forEach(stageElement -> {
                                    if ("rewards".equals(stageElement.getName())) {
                                        stageElement.getChildren().stream().filter(rewardElement -> "reward".equalsIgnoreCase(rewardElement.getName())).forEach(rewardElement -> {
                                            final int itemId = Integer.parseInt(rewardElement.getAttributeValue("item_id"));
                                            final long minAmmount = Long.parseLong(rewardElement.getAttributeValue("min"));
                                            final long maxAmmount = Long.parseLong(rewardElement.getAttributeValue("max"));
                                            final int chance = (int) (Double.parseDouble(rewardElement.getAttributeValue("chance")) * 10000.0);
                                            final RewardData data = new RewardData(itemId);
                                            data.setChance((double) chance);
                                            data.setMinDrop(minAmmount);
                                            data.setMaxDrop(maxAmmount);
                                            achInfoLevel.addRewardData(data);
                                        });
                                    } else {
                                        if (!"conds".equalsIgnoreCase(stageElement.getName())) {
                                            return;
                                        }
                                        stageElement.getChildren().stream().filter(condElement2 -> "cond".equalsIgnoreCase(condElement2.getName())).forEach(condElement2 -> {
                                            final AchievementCondition cond2 = AchievementCondition.makeCond(condElement2.getAttributeValue("name"), condElement2.getAttributeValue("value"));
                                            if (cond2 == null) {
                                                throw new RuntimeException("Unknown condition \"" + condElement2.getName() + " of achievement " + name + "(" + achInfo.getId() + ")");
                                            }
                                            achInfoLevel.addCond(cond2);
                                        });
                                    }
                                });
                            }
                        }
                        List<AchievementInfo> byMetric = achievementsByMetric.computeIfAbsent(achInfo.getMetricType(), k -> new ArrayList<>());
                        byMetric.add(achInfo);
                        List<AchievementInfo> byCategory = achievementsByCategory.computeIfAbsent(achInfo.getCategory(), k -> new ArrayList<>());
                        byCategory.add(achInfo);
                        achievementList.add(achInfo);
                    }
                }
            }
            _categories = categoryList;
            _achievementByMetric = achievementsByMetric;
            _achievementByCategory = achievementsByCategory;
            _achievementInfos = achievementList;
            LOG.info("Achievements: Loaded " + _achievementInfos.size() + " achievement(s) for " + _categories.size() + " category(ies).");
        } catch (Exception ex) {
            LOG.warn("Can't parse achievements", ex);
        }
    }

    @Override
    public void onInit() {
        getInstance().parse();
        if (getInstance().isEnabled()) {
            AchievementMetricListeners.getInstance().init();
            if (VOICED_COMMAND_HANDLER.getVoicedCommandList().length > 0) {
                VoicedCommandHandler.getInstance().registerVoicedCommandHandler(VOICED_COMMAND_HANDLER);
            }
        }
    }

    private static class AchVoicedCommandHandler implements IVoicedCommandHandler {
        @Override
        public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
            if (!getInstance().isEnabled()) {
                return false;
            }
            for (final String achVCmd : getInstance().getVoiceCommands()) {
                if (!achVCmd.isEmpty()) {
                    if (achVCmd.equalsIgnoreCase(command)) {
                        Scripts.getInstance().callScripts(activeChar, AchievementUI.class.getName(), "achievements");
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public String[] getVoicedCommandList() {
            if (getInstance().isEnabled()) {
                return getInstance().getVoiceCommands();
            }
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
    }
}
