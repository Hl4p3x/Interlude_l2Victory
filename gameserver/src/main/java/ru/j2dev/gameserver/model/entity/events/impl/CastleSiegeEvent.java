package ru.j2dev.gameserver.model.entity.events.impl;

import org.apache.commons.lang3.tuple.Pair;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.time.cron.NextTime;
import ru.j2dev.commons.time.cron.SchedulingPattern;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.dao.CastleDamageZoneDAO;
import ru.j2dev.gameserver.dao.CastleDoorUpgradeDAO;
import ru.j2dev.gameserver.dao.CastleHiredGuardDAO;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.RestartType;
import ru.j2dev.gameserver.model.entity.events.objects.*;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.instances.residences.SiegeToggleNpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.pledge.UnitMember;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.taskmanager.DelayedItemsManager;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.item.support.MerchantGuard;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;
import ru.j2dev.gameserver.utils.TeleportUtils;

import java.util.*;
import java.util.concurrent.Future;

public class CastleSiegeEvent extends SiegeEvent<Castle, SiegeClanObject> {
    public static final int MAX_SIEGE_CLANS = 20;
    public static final long DAY_IN_MILISECONDS = 86400000L;
    public static final String DEFENDERS_WAITING = "defenders_waiting";
    public static final String DEFENDERS_REFUSED = "defenders_refused";
    public static final String CONTROL_TOWERS = "control_towers";
    public static final String FLAME_TOWERS = "flame_towers";
    public static final String BOUGHT_ZONES = "bought_zones";
    public static final String GUARDS = "guards";
    public static final String HIRED_GUARDS = "hired_guards";
    private final long _nextSiegeDateSetDelay;
    private Set<Integer> _nextSiegeTimes;
    private Future<?> _nextSiegeDateSetTask;
    private boolean _firstStep;
    private NextTime[] _nextSiegeTimesPatterns;
    private Pair<ItemTemplate, Long> _onSiegeEndAttackerOwnedLeaderReward;
    private Pair<ItemTemplate, Long> _onSiegeEndDefenderOwnedLeaderReward;

    public CastleSiegeEvent(final MultiValueSet<String> set) {
        super(set);
        _nextSiegeTimes = Collections.emptySet();
        _nextSiegeDateSetTask = null;
        _firstStep = false;
        _nextSiegeTimesPatterns = new NextTime[0];
        _onSiegeEndAttackerOwnedLeaderReward = null;
        _onSiegeEndDefenderOwnedLeaderReward = null;
        final String nextSiegeTimesPattern = set.getString("siege_schedule");
        final StringTokenizer st = new StringTokenizer(nextSiegeTimesPattern, "|;");
        while (st.hasMoreTokens()) {
            _nextSiegeTimesPatterns = ArrayUtils.add(_nextSiegeTimesPatterns, new SchedulingPattern(st.nextToken()));
        }
        final String onSiegeEndAttackerOwnedLeaderReward = set.getString("on_siege_end_attacker_owned_leader_reward", null);
        if (onSiegeEndAttackerOwnedLeaderReward != null) {
            final String[] onSiegEndAttackerOwnedLeaderRewardParts = onSiegeEndAttackerOwnedLeaderReward.split(":");
            _onSiegeEndAttackerOwnedLeaderReward = Pair.of(ItemTemplateHolder.getInstance().getTemplate(Integer.parseInt(onSiegEndAttackerOwnedLeaderRewardParts[0])), Long.valueOf(onSiegEndAttackerOwnedLeaderRewardParts[1]));
        }
        final String onSiegeEndDefenderOwnedLeaderReward = set.getString("on_siege_end_defender_owned_leader_reward", null);
        if (onSiegeEndDefenderOwnedLeaderReward != null) {
            final String[] onSiegeEndDefenderOwnedLeaderRewardParts = onSiegeEndDefenderOwnedLeaderReward.split(":");
            _onSiegeEndDefenderOwnedLeaderReward = Pair.of(ItemTemplateHolder.getInstance().getTemplate(Integer.parseInt(onSiegeEndDefenderOwnedLeaderRewardParts[0])), Long.valueOf(onSiegeEndDefenderOwnedLeaderRewardParts[1]));
        }
        _nextSiegeDateSetDelay = set.getLong("next_siege_date_set_delay", 86400L) * 1000L;
    }

    @Override
    public void initEvent() {
        super.initEvent();
        final List<DoorObject> doorObjects = getObjects("doors");
        addObjects("bought_zones", CastleDamageZoneDAO.getInstance().load(getResidence()));
        doorObjects.forEach(doorObject -> {
            doorObject.setUpgradeValue(this, CastleDoorUpgradeDAO.getInstance().load(doorObject.getUId()));
            doorObject.getDoor().addListener(_doorDeathListener);
        });
    }

    @Override
    public void processStep(final Clan newOwnerClan) {
        final Clan oldOwnerClan = getResidence().getOwner();
        getResidence().changeOwner(newOwnerClan);
        if (oldOwnerClan != null) {
            final SiegeClanObject ownerSiegeClan = getSiegeClan("defenders", oldOwnerClan);
            removeObject("defenders", ownerSiegeClan);
            ownerSiegeClan.setType("attackers");
            addObject("attackers", ownerSiegeClan);
        } else {
            if (getObjects("attackers").size() == 1) {
                stopEvent();
                return;
            }
            final int allianceObjectId = newOwnerClan.getAllyId();
            if (allianceObjectId > 0) {
                final List<SiegeClanObject> attackers = getObjects("attackers");
                boolean sameAlliance = true;
                for (final SiegeClanObject sc : attackers) {
                    if (sc != null && sc.getClan().getAllyId() != allianceObjectId) {
                        sameAlliance = false;
                    }
                }
                if (sameAlliance) {
                    stopEvent();
                    return;
                }
            }
        }
        final SiegeClanObject newOwnerSiegeClan = getSiegeClan("attackers", newOwnerClan);
        newOwnerSiegeClan.deleteFlag();
        newOwnerSiegeClan.setType("defenders");
        removeObject("attackers", newOwnerSiegeClan);
        final List<SiegeClanObject> defenders = removeObjects("defenders");
        for (final SiegeClanObject siegeClan : defenders) {
            siegeClan.setType("attackers");
        }
        addObject("defenders", newOwnerSiegeClan);
        addObjects("attackers", defenders);
        updateParticles(true, "attackers", "defenders");
        teleportPlayers("attackers");
        teleportPlayers("spectators");
        if (!_firstStep) {
            _firstStep = true;
            broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_HAS_BEEN_DISSOLVED, "attackers", "defenders");
            if (_oldOwner != null) {
                spawnAction("hired_guards", false);
                damageZoneAction(false);
                removeObjects("hired_guards");
                removeObjects("bought_zones");
                CastleDamageZoneDAO.getInstance().delete(getResidence());
            } else {
                spawnAction("guards", false);
            }
            final List<DoorObject> doorObjects = getObjects("doors");
            for (final DoorObject doorObject : doorObjects) {
                doorObject.setWeak(true);
                doorObject.setUpgradeValue(this, 0);
                CastleDoorUpgradeDAO.getInstance().delete(doorObject.getUId());
            }
        }
        spawnAction("doors", true);
        spawnAction("control_towers", true);
        spawnAction("flame_towers", true);
        despawnSiegeSummons();
    }

    @Override
    public void startEvent() {
        _oldOwner = getResidence().getOwner();
        if (_oldOwner != null) {
            addObject("defenders", new SiegeClanObject("defenders", _oldOwner, 0L));
            if (getResidence().getSpawnMerchantTickets().size() > 0) {
                for (final ItemInstance item : getResidence().getSpawnMerchantTickets()) {
                    final MerchantGuard guard = getResidence().getMerchantGuard(item.getItemId());
                    addObject("hired_guards", new SpawnSimpleObject(guard.getNpcId(), item.getLoc()));
                    item.deleteMe();
                }
                CastleHiredGuardDAO.getInstance().delete(getResidence());
                spawnAction("hired_guards", true);
            }
        }
        final List<SiegeClanObject> attackers = getObjects("attackers");
        if (attackers.isEmpty()) {
            if (_oldOwner == null) {
                broadcastToWorld((new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_BEEN_CANCELED_DUE_TO_LACK_OF_INTEREST)).addResidenceName(getResidence()));
            } else {
                broadcastToWorld((new SystemMessage2(SystemMsg.S1S_SIEGE_WAS_CANCELED_BECAUSE_THERE_WERE_NO_CLANS_THAT_PARTICIPATED)).addResidenceName(getResidence()));
            }
            reCalcNextTime(false);
            return;
        }
        updateParticles(true, "attackers", "defenders");
        broadcastTo(SystemMsg.THE_TEMPORARY_ALLIANCE_OF_THE_CASTLE_ATTACKER_TEAM_IS_IN_EFFECT, "attackers");
        super.startEvent();
        if (_oldOwner == null) {
            initControlTowers();
        } else {
            damageZoneAction(true);
        }
    }

    @Override
    public void stopEvent(final boolean step) {
        final List<DoorObject> doorObjects = getObjects("doors");
        for (final DoorObject doorObject : doorObjects) {
            doorObject.setWeak(false);
        }
        damageZoneAction(false);
        updateParticles(false, "attackers", "defenders");
        final List<SiegeClanObject> attackers = removeObjects("attackers");
        for (final SiegeClanObject siegeClan : attackers) {
            siegeClan.deleteFlag();
        }
        broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_IS_FINISHED).addResidenceName((getResidence())));
        removeObjects("defenders");
        removeObjects("defenders_waiting");
        removeObjects("defenders_refused");
        final Clan ownerClan = getResidence().getOwner();
        if (ownerClan != null) {
            if (_oldOwner == ownerClan) {
                ownerClan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1000, false, toString())));
                final UnitMember leader = ownerClan.getLeader();
                if (leader != null && _onSiegeEndDefenderOwnedLeaderReward != null) {
                    final int rewardItemId = _onSiegeEndDefenderOwnedLeaderReward.getLeft().getItemId();
                    final long rewardItemCount = _onSiegeEndDefenderOwnedLeaderReward.getRight();
                    final Player leaderPlayer = World.getPlayer(leader.getObjectId());
                    if (leaderPlayer != null && leaderPlayer.isOnline()) {
                        ItemFunctions.addItem(leaderPlayer, rewardItemId, rewardItemCount, true);
                        Log.LogItem(leaderPlayer, ItemLog.PostSend, rewardItemId, rewardItemCount);
                    } else {
                        DelayedItemsManager.getInstance().addDelayed(leader.getObjectId(), rewardItemId, (int) rewardItemCount, 0, "End siege owner leader reward item " + rewardItemId + "(" + rewardItemCount + ")");
                    }
                }
            } else {
                broadcastToWorld(new SystemMessage2(SystemMsg.CLAN_S1_IS_VICTORIOUS_OVER_S2S_CASTLE_SIEGE).addString(ownerClan.getName()).addResidenceName(getResidence()));
                ownerClan.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.SINCE_YOUR_CLAN_EMERGED_VICTORIOUS_FROM_THE_SIEGE_S1_POINTS_HAVE_BEEN_ADDED_TO_YOUR_CLANS_REPUTATION_SCORE).addInteger(ownerClan.incReputation(1500, false, toString())));
                if (_oldOwner != null) {
                    _oldOwner.broadcastToOnlineMembers(new SystemMessage2(SystemMsg.YOUR_CLAN_HAS_FAILED_TO_DEFEND_THE_CASTLE_S1_POINTS_HAVE_BEEN_DEDUCTED_FROM_YOU_CLAN_REPUTATION_SCORE_AND_ADDED_TO_YOUR_OPPONENTS).addInteger(-_oldOwner.incReputation(-1500, false, toString())));
                }
                final UnitMember leader = ownerClan.getLeader();
                for (final UnitMember member : ownerClan) {
                    final Player player = member.getPlayer();
                    if (player != null) {
                        player.sendPacket(PlaySound.SIEGE_VICTORY);
                        if (!player.isOnline() || !player.isNoble()) {
                            continue;
                        }
                        HeroManager.getInstance().addHeroDiary(player.getObjectId(), 3, getResidence().getId());
                    }
                }
                if (_onSiegeEndAttackerOwnedLeaderReward != null) {
                    final int rewardItemId = _onSiegeEndAttackerOwnedLeaderReward.getLeft().getItemId();
                    final long rewardItemCount = _onSiegeEndAttackerOwnedLeaderReward.getRight();
                    final Player leaderPlayer = World.getPlayer(leader.getObjectId());
                    if (leaderPlayer != null && leaderPlayer.isOnline()) {
                        ItemFunctions.addItem(leaderPlayer, rewardItemId, rewardItemCount, true);
                        Log.LogItem(leaderPlayer, ItemLog.PostSend, rewardItemId, rewardItemCount);
                    } else {
                        DelayedItemsManager.getInstance().addDelayed(leader.getObjectId(), rewardItemId, (int) rewardItemCount, 0, "End siege owner leader reward item " + rewardItemId + "(" + rewardItemCount + ")");
                    }
                }
            }
            getResidence().getOwnDate().setTimeInMillis(System.currentTimeMillis());
            getResidence().getLastSiegeDate().setTimeInMillis(getResidence().getSiegeDate().getTimeInMillis());
        } else {
            broadcastToWorld(new SystemMessage2(SystemMsg.THE_SIEGE_OF_S1_HAS_ENDED_IN_A_DRAW).addResidenceName(getResidence()));
            getResidence().getOwnDate().setTimeInMillis(0L);
            getResidence().getLastSiegeDate().setTimeInMillis(0L);
        }
        SiegeClanDAO.getInstance().delete(getResidence());
        despawnSiegeSummons();
        if (_oldOwner != null) {
            spawnAction("hired_guards", false);
            removeObjects("hired_guards");
        }
        super.stopEvent(step);
    }

    @Override
    public void reCalcNextTime(final boolean onInit) {
        clearActions();
        final long currentTimeMillis = System.currentTimeMillis();
        final Calendar startSiegeDate = getResidence().getSiegeDate();
        final Calendar ownSiegeDate = getResidence().getOwnDate();
        if (onInit) {
            if (startSiegeDate.getTimeInMillis() > currentTimeMillis) {
                registerActions();
            } else if (startSiegeDate.getTimeInMillis() == 0L) {
                if (currentTimeMillis - ownSiegeDate.getTimeInMillis() > _nextSiegeDateSetDelay) {
                    setNextSiegeTime();
                } else {
                    generateNextSiegeDates();
                }
            } else if (startSiegeDate.getTimeInMillis() <= currentTimeMillis) {
                setNextSiegeTime();
            }
        } else if (getResidence().getOwner() != null) {
            getResidence().getSiegeDate().setTimeInMillis(0L);
            getResidence().setJdbcState(JdbcEntityState.UPDATED);
            getResidence().update();
            generateNextSiegeDates();
        } else {
            setNextSiegeTime();
        }
    }

    @Override
    public void loadSiegeClans() {
        super.loadSiegeClans();
        addObjects("defenders_waiting", SiegeClanDAO.getInstance().load(getResidence(), "defenders_waiting"));
        addObjects("defenders_refused", SiegeClanDAO.getInstance().load(getResidence(), "defenders_refused"));
    }

    @Override
    public void setRegistrationOver(final boolean b) {
        if (b) {
            broadcastToWorld(new SystemMessage2(SystemMsg.THE_DEADLINE_TO_REGISTER_FOR_THE_SIEGE_OF_S1_HAS_PASSED).addResidenceName(getResidence()));
        }
        super.setRegistrationOver(b);
    }

    @Override
    public void announce(final int val) {
        final int min = val / 60;
        final int hour = min / 60;
        SystemMessage2 msg;
        if (hour > 0) {
            msg = new SystemMessage2(SystemMsg.S1_HOURS_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(hour);
        } else if (min > 0) {
            msg = new SystemMessage2(SystemMsg.S1_MINUTES_UNTIL_CASTLE_SIEGE_CONCLUSION).addInteger(min);
        } else {
            msg = new SystemMessage2(SystemMsg.THIS_CASTLE_SIEGE_WILL_END_IN_S1_SECONDS).addInteger(val);
        }
        broadcastTo(msg, "attackers", "defenders");
    }

    private void initControlTowers() {
        final List<SpawnExObject> objects = getObjects("guards");
        final List<Spawner> spawns = new ArrayList<>();
        objects.stream().map(SpawnExObject::getSpawns).forEach(spawns::addAll);
        final List<SiegeToggleNpcObject> ct = getObjects("control_towers");
        for (final Spawner spawn : spawns) {
            final Location spawnLoc = spawn.getCurrentSpawnRange().getRandomLoc(ReflectionManager.DEFAULT.getGeoIndex());
            SiegeToggleNpcInstance closestCt = null;
            double distanceClosest = 0.0;
            for (final SiegeToggleNpcObject c : ct) {
                final SiegeToggleNpcInstance npcTower = c.getToggleNpc();
                final double distance = npcTower.getDistance(spawnLoc);
                if (closestCt == null || distance < distanceClosest) {
                    closestCt = npcTower;
                    distanceClosest = distance;
                }
                closestCt.register(spawn);
            }
        }
    }

    private void damageZoneAction(final boolean active) {
        zoneAction("bought_zones", active);
    }

    private void setNextSiegeTime() {
        final Calendar calendar = (Calendar) Config.CASTLE_VALIDATION_DATE.clone();
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        calendar.set(Calendar.HOUR_OF_DAY, getResidence().getLastSiegeDate().get(Calendar.HOUR_OF_DAY));
        if (calendar.before(Config.CASTLE_VALIDATION_DATE)) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        calendar.setTimeInMillis(scheduleNextTime(calendar.getTimeInMillis(), _nextSiegeTimesPatterns[0]));
        setNextSiegeTime(calendar.getTimeInMillis());
    }

    public void generateNextSiegeDates() {
        if (getResidence().getSiegeDate().getTimeInMillis() != 0L) {
            return;
        }
        final Calendar calendar = (Calendar) Config.CASTLE_VALIDATION_DATE.clone();
        calendar.set(Calendar.DAY_OF_WEEK, 1);
        if (calendar.before(Config.CASTLE_VALIDATION_DATE)) {
            calendar.add(Calendar.WEEK_OF_YEAR, 1);
        }
        _nextSiegeTimes = new TreeSet<>();
        for (final NextTime nextTime : _nextSiegeTimesPatterns) {
            _nextSiegeTimes.add((int) (scheduleNextTime(calendar.getTimeInMillis(), nextTime) / 1000L));
        }
        final long diff = getResidence().getOwnDate().getTimeInMillis() + _nextSiegeDateSetDelay - System.currentTimeMillis();
        _nextSiegeDateSetTask = ThreadPoolManager.getInstance().schedule(new NextSiegeDateSet(), diff);
    }

    protected long scheduleNextTime(final long baseMs, final NextTime nextTime) {
        final Calendar cal = new GregorianCalendar(TimeZone.getDefault());
        cal.setTimeInMillis(baseMs);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        long nextTimeMs;
        for (nextTimeMs = cal.getTimeInMillis(); nextTimeMs < System.currentTimeMillis(); nextTimeMs = nextTime.next(nextTimeMs)) {
        }
        return nextTimeMs;
    }

    public void setNextSiegeTime(final int id) {
        if (!_nextSiegeTimes.contains(id) || _nextSiegeDateSetTask == null) {
            return;
        }
        _nextSiegeTimes = Collections.emptySet();
        _nextSiegeDateSetTask.cancel(false);
        _nextSiegeDateSetTask = null;
        setNextSiegeTime(id * 1000L);
    }

    private void setNextSiegeTime(final long g) {
        broadcastToWorld((new SystemMessage2(SystemMsg.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME)).addResidenceName(getResidence()));
        clearActions();
        getResidence().getSiegeDate().setTimeInMillis(g);
        getResidence().setJdbcState(JdbcEntityState.UPDATED);
        getResidence().update();
        registerActions();
    }

    @Override
    public boolean isAttackersInAlly() {
        return !_firstStep;
    }

    public Integer[] getNextSiegeTimes() {
        return _nextSiegeTimes.toArray(new Integer[0]);
    }

    @Override
    public boolean canResurrect(final Player resurrectPlayer, final Creature target, final boolean force) {
        final boolean playerInZone = resurrectPlayer.isInZone(ZoneType.SIEGE);
        final boolean targetInZone = target.isInZone(ZoneType.SIEGE);
        if (!playerInZone && !targetInZone) {
            return true;
        }
        if (!targetInZone) {
            return false;
        }
        final Player targetPlayer = target.getPlayer();
        final CastleSiegeEvent siegeEvent = target.getEvent(CastleSiegeEvent.class);
        if (siegeEvent != this) {
            if (force) {
                targetPlayer.sendPacket(SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE);
            }
            resurrectPlayer.sendPacket(force ? SystemMsg.IT_IS_NOT_POSSIBLE_TO_RESURRECT_IN_BATTLEFIELDS_WHERE_A_SIEGE_WAR_IS_TAKING_PLACE : SystemMsg.INVALID_TARGET);
            return false;
        }
        SiegeClanObject targetSiegeClan = siegeEvent.getSiegeClan("attackers", targetPlayer.getClan());
        if (targetSiegeClan == null) {
            targetSiegeClan = siegeEvent.getSiegeClan("defenders", targetPlayer.getClan());
        }
        if ("attackers".equals(targetSiegeClan.getType())) {
            if (targetSiegeClan.getFlag() == null) {
                if (force) {
                    targetPlayer.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
                }
                resurrectPlayer.sendPacket(force ? SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
                return false;
            }
        } else {
            final List<SiegeToggleNpcObject> towers = getObjects("control_towers");
            int deadTowerCnt = (int) towers.stream().filter(t -> !t.isAlive()).count();
            if (deadTowerCnt > 1) {
                if (force) {
                    targetPlayer.sendPacket(SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE);
                }
                resurrectPlayer.sendPacket(force ? SystemMsg.THE_GUARDIAN_TOWER_HAS_BEEN_DESTROYED_AND_RESURRECTION_IS_NOT_POSSIBLE : SystemMsg.INVALID_TARGET);
                return false;
            }
        }
        return true;
    }

    @Override
    public Location getRestartLoc(final Player player, final RestartType type) {
        Location loc;
        switch (type) {
            case TO_VILLAGE: {
                loc = TeleportUtils.getRestartLocation(player, RestartType.TO_VILLAGE);
                break;
            }
            default: {
                loc = super.getRestartLoc(player, type);
                break;
            }
        }
        return loc;
    }

    private class NextSiegeDateSet extends RunnableImpl {
        @Override
        public void runImpl() {
            setNextSiegeTime();
        }
    }
}
