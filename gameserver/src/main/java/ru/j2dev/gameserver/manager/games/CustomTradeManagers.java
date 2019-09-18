package ru.j2dev.gameserver.manager.games;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.data.xml.holder.ShadowTradeHolder;
import ru.j2dev.gameserver.listener.game.OnDayNightChangeListener;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.shadowtrade.ShadowTradeLoc;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.NpcUtils;

/**
 * Created by JunkyFunky
 * on 18.01.2018 23:08
 * group j2dev
 */
public class CustomTradeManagers {
    private static final Logger LOGGER = LoggerFactory.getLogger(CustomTradeManagers.class);
    private static CustomTradeManagers _instance;
    private static NpcInstance shadowTrader;
    private static NpcInstance dayTrader;

    private CustomTradeManagers() {
        LOGGER.info("CustomTrade Spawn Manager : Loading...");
        GameTimeController.getInstance().addListener(new DayNightChange());
        if (GameTimeController.getInstance().isNowNight()) {
            spawnNightManager();
        }
        if (!GameTimeController.getInstance().isNowNight() && Rnd.chance(50)) {
            spawnDayManager();
        }
    }

    public static CustomTradeManagers getInstance() {
        if (_instance == null) {
            _instance = new CustomTradeManagers();
        }
        return _instance;
    }

    private void spawnNightManager() {
        ShadowTradeLoc tempLoc = ShadowTradeHolder.getInstance().getRndNightLoc();
        int npcId = 45001;
        shadowTrader = NpcUtils.spawnSingle(npcId, tempLoc);
        Announcements.getInstance().announceByCustomMessage("ru.j2dev.gameserver.instancemanager.ShadowTraderManager", null);
        if (tempLoc.getDecription() != null && !tempLoc.getDecription().isEmpty()) {
            Announcements.getInstance().announceToAll(tempLoc.getDecription());
        }
        Log.add("onNight Manager Spawned in Location :" + tempLoc.toString(), "ShadowTrader");
    }

    private void spawnDayManager() {
        ShadowTradeLoc tempLoc = ShadowTradeHolder.getInstance().getRndDayLoc();
        int npcId = 45002;
        dayTrader = NpcUtils.spawnSingle(npcId, tempLoc);
        Announcements.getInstance().announceByCustomMessage("ru.j2dev.gameserver.instancemanager.DayTraderManger", null);
        if (tempLoc.getDecription() != null && !tempLoc.getDecription().isEmpty()) {
            Announcements.getInstance().announceToAll(tempLoc.getDecription());
        }
        Log.add("onDay Manager Spawned in Location :" + tempLoc.toString(), "DayTrader");
    }

    private class DayNightChange implements OnDayNightChangeListener {

        @Override
        public void onDay() {
            if (shadowTrader != null) {
                shadowTrader.deleteMe();
                shadowTrader = null;
            }
            if (Rnd.chance(50)) {
                spawnDayManager();
            }
        }

        @Override
        public void onNight() {
            if (dayTrader != null) {
                dayTrader.deleteMe();
                dayTrader = null;
            }
            spawnNightManager();
        }
    }

}
