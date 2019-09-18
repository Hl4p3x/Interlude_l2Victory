package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import gnu.trove.set.TIntSet;
import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExSendUIEvent;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Revive;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

@HideAccess
@StringEncryption
public class PvpEventTeam {
    private final TeamType _teamType;
    private final String _teamName;
    private final AtomicInteger _teamPoints;
    private final TIntSet _players = new TIntHashSet();
    private final Map<Integer, PvpEventPlayerInfo> _playerInfos = new ConcurrentHashMap<>();
    private TIntSet _live_players = new TIntHashSet();

    public PvpEventTeam(final TeamType teamType, final String teamName) {
        _teamType = teamType;
        _teamName = teamName;
        _teamPoints = new AtomicInteger(0);
    }

    /**
     * Показывает игроку на экране таймер, отсчитывающий время до конца эвента/раунда
     *
     * @param player        - игрок
     * @param remainingTime - оставшееся время в секундах
     * @param zone          - зона, в которой должен находиться игрок (или null, если это неважно)
     */
    public static void showTimer(Player player, int remainingTime, Zone zone) {
        if (player == null) {
            return;
        }

        if (zone == null || player.isInZone(zone)) {
            player.sendPacket(new ExSendUIEvent(player, false, false, remainingTime, 10, ""));
        }
    }

    /**
     * Показывает игрокам на экране таймер, отсчитывающий время до конца эвента/раунда
     *
     * @param players       - игроки
     * @param remainingTime - оставшееся время в секундах
     * @param zone          - зона, в которой должны находиться игроки (или null, если это неважно)
     */
    public static void showTimer(List<Player> players, int remainingTime, Zone zone) {
        if (players == null || players.isEmpty()) {
            return;
        }

        players.forEach(player -> showTimer(player, remainingTime, zone));
    }

    /**
     * Прячет отображение таймера на экране
     *
     * @param player - игрок
     */
    public static void hideTimer(Player player) {
        if (player == null) {
            return;
        }

        player.sendPacket(new ExSendUIEvent(player, true, false, 0, 0, ""));
    }

    /**
     * Прячет отображение таймера на экране
     *
     * @param players - игроки
     */
    public static void hideTimer(List<Player> players) {
        if (players == null || players.isEmpty()) {
            return;
        }

        for (Player player : players) {
            hideTimer(player);
        }
    }

    public final TeamType getTeamType() {
        return _teamType;
    }

    public final String getTeamName() {
        return _teamName;
    }

    public final int increaseTeamPoints() {
        return _teamPoints.incrementAndGet();
    }

    public final int decreaseTeamPoints() {
        return _teamPoints.decrementAndGet();
    }

    public final int getTeamPoints() {
        return _teamPoints.get();
    }

    public final void addPlayer(final Player player) {
        final int objId = player.getObjectId();
        _players.add(objId);
        _live_players.add(objId);
        _playerInfos.put(objId, new PvpEventPlayerInfo(objId));
    }

    public final void removePlayer(final Player player) {
        final int objId = player.getObjectId();
        _players.remove(objId);
        _live_players.remove(objId);
        _playerInfos.remove(objId);
        player.setTeam(TeamType.NONE);
        unParalizePlayer(player);
    }

    public final void removeFromLive(final Player player) {
        _live_players.remove(player.getObjectId());
    }

    public final void removeAura() {
        getLivePlayers().stream().filter(Objects::nonNull).forEach(player -> player.setTeam(TeamType.NONE));
    }

    public final boolean isInTeam(final int playerStoreId) {
        return _players.contains(playerStoreId);
    }

    public final boolean isInTeam(final Player player) {
        return isInTeam(player.getObjectId());
    }

    public final boolean isLive(final int playerStoreId) {
        return _live_players.contains(playerStoreId);
    }

    public final boolean isLive(final Player player) {
        return isLive(player.getObjectId());
    }

    public final int getPlayersCount() {
        return _players.size();
    }

    public final boolean isEmpty() {
        return _players.isEmpty();
    }

    public final int getLivePlayersCount() {
        return _live_players.size();
    }

    public final Collection<PvpEventPlayerInfo> getPlayerInfos() {
        return _playerInfos.values();
    }

    public final PvpEventPlayerInfo getPlayerInfo(final int playerStoredId) {
        return _playerInfos.get(playerStoredId);
    }

    public void clear() {
        _players.clear();
        _live_players.clear();
        _playerInfos.clear();
    }

    public Player getTopPlayer() {
        final Collection<PvpEventPlayerInfo> players = getPlayerInfos();
        if (players.size() == 0) {
            return null;
        }
        return players.stream().filter(pvPEventPlayerInfo -> pvPEventPlayerInfo.getKillsCount() > 0).max(PvpEventUtils.SCORE_COMPARATOR).get().getPlayer();
    }

    public List<Player> getAllPlayers() {
        final List<Player> result = new CopyOnWriteArrayList<>();
        for (final int storeId : _players.toArray()) {
            final Player player = GameObjectsStorage.getPlayer(storeId);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public List<Player> getLivePlayers() {
        final List<Player> result = new CopyOnWriteArrayList<>();
        for (final int storeId : _live_players.toArray()) {
            final Player player = GameObjectsStorage.getPlayer(storeId);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public synchronized void updateLivePlayers() {
        final TIntSet new_live_list = new TIntHashSet();
        for (final int storeId : _live_players.toArray()) {
            final Player player = GameObjectsStorage.getPlayer(storeId);
            if (player != null) {
                new_live_list.add(storeId);
            }
        }

        _live_players = new_live_list;
    }

    public void paralyzePlayers() {
        getAllPlayers().forEach(this::paralizePlayer);
    }

    public void upParalyzePlayers() {
        getAllPlayers().forEach(this::unParalizePlayer);
    }

    private void paralizePlayer(final Player player) {
        if (!player.isFrozen()) {
            player.startFrozen();
            player.startDamageBlocked();
        }
        if (player.getPet() != null && !player.getPet().isFrozen()) {
            player.getPet().startFrozen();
            player.startDamageBlocked();
        }
    }

    private void unParalizePlayer(final Player player) {
        if (player.isFrozen()) {
            player.stopFrozen();
            player.stopDamageBlocked();
        }
        if (player.getPet() != null && player.getPet().isFrozen()) {
            player.getPet().stopFrozen();
            player.stopDamageBlocked();
        }
    }

    public void ressurectPlayers() {
        getAllPlayers().stream().filter(Player::isDead).forEach(player -> {
            player.restoreExp();
            healPlayer(player);
            player.broadcastPacket(new Revive(player));
        });
    }

    public void healPlayers() {
        getAllPlayers().forEach(this::healPlayer);
    }

    private void healPlayer(final Player player) {
        player.setCurrentCp(player.getMaxCp());
        player.setCurrentHp(player.getMaxHp(), true);
        player.setCurrentMp(player.getMaxMp());
    }

    public void sendPacketToLive(final IStaticPacket packet) {
        getLivePlayers().stream().filter(player -> player != null && player.isOnline()).forEach(player -> player.sendPacket(packet));
    }

    public void sendPacketToAll(final IStaticPacket packet) {
        getAllPlayers().stream().filter(player -> player != null && player.isOnline()).forEach(player -> player.sendPacket(packet));
    }

    /**
     * Показывает всем участвующим в эвенте игрокам на экране таймер, отсчитывающий время до конца эвента/раунда
     *
     * @param remainingTime - оставшееся время в секундах
     * @param zone          - зона, в которой должны находиться игроки (или null, если это неважно)
     */
    public void showTimer(int remainingTime, Zone zone) {
        showTimer(getAllPlayers(), remainingTime, zone);
    }

    /**
     * Прячет отображение таймера на экране у всех участвующих в эвенте игроков
     */
    public void hideTimer() {
        hideTimer(getAllPlayers());
    }
}

