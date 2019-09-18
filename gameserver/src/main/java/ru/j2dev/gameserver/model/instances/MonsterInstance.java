package ru.j2dev.gameserver.model.instances;

import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.CursedWeaponsManager;
import ru.j2dev.gameserver.model.AggroList.HateInfo;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestEventType;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.model.reward.RewardItem;
import ru.j2dev.gameserver.model.reward.RewardList;
import ru.j2dev.gameserver.model.reward.RewardType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SocialAction;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.Faction;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;

public class MonsterInstance extends NpcInstance {
    private static final int MONSTER_MAINTENANCE_INTERVAL = 1000;
    private final Lock harvestLock;
    private final Lock absorbLock;
    private final Lock sweepLock;
    private final double MIN_DISTANCE_FOR_USE_UD = 200.0;
    private final double MIN_DISTANCE_FOR_CANCEL_UD = 50.0;
    private final double UD_USE_CHANCE = 30.0;
    private final MinionList minionList;
    private ScheduledFuture<?> minionMaintainTask;
    private boolean _isSeeded;
    private int _seederId;
    private boolean _altSeed;
    private RewardItem _harvestItem;
    private int overhitAttackerId;
    private double _overhitDamage;
    private TIntHashSet _absorbersIds;
    private boolean _isSpoiled;
    private int spoilerId;
    private List<RewardItem> _sweepItems;
    private int _isChampion;
    private boolean _thisChampRed;
    private boolean _thisChampBlue;

    public MonsterInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        harvestLock = new ReentrantLock();
        absorbLock = new ReentrantLock();
        sweepLock = new ReentrantLock();
        minionList = new MinionList(this);
    }

    @Override
    public boolean isMovementDisabled() {
        return getNpcId() == 18344 || getNpcId() == 18345 || super.isMovementDisabled();
    }

    @Override
    public boolean isLethalImmune() {
        return getMaxHp() >= 50000 || _isChampion > 0 || getNpcId() == 22215 || getNpcId() == 22216 || getNpcId() == 22217 || super.isLethalImmune();
    }

    @Override
    public boolean isFearImmune() {
        return _isChampion > 0 || super.isFearImmune();
    }

    @Override
    public boolean isParalyzeImmune() {
        return _isChampion > 0 || super.isParalyzeImmune();
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return !attacker.isMonster();
    }

    public int getChampion() {
        return _isChampion;
    }

    public boolean isChampionRed() {
        return _thisChampRed;
    }

    public boolean isChampionBlue() {
        return _thisChampBlue;
    }

    public void setChampion(final int level) {
        if (level == 0) {
            removeSkillById(4407);
            _isChampion = 0;
        } else {
            addSkill(SkillTable.getInstance().getInfo(4407, level));
            _isChampion = level;
        }
    }

    public void setChampion() {
        if (getReflection().canChampions() && canChampion()) {
            final double random = Rnd.nextDouble();
            if (Config.ALT_CHAMPION_CHANCE2 / 100.0 >= random) {
                setChampion(2);
                _thisChampRed = true;
            } else if ((Config.ALT_CHAMPION_CHANCE1 + Config.ALT_CHAMPION_CHANCE2) / 100.0 >= random) {
                setChampion(1);
                _thisChampBlue = true;
            } else {
                _thisChampBlue = false;
                _thisChampRed = false;
                setChampion(0);
            }
        } else {
            setChampion(0);
        }
    }

    public boolean canChampion() {
        return getTemplate().rewardExp > 0L && getTemplate().level >= Config.ALT_CHAMPION_MIN_LEVEL && getTemplate().level <= Config.ALT_CHAMPION_TOP_LEVEL;
    }

    @Override
    public TeamType getTeam() {
        return (getChampion() == 2) ? TeamType.RED : ((getChampion() == 1) ? TeamType.BLUE : TeamType.NONE);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        setCurrentHpMp(getMaxHp(), getMaxMp(), true);
        if (getMinionList().hasMinions()) {
            if (minionMaintainTask != null) {
                minionMaintainTask.cancel(false);
                minionMaintainTask = null;
            }
            minionMaintainTask = ThreadPoolManager.getInstance().schedule(new MinionMaintainTask(), 1000L);
        }
    }

    @Override
    protected void onDespawn() {
        setOverhitDamage(0.0);
        setOverhitAttacker(null);
        clearSweep();
        clearHarvest();
        clearAbsorbers();
        super.onDespawn();
    }

    @Override
    public MinionList getMinionList() {
        return minionList;
    }

    public Location getMinionPosition() {
        return Location.findPointToStay(this, 100, 150);
    }

    public void notifyMinionDied(final MinionInstance minion) {
    }

    public void spawnMinion(final MonsterInstance minion) {
        minion.setReflection(getReflection());
        if (getChampion() == 2) {
            minion.setChampion(1);
        } else {
            minion.setChampion(0);
        }
        minion.setHeading(getHeading());
        minion.setCurrentHpMp(minion.getMaxHp(), minion.getMaxMp(), true);
        minion.spawnMe(getMinionPosition());
    }

    @Override
    public boolean hasMinions() {
        return getMinionList().hasMinions();
    }

    @Override
    public void setReflection(final Reflection reflection) {
        super.setReflection(reflection);
        if (hasMinions()) {
            for (final MinionInstance m : getMinionList().getAliveMinions()) {
                m.setReflection(reflection);
            }
        }
    }

    @Override
    protected void onDelete() {
        if (minionMaintainTask != null) {
            minionMaintainTask.cancel(false);
            minionMaintainTask = null;
        }
        getMinionList().deleteMinions();
        super.onDelete();
    }

    @Override
    protected void onDeath(final Creature killer) {
        if (minionMaintainTask != null) {
            minionMaintainTask.cancel(false);
            minionMaintainTask = null;
        }
        calculateRewards(killer);
        super.onDeath(killer);
    }

    @Override
    protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        if (skill != null && skill.isOverhit()) {
            final double overhitDmg = (getCurrentHp() - damage) * -1.0;
            if (overhitDmg <= 0.0) {
                setOverhitDamage(0.0);
                setOverhitAttacker(null);
            } else {
                setOverhitDamage(overhitDmg);
                setOverhitAttacker(attacker);
            }
        }
        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }

    private int getPlayersDamage(final Iterable<Player> players, final Map<Playable, HateInfo> hateMap) {
        int damage = 0;
        for (final Player player : players) {
            if (player.isDead()) {
                continue;
            }
            final HateInfo hateInfo = hateMap.get(player);
            if (hateInfo == null) {
                continue;
            }
            if (hateInfo.damage <= 1) {
                continue;
            }
            damage += hateInfo.damage;
            final Summon playerSummon;
            if ((playerSummon = player.getPet()) == null) {
                continue;
            }
            final HateInfo summonHateInfo = hateMap.get(playerSummon);
            if (summonHateInfo == null) {
                continue;
            }
            damage += summonHateInfo.damage;
        }
        return damage;
    }

    private Creature getTopDamager(final Iterable<? extends PlayerGroup> playerGroups, final Map<Playable, HateInfo> hateMap) {
        if (hateMap.isEmpty()) {
            return null;
        }
        int topDamage = Integer.MIN_VALUE;
        PlayerGroup topPlayerGroup = null;
        for (final PlayerGroup playerGroup : playerGroups) {
            if (playerGroup != topPlayerGroup) {
                final int playerGroupDamage = getPlayersDamage(playerGroup, hateMap);
                if (playerGroupDamage <= topDamage) {
                    continue;
                }
                topDamage = playerGroupDamage;
                topPlayerGroup = playerGroup;
            }
        }
        if (topPlayerGroup == null) {
            return null;
        }
        if (topPlayerGroup instanceof Player) {
            return (Player) topPlayerGroup;
        }
        if (topPlayerGroup instanceof Party) {
            final Party party = (Party) topPlayerGroup;
            return getTopDamager(party.getPartyMembers(), hateMap);
        }
        if (topPlayerGroup instanceof CommandChannel) {
            final CommandChannel commandChannel = (CommandChannel) topPlayerGroup;
            return getTopDamager(commandChannel.getParties(), hateMap);
        }
        return null;
    }

    private Creature getTopDamager(final Map<Playable, HateInfo> hateMap) {
        final Set<PlayerGroup> players = new HashSet<>();
        for (final Playable playable : hateMap.keySet()) {
            if (playable instanceof Player) {
                players.add(((Player) playable).getPlayerGroup());
            }
        }
        return getTopDamager(players, hateMap);
    }

    protected Creature getTopDamager() {
        return getTopDamager(getAggroList().getPlayableMap());
    }

    public void calculateRewards(Creature lastAttacker) {
        final Creature topDamager = getTopDamager();
        if (lastAttacker == null || !lastAttacker.isPlayable()) {
            lastAttacker = topDamager;
        }
        if (lastAttacker == null || !lastAttacker.isPlayable()) {
            return;
        }
        final Player killer = lastAttacker.getPlayer();
        if (killer == null) {
            return;
        }
        final Map<Playable, HateInfo> aggroMap = getAggroList().getPlayableMap();
        final Quest[] quests = getTemplate().getEventQuests(QuestEventType.MOB_KILLED_WITH_QUEST);
        if (quests != null && quests.length > 0) {
            List<Player> players = null;
            if (isRaid() && Config.ALT_NO_LASTHIT) {
                players = new ArrayList<>();
                for (final Playable pl : aggroMap.keySet()) {
                    if (!pl.isDead() && (isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl, Config.ALT_PARTY_DISTRIBUTION_RANGE)) && !players.contains(pl.getPlayer())) {
                        players.add(pl.getPlayer());
                    }
                }
            } else if (killer.getParty() != null) {
                players = new ArrayList<>(killer.getParty().getMemberCount());
                for (final Player pl2 : killer.getParty().getPartyMembers()) {
                    if (!pl2.isDead() && (isInRangeZ(pl2, Config.ALT_PARTY_DISTRIBUTION_RANGE) || killer.isInRangeZ(pl2, Config.ALT_PARTY_DISTRIBUTION_RANGE))) {
                        players.add(pl2);
                    }
                }
            }
            for (final Quest quest : quests) {
                Player toReward = killer;
                if (quest.getParty() != 0 && players != null) {
                    if (isRaid() || quest.getParty() == 2) {
                        for (final Player pl3 : players) {
                            final QuestState qs = pl3.getQuestState(quest.getName());
                            if (qs != null && !qs.isCompleted()) {
                                quest.notifyKill(this, qs);
                            }
                        }
                        toReward = null;
                    } else {
                        final List<Player> interested = new ArrayList<>(players.size());
                        for (final Player pl4 : players) {
                            final QuestState qs2 = pl4.getQuestState(quest.getName());
                            if (qs2 != null && !qs2.isCompleted()) {
                                interested.add(pl4);
                            }
                        }

                        if (interested.isEmpty()) {
                            continue;
                        }
                        toReward = interested.get(Rnd.get(interested.size()));
                        if (toReward == null) {
                            toReward = killer;
                        }
                    }
                }
                if (toReward != null) {
                    final QuestState qs3 = toReward.getQuestState(quest.getName());
                    if (qs3 != null && !qs3.isCompleted()) {
                        quest.notifyKill(this, qs3);
                    }
                }
            }
        }
        final Map<Player, RewardInfo> rewards = new HashMap<>();
        for (final HateInfo info : aggroMap.values()) {
            if (info.damage <= 1) {
                continue;
            }
            final Playable attacker = (Playable) info.attacker;
            final Player player = attacker.getPlayer();
            final RewardInfo reward = rewards.get(player);
            if (reward == null) {
                rewards.put(player, new RewardInfo(player, info.damage));
            } else {
                reward.addDamage(info.damage);
            }
        }
        final Player[] attackers = rewards.keySet().toArray(new Player[0]);
        double[] xpsp = new double[2];
        for (final Player attacker2 : attackers) {
            if (!attacker2.isDead()) {
                final RewardInfo reward2 = rewards.get(attacker2);
                if (reward2 != null) {
                    final Party party = attacker2.getParty();
                    final int maxHp = getMaxHp();
                    xpsp[1] = (xpsp[0] = 0.0);
                    if (party == null) {
                        final int damage = Math.min(reward2._dmg, maxHp);
                        if (damage > 0) {
                            if (isInRangeZ(attacker2, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                                xpsp = calculateExpAndSp(attacker2.getLevel(), damage);
                            }
                            xpsp[0] = applyOverhit(killer, xpsp[0]);
                            attacker2.addExpAndCheckBonus(this, (long) xpsp[0], (long) xpsp[1]);
                        }
                        rewards.remove(attacker2);
                    } else {
                        int partyDmg = 0;
                        int partyMaxLevel = 1;
                        final List<Player> rewardedMembers = new ArrayList<>();
                        for (final Player partyMember : party.getPartyMembers()) {
                            final RewardInfo ai = rewards.remove(partyMember);
                            if (!partyMember.isDead()) {
                                if (!isInRangeZ(partyMember, Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                                    continue;
                                }
                                if (ai != null) {
                                    partyDmg += ai._dmg;
                                }
                                rewardedMembers.add(partyMember);
                                if (partyMember.getLevel() <= partyMaxLevel) {
                                    continue;
                                }
                                partyMaxLevel = partyMember.getLevel();
                            }
                        }
                        partyDmg = Math.min(partyDmg, maxHp);
                        if (partyDmg > 0) {
                            xpsp = calculateExpAndSp(partyMaxLevel, partyDmg);
                            final double partyMul = partyDmg / maxHp;
                            final double[] array3 = xpsp;
                            final int n = 0;
                            array3[n] *= partyMul;
                            final double[] array4 = xpsp;
                            final int n2 = 1;
                            array4[n2] *= partyMul;
                            party.distributeXpAndSp(xpsp[0] = applyOverhit(killer, xpsp[0]), xpsp[1], rewardedMembers, lastAttacker, this);
                        }
                    }
                }
            }
        }
        CursedWeaponsManager.getInstance().dropAttackable(this, killer);
        if (topDamager == null || !topDamager.isPlayable()) {
            return;
        }
        for (final Entry<RewardType, RewardList> entry : getTemplate().getRewards().entrySet()) {
            rollRewards(entry, lastAttacker, topDamager);
        }
        IntStream.range(0, Config.ALT_CHAMPION_DROP_ITEM_ID.length).
                filter(i -> getChampion() > 0 && Config.ALT_CHAMPION_DROP_ITEM_ID[i] > 0 && Math.abs(getLevel() - topDamager.getLevel()) < 9 && Rnd.chance(Config.ALT_CHAMPION_DROP_CHANCE[i]) && topDamager.getPlayer() != null).
                forEach(i -> dropItem(topDamager.getPlayer(), Config.ALT_CHAMPION_DROP_ITEM_ID[i], Config.ALT_CHAMPION_DROP_COUNT[i]));
    }

    @Override
    public void onRandomAnimation() {
        if (System.currentTimeMillis() - _lastSocialAction > 10000L) {
            broadcastPacket(new SocialAction(getObjectId(), 1));
            _lastSocialAction = System.currentTimeMillis();
        }
    }

    @Override
    public void startRandomAnimation() {
    }

    @Override
    public int getKarma() {
        return 0;
    }

    public void addAbsorber(final Player attacker) {
        if (attacker == null) {
            return;
        }
        if (getCurrentHpPercents() > 50.0) {
            return;
        }
        absorbLock.lock();
        try {
            if (_absorbersIds == null) {
                _absorbersIds = new TIntHashSet();
            }
            _absorbersIds.add(attacker.getObjectId());
        } finally {
            absorbLock.unlock();
        }
    }

    public boolean isAbsorbed(final Player player) {
        absorbLock.lock();
        try {
            if (_absorbersIds == null) {
                return false;
            }
            if (!_absorbersIds.contains(player.getObjectId())) {
                return false;
            }
        } finally {
            absorbLock.unlock();
        }
        return true;
    }

    public void clearAbsorbers() {
        absorbLock.lock();
        try {
            if (_absorbersIds != null) {
                _absorbersIds.clear();
            }
        } finally {
            absorbLock.unlock();
        }
    }

    public RewardItem takeHarvest() {
        harvestLock.lock();
        try {
            final RewardItem harvest = _harvestItem;
            clearHarvest();
            return harvest;
        } finally {
            harvestLock.unlock();
        }
    }

    public void clearHarvest() {
        harvestLock.lock();
        try {
            _harvestItem = null;
            _altSeed = false;
            _seederId = 0;
            _isSeeded = false;
        } finally {
            harvestLock.unlock();
        }
    }

    public boolean setSeeded(final Player player, final int seedId, final boolean altSeed) {
        harvestLock.lock();
        try {
            if (isSeeded()) {
                return false;
            }
            _isSeeded = true;
            _altSeed = altSeed;
            _seederId = player.getObjectId();
            _harvestItem = new RewardItem(Manor.getInstance().getCropType(seedId));
            if (getTemplate().rateHp > 1.0) {
                _harvestItem.count = Rnd.get(Math.round(getTemplate().rateHp), Math.round(1.5 * getTemplate().rateHp));
            }
        } finally {
            harvestLock.unlock();
        }
        return true;
    }

    public boolean isSeeded(final Player player) {
        return isSeeded() && _seederId == player.getObjectId() && getDeadTime() < 20000L;
    }

    public boolean isSeeded() {
        return _isSeeded;
    }

    public boolean isSpoiled() {
        return _isSpoiled;
    }

    public boolean isSpoiled(final Player player) {
        if (!isSpoiled()) {
            return false;
        }
        if (player.getObjectId() == spoilerId && getDeadTime() < 20000L) {
            return true;
        }
        if (player.isInParty()) {
            for (final Player pm : player.getParty().getPartyMembers()) {
                if (pm.getObjectId() == spoilerId && getDistance(pm) < Config.ALT_PARTY_DISTRIBUTION_RANGE) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean setSpoiled(final Player player) {
        sweepLock.lock();
        try {
            if (isSpoiled()) {
                return false;
            }
            _isSpoiled = true;
            spoilerId = player.getObjectId();
        } finally {
            sweepLock.unlock();
        }
        return true;
    }

    public boolean isSweepActive() {
        sweepLock.lock();
        try {
            return _sweepItems != null && _sweepItems.size() > 0;
        } finally {
            sweepLock.unlock();
        }
    }

    public List<RewardItem> takeSweep() {
        sweepLock.lock();
        try {
            final List<RewardItem> sweep = _sweepItems;
            clearSweep();
            return sweep;
        } finally {
            sweepLock.unlock();
        }
    }

    public void clearSweep() {
        sweepLock.lock();
        try {
            _isSpoiled = false;
            spoilerId = 0;
            _sweepItems = null;
        } finally {
            sweepLock.unlock();
        }
    }

    public void rollRewards(final Entry<RewardType, RewardList> entry, final Creature lastAttacker, final Creature topDamager) {
        final RewardType type = entry.getKey();
        final RewardList list = entry.getValue();
        if (type == RewardType.SWEEP && !isSpoiled()) {
            return;
        }
        final Creature activeChar = (type == RewardType.SWEEP) ? lastAttacker : topDamager;
        final Player activePlayer = activeChar.getPlayer();
        if (activePlayer == null) {
            return;
        }
        final int diff = calculateLevelDiffForDrop(topDamager.getLevel());
        double mod = calcStat(Stats.ITEM_REWARD_MULTIPLIER, 1.0, activeChar, null);
        mod *= Experience.penaltyModifier(diff, 9.0);
        final List<RewardItem> rewardItems = list.roll(activePlayer, mod, this instanceof RaidBossInstance);
        switch (type) {
            case SWEEP: {
                _sweepItems = rewardItems;
                break;
            }
            default: {
                for (final RewardItem drop : rewardItems) {
                    if (isSeeded() && !_altSeed && !drop.isAdena && !drop.isSealStone) {
                        continue;
                    }
                    // Конфиг на дроп только определённых итемов
                    if (!Config.DROP_ONLY_THIS.isEmpty() && !Config.DROP_ONLY_THIS.contains(drop.itemId) && (!Config.INCLUDE_RAID_DROP || !isRaid())) {
                        continue;
                    }
                    if (!Config.DROP_WHITHOUT_THIS.isEmpty() && Config.DROP_WHITHOUT_THIS.contains(drop.itemId) && (!Config.INCLUDE_WHITHOUT_RAID_DROP || !isRaid())) {
                        continue;
                    }
                    dropItem(activePlayer, drop.itemId, drop.count);
                }
                break;
            }
        }
    }

    private double[] calculateExpAndSp(final int level, final long damage) {
        final int diff = level - getLevel();
        double xp = getExpReward() * damage / getMaxHp();
        double sp = getSpReward() * damage / getMaxHp();
        if (Config.EXP_SP_DIFF_LIMIT != 0 && Math.abs(diff) > Config.EXP_SP_DIFF_LIMIT) {
            xp = 0.0;
            sp = 0.0;
        }
        if (diff > Config.THRESHOLD_LEVEL_DIFF) {
            final double mod = Math.pow(0.83, diff - 5);
            xp *= mod;
            sp *= mod;
        }
        xp = Math.max(0.0, xp);
        sp = Math.max(0.0, sp);
        return new double[]{xp, sp};
    }

    private double applyOverhit(final Player killer, double xp) {
        if (xp > 0.0 && killer.getObjectId() == overhitAttackerId) {
            final int overHitExp = calculateOverhitExp(xp);
            killer.sendPacket(Msg.OVER_HIT, new SystemMessage(362).addNumber(overHitExp));
            xp += overHitExp;
        }
        return xp;
    }

    @Override
    public void setOverhitAttacker(final Creature attacker) {
        overhitAttackerId = ((attacker == null) ? 0 : attacker.getObjectId());
    }

    public double getOverhitDamage() {
        return _overhitDamage;
    }

    @Override
    public void setOverhitDamage(final double damage) {
        _overhitDamage = damage;
    }

    public int calculateOverhitExp(final double normalExp) {
        double overhitPercentage = getOverhitDamage() * 100.0 / getMaxHp();
        if (overhitPercentage > 25.0) {
            overhitPercentage = 25.0;
        }
        final double overhitExp = overhitPercentage / 100.0 * normalExp;
        setOverhitAttacker(null);
        setOverhitDamage(0.0);
        return (int) Math.round(overhitExp);
    }

    @Override
    public boolean isAggressive() {
        return (Config.ALT_CHAMPION_CAN_BE_AGGRO || getChampion() == 0) && super.isAggressive();
    }

    @Override
    public Faction getFaction() {
        return (Config.ALT_CHAMPION_CAN_BE_SOCIAL || getChampion() == 0) ? super.getFaction() : Faction.NONE;
    }

    @Override
    public void reduceCurrentHp(final double i, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        checkUD(attacker, i);
        super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    private void checkUD(final Creature attacker, final double damage) {
        if (getTemplate().getBaseAtkRange() > 200.0 || getLevel() < 20 || getLevel() > 78 || attacker.getLevel() - getLevel() > 9 || getLevel() - attacker.getLevel() > 9) {
            return;
        }
        if (isMinion() || getMinionList() != null || isRaid() || this instanceof ReflectionBossInstance || this instanceof ChestInstance || getChampion() > 0) {
            return;
        }
        final int skillId = 5044;
        int skillLvl = 1;
        if (getLevel() >= 41 || getLevel() <= 60) {
            skillLvl = 2;
        } else if (getLevel() > 60) {
            skillLvl = 3;
        }
        final double distance = getDistance(attacker);
        if (distance <= 50.0) {
            if (getEffectList() != null && getEffectList().getEffectsBySkillId(skillId) != null) {
                for (final Effect e : getEffectList().getEffectsBySkillId(skillId)) {
                    e.exit();
                }
            }
        } else if (distance >= 200.0) {
            final double chance = 30.0 / (getMaxHp() / damage);
            if (Rnd.chance(chance)) {
                final Skill skill = SkillTable.getInstance().getInfo(skillId, skillLvl);
                if (skill != null) {
                    skill.getEffects(this, this, false, false);
                }
            }
        }
    }

    @Override
    public boolean isMonster() {
        return true;
    }

    @Override
    public Clan getClan() {
        return null;
    }

    @Override
    public boolean isInvul() {
        return _isInvul;
    }

    protected static final class RewardInfo {
        protected final Creature _attacker;
        protected int _dmg;

        public RewardInfo(final Creature attacker, final int dmg) {
            _dmg = 0;
            _attacker = attacker;
            _dmg = dmg;
        }

        public void addDamage(int dmg) {
            if (dmg < 0) {
                dmg = 0;
            }
            _dmg += dmg;
        }

        @Override
        public int hashCode() {
            return _attacker.getObjectId();
        }
    }

    public class MinionMaintainTask extends RunnableImpl {
        @Override
        public void runImpl() {
            if (isDead()) {
                return;
            }
            getMinionList().spawnMinions();
        }
    }
}
