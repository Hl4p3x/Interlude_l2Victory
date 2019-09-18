package ru.j2dev.gameserver.model.entity.residence;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.math.SafeMath;
import ru.j2dev.gameserver.dao.CastleDAO;
import ru.j2dev.gameserver.dao.CastleHiredGuardDAO;
import ru.j2dev.gameserver.dao.ClanDataDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.SevenSigns;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.items.Warehouse;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.components.NpcString;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.item.support.MerchantGuard;
import ru.j2dev.gameserver.templates.manor.CropProcure;
import ru.j2dev.gameserver.templates.manor.SeedProduction;
import ru.j2dev.gameserver.utils.GameStats;
import ru.j2dev.gameserver.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.stream.Collectors;

public class Castle extends Residence {
    private static final Logger LOGGER = LoggerFactory.getLogger(Castle.class);
    private static final String CASTLE_MANOR_DELETE_PRODUCTION = "DELETE FROM castle_manor_production WHERE castle_id=?;";
    private static final String CASTLE_MANOR_DELETE_PRODUCTION_PERIOD = "DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;";
    private static final String CASTLE_MANOR_DELETE_PROCURE = "DELETE FROM castle_manor_procure WHERE castle_id=?;";
    private static final String CASTLE_MANOR_DELETE_PROCURE_PERIOD = "DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;";
    private static final String CASTLE_UPDATE_CROP = "UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?";
    private static final String CASTLE_UPDATE_SEED = "UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?";

    private final Map<Integer, MerchantGuard> _merchantGuards = new HashMap<>();
    private final NpcString _npcStringName = NpcString.valueOf(1001000 + _id);
    private final Set<ItemInstance> _spawnMerchantTickets = new CopyOnWriteArraySet<>();
    private List<CropProcure> _procure = new ArrayList<>();
    private List<SeedProduction> _production;
    private List<CropProcure> _procureNext;
    private List<SeedProduction> _productionNext;
    private boolean _isNextPeriodApproved;
    private int _TaxPercent;
    private double _TaxRate;
    private long _treasury;
    private long _collectedShops;
    private long _collectedSeed;

    public Castle(final StatsSet set) {
        super(set);
    }

    @Override
    public ResidenceType getType() {
        return ResidenceType.Castle;
    }

    @Override
    public void changeOwner(final Clan newOwner) {
        if (newOwner != null && newOwner.getCastle() != 0) {
            final Castle oldCastle = ResidenceHolder.getInstance().getResidence(Castle.class, newOwner.getCastle());
            if (oldCastle != null) {
                oldCastle.changeOwner(null);
            }
        }
        Clan oldOwner;
        if (getOwnerId() > 0 && (newOwner == null || newOwner.getClanId() != getOwnerId())) {
            removeSkills();
            setTaxPercent(null, 0);
            cancelCycleTask();
            oldOwner = getOwner();
            if (oldOwner != null) {
                final long amount = getTreasury();
                if (amount > 0L) {
                    final Warehouse warehouse = oldOwner.getWarehouse();
                    if (warehouse != null) {
                        warehouse.addItem(57, amount);
                        addToTreasuryNoTax(-amount, false, false);
                        Log.add(getName() + "|" + -amount + "|Castle:changeOwner", "treasury");
                    }
                }
                oldOwner.getOnlineMembers(0).stream().filter(clanMember -> clanMember != null && clanMember.getInventory() != null).forEach(clanMember -> clanMember.getInventory().validateItems());
                oldOwner.setHasCastle(0);
            }
        }
        if (newOwner != null) {
            newOwner.setHasCastle(getId());
        }
        updateOwnerInDB(newOwner);
        rewardSkills();
        update();
    }

    @Override
    protected void loadData() {
        _TaxPercent = 0;
        _TaxRate = 0.0;
        _treasury = 0L;
        _procure = new ArrayList<>();
        _production = new ArrayList<>();
        _procureNext = new ArrayList<>();
        _productionNext = new ArrayList<>();
        _isNextPeriodApproved = false;
        _owner = ClanDataDAO.getInstance().getOwner(this);
        CastleDAO.getInstance().select(this);
        CastleHiredGuardDAO.getInstance().load(this);
    }

    private void updateOwnerInDB(final Clan clan) {
        _owner = clan;
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET hasCastle=0 WHERE hasCastle=? LIMIT 1");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            if (clan != null) {
                statement = con.prepareStatement("UPDATE clan_data SET hasCastle=? WHERE clan_id=? LIMIT 1");
                statement.setInt(1, getId());
                statement.setInt(2, getOwnerId());
                statement.execute();
                clan.broadcastClanStatus(true, false, false);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public int getTaxPercent() {
        if (_TaxPercent > 5 && SevenSigns.getInstance().getSealOwner(3) == 1) {
            _TaxPercent = 5;
        }
        return _TaxPercent;
    }

    public void setTaxPercent(final int p) {
        _TaxPercent = Math.min(Math.max(0, p), 100);
        _TaxRate = _TaxPercent / 100.0;
    }

    public int getTaxPercent0() {
        return _TaxPercent;
    }

    public long getCollectedShops() {
        return _collectedShops;
    }

    public void setCollectedShops(final long value) {
        _collectedShops = value;
    }

    public long getCollectedSeed() {
        return _collectedSeed;
    }

    public void setCollectedSeed(final long value) {
        _collectedSeed = value;
    }

    public void addToTreasury(long amount, final boolean shop, final boolean seed) {
        if (getOwnerId() <= 0) {
            return;
        }
        if (amount == 0L) {
            return;
        }
        if (amount > 1L && _id != 5 && _id != 8) {
            final Castle royal = ResidenceHolder.getInstance().getResidence(Castle.class, (_id >= 7) ? 8 : 5);
            if (royal != null) {
                final long royalTax = (long) (amount * royal.getTaxRate());
                if (royal.getOwnerId() > 0) {
                    royal.addToTreasury(royalTax, shop, seed);
                    if (_id == 5) {
                        Log.add("Aden|" + royalTax + "|Castle:adenTax", "treasury");
                    } else if (_id == 8) {
                        Log.add("Rune|" + royalTax + "|Castle:runeTax", "treasury");
                    }
                }
                amount -= royalTax;
            }
        }
        addToTreasuryNoTax(amount, shop, seed);
    }

    public void addToTreasuryNoTax(final long amount, final boolean shop, final boolean seed) {
        if (getOwnerId() <= 0) {
            return;
        }
        if (amount == 0L) {
            return;
        }
        GameStats.addAdena(amount);
        _treasury = SafeMath.addAndLimit(_treasury, amount);
        if (shop) {
            _collectedShops += amount;
        }
        if (seed) {
            _collectedSeed += amount;
        }
        setJdbcState(JdbcEntityState.UPDATED);
        update();
    }

    public int getCropRewardType(final int crop) {
        int rw = 0;
        for (final CropProcure cp : _procure) {
            if (cp.getId() == crop) {
                rw = cp.getReward();
            }
        }
        return rw;
    }

    public void setTaxPercent(final Player activeChar, final int taxPercent) {
        setTaxPercent(taxPercent);
        setJdbcState(JdbcEntityState.UPDATED);
        update();
        if (activeChar != null) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.model.entity.Castle.OutOfControl.CastleTaxChangetTo", activeChar).addString(getName()).addNumber(taxPercent));
        }
    }

    public double getTaxRate() {
        if (_TaxRate > 0.05 && SevenSigns.getInstance().getSealOwner(3) == 1) {
            _TaxRate = 0.05;
        }
        return _TaxRate;
    }

    public long getTreasury() {
        return _treasury;
    }

    public void setTreasury(final long t) {
        _treasury = t;
    }

    public List<SeedProduction> getSeedProduction(final int period) {
        return (period == 0) ? _production : _productionNext;
    }

    public List<CropProcure> getCropProcure(final int period) {
        return (period == 0) ? _procure : _procureNext;
    }

    public void setSeedProduction(final List<SeedProduction> seed, final int period) {
        if (period == 0) {
            _production = seed;
        } else {
            _productionNext = seed;
        }
    }

    public void setCropProcure(final List<CropProcure> crop, final int period) {
        if (period == 0) {
            _procure = crop;
        } else {
            _procureNext = crop;
        }
    }

    public synchronized SeedProduction getSeed(final int seedId, final int period) {
        return getSeedProduction(period).stream().filter(seed -> seed.getId() == seedId).findFirst().orElse(null);
    }

    public synchronized CropProcure getCrop(final int cropId, final int period) {
        return getCropProcure(period).stream().filter(crop -> crop.getId() == cropId).findFirst().orElse(null);
    }

    public long getManorCost(final int period) {
        List<CropProcure> procure;
        List<SeedProduction> production;
        if (period == 0) {
            procure = _procure;
            production = _production;
        } else {
            procure = _procureNext;
            production = _productionNext;
        }
        long total = 0L;
        if (production != null) {
            for (final SeedProduction seed : production) {
                total += Manor.getInstance().getSeedBuyPrice(seed.getId()) * seed.getStartProduce();
            }
        }
        if (procure != null) {
            total += procure.stream().mapToLong(crop -> crop.getPrice() * crop.getStartAmount()).sum();
        }
        return total;
    }

    public void saveSeedData() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=?;");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            if (_production != null) {
                int count = 0;
                StringBuilder query = new StringBuilder("INSERT INTO castle_manor_production VALUES ");
                final String[] values = new String[_production.size()];
                for (final SeedProduction s : _production) {
                    values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 0 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    query.append(values[0]);
                    for (int i = 1; i < values.length; ++i) {
                        query.append(",").append(values[i]);
                    }
                    statement = con.prepareStatement(query.toString());
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
            if (_productionNext != null) {
                int count = 0;
                StringBuilder query = new StringBuilder("INSERT INTO castle_manor_production VALUES ");
                final String[] values = new String[_productionNext.size()];
                for (final SeedProduction s : _productionNext) {
                    values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + 1 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    query.append(values[0]);
                    for (int i = 1; i < values.length; ++i) {
                        query.append(",").append(values[i]);
                    }
                    statement = con.prepareStatement(query.toString());
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error adding seed production data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void saveSeedData(final int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_production WHERE castle_id=? AND period=?;");
            statement.setInt(1, getId());
            statement.setInt(2, period);
            statement.execute();
            DbUtils.close(statement);
            List<SeedProduction> prod;
            prod = getSeedProduction(period);
            if (prod != null) {
                int count = 0;
                String query;
                final String[] values = new String[prod.size()];
                for (final SeedProduction s : prod) {
                    values[count] = "(" + getId() + "," + s.getId() + "," + s.getCanProduce() + "," + s.getStartProduce() + "," + s.getPrice() + "," + period + ")";
                    ++count;
                }
                if (values.length > 0) {
                    query = Arrays.stream(values).collect(Collectors.joining(",", "INSERT INTO castle_manor_production VALUES ", ""));
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error adding seed production data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void saveCropData() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=?;");
            statement.setInt(1, getId());
            statement.execute();
            DbUtils.close(statement);
            if (_procure != null) {
                int count = 0;
                String query;
                final String[] values = new String[_procure.size()];
                for (final CropProcure cp : _procure) {
                    values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 0 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    query = Arrays.stream(values, 0, values.length).collect(Collectors.joining(",", "INSERT INTO castle_manor_procure VALUES ", ""));
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
            if (_procureNext != null) {
                int count = 0;
                String query;
                final String[] values = new String[_procureNext.size()];
                for (final CropProcure cp : _procureNext) {
                    values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + 1 + ")";
                    ++count;
                }
                if (values.length > 0) {
                    query = Arrays.stream(values, 0, values.length).collect(Collectors.joining(",", "INSERT INTO castle_manor_procure VALUES ", ""));
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error adding crop data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void saveCropData(final int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM castle_manor_procure WHERE castle_id=? AND period=?;");
            statement.setInt(1, getId());
            statement.setInt(2, period);
            statement.execute();
            DbUtils.close(statement);
            List<CropProcure> proc;
            proc = getCropProcure(period);
            if (proc != null) {
                int count = 0;
                String query;
                final String[] values = new String[proc.size()];
                for (final CropProcure cp : proc) {
                    values[count] = "(" + getId() + "," + cp.getId() + "," + cp.getAmount() + "," + cp.getStartAmount() + "," + cp.getPrice() + "," + cp.getReward() + "," + period + ")";
                    ++count;
                }
                if (values.length > 0) {
                    query = Arrays.stream(values).collect(Collectors.joining(",", "INSERT INTO castle_manor_procure VALUES ", ""));
                    statement = con.prepareStatement(query);
                    statement.execute();
                    DbUtils.close(statement);
                }
            }
        } catch (Exception e) {
            LOGGER.error("Error adding crop data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void updateCrop(final int cropId, final long amount, final int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE castle_manor_procure SET can_buy=? WHERE crop_id=? AND castle_id=? AND period=?");
            statement.setLong(1, amount);
            statement.setInt(2, cropId);
            statement.setInt(3, getId());
            statement.setInt(4, period);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("Error adding crop data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void updateSeed(final int seedId, final long amount, final int period) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE castle_manor_production SET can_produce=? WHERE seed_id=? AND castle_id=? AND period=?");
            statement.setLong(1, amount);
            statement.setInt(2, seedId);
            statement.setInt(3, getId());
            statement.setInt(4, period);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("Error adding seed production data for castle " + getName() + "!", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public boolean isNextPeriodApproved() {
        return _isNextPeriodApproved;
    }

    public void setNextPeriodApproved(final boolean val) {
        _isNextPeriodApproved = val;
    }

    @Override
    public void update() {
        CastleDAO.getInstance().update(this);
    }

    public NpcString getNpcStringName() {
        return _npcStringName;
    }

    public void addMerchantGuard(final MerchantGuard merchantGuard) {
        _merchantGuards.put(merchantGuard.getItemId(), merchantGuard);
    }

    public MerchantGuard getMerchantGuard(final int itemId) {
        return _merchantGuards.get(itemId);
    }

    public Map<Integer, MerchantGuard> getMerchantGuards() {
        return _merchantGuards;
    }

    public Set<ItemInstance> getSpawnMerchantTickets() {
        return _spawnMerchantTickets;
    }

    @Override
    public void startCycleTask() {
    }
}
