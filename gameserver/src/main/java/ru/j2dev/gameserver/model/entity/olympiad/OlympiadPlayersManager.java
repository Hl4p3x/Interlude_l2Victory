package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.utils.Util;

import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@HideAccess
@StringEncryption
public class OlympiadPlayersManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(OlympiadPlayersManager.class);

    private Map<OlympiadGameType, ArrayList<EntryRec>> _pools = new HashMap<>();

    public static OlympiadPlayersManager getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void AllocatePools() {
        Arrays.stream(OlympiadGameType.values()).forEach(type -> _pools.put(type, new ArrayList<>()));
        LOGGER.info("OlympiadPlayersManager: Allocated " + _pools.size() + " particiant pools.");
    }

    public void FreePools() {
        if (_pools != null) {
            _pools.clear();
        }
        LOGGER.info("OlympiadPlayersManager: pools cleared.");
    }

    public boolean isEnough(final OlympiadGameType type, final int cls_id) {
        switch (type) {
            case CLASS_FREE: {
                return _pools.get(type).size() >= Config.OLY_MIN_CF_START;
            }
            case TEAM_CLASS_FREE: {
                return _pools.get(type).size() >= Config.OLY_MIN_TB_START;
            }
            case CLASS_INDIVIDUAL: {
                int cnt = (int) _pools.get(type).stream().filter(er -> er.cls_id == cls_id).count();
                return cnt >= Config.OLY_MIN_CB_START;
            }
            default: {
                return false;
            }
        }
    }

    public int getNearestIndex(final OlympiadGameType type, final int idx, final int cls_id) {
        final ArrayList<EntryRec> pool = _pools.get(type);
        final EntryRec base_rec = pool.get(idx);
        if (base_rec == null) {
            return -1;
        }
        int ndelta = Integer.MAX_VALUE;
        int nidx = Integer.MIN_VALUE;
        for (int i = 0; i < idx; ++i) {
            final EntryRec pr = pool.get(i);
            if (pr != null) {
                if (type != OlympiadGameType.CLASS_INDIVIDUAL || cls_id <= 0 || cls_id == pr.cls_id) {
                    final int delta = Math.abs(base_rec.average - pr.average);
                    if (delta < ndelta) {
                        nidx = i;
                        ndelta = delta;
                    }
                }
            }
        }
        for (int i = idx + 1; i < pool.size(); ++i) {
            final EntryRec pr = pool.get(i);
            if (pr != null) {
                if (type != OlympiadGameType.CLASS_INDIVIDUAL || cls_id <= 0 || cls_id == pr.cls_id) {
                    final int delta = Math.abs(base_rec.average - pr.average);
                    if (delta < ndelta) {
                        nidx = i;
                        ndelta = delta;
                    }
                }
            }
        }
        return nidx;
    }

    public void createEntry(final OlympiadGameType type, final Player[] players) {
        if (players == null || players.length == 0) {
            return;
        }
        final ArrayList<EntryRec> pool = _pools.get(type);
        synchronized (pool) {
            _pools.get(type).add(new EntryRec(players));
        }
    }

    public Player[][] retrieveEntrys(final OlympiadGameType type, final int cls_id) {
        cleadInvalidEntrys(type);
        final ArrayList<EntryRec> pool = _pools.get(type);
        int oldest_idx = -1;
        long oldest_time = Long.MIN_VALUE;
        int pair_idx;
        Player[][] ret;
        synchronized (pool) {
            for (int i = 0; i < pool.size(); ++i) {
                final EntryRec pr = pool.get(i);
                if (pr != null) {
                    if (type != OlympiadGameType.CLASS_INDIVIDUAL || cls_id <= 0 || cls_id == pr.cls_id) {
                        if (pr.reg_time > oldest_time) {
                            oldest_idx = i;
                            oldest_time = pr.reg_time;
                        }
                    }
                }
            }
            if (oldest_idx < 0) {
                return null;
            }
            pair_idx = getNearestIndex(type, oldest_idx, cls_id);
            if (pair_idx < 0) {
                return null;
            }
            ret = new Player[][]{Util.GetPlayersFromStoredIds(pool.remove(oldest_idx).sids), Util.GetPlayersFromStoredIds(pool.remove(pair_idx).sids)};
            pool.trimToSize();
        }
        return ret;
    }

    public boolean removeEntryByPlayer(final OlympiadGameType type, final Player player) {
        final long psid = player.getObjectId();
        final ArrayList<EntryRec> pool = _pools.get(type);
        synchronized (pool) {
            for (int i = 0; i < pool.size(); ++i) {
                final EntryRec pr = pool.get(i);
                if (pr != null) {
                    for (final long sid : pr.sids) {
                        if (sid == psid) {
                            pool.remove(i);
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public OlympiadGameType getCompTypeOf(final Player player) {
        final long psid = player.getObjectId();
        for (final Entry<OlympiadGameType, ArrayList<EntryRec>> e : _pools.entrySet()) {
            final ArrayList<EntryRec> pool = e.getValue();
            if (pool.stream().filter(Objects::nonNull).flatMapToInt(pr -> Arrays.stream(pr.sids)).anyMatch(sid -> sid == psid)) {
                return e.getKey();
            }
        }
        return null;
    }

    public boolean isRegistred(final Player player) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed() || player == null || player.isPhantom()) {
            return false;
        }
        return _pools.keySet().stream().anyMatch(type -> isRegistred(type, player));
    }

    public boolean isHWIDRegistred(final String hwid) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
            return false;
        }
        final List<EntryRec> recs = new LinkedList<>();
        _pools.forEach((key, entryRecs) -> {
            synchronized (entryRecs) {
                recs.addAll(entryRecs);
            }
        });
        return recs.
                stream().
                anyMatch(er -> Arrays.stream(er.sids).
                        mapToObj(GameObjectsStorage::getPlayer).
                        filter(player -> player != null && player.getNetConnection() != null).
                        filter(player -> player.getNetConnection().getHwid() != null).
                        anyMatch(player -> hwid.equalsIgnoreCase(player.getNetConnection().getHwid())));
    }

    public boolean isIPRegistred(final String ip) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
            return false;
        }
        final List<EntryRec> recs = new LinkedList<>();
        _pools.values().forEach(entryRecs -> {
            synchronized (entryRecs) {
                recs.addAll(entryRecs);
            }
        });
        return recs.stream().anyMatch(er -> Arrays.stream(er.sids).
                mapToObj(GameObjectsStorage::getPlayer).
                filter(player -> player != null && player.getNetConnection() != null && player.getNetConnection().getIpAddr() != null).
                filter(player -> !"?.?.?.?".equals(player.getNetConnection().getIpAddr())).
                anyMatch(player -> ip.equalsIgnoreCase(player.getNetConnection().getIpAddr())));
    }

    public boolean isRegistred(final OlympiadGameType type, final Player player) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed() || player == null || player.isPhantom()) {
            return false;
        }
        final long psid = player.getObjectId();
        final ArrayList<EntryRec> pool = _pools.get(type);
        return pool.stream().filter(Objects::nonNull).flatMapToInt(pr -> Arrays.stream(pr.sids)).anyMatch(sid -> sid == psid);
    }

    public void broadcastToEntrys(final OlympiadGameType type, final L2GameServerPacket gsp, final int cls_id) {
        final ArrayList<EntryRec> pool = _pools.get(type);
        pool.
                stream().
                filter(Objects::nonNull).
                flatMapToInt(pr -> Arrays.stream(pr.sids)).
                mapToObj(GameObjectsStorage::getPlayer).
                filter(Objects::nonNull).filter(player -> cls_id <= 0 || player.getClassId().getId() == cls_id).
                forEach(player -> player.sendPacket(gsp));
    }

    private void cleadInvalidEntrys(final OlympiadGameType type) {
        final ArrayList<EntryRec> pool = _pools.get(type);
        synchronized (pool) {
            final ArrayList<Integer> invalid_entrys = IntStream.range(0, pool.size()).filter(i -> !isValidEntry(pool.get(i))).boxed().collect(Collectors.toCollection(ArrayList::new));
            for (int invalid_entry : invalid_entrys) {
                pool.remove(invalid_entry);
            }
        }
    }

    public void onLogout(final Player player) {
        if (!OlympiadSystemManager.getInstance().isRegAllowed()) {
            return;
        }
        final OlympiadGameType ctype = getInstance().getCompTypeOf(player);
        if (ctype != null) {
            removeEntryByPlayer(ctype, player);
        }
    }

    private boolean isValidEntry(final EntryRec pr) {
        return true;
    }

    public int getParticipantCount() {
        return _pools.entrySet().stream().map(Entry::getValue).flatMap(Collection::stream).filter(Objects::nonNull).mapToInt(pr -> pr.sids.length).sum();
    }

    private static class LazyHolder {
        private static final OlympiadPlayersManager INSTANCE = new OlympiadPlayersManager();
    }

    private class EntryRec {
        final int[] sids;
        final int average;
        final long reg_time;
        final int cls_id;

        public EntryRec(final Player[] players) {
            sids = new int[players.length];
            cls_id = players[0].getClassId().getId();
            int sum = 0;
            for (int i = 0; i < players.length; ++i) {
                sids[i] = players[i].getObjectId();
                sum += Math.max(0, NoblessManager.getInstance().getPointsOf(players[i].getObjectId()));
                OlympiadSystemManager.getInstance().incPartCount();
            }
            average = sum / players.length;
            reg_time = System.currentTimeMillis();
        }
    }
}
