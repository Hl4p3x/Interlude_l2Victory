package ru.j2dev.gameserver.model.actor.instances.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExAutoSoulShot;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ShortCutInit;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ShortCutList {
    private static final Logger LOGGER = LoggerFactory.getLogger(ShortCutList.class);

    private final Player player;
    private final Map<Integer, ShortCut> _shortCuts;

    public ShortCutList(final Player owner) {
        _shortCuts = new ConcurrentHashMap<>();
        player = owner;
    }

    public Collection<ShortCut> getAllShortCuts() {
        return _shortCuts.values();
    }

    public void validate() {
        _shortCuts.values().stream().filter(sc -> sc.getType() == 1 && player.getInventory().getItemByObjectId(sc.getId()) == null).forEach(sc -> deleteShortCut(sc.getSlot(), sc.getPage()));
    }

    public ShortCut getShortCut(final int slot, final int page) {
        ShortCut sc = _shortCuts.get(slot + page * 12);
        if (sc != null && sc.getType() == 1 && player.getInventory().getItemByObjectId(sc.getId()) == null) {
            player.sendPacket(Msg.THERE_ARE_NO_MORE_ITEMS_IN_THE_SHORTCUT);
            deleteShortCut(sc.getSlot(), sc.getPage());
            sc = null;
        }
        return sc;
    }

    public void registerShortCut(final ShortCut shortcut) {
        final ShortCut oldShortCut = _shortCuts.put(shortcut.getSlot() + 12 * shortcut.getPage(), shortcut);
        registerShortCutInDb(shortcut, oldShortCut);
    }

    private synchronized void registerShortCutInDb(final ShortCut shortcut, final ShortCut oldShortCut) {
        if (oldShortCut != null) {
            deleteShortCutFromDb(oldShortCut);
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO character_shortcuts SET object_id=?,slot=?,page=?,type=?,shortcut_id=?,level=?,character_type=?,class_index=?");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, shortcut.getSlot());
            statement.setInt(3, shortcut.getPage());
            statement.setInt(4, shortcut.getType());
            statement.setInt(5, shortcut.getId());
            statement.setInt(6, shortcut.getLevel());
            statement.setInt(7, shortcut.getCharacterType());
            statement.setInt(8, player.getActiveClassId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("could not store shortcuts:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private void deleteShortCutFromDb(final ShortCut shortcut) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_shortcuts WHERE object_id=? AND slot=? AND page=? AND class_index=?");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, shortcut.getSlot());
            statement.setInt(3, shortcut.getPage());
            statement.setInt(4, player.getActiveClassId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("could not delete shortcuts:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void deleteShortCut(final int slot, final int page) {
        final ShortCut old = _shortCuts.remove(slot + page * 12);
        if (old == null) {
            return;
        }
        deleteShortCutFromDb(old);
        if (old.getType() == 2) {
            player.sendPacket(new ShortCutInit(player));
            player.getAutoSoulShot().stream().mapToInt(shotId -> shotId).mapToObj(shotId -> new ExAutoSoulShot(shotId, true)).forEach(player::sendPacket);
        }
    }

    public void deleteShortCutByObjectId(final int objectId) {
        _shortCuts.values().stream().filter(shortcut -> shortcut != null && shortcut.getType() == 1 && shortcut.getId() == objectId).forEach(shortcut -> deleteShortCut(shortcut.getSlot(), shortcut.getPage()));
    }

    public void deleteShortCutBySkillId(final int skillId) {
        _shortCuts.values().stream().filter(shortcut -> shortcut != null && shortcut.getType() == 2 && shortcut.getId() == skillId).forEach(shortcut -> deleteShortCut(shortcut.getSlot(), shortcut.getPage()));
    }

    public void restore() {
        _shortCuts.clear();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT character_type, slot, page, type, shortcut_id, level FROM character_shortcuts WHERE object_id=? AND class_index=?");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, player.getActiveClassId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int slot = rset.getInt("slot");
                final int page = rset.getInt("page");
                final int type = rset.getInt("type");
                final int id = rset.getInt("shortcut_id");
                final int level = rset.getInt("level");
                final int character_type = rset.getInt("character_type");
                _shortCuts.put(slot + page * 12, new ShortCut(slot, page, type, id, level, character_type));
            }
        } catch (Exception e) {
            LOGGER.error("could not store shortcuts:", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }
}
