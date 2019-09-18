package achievements;

import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.gameserver.model.AggroList.AggroInfo;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGame;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.Set;
import java.util.StringTokenizer;

public abstract class AchievementCondition {
    public static AchievementCondition makeCond(final String condName, final String condValue) {
        try {
            for (final Class<?> clazz : AchievementCondition.class.getClasses()) {
                if (AchievementCondition.class.isAssignableFrom(clazz)) {
                    final Class<? extends AchievementCondition> achievementClass = (Class<? extends AchievementCondition>) clazz;
                    final AchievementConditionName conditionName = achievementClass.getAnnotation(AchievementConditionName.class);
                    if (conditionName != null) {
                        if (condName.equalsIgnoreCase(conditionName.value())) {
                            final Constructor<? extends AchievementCondition> ctor = achievementClass.getConstructor(String.class);
                            if (ctor != null) {
                                return ctor.newInstance(condValue);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) {
            throw new RuntimeException("Can't make condition " + condName + "(" + condValue + ")", ex);
        }
        return null;
    }

    public abstract boolean test(final Player p0, final Object... p1);

    @Target({ElementType.TYPE})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface AchievementConditionName {
        String value();
    }

    @AchievementConditionName("is_raid_boss")
    public static class AchievementConditionHaveRaid extends AchievementCondition {
        private final boolean _value;

        public AchievementConditionHaveRaid(final String value) {
            _value = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj != null) {
                    if (_value) {
                        if (obj instanceof RaidBossInstance) {
                            return true;
                        }
                    } else if (!(obj instanceof RaidBossInstance)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @AchievementConditionName("is_karma_player")
    public static class AchievementConditionIsKarmaPlayer extends AchievementCondition {
        private final boolean _value;

        public AchievementConditionIsKarmaPlayer(final String value) {
            _value = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj != null) {
                    if (_value) {
                        if (obj instanceof Player && ((Player) obj).getKarma() > 0) {
                            return true;
                        }
                    } else if (obj instanceof Player && ((Player) obj).getKarma() == 0) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    @AchievementConditionName("npc_id_in_list")
    public static class AchievementConditionNpcIdInList extends AchievementCondition {
        private final Set<Integer> _npcIds;

        public AchievementConditionNpcIdInList(final String value) {
            _npcIds = new HashSet<>();
            final StringTokenizer tok = new StringTokenizer(value, ";,");
            while (tok.hasMoreTokens()) {
                _npcIds.add(Integer.parseInt(tok.nextToken()));
            }
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj != null) {
                    if (obj instanceof NpcInstance) {
                        final NpcInstance npc = (NpcInstance) obj;
                        if (_npcIds.contains(npc.getNpcId())) {
                            return true;
                        }
                    }
                }
            }
            return false;
        }
    }

    @AchievementConditionName("now_match_cron")
    public static class AchievementConditionIsNowMatchCron extends AchievementCondition {
        private final SchedulingPattern _pattern;

        public AchievementConditionIsNowMatchCron(final String pattern) {
            _pattern = new SchedulingPattern(pattern);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return _pattern.match(System.currentTimeMillis());
        }
    }

    @AchievementConditionName("is_oly_winner")
    public static class AchIsOlyWinner extends AchievementCondition {
        private final boolean _isWinner;

        public AchIsOlyWinner(final String value) {
            _isWinner = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            boolean haveComp = false;
            for (final Object obj : args) {
                if (obj instanceof OlympiadGame) {
                    haveComp = true;
                }
            }
            if (haveComp) {
                for (final Object obj : args) {
                    if (obj instanceof Boolean) {
                        return _isWinner == (boolean) obj;
                    }
                }
            }
            return false;
        }
    }

    @AchievementConditionName("self_is_class_id_in")
    public static class AchSelfIsActiveClass extends AchievementCondition {
        private final Set<Integer> _classIds;

        public AchSelfIsActiveClass(final String value) {
            _classIds = new HashSet<>();
            final StringTokenizer st = new StringTokenizer(value, ";,");
            while (st.hasMoreTokens()) {
                _classIds.add(Integer.parseInt(st.nextToken()));
            }
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return _classIds.contains(selfPlayer.getClassId().getId());
        }
    }

    @AchievementConditionName("is_target_player_class_id_in")
    public static class AchTargetPlayerIsActiveClass extends AchievementCondition {
        private final Set<Integer> _classIds;

        public AchTargetPlayerIsActiveClass(final String value) {
            _classIds = new HashSet<>();
            final StringTokenizer st = new StringTokenizer(value, ";,");
            while (st.hasMoreTokens()) {
                _classIds.add(Integer.parseInt(st.nextToken()));
            }
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj instanceof Player) {
                    final Player targetPlayer = (Player) obj;
                    return _classIds.contains(targetPlayer.getClassId().getId());
                }
            }
            return false;
        }
    }

    @AchievementConditionName("self_is_subclass_active")
    public static class AchSelfIsSubclassActive extends AchievementCondition {
        private final boolean _isSubclassActive;

        public AchSelfIsSubclassActive(final String value) {
            _isSubclassActive = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return selfPlayer.isSubClassActive() == _isSubclassActive;
        }
    }

    @AchievementConditionName("self_is_clan_leader")
    public static class AchSelfIsClanLeader extends AchievementCondition {
        private final boolean _isClanleader;

        public AchSelfIsClanLeader(final String value) {
            _isClanleader = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return selfPlayer.isClanLeader() == _isClanleader;
        }
    }

    @AchievementConditionName("self_level_in_range")
    public static class AchSelfLevelInRange extends AchievementCondition {
        private final int _minLevel;
        private final int _maxLevel;

        public AchSelfLevelInRange(final String value) {
            final int delimIdx = value.indexOf(45);
            _minLevel = Integer.parseInt(value.substring(0, delimIdx).trim());
            _maxLevel = Integer.parseInt(value.substring(delimIdx + 1).trim());
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return selfPlayer.getLevel() >= _minLevel && selfPlayer.getLevel() < _maxLevel;
        }
    }

    @AchievementConditionName("self_is_noble")
    public static class AchSelfIsNoble extends AchievementCondition {
        private final boolean _isNoble;

        public AchSelfIsNoble(final String value) {
            _isNoble = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return selfPlayer.isNoble() == _isNoble;
        }
    }

    @AchievementConditionName("self_is_hero")
    public static class AchSelfIsHero extends AchievementCondition {
        private final boolean _isHero;

        public AchSelfIsHero(final String value) {
            _isHero = Boolean.parseBoolean(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            return selfPlayer.isHero() == _isHero;
        }
    }

    @AchievementConditionName("self_target_max_lvl_diff")
    public static class AchSelfTargetMaxLvlDiff extends AchievementCondition {
        private final int _maxLvlDiff;

        public AchSelfTargetMaxLvlDiff(final String value) {
            _maxLvlDiff = Integer.parseInt(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj instanceof Creature) {
                    final Creature targetCreature = (Creature) obj;
                    return Math.abs(targetCreature.getLevel() - selfPlayer.getLevel()) <= _maxLvlDiff;
                }
            }
            return false;
        }
    }

    @AchievementConditionName("target_npc_min_hate_to_me")
    public static class AchTargetAggroMinHateToMe extends AchievementCondition {
        private final int _minHate;

        public AchTargetAggroMinHateToMe(final String value) {
            _minHate = Integer.parseInt(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj instanceof NpcInstance) {
                    final NpcInstance targetNpc = (NpcInstance) obj;
                    final AggroInfo aggroInfo = targetNpc.getAggroList().get(selfPlayer);
                    return aggroInfo != null && aggroInfo.hate >= _minHate;
                }
            }
            return false;
        }
    }

    @AchievementConditionName("target_npc_min_damage_to_me")
    public static class AchTargetAggroMinDamageToMe extends AchievementCondition {
        private final int _minDamage;

        public AchTargetAggroMinDamageToMe(final String value) {
            _minDamage = Integer.parseInt(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj instanceof NpcInstance) {
                    final NpcInstance targetNpc = (NpcInstance) obj;
                    final AggroInfo aggroInfo = targetNpc.getAggroList().get(selfPlayer);
                    return aggroInfo != null && aggroInfo.damage >= _minDamage;
                }
            }
            return false;
        }
    }

    @AchievementConditionName("self_quest_id_in")
    public static class AchSelfQuestId extends AchievementCondition {
        private final Set<Integer> _questIds;

        public AchSelfQuestId(final String value) {
            _questIds = new HashSet<>();
            final StringTokenizer tok = new StringTokenizer(value, ";,");
            while (tok.hasMoreTokens()) {
                _questIds.add(Integer.parseInt(tok.nextToken()));
            }
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj != null) {
                    Quest quest = null;
                    if (obj instanceof Quest) {
                        quest = (Quest) obj;
                    }
                    if (obj instanceof QuestState) {
                        quest = ((QuestState) obj).getQuest();
                    }
                    if (quest != null) {
                        return _questIds.contains(quest.getQuestIntId());
                    }
                }
            }
            return false;
        }
    }

    @AchievementConditionName("self_quest_state_is")
    public static class AchSelfQuestStateIs extends AchievementCondition {
        private final int _questStateId;

        public AchSelfQuestStateIs(final String value) {
            _questStateId = Quest.getStateId(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            for (final Object obj : args) {
                if (obj != null) {
                    QuestState questState = null;
                    if (obj instanceof QuestState) {
                        questState = (QuestState) obj;
                    }
                    if (obj instanceof Quest) {
                        questState = selfPlayer.getQuestState((Quest) obj);
                    }
                    if (questState != null) {
                        return questState.getState() == _questStateId;
                    }
                }
            }
            return false;
        }
    }

    @AchievementConditionName("self_min_online_time")
    public static class AchSelfMinOnlineTime extends AchievementCondition {
        private final long _minOnlineTime;

        public AchSelfMinOnlineTime(final String value) {
            _minOnlineTime = Long.parseLong(value);
        }

        @Override
        public boolean test(final Player selfPlayer, final Object... args) {
            final long onlineTimeBegin = selfPlayer.getOnlineBeginTime();
            final long now = System.currentTimeMillis();
            return onlineTimeBegin > 0L && onlineTimeBegin <= now && (now - onlineTimeBegin) / 1000L >= _minOnlineTime;
        }
    }
}
