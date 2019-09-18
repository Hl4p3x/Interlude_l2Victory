package zones;

import gnu.trove.set.hash.TIntHashSet;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Summon;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.ReflectionUtils;

public class ClassIdLimitZone implements OnInitScriptListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(ClassIdLimitZone.class);
    private static final String PLAYER_CLASS_ID_ZONE_PARAM_NAME = "playerClassIdsLimit";
    private static final String PLAYER_CLASS_ID_LOC_ZONE_PARAM_NAME = "playerClassIdsLimitBackLoc";

    private static void init() {
        int count = 0;
        for (final Zone zone : ReflectionUtils.getZones()) {
            final boolean isClassIdsSet = zone.getParams().isSet(PLAYER_CLASS_ID_ZONE_PARAM_NAME);
            final boolean isBackLocSet = zone.getParams().isSet(PLAYER_CLASS_ID_LOC_ZONE_PARAM_NAME);
            if (!isClassIdsSet && !isBackLocSet) {
                continue;
            }
            if (!isClassIdsSet) {
                LOGGER.warn("Class ids not set for zone " + zone);
            } else if (!isBackLocSet) {
                LOGGER.warn("Back location not set for \u0441lassId limit zone " + zone);
            } else {
                final int[] playerClassIds = zone.getParams().getIntegerArray(PLAYER_CLASS_ID_ZONE_PARAM_NAME, ArrayUtils.EMPTY_INT_ARRAY);
                final Location backLoc = Location.parseLoc(zone.getParams().getString(PLAYER_CLASS_ID_LOC_ZONE_PARAM_NAME));
                zone.addListener(new ClassIdLimitZoneListener(playerClassIds, backLoc));
                ++count;
            }
        }
        LOGGER.info("ClassIdLimitZone: Loaded " + count + " player class id limit zone(s).");
    }

    @Override
    public void onInit() {
        init();
    }

    private static class ClassIdLimitZoneListener implements OnZoneEnterLeaveListener {
        private final TIntHashSet _playerClassIds;
        private final Location _playerBackLoc;

        private ClassIdLimitZoneListener(final int[] playerClassIds, final Location playerBackLoc) {
            _playerClassIds = new TIntHashSet(playerClassIds);
            _playerBackLoc = playerBackLoc;
        }

        @Override
        public void onZoneEnter(final Zone zone, final Creature actor) {
            if (actor != null && actor.isPlayer()) {
                final Player player = actor.getPlayer();
                if (player.isGM()) {
                    return;
                }
                if (_playerClassIds.contains(player.getActiveClassId())) {
                    player.sendMessage(new CustomMessage("scripts.zones.epic.banishClassMsg", player));
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
