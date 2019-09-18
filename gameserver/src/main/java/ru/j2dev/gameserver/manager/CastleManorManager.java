package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.items.Warehouse;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.templates.manor.CropProcure;
import ru.j2dev.gameserver.templates.manor.SeedProduction;
import ru.j2dev.gameserver.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class CastleManorManager {
    public static final int PERIOD_CURRENT = 0;
    public static final int PERIOD_NEXT = 1;
    protected static final String var_name = "ManorApproved";
    protected static final long MAINTENANCE_PERIOD = Config.MANOR_MAINTENANCE_PERIOD / 60000;
    private static final Logger LOGGER = LoggerFactory.getLogger(CastleManorManager.class);
    private static final String CASTLE_MANOR_LOAD_PROCURE = "SELECT * FROM castle_manor_procure WHERE castle_id=?";
    private static final String CASTLE_MANOR_LOAD_PRODUCTION = "SELECT * FROM castle_manor_production WHERE castle_id=?";
    private static final int NEXT_PERIOD_APPROVE = Config.MANOR_APPROVE_TIME;
    private static final int NEXT_PERIOD_APPROVE_MIN = Config.MANOR_APPROVE_MIN;
    private static final int MANOR_REFRESH = Config.MANOR_REFRESH_TIME;
    private static final int MANOR_REFRESH_MIN = Config.MANOR_REFRESH_MIN;

    private boolean _underMaintenance;
    private boolean _disabled;

    private CastleManorManager() {
        LOGGER.info("Manor System: Initializing...");
        load();
        init();
        _underMaintenance = false;
        _disabled = !Config.ALLOW_MANOR;
        final List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        castleList.forEach(c -> c.setNextPeriodApproved(ServerVariables.getBool(var_name)));
    }

    public static CastleManorManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void load() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            final List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
            for (final Castle castle : castleList) {
                final List<SeedProduction> production = new ArrayList<>();
                final List<SeedProduction> productionNext = new ArrayList<>();
                final List<CropProcure> procure = new ArrayList<>();
                final List<CropProcure> procureNext = new ArrayList<>();
                statement = con.prepareStatement(CASTLE_MANOR_LOAD_PRODUCTION);
                statement.setInt(1, castle.getId());
                rs = statement.executeQuery();
                while (rs.next()) {
                    final int seedId = rs.getInt("seed_id");
                    final long canProduce = rs.getLong("can_produce");
                    final long startProduce = rs.getLong("start_produce");
                    final long price = rs.getLong("seed_price");
                    final int period = rs.getInt("period");
                    if (period == 0) {
                        production.add(new SeedProduction(seedId, canProduce, price, startProduce));
                    } else {
                        productionNext.add(new SeedProduction(seedId, canProduce, price, startProduce));
                    }
                }
                DbUtils.close(statement, rs);
                castle.setSeedProduction(production, 0);
                castle.setSeedProduction(productionNext, 1);
                statement = con.prepareStatement(CASTLE_MANOR_LOAD_PROCURE);
                statement.setInt(1, castle.getId());
                rs = statement.executeQuery();
                while (rs.next()) {
                    final int cropId = rs.getInt("crop_id");
                    final long canBuy = rs.getLong("can_buy");
                    final long startBuy = rs.getLong("start_buy");
                    final int rewardType = rs.getInt("reward_type");
                    final long price2 = rs.getLong("price");
                    final int period2 = rs.getInt("period");
                    if (period2 == 0) {
                        procure.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price2));
                    } else {
                        procureNext.add(new CropProcure(cropId, canBuy, rewardType, startBuy, price2));
                    }
                }
                castle.setCropProcure(procure, 0);
                castle.setCropProcure(procureNext, 1);
                if (!procure.isEmpty() || !procureNext.isEmpty() || !production.isEmpty() || !productionNext.isEmpty()) {
                    LOGGER.info("Manor System: Loaded data for " + castle.getName() + " castle");
                }
                DbUtils.close(statement, rs);
            }
        } catch (Exception e) {
            LOGGER.error("Manor System: Error restoring manor data!", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    protected void init() {
        if (ServerVariables.getString(var_name, "").isEmpty()) {
            final Calendar manorRefresh = Calendar.getInstance();
            manorRefresh.set(Calendar.HOUR_OF_DAY, MANOR_REFRESH);
            manorRefresh.set(Calendar.MINUTE, MANOR_REFRESH_MIN);
            manorRefresh.set(Calendar.SECOND, 0);
            manorRefresh.set(Calendar.MILLISECOND, 0);
            final Calendar periodApprove = Calendar.getInstance();
            periodApprove.set(Calendar.HOUR_OF_DAY, NEXT_PERIOD_APPROVE);
            periodApprove.set(Calendar.MINUTE, NEXT_PERIOD_APPROVE_MIN);
            periodApprove.set(Calendar.SECOND, 0);
            periodApprove.set(Calendar.MILLISECOND, 0);
            final boolean isApproved = periodApprove.getTimeInMillis() < Calendar.getInstance().getTimeInMillis() && manorRefresh.getTimeInMillis() > Calendar.getInstance().getTimeInMillis();
            ServerVariables.set(var_name, isApproved);
        }
        final Calendar FirstDelay = Calendar.getInstance();
        FirstDelay.set(Calendar.SECOND, 0);
        FirstDelay.set(Calendar.MILLISECOND, 0);
        FirstDelay.add(Calendar.MINUTE, 1);
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new ManorTask(), FirstDelay.getTimeInMillis() - Calendar.getInstance().getTimeInMillis(), 60000L);
    }

    public void setNextPeriod() {
        final List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        for (final Castle c : castleList) {
            if (c.getOwnerId() <= 0) {
                continue;
            }
            final Clan clan = ClanTable.getInstance().getClan(c.getOwnerId());
            if (clan == null) {
                continue;
            }
            final Warehouse cwh = clan.getWarehouse();
            for (final CropProcure crop : c.getCropProcure(0)) {
                if (crop.getStartAmount() == 0L) {
                    continue;
                }
                if (crop.getStartAmount() > crop.getAmount()) {
                    LOGGER.info("Manor System [" + c.getName() + "]: Start Amount of Crop " + crop.getStartAmount() + " > Amount of current " + crop.getAmount());
                    long count = crop.getStartAmount() - crop.getAmount();
                    count = count * 90L / 100L;
                    if (count < 1L && Rnd.get(99) < 90) {
                        count = 1L;
                    }
                    if (count >= 1L) {
                        final int id = Manor.getInstance().getMatureCrop(crop.getId());
                        cwh.addItem(id, count);
                    }
                }
                if (crop.getAmount() > 0L) {
                    c.addToTreasuryNoTax(crop.getAmount() * crop.getPrice(), false, false);
                    Log.add(c.getName() + "|" + crop.getAmount() * crop.getPrice() + "|ManorManager|" + crop.getAmount() + "*" + crop.getPrice(), "treasury");
                }
                c.setCollectedShops(0L);
                c.setCollectedSeed(0L);
            }
            c.setSeedProduction(c.getSeedProduction(1), 0);
            c.setCropProcure(c.getCropProcure(1), 0);
            final long manor_cost = c.getManorCost(0);
            if (c.getTreasury() < manor_cost) {
                c.setSeedProduction(getNewSeedsList(c.getId()), 1);
                c.setCropProcure(getNewCropsList(c.getId()), 1);
                Log.add(c.getName() + "|" + manor_cost + "|ManorManager Error@setNextPeriod", "treasury");
            } else {
                final List<SeedProduction> production = new ArrayList<>();
                final List<CropProcure> procure = new ArrayList<>();
                for (final SeedProduction s : c.getSeedProduction(0)) {
                    s.setCanProduce(s.getStartProduce());
                    production.add(s);
                }
                for (final CropProcure cr : c.getCropProcure(0)) {
                    cr.setAmount(cr.getStartAmount());
                    procure.add(cr);
                }
                c.setSeedProduction(production, 1);
                c.setCropProcure(procure, 1);
            }
            c.saveCropData();
            c.saveSeedData();
            PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_MANOR_INFORMATION_HAS_BEEN_UPDATED);
            c.setNextPeriodApproved(false);
        }
    }

    public void approveNextPeriod() {
        final List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        for (final Castle c : castleList) {
            if (c.getOwnerId() <= 0) {
                continue;
            }
            long manor_cost = c.getManorCost(1);
            if (c.getTreasury() < manor_cost) {
                c.setSeedProduction(getNewSeedsList(c.getId()), 1);
                c.setCropProcure(getNewCropsList(c.getId()), 1);
                manor_cost = c.getManorCost(1);
                if (manor_cost > 0L) {
                    Log.add(c.getName() + "|" + -manor_cost + "|ManorManager Error@approveNextPeriod", "treasury");
                }
                final Clan clan = c.getOwner();
                PlayerMessageStack.getInstance().mailto(clan.getLeaderId(), Msg.THE_AMOUNT_IS_NOT_SUFFICIENT_AND_SO_THE_MANOR_IS_NOT_IN_OPERATION);
            } else {
                c.addToTreasuryNoTax(-manor_cost, false, false);
                Log.add(c.getName() + "|" + -manor_cost + "|ManorManager", "treasury");
            }
            c.setNextPeriodApproved(true);
        }
    }

    private List<SeedProduction> getNewSeedsList(final int castleId) {
        final List<SeedProduction> seeds = new ArrayList<>();
        final List<Integer> seedsIds = Manor.getInstance().getSeedsForCastle(castleId);
        for (final int sd : seedsIds) {
            seeds.add(new SeedProduction(sd));
        }
        return seeds;
    }

    private List<CropProcure> getNewCropsList(final int castleId) {
        final List<CropProcure> crops = new ArrayList<>();
        final List<Integer> cropsIds = Manor.getInstance().getCropsForCastle(castleId);
        for (final int cr : cropsIds) {
            crops.add(new CropProcure(cr));
        }
        return crops;
    }

    public boolean isUnderMaintenance() {
        return _underMaintenance;
    }

    public void setUnderMaintenance(final boolean mode) {
        _underMaintenance = mode;
    }

    public boolean isDisabled() {
        return _disabled;
    }

    public void setDisabled(final boolean mode) {
        _disabled = mode;
    }

    public SeedProduction getNewSeedProduction(final int id, final long amount, final long price, final long sales) {
        return new SeedProduction(id, amount, price, sales);
    }

    public CropProcure getNewCropProcure(final int id, final long amount, final int type, final long price, final long buy) {
        return new CropProcure(id, amount, type, buy, price);
    }

    public void save() {
        final List<Castle> castleList = ResidenceHolder.getInstance().getResidenceList(Castle.class);
        for (final Castle c : castleList) {
            c.saveSeedData();
            c.saveCropData();
        }
    }

    private static class LazyHolder {
        private static final CastleManorManager INSTANCE = new CastleManorManager();
    }

    private class ManorTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final int H = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
            final int M = Calendar.getInstance().get(Calendar.MINUTE);
            if (ServerVariables.getBool(var_name)) {
                if (H < NEXT_PERIOD_APPROVE || H > MANOR_REFRESH || (H == MANOR_REFRESH && M >= MANOR_REFRESH_MIN)) {
                    ServerVariables.set(var_name, false);
                    setUnderMaintenance(true);
                    ManorTask.LOGGER.info("Manor System: Under maintenance mode started");
                }
            } else if (isUnderMaintenance()) {
                if (H != MANOR_REFRESH || M >= MANOR_REFRESH_MIN + MAINTENANCE_PERIOD) {
                    setUnderMaintenance(false);
                    ManorTask.LOGGER.info("Manor System: Next period started");
                    if (isDisabled()) {
                        return;
                    }
                    setNextPeriod();
                    try {
                        save();
                    } catch (Exception e) {
                        ManorTask.LOGGER.info("Manor System: Failed to save manor data: " + e);
                    }
                }
            } else if ((H > NEXT_PERIOD_APPROVE && H < MANOR_REFRESH) || (H == NEXT_PERIOD_APPROVE && M >= NEXT_PERIOD_APPROVE_MIN)) {
                ServerVariables.set(var_name, true);
                ManorTask.LOGGER.info("Manor System: Next period approved");
                if (isDisabled()) {
                    return;
                }
                approveNextPeriod();
            }
        }
    }
}
