package zones;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class LevelLimitZone implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(LevelLimitZone.class);
    private static final String MIN_PLAYER_LEVEL_ZONE_PARAM_NAME = "playerMinLevel";
    private static final String MAX_PLAYER_LEVEL_ZONE_PARAM_NAME = "playerMaxLevel";
    private static final String XYZ_PLAYER_LEVEL_ZONE_PARAM_NAME = "playerLevelLimitBackLoc";

    private static void init() {
        int count = 0;
        for (final Zone zone : ReflectionUtils.getZones()) {
            final boolean isMinSet = zone.getParams().isSet(MIN_PLAYER_LEVEL_ZONE_PARAM_NAME);
            final boolean isMaxSet = zone.getParams().isSet(MAX_PLAYER_LEVEL_ZONE_PARAM_NAME);
            final boolean isCoordSet = zone.getParams().isSet(XYZ_PLAYER_LEVEL_ZONE_PARAM_NAME);
            if (isMinSet != isMaxSet) LOGGER.warn("Min or max level not set for zone " + zone);
            if (isMinSet) {
                if (!isMaxSet) {
                    continue;
                }
                if (!isCoordSet) {
                    LOGGER.warn("Back coord not set for player level limited zone " + zone);
                } else {
                    final int minPlayerLevel = zone.getParams().getInteger(MIN_PLAYER_LEVEL_ZONE_PARAM_NAME, 1);
                    final int maxPlayerLevel = zone.getParams().getInteger(MAX_PLAYER_LEVEL_ZONE_PARAM_NAME, Experience.getMaxLevel());
                    final Location backLoc = Location.parseLoc(zone.getParams().getString(XYZ_PLAYER_LEVEL_ZONE_PARAM_NAME));
                    zone.addListener(new LevelLimitZoneListener(minPlayerLevel, maxPlayerLevel, backLoc));
                    ++count;
                }
            }
        }
        LOGGER.info("LevelLimitZone: Loaded " + count + " player level limit zone(s).");
    }

    @Override
    public void onInit() {
        init();
    }

    private static class LevelLimitZoneListener implements OnZoneEnterLeaveListener {
        private final int _minPlayerLevel;
        private final int _maxPlayerLevel;
        private final Location _playerBackLoc;

        private LevelLimitZoneListener(final int minPlayerLevel, final int maxPlayerLevel, final Location playerBackLoc) {
            _minPlayerLevel = minPlayerLevel;
            _maxPlayerLevel = maxPlayerLevel;
            _playerBackLoc = playerBackLoc;
        }

        @Override
        public void onZoneEnter(final Zone zone, final Creature actor) {
            if (actor != null && actor.isPlayer()) {
                final Player player = actor.getPlayer();
                if (player.isGM()) {
                    return;
                }
                if (player.getLevel() < _minPlayerLevel || player.getLevel() > _maxPlayerLevel) {
                    player.sendMessage(new CustomMessage("scripts.zones.epic.banishMsg", player));
                    player.teleToLocation(_playerBackLoc);
                    final Summon summon = player.getPet();
                    if (summon != null) {
                        summon.teleToLocation(_playerBackLoc);
                    }
                }
            }
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature actor) {
        }
    }
}
