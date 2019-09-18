package ru.j2dev.gameserver.manager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.Couple;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class CoupleManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(CoupleManager.class);

    private List<Couple> _couples;
    private List<Couple> _deletedCouples;

    public CoupleManager() {
        LOGGER.info("Initializing CoupleManager");
        load();
        ThreadPoolManager.getInstance().scheduleAtFixedRate(new StoreTask(), 600000L, 600000L);
    }

    public static CoupleManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    private void load() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rs = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM couples ORDER BY id");
            rs = statement.executeQuery();
            while (rs.next()) {
                final Couple c = new Couple(rs.getInt("id"));
                c.setPlayer1Id(rs.getInt("player1Id"));
                c.setPlayer2Id(rs.getInt("player2Id"));
                c.setMaried(rs.getBoolean("maried"));
                c.setAffiancedDate(rs.getLong("affiancedDate"));
                c.setWeddingDate(rs.getLong("weddingDate"));
                getCouples().add(c);
            }
            LOGGER.info("Loaded: " + getCouples().size() + " couples(s)");
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rs);
        }
    }

    public final Couple getCouple(final int coupleId) {
        return getCouples().stream().filter(c -> c != null && c.getId() == coupleId).findFirst().orElse(null);
    }

    public void engage(final Player cha) {
        final int chaId = cha.getObjectId();
        getCouples().stream().filter(cl -> cl != null && (cl.getPlayer1Id() == chaId || cl.getPlayer2Id() == chaId)).forEach(cl -> {
            if (cl.getMaried()) {
                cha.setMaried(true);
            }
            cha.setCoupleId(cl.getId());
            if (cl.getPlayer1Id() == chaId) {
                cha.setPartnerId(cl.getPlayer2Id());
            } else {
                cha.setPartnerId(cl.getPlayer1Id());
            }
        });
    }

    public void notifyPartner(final Player cha) {
        if (cha.getPartnerId() != 0) {
            final Player partner = GameObjectsStorage.getPlayer(cha.getPartnerId());
            if (partner != null) {
                partner.sendMessage(new CustomMessage("l2p.gameserver.instancemanager.CoupleManager.PartnerEntered", partner));
            }
        }
    }

    public void createCouple(final Player player1, final Player player2) {
        if (player1 != null && player2 != null && player1.getPartnerId() == 0 && player2.getPartnerId() == 0) {
            getCouples().add(new Couple(player1, player2));
        }
    }

    public final List<Couple> getCouples() {
        if (_couples == null) {
            _couples = new CopyOnWriteArrayList<>();
        }
        return _couples;
    }

    public List<Couple> getDeletedCouples() {
        if (_deletedCouples == null) {
            _deletedCouples = new CopyOnWriteArrayList<>();
        }
        return _deletedCouples;
    }

    public void store() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            if (_deletedCouples != null && !_deletedCouples.isEmpty()) {
                statement = con.prepareStatement("DELETE FROM couples WHERE id = ?");
                for (final Couple c : _deletedCouples) {
                    statement.setInt(1, c.getId());
                    statement.execute();
                }
                _deletedCouples.clear();
            }
            if (_couples != null && !_couples.isEmpty()) {
                for (final Couple c : _couples) {
                    if (c != null && c.isChanged()) {
                        c.store(con);
                        c.setChanged(false);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private static class LazyHolder {
        private static final CoupleManager INSTANCE = new CoupleManager();
    }

    private class StoreTask extends RunnableImpl {
        @Override
        public void runImpl() {
            store();
        }
    }
}
