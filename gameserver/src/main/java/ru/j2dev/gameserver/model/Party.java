package ru.j2dev.gameserver.model;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.entity.DimensionalRift;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.entity.SevenSignsFestival.DarknessFestival;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.taskmanager.LazyPrecisionTaskManager;
import ru.j2dev.gameserver.utils.ItemFunctions;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.Log.ItemLog;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.Collectors;

public class Party implements PlayerGroup {
    public static final int MAX_SIZE = 9;
    public static final int ITEM_LOOTER = 0;
    public static final int ITEM_RANDOM = 1;
    public static final int ITEM_RANDOM_SPOIL = 2;
    public static final int ITEM_ORDER = 3;
    public static final int ITEM_ORDER_SPOIL = 4;
    private static final int[] LOOT_SYSSTRINGS = {487, 488, 798, 799, 800};

    private final List<Player> _members;
    private double _rateExp;
    private double _rateSp;
    private double _rateDrop;
    private double _rateAdena;
    private double _rateSpoil;
    private int _partyLvl;
    private int _itemDistribution;
    private int _itemOrder;
    private int _dimentionalRift;
    private Reflection _reflection;
    private CommandChannel _commandChannel;
    private ScheduledFuture<?> positionTask;
    private int _requestChangeLoot;
    private long _requestChangeLootTimer;
    private Set<Integer> _changeLootAnswers;
    private Future<?> _checkTask;
    public Party(final Player leader, final int itemDistribution) {
        _members = new CopyOnWriteArrayList<>();
        _partyLvl = 0;
        _itemDistribution = 0;
        _itemOrder = 0;
        _requestChangeLoot = -1;
        _requestChangeLootTimer = 0L;
        _changeLootAnswers = null;
        _checkTask = null;
        _itemDistribution = itemDistribution;
        _members.add(leader);
        _partyLvl = leader.getLevel();
        _rateExp = leader.getBonus().getRateXp();
        _rateSp = leader.getBonus().getRateSp();
        _rateAdena = leader.getBonus().getDropAdena();
        _rateDrop = leader.getBonus().getDropItems();
        _rateSpoil = leader.getBonus().getDropSpoil();
    }

    public static void TeleportParty(final List<Player> members, final Location dest) {
        members.stream().filter(Objects::nonNull).forEach(_member -> _member.teleToLocation(dest));
    }

    public static void TeleportParty(final List<Player> members, final Territory territory, final Location dest) {
        if (!territory.isInside(dest.x, dest.y)) {
            Log.add("TeleportParty: dest is out of territory", "errors");
            Thread.dumpStack();
            return;
        }
        final int base_x = members.get(0).getX();
        final int base_y = members.get(0).getY();
        for (final Player _member : members) {
            if (_member == null) {
                continue;
            }
            int diff_x = _member.getX() - base_x;
            int diff_y = _member.getY() - base_y;
            final Location loc = new Location(dest.x + diff_x, dest.y + diff_y, dest.z);
            while (!territory.isInside(loc.x, loc.y)) {
                diff_x = loc.x - dest.x;
                diff_y = loc.y - dest.y;
                if (diff_x != 0) {
                    loc.x -= diff_x / Math.abs(diff_x);
                }
                if (diff_y != 0) {
                    loc.y -= diff_y / Math.abs(diff_y);
                }
            }
            _member.teleToLocation(loc);
        }
    }

    public static void RandomTeleportParty(final List<Player> members, final Territory territory) {
        members.forEach(member -> member.teleToLocation(Territory.getRandomLoc(territory, member.getGeoIndex())));
    }

    public double getRateExp() {
        return _rateExp;
    }

    public double getRateSp() {
        return _rateSp;
    }

    public double getRateDrop() {
        return _rateDrop;
    }

    public double getRateAdena() {
        return _rateAdena;
    }

    public double getRateSpoil() {
        return _rateSpoil;
    }

    public int getMemberCount() {
        return _members.size();
    }

    public int getMemberCountInRange(final Player player, final int range) {
        return (int) _members.stream().filter(member -> member == player || member.isInRangeZ(player, range)).count();
    }

    public List<Player> getPartyMembers() {
        return _members;
    }

    public List<Integer> getPartyMembersObjIds() {
        return _members.stream().map(GameObject::getObjectId).collect(Collectors.toCollection(() -> new ArrayList<>(_members.size())));
    }

    public List<Playable> getPartyMembersWithPets() {
        final List<Playable> result = new ArrayList<>();
        _members.forEach(member -> {
            result.add(member);
            if (member.getPet() != null) {
                result.add(member.getPet());
            }
        });
        return result;
    }

    private Player getNextLooterInRange(final Player player, final ItemInstance item, final int range) {
        synchronized (_members) {
            int antiloop = _members.size();
            while (antiloop-- > 0) {
                final int looter = _itemOrder;
                _itemOrder++;
                if (_itemOrder > _members.size() - 1) {
                    _itemOrder = 0;
                }
                final Player ret = (looter < _members.size()) ? _members.get(looter) : player;
                if (ret != null && !ret.isDead() && ret.isInRangeZ(player, range) && ret.getInventory().validateCapacity(item) && ret.getInventory().validateWeight(item)) {
                    return ret;
                }
            }
        }
        return player;
    }

    public boolean isLeader(final Player player) {
        return getPartyLeader() == player;
    }

    public Player getPartyLeader() {
        synchronized (_members) {
            if (_members.size() == 0) {
                return null;
            }
            return _members.get(0);
        }
    }

    @Override
    public void broadCast(final IStaticPacket... msg) {
        _members.forEach(member -> member.sendPacket(msg));
    }

    public void broadcastMessageToPartyMembers(final String msg) {
        broadCast(new SystemMessage(msg));
    }

    public void broadcastToPartyMembers(final Player exclude, final L2GameServerPacket msg) {
        _members.stream().filter(member -> exclude != member).forEach(member -> member.sendPacket(msg));
    }

    public void broadcastToPartyMembersInRange(final Player player, final L2GameServerPacket msg, final int range) {
        _members.stream().filter(member -> player.isInRangeZ(member, range)).forEach(member -> member.sendPacket(msg));
    }

    public boolean containsMember(final Player player) {
        return _members.contains(player);
    }

    public boolean addPartyMember(final Player player) {
        final Player leader = getPartyLeader();
        if (leader == null) {
            return false;
        }
        synchronized (_members) {
            if (_members.isEmpty()) {
                return false;
            }
            if (_members.contains(player)) {
                return false;
            }
            if (_members.size() == Config.ALT_MAX_PARTY_SIZE) {
                return false;
            }
            _members.add(player);
        }
        if (_requestChangeLoot != -1) {
            finishLootRequest(false);
        }
        player.setParty(this);
        player.getListeners().onPartyInvite();
        final List<L2GameServerPacket> addInfo = new ArrayList<>(4 + _members.size() * 4);
        final List<L2GameServerPacket> pplayer = new ArrayList<>(20);
        pplayer.add(new PartySmallWindowAll(this, player));
        pplayer.add(new SystemMessage(106).addName(leader));
        addInfo.add(new SystemMessage(107).addName(player));
        addInfo.add(new PartySpelled(player, true));
        Summon pet;
        if ((pet = player.getPet()) != null) {
            addInfo.add(new PartySpelled(pet, true));
        }
        final PartyMemberPosition pmp = new PartyMemberPosition();
        for (final Player member : _members) {
            if (member != player) {
                final List<L2GameServerPacket> pmember = new ArrayList<>(addInfo.size() + 4);
                pmember.add(new PartySmallWindowAdd(member, player));
                pmember.addAll(addInfo);
                pmember.add(RelationChanged.create(member, player, member));
                pmember.add(new PartyMemberPosition().add(player));
                member.sendPacket(pmember);
                pplayer.add(new PartySpelled(member, true));
                if ((pet = member.getPet()) != null) {
                    pplayer.add(new PartySpelled(pet, true));
                }
                pplayer.add(RelationChanged.create(player, member, player));
                pmp.add(member);
            }
        }
        pplayer.add(pmp);
        if (isInCommandChannel()) {
            pplayer.add(ExMPCCOpen.STATIC);
        }
        player.sendPacket(pplayer);
        startUpdatePositionTask();
        recalculatePartyData();
        if (isInReflection() && getReflection() instanceof DimensionalRift) {
            ((DimensionalRift) getReflection()).partyMemberInvited();
        }
        return true;
    }

    public void dissolveParty() {
        _members.forEach(p -> {
            p.sendPacket(PartySmallWindowDeleteAll.STATIC);
            p.setParty(null);
        });
        synchronized (_members) {
            _members.clear();
        }
        setDimensionalRift(null);
        setCommandChannel(null);
        stopUpdatePositionTask();
    }

    public boolean removePartyMember(final Player player, final boolean kick) {
        final boolean isLeader = isLeader(player);
        boolean dissolve;
        synchronized (_members) {
            if (!_members.remove(player)) {
                return false;
            }
            dissolve = (_members.size() == 1);
        }
        player.getListeners().onPartyLeave();
        player.setParty(null);
        recalculatePartyData();
        final List<L2GameServerPacket> pplayer = new ArrayList<>(4 + _members.size() * 2);
        if (isInCommandChannel()) {
            pplayer.add(ExMPCCClose.STATIC);
        }
        if (kick) {
            pplayer.add(Msg.YOU_HAVE_BEEN_EXPELLED_FROM_THE_PARTY);
        } else {
            pplayer.add(Msg.YOU_HAVE_WITHDRAWN_FROM_THE_PARTY);
        }
        pplayer.add(PartySmallWindowDeleteAll.STATIC);
        final List<L2GameServerPacket> outsInfo = new ArrayList<>(3);
        outsInfo.add(new PartySmallWindowDelete(player));
        if (kick) {
            outsInfo.add(new SystemMessage(201).addName(player));
        } else {
            outsInfo.add(new SystemMessage(108).addName(player));
        }
        _members.forEach(member -> {
            final List<L2GameServerPacket> pmember = new ArrayList<>(2 + outsInfo.size());
            pmember.addAll(outsInfo);
            pmember.add(RelationChanged.create(member, player, member));
            member.sendPacket(pmember);
            pplayer.add(RelationChanged.create(player, member, player));
        });
        player.sendPacket(pplayer);
        final Reflection reflection = getReflection();
        if (reflection instanceof DarknessFestival) {
            ((DarknessFestival) reflection).partyMemberExited();
        } else if (isInReflection() && getReflection() instanceof DimensionalRift) {
            ((DimensionalRift) getReflection()).partyMemberExited(player);
        }
        if (reflection != null && player.getReflection() == reflection && reflection.getReturnLoc() != null) {
            player.teleToLocation(reflection.getReturnLoc(), ReflectionManager.DEFAULT);
        }
        final Player leader = getPartyLeader();
        if (dissolve) {
            if (isInCommandChannel()) {
                _commandChannel.removeParty(this);
            } else if (reflection != null && reflection.getInstancedZone() != null && reflection.getInstancedZone().isCollapseOnPartyDismiss()) {
                if (reflection.getParty() == this) {
                    reflection.startCollapseTimer(reflection.getInstancedZone().getTimerOnCollapse() * 1000);
                }
                if (leader != null && leader.getReflection() == reflection) {
                    leader.broadcastPacket(new SystemMessage(2106).addNumber(1));
                }
            }
            dissolveParty();
        } else {
            if (isInCommandChannel() && _commandChannel.getChannelLeader() == player) {
                _commandChannel.setChannelLeader(leader);
            }
            if (isLeader) {
                updateLeaderInfo();
            }
        }
        if (_checkTask != null) {
            _checkTask.cancel(true);
            _checkTask = null;
        }
        return true;
    }

    public boolean changePartyLeader(final Player player) {
        final Player leader = getPartyLeader();
        synchronized (_members) {
            final int index = _members.indexOf(player);
            if (index == -1) {
                return false;
            }
            _members.set(0, player);
            _members.set(index, leader);
        }
        updateLeaderInfo();
        if (isInCommandChannel() && _commandChannel.getChannelLeader() == leader) {
            _commandChannel.setChannelLeader(player);
        }
        return true;
    }

    private void updateLeaderInfo() {
        final Player leader = getPartyLeader();
        if (leader == null) {
            return;
        }
        final SystemMessage msg = new SystemMessage(1384).addName(leader);
        _members.forEach(member -> member.sendPacket(PartySmallWindowDeleteAll.STATIC, new PartySmallWindowAll(this, member), msg));
        _members.forEach(member -> broadcastToPartyMembers(member, new PartySpelled(member, true)));
    }

    public Player getPlayerByName(final String name) {
        return _members.stream().filter(member -> name.equalsIgnoreCase(member.getName())).findFirst().orElse(null);
    }

    public void distributeItem(final Player player, final ItemInstance item, final NpcInstance fromNpc) {
        switch (item.getItemId()) {
            case 57: {
                distributeAdena(player, item, fromNpc);
                break;
            }
            default: {
                distributeItem0(player, item, fromNpc);
                break;
            }
        }
    }

    private void distributeItem0(final Player player, final ItemInstance item, final NpcInstance fromNpc) {
        Player target = null;
        List<Player> ret;
        switch (_itemDistribution) {
            case 1:
            case 2: {
                ret = new ArrayList<>(_members.size());
                _members.stream().filter(member -> member.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && !member.isDead() && member.getInventory().validateCapacity(item) && member.getInventory().validateWeight(item)).forEach(ret::add);
                target = (ret.isEmpty() ? null : ret.get(Rnd.get(ret.size())));
                break;
            }
            case 3:
            case 4: {
                synchronized (_members) {
                    ret = new CopyOnWriteArrayList<>(_members);
                    while (target == null && !ret.isEmpty()) {
                        final int looter = _itemOrder;
                        ++_itemOrder;
                        if (_itemOrder > ret.size() - 1) {
                            _itemOrder = 0;
                        }
                        final Player looterPlayer = (looter < ret.size()) ? ret.get(looter) : null;
                        if (looterPlayer != null) {
                            if (!looterPlayer.isDead() && looterPlayer.isInRangeZ(player, Config.ALT_PARTY_DISTRIBUTION_RANGE) && ItemFunctions.canAddItem(looterPlayer, item)) {
                                target = looterPlayer;
                            } else {
                                ret.remove(looterPlayer);
                            }
                        }
                    }
                }
                if (target == null) {
                    return;
                }
                break;
            }
            default: {
                target = player;
                break;
            }
        }
        if (target == null) {
            target = player;
        }
        if (target.pickupItem(item, ItemLog.PartyPickup)) {
            if (fromNpc == null) {
                player.broadcastPacket(new GetItem(item, player.getObjectId()));
            }
            player.broadcastPickUpMsg(item);
            item.pickupMe();
            broadcastToPartyMembers(target, SystemMessage2.obtainItemsBy(item, target));
        } else {
            item.dropToTheGround(player, fromNpc);
        }
    }

    private void distributeAdena(final Player player, final ItemInstance item, final NpcInstance fromNpc) {
        if (player == null) {
            return;
        }
        final List<Player> membersInRange = new ArrayList<>();
        if (item.getCount() < _members.size()) {
            membersInRange.add(player);
        } else {
            _members.stream().filter(member -> !member.isDead() && (member == player || player.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && ItemFunctions.canAddItem(player, item)).forEach(membersInRange::add);
        }
        if (membersInRange.isEmpty()) {
            membersInRange.add(player);
        }
        final long totalAdena = item.getCount();
        final long amount = totalAdena / membersInRange.size();
        final long ost = totalAdena % membersInRange.size();
        membersInRange.forEach(member2 -> {
            final long count = member2.equals(player) ? (amount + ost) : amount;
            member2.getInventory().addAdena(count);
            member2.sendPacket(SystemMessage2.obtainItems(57, count, 0));
        });
        if (fromNpc == null) {
            player.broadcastPacket(new GetItem(item, player.getObjectId()));
        }
        item.pickupMe();
    }

    public void distributeXpAndSp(final double xpReward, final double spReward, final List<Player> rewardedMembers, final Creature lastAttacker, final MonsterInstance monster) {
        recalculatePartyData();
        final List<Player> mtr = new ArrayList<>();
        int partyLevel = lastAttacker.getLevel();
        int partyLvlSum = 0;
        for (final Player member : rewardedMembers) {
            if (!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                continue;
            }
            partyLevel = Math.max(partyLevel, member.getLevel());
        }
        for (final Player member : rewardedMembers) {
            if (!monster.isInRangeZ(member, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                continue;
            }
            if (member.getLevel() <= partyLevel - Config.ALT_PARTY_DISTRIBUTION_DIFF_LEVEL_LIMIT) {
                continue;
            }
            partyLvlSum += member.getLevel();
            mtr.add(member);
        }
        if (mtr.isEmpty()) {
            return;
        }
        final double bonus = Config.ALT_PARTY_BONUS[mtr.size() - 1];
        final double XP = xpReward * bonus;
        final double SP = spReward * bonus;
        for (final Player member2 : mtr) {
            double lvlPenalty = Experience.penaltyModifier(monster.calculateLevelDiffForDrop(member2.getLevel()), 9.0);
            final int lvlDiff = partyLevel - member2.getLevel();
            if (lvlDiff >= Config.PARTY_PENALTY_EXP_SP_MAX_LEVEL && lvlDiff <= Config.PARTY_PENALTY_EXP_SP_MIN_LEVEL) {
                lvlPenalty *= 0.3;
            }
            double memberXp = XP * lvlPenalty * member2.getLevel() / partyLvlSum;
            double memberSp = SP * lvlPenalty * member2.getLevel() / partyLvlSum;
            memberXp = Math.min(memberXp, xpReward);
            memberSp = Math.min(memberSp, spReward);
            member2.addExpAndCheckBonus(monster, (long) memberXp, (long) memberSp);
        }
        recalculatePartyData();
    }

    public void recalculatePartyData() {
        _partyLvl = 0;
        double rateExp = 0.0;
        double rateSp = 0.0;
        double rateDrop = 0.0;
        double rateAdena = 0.0;
        double rateSpoil = 0.0;
        double minRateExp = Double.MAX_VALUE;
        double minRateSp = Double.MAX_VALUE;
        double minRateDrop = Double.MAX_VALUE;
        double minRateAdena = Double.MAX_VALUE;
        double minRateSpoil = Double.MAX_VALUE;
        int count = 0;
        for (final Player member : _members) {
            final int level = member.getLevel();
            _partyLvl = Math.max(_partyLvl, level);
            count++;
            rateExp += member.getBonus().getRateXp();
            rateSp += member.getBonus().getRateSp();
            rateDrop += member.getBonus().getDropItems();
            rateAdena += member.getBonus().getDropAdena();
            rateSpoil += member.getBonus().getDropSpoil();
            minRateExp = Math.min(minRateExp, member.getBonus().getRateXp());
            minRateSp = Math.min(minRateSp, member.getBonus().getRateSp());
            minRateDrop = Math.min(minRateDrop, member.getBonus().getDropItems());
            minRateAdena = Math.min(minRateAdena, member.getBonus().getDropAdena());
            minRateSpoil = Math.min(minRateSpoil, member.getBonus().getDropSpoil());
        }
        _rateExp = (Config.RATE_PARTY_MIN ? minRateExp : (rateExp / count));
        _rateSp = (Config.RATE_PARTY_MIN ? minRateSp : (rateSp / count));
        _rateDrop = (Config.RATE_PARTY_MIN ? minRateDrop : (rateDrop / count));
        _rateAdena = (Config.RATE_PARTY_MIN ? minRateAdena : (rateAdena / count));
        _rateSpoil = (Config.RATE_PARTY_MIN ? minRateSpoil : (rateSpoil / count));
    }

    public int getLevel() {
        return _partyLvl;
    }

    public int getLootDistribution() {
        return _itemDistribution;
    }

    public boolean isDistributeSpoilLoot() {
        boolean rv = false;
        if (_itemDistribution == 2 || _itemDistribution == 4) {
            rv = true;
        }
        return rv;
    }

    public boolean isInDimensionalRift() {
        return _dimentionalRift > 0 && getDimensionalRift() != null;
    }

    public DimensionalRift getDimensionalRift() {
        return (_dimentionalRift == 0) ? null : ((DimensionalRift) ReflectionManager.getInstance().get(_dimentionalRift));
    }

    public void setDimensionalRift(final DimensionalRift dr) {
        _dimentionalRift = ((dr == null) ? 0 : dr.getId());
    }

    public boolean isInReflection() {
        return _reflection != null || (_commandChannel != null && _commandChannel.isInReflection());
    }

    public Reflection getReflection() {
        if (_reflection != null) {
            return _reflection;
        }
        if (_commandChannel != null) {
            return _commandChannel.getReflection();
        }
        return null;
    }

    public void setReflection(final Reflection reflection) {
        _reflection = reflection;
    }

    public boolean isInCommandChannel() {
        return _commandChannel != null;
    }

    public CommandChannel getCommandChannel() {
        return _commandChannel;
    }

    public void setCommandChannel(final CommandChannel channel) {
        _commandChannel = channel;
    }

    public void Teleport(final int x, final int y, final int z) {
        TeleportParty(getPartyMembers(), new Location(x, y, z));
    }

    public void Teleport(final Location dest) {
        TeleportParty(getPartyMembers(), dest);
    }

    public void Teleport(final Territory territory) {
        RandomTeleportParty(getPartyMembers(), territory);
    }

    public void Teleport(final Territory territory, final Location dest) {
        TeleportParty(getPartyMembers(), territory, dest);
    }

    private void startUpdatePositionTask() {
        if (positionTask == null) {
            positionTask = LazyPrecisionTaskManager.getInstance().scheduleAtFixedRate(new UpdatePositionTask(), 1000L, 1000L);
        }
    }

    private void stopUpdatePositionTask() {
        if (positionTask != null) {
            positionTask.cancel(false);
        }
    }

    public void requestLootChange(final byte type) {
        if (_requestChangeLoot != -1) {
            if (System.currentTimeMillis() <= _requestChangeLootTimer) {
                return;
            }
            finishLootRequest(false);
        }
        _requestChangeLoot = type;
        final int additionalTime = 45000;
        _requestChangeLootTimer = System.currentTimeMillis() + additionalTime;
        _changeLootAnswers = new CopyOnWriteArraySet<>();
        _checkTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ChangeLootCheck(), additionalTime + 1000, 5000L);
        broadcastToPartyMembers(getPartyLeader(), new ExAskModifyPartyLooting(getPartyLeader().getName(), type));
        final SystemMessage sm = new SystemMessage(3135);
        sm.addSystemString(LOOT_SYSSTRINGS[type]);
        getPartyLeader().sendPacket(sm);
    }

    public synchronized void answerLootChangeRequest(final Player member, final boolean answer) {
        if (_requestChangeLoot == -1) {
            return;
        }
        if (_changeLootAnswers.contains(member.getObjectId())) {
            return;
        }
        if (!answer) {
            finishLootRequest(false);
            return;
        }
        _changeLootAnswers.add(member.getObjectId());
        if (_changeLootAnswers.size() >= getMemberCount() - 1) {
            finishLootRequest(true);
        }
    }

    private synchronized void finishLootRequest(final boolean success) {
        if (_requestChangeLoot == -1) {
            return;
        }
        if (_checkTask != null) {
            _checkTask.cancel(false);
            _checkTask = null;
        }
        if (success) {
            broadCast(new ExSetPartyLooting(1, _requestChangeLoot));
            _itemDistribution = _requestChangeLoot;
            final SystemMessage sm = new SystemMessage(3138);
            sm.addSystemString(LOOT_SYSSTRINGS[_requestChangeLoot]);
            broadCast(sm);
        } else {
            broadCast(new ExSetPartyLooting(0, 0));
            broadCast(new SystemMessage(3137));
        }
        _changeLootAnswers = null;
        _requestChangeLoot = -1;
        _requestChangeLootTimer = 0L;
    }

    @Override
    public Iterator<Player> iterator() {
        return _members.iterator();
    }

    private class UpdatePositionTask extends RunnableImpl {
        @Override
        public void runImpl() {
            final List<Player> update = new ArrayList<>();
            _members.forEach(member -> {
                final Location loc = member.getLastPartyPosition();
                if (loc == null || member.getDistance(loc) > 256.0) {
                    member.setLastPartyPosition(member.getLoc());
                    update.add(member);
                }
            });
            if (!update.isEmpty()) {
                _members.forEach(member -> {
                    final PartyMemberPosition pmp = new PartyMemberPosition();
                    for (final Player m : update) {
                        if (m != member) {
                            pmp.add(m);
                        }
                    }
                    if (pmp.size() > 0) {
                        member.sendPacket(pmp);
                    }
                });
            }
            update.clear();
        }
    }

    private class ChangeLootCheck extends RunnableImpl {
        @Override
        public void runImpl() {
            if (System.currentTimeMillis() > _requestChangeLootTimer) {
                finishLootRequest(false);
            }
        }
    }
}
