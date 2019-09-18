package ru.j2dev.gameserver.dao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.base.Element;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.ItemInstance.ItemLocation;
import ru.j2dev.gameserver.model.items.ItemStateFlags;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

public class ItemsDAO {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemsDAO.class);
    private static final String SQLP_GET_ITEM = "{CALL `lip_GetItem`(?)}";
    private static final String SQLP_LOAD_ITEMS_BY_OWNER = "{CALL `lip_LoadItemsByOwner`(?)}";
    private static final String SQLP_LOAD_ITEMS_BY_OWNER_AND_LOC = "{CALL `lip_LoadItemsByOwnerAndLoc`(?, ?)}";
    private static final String SQLP_DELETE_ITEM = "{CALL `lip_DeleteItem`(?)}";
    private static final String SQLP_STORE_ITEM = "{CALL `lip_StoreItem`(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)}";


    public static ItemsDAO getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void store(final ItemInstance item) {
        if (!item.getItemStateFlag().get(ItemStateFlags.STATE_CHANGED)) {
            return;
        }
        Connection conn = null;
        CallableStatement cstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cstmt = conn.prepareCall(SQLP_STORE_ITEM);
            store0(item, cstmt);
            cstmt.execute();
            item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
        } catch (SQLException ex) {
            LOGGER.error("Exception while store item", ex);
        } finally {
            DbUtils.closeQuietly(conn, cstmt);
        }
    }

    public void store(final Collection<ItemInstance> items) {
        Connection conn = null;
        CallableStatement cstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cstmt = conn.prepareCall(SQLP_STORE_ITEM);
            for (final ItemInstance item : items) {
                if (!item.getItemStateFlag().get(ItemStateFlags.STATE_CHANGED)) {
                    continue;
                }
                store0(item, cstmt);
                cstmt.execute();
                item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception while store items", ex);
        } finally {
            DbUtils.closeQuietly(conn, cstmt);
        }
    }

    private void store0(final ItemInstance item, final CallableStatement cStmt) throws SQLException {
        cStmt.setInt(1, item.getObjectId());
        cStmt.setInt(2, item.getOwnerId());
        cStmt.setInt(3, item.getItemId());
        cStmt.setLong(4, item.getCount());
        cStmt.setInt(5, item.getLocData());
        cStmt.setString(6, item.getLocName());
        cStmt.setInt(7, item.getEnchantLevel());
        cStmt.setInt(8, item.getDuration());
        cStmt.setInt(9, item.getPeriodBegin());
        if (item.isWeapon()) {
            cStmt.setByte(10, item.getAttackElement().getId());
            cStmt.setInt(11, item.getAttackElementValue());
            cStmt.setInt(12, 0);
            cStmt.setInt(13, 0);
            cStmt.setInt(14, 0);
            cStmt.setInt(15, 0);
            cStmt.setInt(16, 0);
            cStmt.setInt(17, 0);
        } else {
            cStmt.setByte(10, Element.NONE.getId());
            cStmt.setInt(11, 0);
            cStmt.setInt(12, item.getAttributes().getFire());
            cStmt.setInt(13, item.getAttributes().getWater());
            cStmt.setInt(14, item.getAttributes().getWind());
            cStmt.setInt(15, item.getAttributes().getEarth());
            cStmt.setInt(16, item.getAttributes().getHoly());
            cStmt.setInt(17, item.getAttributes().getUnholy());
        }
        cStmt.setInt(18, item.getVariationStat1());
        cStmt.setInt(19, item.getVariationStat2());
        cStmt.setInt(20, item.getBlessed());
        cStmt.setInt(21, item.getDamaged());
        cStmt.setInt(22, item.getAgathionEnergy());
        cStmt.setInt(23, item.getCustomFlags());
        if (item.getVisibleItemId() != item.getItemId()) {
            cStmt.setInt(24, item.getVisibleItemId());
        } else {
            cStmt.setInt(24, 0);
        }
    }

    public void delete(final Collection<ItemInstance> items) {
        Connection conn = null;
        CallableStatement cStmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cStmt = conn.prepareCall(SQLP_DELETE_ITEM);
            for (final ItemInstance item : items) {
                cStmt.setInt(1, item.getObjectId());
                cStmt.execute();
                item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception while deleting items", ex);
        } finally {
            DbUtils.closeQuietly(conn, cStmt);
        }
    }

    public void delete(final int itemObjectId) {
        Connection conn = null;
        CallableStatement cStmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cStmt = conn.prepareCall(SQLP_DELETE_ITEM);
            cStmt.setInt(1, itemObjectId);
            cStmt.execute();
        } catch (SQLException ex) {
            LOGGER.error("Exception while deleting item", ex);
        } finally {
            DbUtils.closeQuietly(conn, cStmt);
        }
    }

    public void delete(final ItemInstance item) {
        Connection conn = null;
        CallableStatement cStmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cStmt = conn.prepareCall(SQLP_DELETE_ITEM);
            cStmt.setInt(1, item.getObjectId());
            cStmt.execute();
            item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
        } catch (SQLException ex) {
            LOGGER.error("Exception while deleting item", ex);
        } finally {
            DbUtils.closeQuietly(conn, cStmt);
        }
    }

    private ItemInstance load0(final ResultSet rset) throws SQLException {
        final int item_obj_id = rset.getInt("item_id");
        final ItemInstance item = new ItemInstance(item_obj_id);
        final int ownerObjId = rset.getInt("owner_id");
        item.setOwnerId(ownerObjId);
        final int itemTypeId = rset.getInt("item_type");
        final ItemTemplate itemTemplate = ItemTemplateHolder.getInstance().getTemplate(itemTypeId);
        if (itemTemplate == null) {
            LOGGER.error("Not defined itemTypeId " + itemTypeId + " for [" + ownerObjId + "]");
            return null;
        }
        item.setItemId(itemTypeId);
        item.setCount(rset.getLong("amount"));
        item.setLocName(rset.getString("location"));
        item.setLocData(rset.getInt("slot"));
        item.setEnchantLevel(rset.getInt("enchant"));
        item.setDuration(rset.getInt("duration"));
        item.setPeriodBegin(rset.getInt("period"));
        final byte atkAttrElem = rset.getByte("attack_attr_type");
        final int atkAttrVal = rset.getInt("attack_attr_val");
        final int defAttrFire = rset.getInt("defence_attr_fire");
        final int defAttrWater = rset.getInt("defence_attr_water");
        final int defAttrWind = rset.getInt("defence_attr_wind");
        final int defAttrEarth = rset.getInt("defence_attr_earth");
        final int defAttrHoly = rset.getInt("defence_attr_holy");
        final int defAttrUnholy = rset.getInt("defence_attr_unholy");
        if (atkAttrElem != Element.NONE.getId()) {
            item.setAttributeElement(Element.VALUES[atkAttrElem], atkAttrVal);
        } else {
            item.setAttributeElement(Element.FIRE, defAttrFire);
            item.setAttributeElement(Element.WATER, defAttrWater);
            item.setAttributeElement(Element.WIND, defAttrWind);
            item.setAttributeElement(Element.EARTH, defAttrEarth);
            item.setAttributeElement(Element.HOLY, defAttrHoly);
            item.setAttributeElement(Element.UNHOLY, defAttrUnholy);
        }
        item.setVariationStat1(rset.getInt("variation_stat1"));
        item.setVariationStat2(rset.getInt("variation_stat2"));
        item.setBlessed(rset.getInt("blessed"));
        item.setDamaged(rset.getInt("damaged"));
        item.setAgathionEnergy(rset.getInt("item_energy"));
        item.setCustomFlags(rset.getInt("custom_flags"));
        item.setVisibleItemId(rset.getInt("item_vis_type"));
        item.getItemStateFlag().set(ItemStateFlags.STATE_CHANGED, false);
        return item;
    }


    public Collection<ItemInstance> loadItemsByOwnerIdAndLoc(final int ownerId, final ItemLocation baseLocation) {
        final LinkedList<ItemInstance> result = new LinkedList<>();
        Connection conn = null;
        CallableStatement cStmt = null;
        ResultSet rSet = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cStmt = conn.prepareCall(SQLP_LOAD_ITEMS_BY_OWNER_AND_LOC);
            cStmt.setInt(1, ownerId);
            cStmt.setString(2, baseLocation.name());
            rSet = cStmt.executeQuery();
            while (rSet.next()) {
                result.add(load0(rSet));
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception while load items", ex);
        } finally {
            DbUtils.closeQuietly(conn, cStmt, rSet);
        }
        return result;
    }


    public Collection<Integer> loadItemObjectIdsByOwner(final int ownerId) {
        Connection conn = null;
        CallableStatement cStmt = null;
        ResultSet rSet = null;
        final LinkedList<Integer> result = new LinkedList<>();
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cStmt = conn.prepareCall(SQLP_LOAD_ITEMS_BY_OWNER);
            cStmt.setInt(1, ownerId);
            rSet = cStmt.executeQuery();
            while (rSet.next()) {
                final ItemInstance itemInstance = load0(rSet);
                if (itemInstance == null) {
                    continue;
                }
                result.add(rSet.getInt("item_id"));
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception while load items", ex);
        } finally {
            DbUtils.closeQuietly(conn, cStmt, rSet);
        }
        return result;
    }


    public ItemInstance load(final int item_obj_id) {
        ItemInstance result = null;
        Connection conn = null;
        CallableStatement cStmt = null;
        ResultSet rSet = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            cStmt = conn.prepareCall(SQLP_GET_ITEM);
            cStmt.setInt(1, item_obj_id);
            rSet = cStmt.executeQuery();
            if (rSet.next()) {
                result = load0(rSet);
            }
        } catch (SQLException ex) {
            LOGGER.error("Exception while load items", ex);
        } finally {
            DbUtils.closeQuietly(conn, cStmt, rSet);
        }
        return result;
    }

    private static class LazyHolder {
        private static final ItemsDAO INSTANCE = new ItemsDAO();
    }
}
