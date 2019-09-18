package achievements;

import ru.j2dev.gameserver.model.Player;

public enum AchievementMetricType {
    LOGIN,
    NPC_KILL,
    PVP_KILL,
    DEATH,
    LEVEL {
        @Override
        public AchievementCounter getCounter(final Player player, final AchievementInfo achInfo) {
            final int playerLevel = player.getLevel();
            return new AchievementCounter(player.getObjectId(), achInfo.getId()) {
                @Override
                public int getVal() {
                    return playerLevel;
                }

                @Override
                public void setVal(final int val) {
                }

                @Override
                public int incrementAndGetValue() {
                    return playerLevel;
                }

                @Override
                public void store() {
                }

                @Override
                public boolean isStorable() {
                    return false;
                }
            };
        }
    },
    OLYMPIAD,
    RAID_PARTICIPATION,
    QUEST_STATE;

    public AchievementCounter getCounter(final Player player, final AchievementInfo achInfo) {
        return AchievementCounter.makeDBStorableCounter(player.getObjectId(), achInfo.getId());
    }
}
