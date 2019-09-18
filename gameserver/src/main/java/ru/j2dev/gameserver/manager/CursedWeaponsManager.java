package ru.j2dev.gameserver.manager;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.j2dev.commons.collections.ConcurrentHashSet;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.CursedWeapon;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Location;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class CursedWeaponsManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CursedWeaponsManager.class);
    private static final int CURSEDWEAPONS_MAINTENANCE_INTERVAL = 300000;
    private final TIntObjectHashMap<CursedWeapon> _cursedWeaponsMap = new TIntObjectHashMap<>();
    private Set<CursedWeapon> _cursedWeapons = new ConcurrentHashSet<>();
    private ScheduledFuture<?> _removeTask;

    public CursedWeaponsManager() {
        if (!Config.ALLOW_CURSED_WEAPONS) {
            return;
        }
        load();
        restore();
        checkConditions();
        cancelTask();
        _removeTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RemoveTask(), CURSEDWEAPONS_MAINTENANCE_INTERVAL, CURSEDWEAPONS_MAINTENANCE_INTERVAL);
        LOGGER.info("CursedWeaponsManager: Loaded " + _cursedWeapons.size() + " cursed weapon(s).");
    }

    public static CursedWeaponsManager getInstance() {
        return LazyHolder.ourInstance;
    }

    @Deprecated
    public final void reload() {
    }

    private void load() {
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            final File file = new File(Config.DATAPACK_ROOT, "data/cursed_weapons.xml");
            if (!file.exists()) {
                return;
            }
            final Document doc = factory.newDocumentBuilder().parse(file);
            for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
                if ("list".equalsIgnoreCase(n.getNodeName())) {
                    for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                        if ("item".equalsIgnoreCase(d.getNodeName())) {
                            NamedNodeMap attrs = d.getAttributes();
                            final int id = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
                            final int skillId = Integer.parseInt(attrs.getNamedItem("skillId").getNodeValue());
                            String name = "Unknown cursed weapon";
                            if (attrs.getNamedItem("name") != null) {
                                name = attrs.getNamedItem("name").getNodeValue();
                            } else if (ItemTemplateHolder.getInstance().getTemplate(id) != null) {
                                name = ItemTemplateHolder.getInstance().getTemplate(id).getName();
                            }
                            if (id != 0) {
                                final CursedWeapon cw = new CursedWeapon(id, skillId, name);
                                for (Node cd = d.getFirstChild(); cd != null; cd = cd.getNextSibling()) {
                                    if ("dropRate".equalsIgnoreCase(cd.getNodeName())) {
                                        cw.setDropRate(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
                                    } else if ("duration".equalsIgnoreCase(cd.getNodeName())) {
                                        attrs = cd.getAttributes();
                                        cw.setDurationMin(Integer.parseInt(attrs.getNamedItem("min").getNodeValue()));
                                        cw.setDurationMax(Integer.parseInt(attrs.getNamedItem("max").getNodeValue()));
                                    } else if ("durationLost".equalsIgnoreCase(cd.getNodeName())) {
                                        cw.setDurationLost(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
                                    } else if ("disapearChance".equalsIgnoreCase(cd.getNodeName())) {
                                        cw.setDisapearChance(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
                                    } else if ("stageKills".equalsIgnoreCase(cd.getNodeName())) {
                                        cw.setStageKills(Integer.parseInt(cd.getAttributes().getNamedItem("val").getNodeValue()));
                                    } else if ("fakeName".equalsIgnoreCase(cd.getNodeName())) {
                                        cw.setTransformationName(cd.getAttributes().getNamedItem("val").getNodeValue());
                                    }
                                }
                                _cursedWeaponsMap.put(id, cw);
                            }
                        }
                    }
                }
            }
            _cursedWeapons.addAll(_cursedWeaponsMap.valueCollection());
        } catch (Exception e) {
            LOGGER.error("CursedWeaponsManager: Error parsing cursed_weapons file. " + e);
        }
    }

    private void restore() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM cursed_weapons");
            rset = statement.executeQuery();
            while (rset.next()) {
                final int itemId = rset.getInt("item_id");
                final CursedWeapon cw = _cursedWeaponsMap.get(itemId);
                if (cw != null) {
                    cw.setPlayerId(rset.getInt("player_id"));
                    cw.setPlayerKarma(rset.getInt("player_karma"));
                    cw.setPlayerPkKills(rset.getInt("player_pkkills"));
                    cw.setNbKills(rset.getInt("nb_kills"));
                    cw.setLoc(new Location(rset.getInt("x"), rset.getInt("y"), rset.getInt("z")));
                    cw.setEndTime(rset.getLong("end_time") * 1000L);
                    cw.giveSkillAndUpdateStats();
                    if (cw.reActivate()) {
                        continue;
                    }
                    endOfLife(cw);
                } else {
                    removeFromDb(itemId);
                    LOGGER.warn("CursedWeaponsManager: Unknown cursed weapon " + itemId + ", deleted");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("CursedWeaponsManager: Could not restore cursed_weapons data: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void checkConditions() {
        Connection con = null;
        PreparedStatement statement1 = null;
        PreparedStatement statement2 = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement1 = con.prepareStatement("DELETE FROM character_skills WHERE skill_id=?");
            statement2 = con.prepareStatement("SELECT owner_id FROM items WHERE item_type=?");
            for (final CursedWeapon cw : _cursedWeapons) {
                final int itemId = cw.getItemId();
                final int skillId = cw.getSkillId();
                boolean foundedInItems = false;
                statement1.setInt(1, skillId);
                statement1.executeUpdate();
                statement2.setInt(1, itemId);
                rset = statement2.executeQuery();
                while (rset.next()) {
                    final int playerId = rset.getInt("owner_id");
                    if (!foundedInItems) {
                        if (playerId != cw.getPlayerId() || cw.getPlayerId() == 0) {
                            emptyPlayerCursedWeapon(playerId, itemId, cw);
                            LOGGER.info("CursedWeaponsManager[254]: Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
                        } else {
                            foundedInItems = true;
                        }
                    } else {
                        emptyPlayerCursedWeapon(playerId, itemId, cw);
                        LOGGER.info("CursedWeaponsManager[262]: Player " + playerId + " owns the cursed weapon " + itemId + " but he shouldn't.");
                    }
                }
                if (!foundedInItems && cw.getPlayerId() != 0) {
                    removeFromDb(cw.getItemId());
                    LOGGER.info("CursedWeaponsManager: Unownered weapon, removing from table...");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("CursedWeaponsManager: Could not check cursed_weapons data: " + e);
        } finally {
            DbUtils.closeQuietly(statement1);
            DbUtils.closeQuietly(con, statement2, rset);
        }
    }

    private void emptyPlayerCursedWeapon(final int playerId, final int itemId, final CursedWeapon cw) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_type=?");
            statement.setInt(1, playerId);
            statement.setInt(2, itemId);
            statement.executeUpdate();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_id=?");
            statement.setInt(1, cw.getPlayerKarma());
            statement.setInt(2, cw.getPlayerPkKills());
            statement.setInt(3, playerId);
            if (statement.executeUpdate() != 1) {
                LOGGER.warn("Error while updating karma & pkkills for userId " + cw.getPlayerId());
            }
            removeFromDb(itemId);
        } catch (SQLException e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void removeFromDb(final int itemId) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE item_id = ?");
            statement.setInt(1, itemId);
            statement.executeUpdate();
            if (getCursedWeapon(itemId) != null) {
                getCursedWeapon(itemId).initWeapon();
            }
        } catch (SQLException e) {
            LOGGER.error("CursedWeaponsManager: Failed to remove data: " + e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private void cancelTask() {
        if (_removeTask != null) {
            _removeTask.cancel(false);
            _removeTask = null;
        }
    }

    public void endOfLife(final CursedWeapon cw) {
        if (cw.isActivated()) {
            final Player player = cw.getOnlineOwner();
            if (player != null) {
                LOGGER.info("CursedWeaponsManager: " + cw.getName() + " being removed online from " + player + ".");
                player.abortAttack(true, true);
                player.setKarma(cw.getPlayerKarma());
                player.setPkKills(cw.getPlayerPkKills());
                player.setCursedWeaponEquippedId(0);
                player.setTransformationName(null);
                player.getInventory().destroyItemByItemId(cw.getItemId(), 1L);
                player.removeSkill(SkillTable.getInstance().getInfo(cw.getSkillId(), player.getSkillLevel(cw.getSkillId())), true);
                player.broadcastCharInfo();
            } else {
                LOGGER.info("CursedWeaponsManager: " + cw.getName() + " being removed offline.");
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    statement = con.prepareStatement("DELETE FROM items WHERE owner_id=? AND item_type=?");
                    statement.setInt(1, cw.getPlayerId());
                    statement.setInt(2, cw.getItemId());
                    statement.executeUpdate();
                    DbUtils.close(statement);
                    statement = con.prepareStatement("DELETE FROM character_skills WHERE char_obj_id=? AND skill_id=?");
                    statement.setInt(1, cw.getPlayerId());
                    statement.setInt(2, cw.getSkillId());
                    statement.executeUpdate();
                    DbUtils.close(statement);
                    statement = con.prepareStatement("UPDATE characters SET karma=?, pkkills=? WHERE obj_Id=?");
                    statement.setInt(1, cw.getPlayerKarma());
                    statement.setInt(2, cw.getPlayerPkKills());
                    statement.setInt(3, cw.getPlayerId());
                    statement.executeUpdate();
                } catch (SQLException e) {
                    LOGGER.warn("CursedWeaponsManager: Could not delete : " + e);
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
            }
        } else if (cw.getPlayer() != null && cw.getPlayer().getInventory().getItemByItemId(cw.getItemId()) != null) {
            final Player player = cw.getPlayer();
            if (!cw.getPlayer().getInventory().destroyItemByItemId(cw.getItemId(), 1L)) {
                LOGGER.info("CursedWeaponsManager[453]: Error! Cursed weapon not found!!!");
            }
            player.sendChanges();
            player.broadcastUserInfo(true);
        } else if (cw.getItem() != null) {
            cw.getItem().deleteMe();
            cw.getItem().delete();
            LOGGER.info("CursedWeaponsManager: " + cw.getName() + " item has been removed from World.");
        }
        cw.initWeapon();
        removeFromDb(cw.getItemId());
        announce(new SystemMessage(1818).addString(cw.getName()));
    }

    public void saveData(final CursedWeapon cw) {
        Connection con = null;
        PreparedStatement statement = null;
        synchronized (cw) {
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("DELETE FROM cursed_weapons WHERE item_id = ?");
                statement.setInt(1, cw.getItemId());
                statement.executeUpdate();
                if (cw.isActive()) {
                    DbUtils.close(statement);
                    statement = con.prepareStatement("REPLACE INTO cursed_weapons (item_id, player_id, player_karma, player_pkkills, nb_kills, x, y, z, end_time) VALUES (?,?,?,?,?,?,?,?,?)");
                    statement.setInt(1, cw.getItemId());
                    statement.setInt(2, cw.getPlayerId());
                    statement.setInt(3, cw.getPlayerKarma());
                    statement.setInt(4, cw.getPlayerPkKills());
                    statement.setInt(5, cw.getNbKills());
                    statement.setInt(6, cw.getLoc().x);
                    statement.setInt(7, cw.getLoc().y);
                    statement.setInt(8, cw.getLoc().z);
                    statement.setLong(9, cw.getEndTime() / 1000L);
                    statement.executeUpdate();
                }
            } catch (SQLException e) {
                LOGGER.error("CursedWeapon: Failed to save data: " + e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
    }

    public void saveData() {
        _cursedWeapons.forEach(this::saveData);
    }

    public void checkPlayer(final Player player, final ItemInstance item) {
        if (player == null || item == null || player.isOlyParticipant()) {
            return;
        }
        final CursedWeapon cw = _cursedWeaponsMap.get(item.getItemId());
        if (cw == null) {
            return;
        }
        if (player.getObjectId() == cw.getPlayerId() || cw.getPlayerId() == 0 || cw.isDropped()) {
            activate(player, item);
            showUsageTime(player, cw);
        } else {
            LOGGER.warn("CursedWeaponsManager: " + player + " tried to obtain " + item + " in wrong way");
            player.getInventory().destroyItem(item, item.getCount());
        }
    }

    public void activate(final Player player, final ItemInstance item) {
        if (player == null || player.isOlyParticipant()) {
            return;
        }
        final CursedWeapon cw = _cursedWeaponsMap.get(item.getItemId());
        if (cw == null) {
            return;
        }
        if (player.isCursedWeaponEquipped()) {
            if (player.getCursedWeaponEquippedId() != item.getItemId()) {
                final CursedWeapon cw2 = _cursedWeaponsMap.get(player.getCursedWeaponEquippedId());
                cw2.setNbKills(cw2.getStageKills() - 1);
                cw2.increaseKills();
            }
            endOfLife(cw);
            player.getInventory().destroyItem(item, 1L);
        } else if (cw.getTimeLeft() > 0L) {
            cw.activate(player, item);
            saveData(cw);
            announce(new SystemMessage(1816).addZoneName(player.getLoc()).addString(cw.getName()));
        } else {
            endOfLife(cw);
            player.getInventory().destroyItem(item, 1L);
        }
    }

    public void doLogout(final Player player) {
        _cursedWeapons.stream().filter(cw -> player.getInventory().getItemByItemId(cw.getItemId()) != null).forEach(cw -> {
            cw.setPlayer(null);
            cw.setItem(null);
        });
    }

    public void dropAttackable(final NpcInstance attackable, final Player killer) {
        if (killer.isOlyParticipant() || killer.isCursedWeaponEquipped() || _cursedWeapons.size() == 0 || killer.getReflection() != ReflectionManager.DEFAULT) {
            return;
        }
        List<CursedWeapon> cursedWeapons = _cursedWeapons.stream().filter(cw -> !cw.isActive()).collect(Collectors.toList());
        if (cursedWeapons.size() > 0) {
                final CursedWeapon cw2 = Rnd.get(cursedWeapons);
                if (Rnd.get(100000000) <= cw2.getDropRate()) {
                    cw2.create(attackable, killer);
                }
            }
    }

    public void dropPlayer(final Player player) {
        final CursedWeapon cw = _cursedWeaponsMap.get(player.getCursedWeaponEquippedId());
        if (cw == null) {
            return;
        }
        if (cw.dropIt(null, null, player)) {
            saveData(cw);
            announce(new SystemMessage(1815).addZoneName(player.getLoc()).addItemName(cw.getItemId()));
        } else {
            endOfLife(cw);
        }
    }

    public void increaseKills(final int itemId) {
        final CursedWeapon cw = _cursedWeaponsMap.get(itemId);
        if (cw != null) {
            cw.increaseKills();
            saveData(cw);
        }
    }

    public int getLevel(final int itemId) {
        final CursedWeapon cw = _cursedWeaponsMap.get(itemId);
        return (cw != null) ? cw.getLevel() : 0;
    }

    public void announce(final SystemMessage sm) {
        GameObjectsStorage.getPlayers().forEach(player -> player.sendPacket(sm));
    }

    public void showUsageTime(final Player player, final int itemId) {
        final CursedWeapon cw = _cursedWeaponsMap.get(itemId);
        if (cw != null) {
            showUsageTime(player, cw);
            cw.giveSkill(player);
        }
    }

    /**
     *
     * @param player
     * @param cw
     */
    public void showUsageTime(final Player player, final CursedWeapon cw) {
        final SystemMessage sm = new SystemMessage(1814);
        sm.addString(cw.getName());
        sm.addNumber(cw.getTimeLeft() / 60000L);
        player.sendPacket(sm);
    }

    public boolean isCursed(final int itemId) {
        return _cursedWeaponsMap.containsKey(itemId);
    }

    public Set<CursedWeapon> getCursedWeapons() {
        return _cursedWeapons;
    }

    public int[] getCursedWeaponsIds() {
        return _cursedWeaponsMap.keys();
    }

    public CursedWeapon getCursedWeapon(final int itemId) {
        return _cursedWeaponsMap.get(itemId);
    }

    private static class LazyHolder {
        private static final CursedWeaponsManager ourInstance = new CursedWeaponsManager();
    }

    private class RemoveTask extends RunnableImpl {
        @Override
        public void runImpl() {
            _cursedWeapons.stream().filter(cw -> cw.isActive() && cw.getTimeLeft() <= 0L).forEach(CursedWeaponsManager.this::endOfLife);
        }
    }
}
