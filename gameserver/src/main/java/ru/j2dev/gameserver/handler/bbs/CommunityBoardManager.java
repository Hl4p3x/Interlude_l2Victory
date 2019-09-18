package ru.j2dev.gameserver.handler.bbs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class CommunityBoardManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityBoardManager.class);
    private static final CommunityBoardManager _instance = new CommunityBoardManager();

    private final Map<String, ICommunityBoardHandler> _handlers = new HashMap<>();
    private final StatsSet _properties = new StatsSet();

    public static CommunityBoardManager getInstance() {
        return _instance;
    }

    public void registerHandler(final ICommunityBoardHandler commHandler) {
        for (final String bypass : commHandler.getBypassCommands()) {
            if (_handlers.containsKey(bypass)) {
                LOGGER.warn("CommunityBoard: dublicate bypass registered! First handler: " + _handlers.get(bypass).getClass().getSimpleName() + " second: " + commHandler.getClass().getSimpleName());
            }
            _handlers.put(bypass, commHandler);
        }
    }

    public void removeHandler(final ICommunityBoardHandler handler) {
        for (final String bypass : handler.getBypassCommands()) {
            _handlers.remove(bypass);
        }
        LOGGER.info("CommunityBoard: " + handler.getClass().getSimpleName() + " unloaded.");
    }

    public ICommunityBoardHandler getCommunityHandler(final String bypass) {
        if (!Config.COMMUNITYBOARD_ENABLED || _handlers.isEmpty()) {
            return null;
        }
        for (final Entry<String, ICommunityBoardHandler> entry : _handlers.entrySet()) {
            if (bypass.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public void setProperty(final String name, final String val) {
        _properties.set(name, val);
    }

    public void setProperty(final String name, final int val) {
        _properties.set(name, val);
    }

    public int getIntProperty(final String name) {
        return _properties.getInteger(name, 0);
    }
}
