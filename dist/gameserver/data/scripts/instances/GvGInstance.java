package instances;

import gnu.trove.map.hash.TIntObjectHashMap;
import org.apache.commons.lang3.mutable.MutableInt;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.actor.player.OnPlayerPartyLeaveListener;
import ru.j2dev.gameserver.listener.actor.player.OnTeleportListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Party;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Revive;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledFuture;

import static events.GvG.GvG.*;

public class GvGInstance extends Reflection {
    private static final int BOX_ID = 25656;
    private static final int BOSS_ID = 25655;
    private static final int SCORE_BOX = 20;
    private static final int SCORE_BOSS = 100;
    private static final int SCORE_KILL = 5;
    private static final int SCORE_DEATH = 3;
    private final int eventTime = 1200;
    private final long bossSpawnTime = 600000L;
    private final List<HardReference<Player>> bothTeams = new CopyOnWriteArrayList<>();
    private final TIntObjectHashMap<MutableInt> score = new TIntObjectHashMap<>();
    private final DeathListener _deathListener = new DeathListener();
    private final TeleportListener _teleportListener = new TeleportListener();
    private final PlayerPartyLeaveListener _playerPartyLeaveListener = new PlayerPartyLeaveListener();
    private boolean active;
    private Party team1;
    private Party team2;
    private int team1Score;
    private int team2Score;
    private long startTime;
    private ScheduledFuture<?> _bossSpawnTask;
    private ScheduledFuture<?> _countDownTask;
    private ScheduledFuture<?> _battleEndTask;
    private Zone zonepvp;
    private Zone peace1;
    private Zone peace2;

    public void setTeam1(final Party party1) {
        team1 = party1;
    }

    public void setTeam2(final Party party2) {
        team2 = party2;
    }

    public void start() {
        zonepvp = getZone("[gvg_battle_zone]");
        peace1 = getZone("[gvg_1_peace]");
        peace2 = getZone("[gvg_2_peace]");
        final Location[] boxes = {new Location(78568, 92408, -2440, 0), new Location(78664, 92440, -2440, 0), new Location(78760, 92488, -2440, 0), new Location(78872, 92264, -2440, 0), new Location(78696, 92168, -2440, 0), new Location(79624, 89736, -2440, 0), new Location(79576, 89830, -2440, 0), new Location(79534, 89917, -2440, 0), new Location(79743, 90028, -2440, 0)};
        for (Location boxe : boxes) {
            addSpawnWithoutRespawn(BOX_ID, boxe, 0);
        }
        addSpawnWithoutRespawn(35423, new Location(79928, 88328, -2880), 0);
        addSpawnWithoutRespawn(35426, new Location(77016, 93080, -2880), 0);
        _bossSpawnTask = ThreadPoolManager.getInstance().schedule(new BossSpawn(), bossSpawnTime);
        _countDownTask = ThreadPoolManager.getInstance().schedule(new CountingDown(), (eventTime - 1) * 1000L);
        _battleEndTask = ThreadPoolManager.getInstance().schedule(new BattleEnd(), (eventTime - 6) * 1000L);
        addListeners(team1);
        addListeners(team2);
        startTime = System.currentTimeMillis() + eventTime * 1000L;
        HardReferences.unwrap(bothTeams).forEach(tm -> {
            score.put(tm.getObjectId(), new MutableInt());
            tm.setCurrentCp((double) tm.getMaxCp());
            tm.setCurrentHp((double) tm.getMaxHp(), false);
            tm.setCurrentMp((double) tm.getMaxMp());
            tm.sendActionFailed();
        });
        active = true;
    }

    protected void addListeners(Party team1) {
        team1.getPartyMembers().forEach(member -> {
            bothTeams.add(member.getRef());
            member.addListener(_deathListener);
            member.addListener(_teleportListener);
            member.addListener(_playerPartyLeaveListener);
        });
    }

    private void broadCastPacketToBothTeams(final L2GameServerPacket packet) {
        HardReferences.unwrap(bothTeams).forEach(tm -> tm.sendPacket(packet));
    }

    private boolean isActive() {
        return active;
    }

    private boolean isRedTeam(final Player player) {
        return team2.containsMember(player);
    }

    private void end() {
        active = false;
        startCollapseTimer(60000L);
        paralyzePlayers();
        ThreadPoolManager.getInstance().schedule(new Finish(), 55000L);
        if (_bossSpawnTask != null) {
            _bossSpawnTask.cancel(false);
            _bossSpawnTask = null;
        }
        if (_countDownTask != null) {
            _countDownTask.cancel(false);
            _countDownTask = null;
        }
        if (_battleEndTask != null) {
            _battleEndTask.cancel(false);
            _battleEndTask = null;
        }
        boolean isRedWinner;
        isRedWinner = (getRedScore() >= getBlueScore());
        reward(isRedWinner ? team2 : team1);
        updateWinner(isRedWinner ? team2.getPartyLeader() : team1.getPartyLeader());
        zonepvp.setActive(false);
        peace1.setActive(false);
        peace2.setActive(false);
    }

    private void reward(final Party party) {
        party.getPartyMembers().forEach(member -> {
            final String msg = new CustomMessage("scripts.event.gvg.youpartywin", member, new Object[0]).toString();
            member.sendMessage(msg);
            Functions.addItem(member, Config.GVG_REWARD_ID, Config.GVG_REWARD_AMOUNT);
        });
    }

    private synchronized void changeScore(final int teamId, final int toAdd, final int toSub, final boolean subbing, final boolean affectAnotherTeam, final Player player) {
        final int timeLeft = (int) ((startTime - System.currentTimeMillis()) / 1000L);
        if (teamId == 1) {
            if (subbing) {
                team1Score -= toSub;
                if (team1Score < 0) {
                    team1Score = 0;
                }
                if (affectAnotherTeam) {
                    team2Score += toAdd;
                }
            } else {
                team1Score += toAdd;
                if (affectAnotherTeam) {
                    team2Score -= toSub;
                    if (team2Score < 0) {
                        team2Score = 0;
                    }
                }
            }
        } else if (teamId == 2) {
            if (subbing) {
                team2Score -= toSub;
                if (team2Score < 0) {
                    team2Score = 0;
                }
                if (affectAnotherTeam) {
                    team1Score += toAdd;
                }
            } else {
                team2Score += toAdd;
                if (affectAnotherTeam) {
                    team1Score -= toSub;
                    if (team1Score < 0) {
                        team1Score = 0;
                    }
                }
            }
        }
    }

    private void addPlayerScore(final Player player) {
        final MutableInt points = score.get(player.getObjectId());
        points.increment();
    }

    public int getPlayerScore(final Player player) {
        final MutableInt points = score.get(player.getObjectId());
        return points.intValue();
    }

    public void paralyzePlayers() {
        HardReferences.unwrap(bothTeams).forEach(tm -> {
            if (tm.isDead()) {
                tm.setCurrentHp((double) tm.getMaxHp(), true);
                tm.broadcastPacket(new Revive(tm));
            } else {
                tm.setCurrentHp((double) tm.getMaxHp(), false);
            }
            tm.setCurrentMp((double) tm.getMaxMp());
            tm.setCurrentCp((double) tm.getMaxCp());
            tm.getEffectList().stopEffect(1411);
            tm.block();
        });
    }

    public void unParalyzePlayers() {
        HardReferences.unwrap(bothTeams).forEach(tm -> {
            tm.unblock();
            removePlayer(tm, true);
        });
    }

    private void cleanUp() {
        team1 = null;
        team2 = null;
        bothTeams.clear();
        team1Score = 0;
        team2Score = 0;
        score.clear();
    }

    public void resurrectAtBase(final Player player) {
        if (player.isDead()) {
            player.setCurrentHp(0.7 * player.getMaxHp(), true);
            player.broadcastPacket(new Revive(player));
        }
        Location pos;
        if (team1.containsMember(player)) {
            pos = Location.findPointToStay(TEAM1_LOC, 0, 150, getGeoIndex());
        } else {
            pos = Location.findPointToStay(TEAM2_LOC, 0, 150, getGeoIndex());
        }
        player.teleToLocation(pos, this);
    }

    private void removePlayer(final Player player, final boolean legalQuit) {
        bothTeams.remove(player.getRef());
        player.removeListener(_deathListener);
        player.removeListener(_teleportListener);
        player.removeListener(_playerPartyLeaveListener);
        player.leaveParty();
        if (!legalQuit) {
            player.teleToLocation(Location.findPointToStay(RETURN_LOC, 0, 150, ReflectionManager.DEFAULT.getGeoIndex()), 0);
        }
    }

    private void teamWithdraw(final Party party) {
        if (party == team1) {
            team1.getPartyMembers().forEach(player -> removePlayer(player, false));
            final Player player2 = team2.getPartyLeader();
            changeScore(2, 200, 0, false, false, player2);
        } else {
            team2.getPartyMembers().forEach(player -> removePlayer(player, false));
            final Player player2 = team1.getPartyLeader();
            changeScore(1, 200, 0, false, false, player2);
        }
        HardReferences.unwrap(bothTeams).forEach(tm -> tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.partydispell", tm, new Object[0]).toString(), 4000, ScreenMessageAlign.MIDDLE_CENTER, true)));
    }

    private int getBlueScore() {
        return team1Score;
    }

    private int getRedScore() {
        return team2Score;
    }

    @Override
    public NpcInstance addSpawnWithoutRespawn(final int npcId, final Location loc, final int randomOffset) {
        final NpcInstance npc = super.addSpawnWithoutRespawn(npcId, loc, randomOffset);
        npc.addListener((Listener) _deathListener);
        return npc;
    }

    private class DeathListener implements OnDeathListener {
        @Override
        public void onDeath(final Creature self, final Creature killer) {
            if (!isActive()) {
                return;
            }
            if (self.getReflection() != killer.getReflection() || self.getReflection() != GvGInstance.this) {
                return;
            }
            if (self.isPlayer() && killer.isPlayable()) {
                if (team1.containsMember(self.getPlayer()) && team2.containsMember(killer.getPlayer())) {
                    addPlayerScore(killer.getPlayer());
                    changeScore(1, SCORE_KILL, SCORE_DEATH, true, true, killer.getPlayer());
                } else if (team2.containsMember(self.getPlayer()) && team1.containsMember(killer.getPlayer())) {
                    addPlayerScore(killer.getPlayer());
                    changeScore(2, SCORE_KILL, SCORE_DEATH, true, true, killer.getPlayer());
                }
                resurrectAtBase(self.getPlayer());
            } else if (self.isPlayer() && !killer.isPlayable()) {
                resurrectAtBase(self.getPlayer());
            } else if (self.isNpc() && killer.isPlayable()) {
                if (self.getNpcId() == BOX_ID) {
                    if (team1.containsMember(killer.getPlayer())) {
                        changeScore(1, SCORE_BOX, 0, false, false, killer.getPlayer());
                    } else if (team2.containsMember(killer.getPlayer())) {
                        changeScore(2, SCORE_BOX, 0, false, false, killer.getPlayer());
                    }
                } else if (self.getNpcId() == BOSS_ID) {
                    if (team1.containsMember(killer.getPlayer())) {
                        changeScore(1, SCORE_BOSS, 0, false, false, killer.getPlayer());
                    } else if (team2.containsMember(killer.getPlayer())) {
                        changeScore(2, SCORE_BOSS, 0, false, false, killer.getPlayer());
                    }
                    broadCastPacketToBothTeams(new ExShowScreenMessage("Treasure guard Gerald died at the hand " + killer.getName(), 5000, ScreenMessageAlign.MIDDLE_CENTER, true));
                    end();
                }
            }
        }
    }

    public class BossSpawn extends RunnableImpl {
        @Override
        public void runImpl() {
            HardReferences.unwrap(bothTeams).forEach(tm -> tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.geraldguard", tm, new Object[0]).toString(), 5000, ScreenMessageAlign.MIDDLE_CENTER, true)));
            addSpawnWithoutRespawn(25655, new Location(79128, 91000, -2880, 4836), 0);
            openDoor(22200004);
            openDoor(22200005);
            openDoor(22200006);
            openDoor(22200007);
        }
    }

    public class CountingDown extends RunnableImpl {
        @Override
        public void runImpl() {
            HardReferences.unwrap(bothTeams).forEach(tm -> tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.1mintofinish", tm, new Object[0]).toString(), 4000, ScreenMessageAlign.MIDDLE_CENTER, true)));
        }
    }

    public class BattleEnd extends RunnableImpl {
        @Override
        public void runImpl() {
            HardReferences.unwrap(bothTeams).forEach(tm -> tm.sendPacket(new ExShowScreenMessage(new CustomMessage("scripts.event.gvg.teleport1min", tm, new Object[0]).toString(), 4000, ScreenMessageAlign.MIDDLE_CENTER, true)));
            end();
        }
    }

    public class Finish extends RunnableImpl {
        @Override
        public void runImpl() {
            unParalyzePlayers();
            cleanUp();
        }
    }

    private class TeleportListener implements OnTeleportListener {
        @Override
        public void onTeleport(final Player player, final int x, final int y, final int z, final Reflection reflection) {
            if (zonepvp.checkIfInZone(x, y, z, reflection) || peace1.checkIfInZone(x, y, z, reflection) || peace2.checkIfInZone(x, y, z, reflection)) {
                return;
            }
            removePlayer(player, false);
            player.sendMessage(new CustomMessage("scripts.event.gvg.expelled", player));
        }
    }

    private class PlayerPartyLeaveListener implements OnPlayerPartyLeaveListener {
        @Override
        public void onPartyLeave(final Player player) {
            if (!isActive()) {
                return;
            }
            final Party party = player.getParty();
            if (party.getMemberCount() >= 3) {
                removePlayer(player, false);
                return;
            }
            teamWithdraw(party);
        }
    }
}
