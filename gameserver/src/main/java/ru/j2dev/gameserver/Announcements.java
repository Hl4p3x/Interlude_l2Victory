package ru.j2dev.gameserver;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.MapUtils;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.Future;

public class Announcements {
    private static final Logger LOGGER = LoggerFactory.getLogger(Announcements.class);
    private static final Announcements _instance = new Announcements();

    private final List<Announce> _announcements = new ArrayList<>();

    private Announcements() {
        loadAnnouncements();
    }

    public static Announcements getInstance() {
        return _instance;
    }

    public static void shout(final Creature activeChar, final String text, final ChatType type) {
        final Say2 cs = new Say2(activeChar.getObjectId(), type, activeChar.getName(), text);
        final int rx = MapUtils.regionX(activeChar);
        final int ry = MapUtils.regionY(activeChar);
        final int offset = Config.SHOUT_OFFSET;
        GameObjectsStorage.getPlayers().stream().filter(player -> player != activeChar).filter(player -> activeChar.getReflection() == player.getReflection()).forEach(player -> {
            final int tx = MapUtils.regionX(player);
            final int ty = MapUtils.regionY(player);
            if ((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !activeChar.isInRangeZ(player, Config.CHAT_RANGE)) {
                return;
            }
            player.sendPacket(cs);
        });
        activeChar.sendPacket(cs);
    }

    public List<Announce> getAnnouncements() {
        return _announcements;
    }

    public void loadAnnouncements() {
        _announcements.clear();
        try {
            final List<String> lines = Arrays.asList(FileUtils.readFileToString(new File("config/announcements.txt"), "UTF-8").split("\n"));
            lines.stream().filter(line -> !StringUtils.isEmpty(line)).forEach(line -> {
                final StringTokenizer token = new StringTokenizer(line, "\t");
                if (token.countTokens() > 1) {
                    addAnnouncement(Integer.parseInt(token.nextToken()), token.nextToken(), false);
                } else {
                    addAnnouncement(0, line, false);
                }
            });
        } catch (Exception e) {
            LOGGER.error("Error while loading config/announcements.txt!");
        }
    }

    public void showAnnouncements(final Player activeChar) {
        _announcements.forEach(announce -> announce.showAnnounce(activeChar));
    }

    public void addAnnouncement(final int val, final String text, final boolean save) {
        final Announce announce = new Announce(val, text);
        announce.start();
        _announcements.add(announce);
        if (save) {
            saveToDisk();
        }
    }

    public void delAnnouncement(final int line) {
        final Announce announce = _announcements.remove(line);
        if (announce != null) {
            announce.stop();
        }
        saveToDisk();
    }

    private void saveToDisk() {
        try {
            final File f = new File("config/announcements.txt");
            final FileWriter writer = new FileWriter(f, false);
            for (final Announce announce : _announcements) {
                writer.write(announce.getTime() + "\t" + announce.getAnnounce() + "\n");
            }
            writer.close();
        } catch (Exception e) {
            LOGGER.error("Error while saving config/announcements.txt!", e);
        }
    }

    public void announceToAll(final String... text) {
        for (String s : text) {
            announceToAll(s, ChatType.ANNOUNCEMENT);
        }
    }

    public void announceToAll(final String text) {
        announceToAll(text, ChatType.ANNOUNCEMENT);
    }

    public void announceToAll(final String text, final ChatType type) {
        final Say2 cs = new Say2(0, type, "", text);
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(cs));
    }

    public void announceByCustomMessage(final String address, final String[] replacements) {
        GameObjectsStorage.getPlayers().forEach(player -> announceToPlayerByCustomMessage(player, address, replacements));
    }

    public void announceByCustomMessage(final String address, final String[] replacements, final ChatType type) {
        GameObjectsStorage.getPlayers().forEach(player -> announceToPlayerByCustomMessage(player, address, replacements, type));
    }

    public void announceToPlayerByCustomMessage(final Player player, final String address, final String[] replacements) {
        final CustomMessage cm = new CustomMessage(address, player);
        if (replacements != null) {
            Arrays.stream(replacements).forEach(cm::addString);
        }
        player.sendPacket(new Say2(0, ChatType.ANNOUNCEMENT, "", cm.toString()));
    }

    public void announceToPlayerByCustomMessage(final Player player, final String address, final String[] replacements, final ChatType type) {
        final CustomMessage cm = new CustomMessage(address, player);
        if (replacements != null) {
            Arrays.stream(replacements).forEach(cm::addString);
        }
        player.sendPacket(new Say2(0, type, "", cm.toString()));
    }

    public void announceToAll(final SystemMessage sm) {
        GameObjectsStorage.sendPacketToAllPlayers(sm);
    }

    public class Announce extends RunnableImpl {
        private final int _time;
        private final String _announce;
        private Future<?> _task;

        Announce(final int t, final String announce) {
            _time = t;
            _announce = announce;
        }

        @Override
        public void runImpl() {
            announceToAll(_announce);
        }

        void showAnnounce(final Player player) {
            final Say2 cs = new Say2(0, ChatType.ANNOUNCEMENT, player.getName(), _announce);
            player.sendPacket(cs);
        }

        public void start() {
            if (_time > 0) {
                _task = ThreadPoolManager.getInstance().scheduleAtFixedRate(this, _time * 1000L, _time * 1000L);
            }
        }

        public void stop() {
            if (_task != null) {
                _task.cancel(false);
                _task = null;
            }
        }

        public int getTime() {
            return _time;
        }

        public String getAnnounce() {
            return _announce;
        }
    }
}
