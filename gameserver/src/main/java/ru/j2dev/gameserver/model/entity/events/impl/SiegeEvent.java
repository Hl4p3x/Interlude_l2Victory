package ru.j2dev.gameserver.model.entity.events.impl;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.dao.SiegeClanDAO;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.RestartType;
import ru.j2dev.gameserver.model.entity.events.GlobalEvent;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.entity.events.objects.ZoneObject;
import ru.j2dev.gameserver.model.entity.residence.Residence;
import ru.j2dev.gameserver.model.instances.DoorInstance;
import ru.j2dev.gameserver.model.instances.SummonInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.templates.DoorTemplate.DoorType;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;

public abstract class SiegeEvent<R extends Residence, S extends SiegeClanObject> extends GlobalEvent {
    public static final String EVENT = "event";
    public static final String OWNER = "owner";
    public static final String OLD_OWNER = "old_owner";
    public static final String ATTACKERS = "attackers";
    public static final String DEFENDERS = "defenders";
    public static final String HOUR_OF_DAY = "hour_of_day";
    public static final String SPECTATORS = "spectators";
    public static final String SIEGE_ZONES = "siege_zones";
    public static final String FLAG_ZONES = "flag_zones";
    public static final String DAY_OF_WEEK = "day_of_week";
    public static final String REGISTRATION = "registration";
    protected final int _dayOfWeek;
    protected final int _hourOfDay;
    protected final OnDeathListener _doorDeathListener;
    protected final List<HardReference<SummonInstance>> _siegeSummons;
    protected R _residence;
    protected Clan _oldOwner;
    private boolean _isInProgress;
    private boolean _isRegistrationOver;

    public SiegeEvent(final MultiValueSet<String> set) {
        super(set);
        _doorDeathListener = new DoorDeathListener();
        _siegeSummons = new ArrayList<>();
        _dayOfWeek = set.getInteger(DAY_OF_WEEK, 0);
        _hourOfDay = set.getInteger(HOUR_OF_DAY, 0);
    }

    @Override
    public void startEvent() {
        setInProgress(true);
        super.startEvent();
    }

    @Override
    public final void stopEvent() {
        stopEvent(false);
    }

    public void stopEvent(final boolean step) {
        despawnSiegeSummons();
        setInProgress(false);
        reCalcNextTime(false);
        super.stopEvent();
    }

    public void processStep(final Clan clan) {
    }

    @Override
    public void reCalcNextTime(final boolean onInit) {
        clearActions();
        final Calendar startSiegeDate = getResidence().getSiegeDate();
        if (onInit) {
            if (startSiegeDate.getTimeInMillis() <= System.currentTimeMillis()) {
                startSiegeDate.set(Calendar.DAY_OF_WEEK, _dayOfWeek);
                startSiegeDate.set(Calendar.HOUR_OF_DAY, _hourOfDay);
                validateSiegeDate(startSiegeDate, 2);
                getResidence().setJdbcState(JdbcEntityState.UPDATED);
            }
        } else {
            startSiegeDate.add(Calendar.WEEK_OF_YEAR, 2);
            getResidence().setJdbcState(JdbcEntityState.UPDATED);
        }
        registerActions();
        getResidence().update();
    }

    protected void validateSiegeDate(final Calendar calendar, final int add) {
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        while (calendar.getTimeInMillis() < System.currentTimeMillis()) {
            calendar.add(Calendar.WEEK_OF_YEAR, add);
        }
    }

    @Override
    protected long startTimeMillis() {
        return getResidence().getSiegeDate().getTimeInMillis();
    }

    @Override
    public void teleportPlayers(final String t) {
        List<Player> players = new ArrayList<>();
        final Clan ownerClan = getResidence().getOwner();
        if (OWNER.equalsIgnoreCase(t)) {
            if (ownerClan != null) {
                for (final Player player : getPlayersInZone()) {
                    if (player.getClan() == ownerClan) {
                        players.add(player);
                    }
                }
            }
        } else if (ATTACKERS.equalsIgnoreCase(t)) {
            for (final Player player : getPlayersInZone()) {
                final S siegeClan = getSiegeClan(ATTACKERS, player.getClan());
                if (siegeClan != null && siegeClan.isParticle(player)) {
                    players.add(player);
                }
            }
        } else if (DEFENDERS.equalsIgnoreCase(t)) {
            for (final Player player : getPlayersInZone()) {
                if (ownerClan != null && player.getClan() != null && player.getClan() == ownerClan) {
                    continue;
                }
                final S siegeClan = getSiegeClan(DEFENDERS, player.getClan());
                if (siegeClan == null || !siegeClan.isParticle(player)) {
                    continue;
                }
                players.add(player);
            }
        } else if (SPECTATORS.equalsIgnoreCase(t)) {
            for (final Player player : getPlayersInZone()) {
                if (ownerClan != null && player.getClan() != null && player.getClan() == ownerClan) {
                    continue;
                }
                if (player.getClan() != null && (getSiegeClan(ATTACKERS, player.getClan()) != null || getSiegeClan(DEFENDERS, player.getClan()) != null)) {
                    continue;
                }
                players.add(player);
            }
        } else {
            players = getPlayersInZone();
        }
        for (final Player player : players) {
            Location loc;
            if (OWNER.equalsIgnoreCase(t) || DEFENDERS.equalsIgnoreCase(t)) {
                loc = getResidence().getOwnerRestartPoint();
            } else {
                loc = getResidence().getNotOwnerRestartPoint(player);
            }
            player.teleToLocation(loc, ReflectionManager.DEFAULT);
        }
    }

    public List<Player> getPlayersInZone() {
        final List<ZoneObject> zones = getObjects(SIEGE_ZONES);
        final List<Player> result = new ArrayList<>();
        for (final ZoneObject zone : zones) {
            result.addAll(zone.getInsidePlayers());
        }
        return result;
    }

    public void broadcastInZone(final L2GameServerPacket... packet) {
        for (final Player player : getPlayersInZone()) {
            player.sendPacket((IStaticPacket[]) packet);
        }
    }

    public void broadcastInZone(final IStaticPacket... packet) {
        for (final Player player : getPlayersInZone()) {
            player.sendPacket(packet);
        }
    }

    public boolean checkIfInZone(final Creature character) {
        final List<ZoneObject> zones = getObjects(SIEGE_ZONES);
        for (final ZoneObject zone : zones) {
            if (zone.checkIfInZone(character)) {
                return true;
            }
        }
        return false;
    }

    public void broadcastInZone2(final IStaticPacket... packet) {
        for (final Player player : getResidence().getZone().getInsidePlayers()) {
            player.sendPacket(packet);
        }
    }

    public void broadcastInZone2(final L2GameServerPacket... packet) {
        for (final Player player : getResidence().getZone().getInsidePlayers()) {
            player.sendPacket((IStaticPacket[]) packet);
        }
    }

    public void loadSiegeClans() {
        addObjects(ATTACKERS, SiegeClanDAO.getInstance().load(getResidence(), ATTACKERS));
        addObjects(DEFENDERS, SiegeClanDAO.getInstance().load(getResidence(), DEFENDERS));
    }

    @SuppressWarnings("unchecked")
    public S newSiegeClan(final String type, final int clanId, final long param, final long date) {
        final Clan clan = ClanTable.getInstance().getClan(clanId);
        return (S) ((clan == null) ? null : new SiegeClanObject(type, clan, param, date));
    }

    public void updateParticles(final boolean start, final String... arg) {
        for (final String a : arg) {
            final List<SiegeClanObject> siegeClans = getObjects(a);
            for (final SiegeClanObject s : siegeClans) {
                s.setEvent(start, this);
            }
        }
    }

    public S getSiegeClan(final String name, final Clan clan) {
        if (clan == null) {
            return null;
        }
        return getSiegeClan(name, clan.getClanId());
    }

    @SuppressWarnings("unchecked")
    public S getSiegeClan(final String name, final int objectId) {
        final List<SiegeClanObject> siegeClanList = getObjects(name);
        if (siegeClanList.isEmpty()) {
            return null;
        }
        for (final SiegeClanObject siegeClan : siegeClanList) {
            if (siegeClan.getObjectId() == objectId) {
                return (S) siegeClan;
            }
        }
        return null;
    }

    public void broadcastTo(final IStaticPacket packet, final String... types) {
        for (final String type : types) {
            final List<SiegeClanObject> siegeClans = getObjects(type);
            for (final SiegeClanObject siegeClan : siegeClans) {
                siegeClan.broadcast(packet);
            }
        }
    }

    public void broadcastTo(final L2GameServerPacket packet, final String... types) {
        for (final String type : types) {
            final List<SiegeClanObject> siegeClans = getObjects(type);
            for (final SiegeClanObject siegeClan : siegeClans) {
                siegeClan.broadcast(packet);
            }
        }
    }

    @Override
    public void initEvent() {
        _residence = ResidenceHolder.getInstance().getResidence(getId());
        loadSiegeClans();
        clearActions();
        super.initEvent();
    }

    @Override
    protected void printInfo() {
        final long startSiegeMillis = startTimeMillis();
        if (startSiegeMillis == 0L) {
            info(getName() + " time - undefined");
        } else {
            info(getName() + " time - " + TimeUtils.toSimpleFormat(startSiegeMillis));
        }
    }

    @Override
    public boolean ifVar(final String name) {
        if (OWNER.equals(name)) {
            return getResidence().getOwner() != null;
        }
        return OLD_OWNER.equals(name) && _oldOwner != null;
    }

    @Override
    public boolean isParticle(final Player player) {
        return isInProgress() && player.getClan() != null && (getSiegeClan(ATTACKERS, player.getClan()) != null || getSiegeClan(DEFENDERS, player.getClan()) != null);
    }

    @Override
    public void checkRestartLocs(final Player player, final Map<RestartType, Boolean> r) {
        if (getObjects(FLAG_ZONES).isEmpty()) {
            return;
        }
        final S clan = getSiegeClan(ATTACKERS, player.getClan());
        if (clan != null && clan.getFlag() != null) {
            r.put(RestartType.TO_FLAG, Boolean.TRUE);
        }
    }

    @Override
    public Location getRestartLoc(final Player player, final RestartType type) {
        final S attackerClan = getSiegeClan(ATTACKERS, player.getClan());
        Location loc = null;
        switch (type) {
            case TO_FLAG: {
                if (!getObjects(FLAG_ZONES).isEmpty() && attackerClan != null && attackerClan.getFlag() != null) {
                    loc = Location.findPointToStay(attackerClan.getFlag(), 50, 75);
                    break;
                }
                player.sendPacket(SystemMsg.IF_A_BASE_CAMP_DOES_NOT_EXIST_RESURRECTION_IS_NOT_POSSIBLE);
                break;
            }
        }
        return loc;
    }

    @Override
    public int getRelation(final Player thisPlayer, final Player targetPlayer, int result) {
        final Clan clan1 = thisPlayer.getClan();
        final Clan clan2 = targetPlayer.getClan();
        if (clan1 == null || clan2 == null) {
            return result;
        }
        final SiegeEvent<?, ?> siegeEvent2 = targetPlayer.getEvent(SiegeEvent.class);
        if (this == siegeEvent2) {
            result |= 0x200;
            final SiegeClanObject siegeClan1 = getSiegeClan(ATTACKERS, clan1);
            final SiegeClanObject siegeClan2 = getSiegeClan(ATTACKERS, clan2);
            if ((siegeClan1 == null && siegeClan2 == null) || (siegeClan1 != null && siegeClan2 != null && (siegeClan1 == siegeClan2 || isAttackersInAlly()))) {
                result |= 0x800;
            } else {
                result |= 0x1000;
            }
            if (siegeClan1 != null) {
                result |= 0x400;
            }
        }
        return result;
    }

    @Override
    public int getUserRelation(final Player thisPlayer, int oldRelation) {
        final SiegeClanObject siegeClan = getSiegeClan(ATTACKERS, thisPlayer.getClan());
        if (siegeClan != null) {
            oldRelation |= 0x180;
        } else {
            oldRelation |= 0x80;
        }
        return oldRelation;
    }

    @Override
    public SystemMsg checkForAttack(final Creature target, final Creature attacker, final Skill skill, final boolean force) {
        final SiegeEvent<?, ?> siegeEvent = target.getEvent(SiegeEvent.class);
        if (this != siegeEvent) {
            return null;
        }
        if (!checkIfInZone(target) || !checkIfInZone(attacker)) {
            return null;
        }
        final Player player = target.getPlayer();
        if (player == null) {
            return null;
        }
        final SiegeClanObject siegeClan1 = getSiegeClan(ATTACKERS, player.getClan());
        if (siegeClan1 == null && attacker.isSiegeGuard()) {
            return SystemMsg.INVALID_TARGET;
        }
        final Player playerAttacker = attacker.getPlayer();
        if (playerAttacker == null) {
            return SystemMsg.INVALID_TARGET;
        }
        final SiegeClanObject siegeClan2 = getSiegeClan(ATTACKERS, playerAttacker.getClan());
        if (Config.ALLOW_TEMPORARILY_ALLY_ON_FIRST_SIEGE && ((siegeClan1 == null && siegeClan2 == null) || (siegeClan1 != null && siegeClan2 != null && (siegeClan1 == siegeClan2 || isAttackersInAlly())))) {
            return SystemMsg.INVALID_TARGET;
        }
        if (siegeClan1 == null && siegeClan2 == null) {
            return SystemMsg.INVALID_TARGET;
        }
        return null;
    }

    @Override
    public boolean isInProgress() {
        return _isInProgress;
    }

    public void setInProgress(final boolean b) {
        _isInProgress = b;
    }

    @Override
    public void action(final String name, final boolean start) {
        if (REGISTRATION.equalsIgnoreCase(name)) {
            setRegistrationOver(!start);
        } else {
            super.action(name, start);
        }
    }

    public boolean isAttackersInAlly() {
        return false;
    }

    @Override
    public List<Player> broadcastPlayers(final int range) {
        return itemObtainPlayers();
    }

    @Override
    public List<Player> itemObtainPlayers() {
        final List<Player> playersInZone = getPlayersInZone();
        final List<Player> list = new ArrayList<>(playersInZone.size());
        for (final Player player : getPlayersInZone()) {
            if (player.getEvent(getClass()) == this) {
                list.add(player);
            }
        }
        return list;
    }

    public Location getEnterLoc(final Player player) {
        final S siegeClan = getSiegeClan(ATTACKERS, player.getClan());
        if (siegeClan == null) {
            return getResidence().getOwnerRestartPoint();
        }
        if (siegeClan.getFlag() != null) {
            return Location.findAroundPosition(siegeClan.getFlag(), 50, 75);
        }
        return getResidence().getNotOwnerRestartPoint(player);
    }

    public R getResidence() {
        return _residence;
    }

    public boolean isRegistrationOver() {
        return _isRegistrationOver;
    }

    public void setRegistrationOver(final boolean b) {
        _isRegistrationOver = b;
    }

    public void addSiegeSummon(final SummonInstance summon) {
        _siegeSummons.add(summon.getRef());
    }

    public boolean containsSiegeSummon(final SummonInstance cha) {
        return _siegeSummons.contains(cha.getRef());
    }

    public void despawnSiegeSummons() {
        for (final HardReference<SummonInstance> ref : _siegeSummons) {
            final SummonInstance summon = ref.get();
            if (summon != null) {
                summon.unSummon();
            }
        }
        _siegeSummons.clear();
    }

    public class DoorDeathListener implements OnDeathListener {
        @Override
        public void onDeath(final Creature actor, final Creature killer) {
            if (!isInProgress()) {
                return;
            }
            final DoorInstance door = (DoorInstance) actor;
            if (door.getDoorType() == DoorType.WALL) {
                return;
            }
            broadcastTo(SystemMsg.THE_CASTLE_GATE_HAS_BEEN_DESTROYED, ATTACKERS, DEFENDERS);
        }
    }
}
