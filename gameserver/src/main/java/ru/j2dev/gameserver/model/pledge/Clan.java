package ru.j2dev.gameserver.model.pledge;

import com.google.common.collect.Iterators;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.CrestCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ResidenceHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.residence.Castle;
import ru.j2dev.gameserver.model.entity.residence.ResidenceType;
import ru.j2dev.gameserver.model.items.ClanWarehouse;
import ru.j2dev.gameserver.network.lineage2.components.IStaticPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.tables.ClanTable;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Log;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.stream.Collectors;

public class Clan implements Iterable<UnitMember> {
    public static final int CP_NOTHING = 0x0;
    public static final int CP_CL_INVITE_CLAN = 0x2;
    public static final int CP_CL_MANAGE_TITLES = 0x4;
    public static final int CP_CL_WAREHOUSE_SEARCH = 0x8;
    public static final int CP_CL_MANAGE_RANKS = 0x10;
    public static final int CP_CL_CLAN_WAR = 0x20;
    public static final int CP_CL_DISMISS = 0x40;
    public static final int CP_CL_EDIT_CREST = 0x80;
    public static final int CP_CL_APPRENTICE = 0x100;
    public static final int CP_CL_TROOPS_FAME = 0x200;
    public static final int CP_CH_ENTRY_EXIT = 0x400;
    public static final int CP_CH_USE_FUNCTIONS = 0x800;
    public static final int CP_CH_AUCTION = 0x1000;
    public static final int CP_CH_DISMISS = 0x2000;
    public static final int CP_CH_SET_FUNCTIONS = 0x4000;
    public static final int CP_CS_ENTRY_EXIT = 0x8000;
    public static final int CP_CS_MANOR_ADMIN = 0x10000;
    public static final int CP_CS_MANAGE_SIEGE = 0x20000;
    public static final int CP_CS_USE_FUNCTIONS = 0x40000;
    public static final int CP_CS_DISMISS = 0x80000;
    public static final int CP_CS_TAXES = 0x100000;
    public static final int CP_CS_MERCENARIES = 0x200000;
    public static final int CP_CS_SET_FUNCTIONS = 0x400000;
    public static final int CP_ALL = 0x7ffffe;
    public static final int RANK_FIRST = 0x1;
    public static final int RANK_LAST = 0x9;
    public static final int SUBUNIT_NONE = 0xffffff80;
    public static final int SUBUNIT_ACADEMY = 0xffffffff;
    public static final int SUBUNIT_MAIN_CLAN = 0x0;
    public static final int SUBUNIT_ROYAL1 = 0x64;
    public static final int SUBUNIT_ROYAL2 = 0xc8;
    public static final int SUBUNIT_KNIGHT1 = 0x3e9;
    public static final int SUBUNIT_KNIGHT2 = 0x3ea;
    public static final int SUBUNIT_KNIGHT3 = 0x7d1;
    public static final int SUBUNIT_KNIGHT4 = 0x7d2;
    private static final Logger LOGGER = LoggerFactory.getLogger(Clan.class);
    private static final ClanReputationComparator REPUTATION_COMPARATOR = new ClanReputationComparator();
    private static final int REPUTATION_PLACES = 0x64;
    public static long DISBAND_PENALTY = 604800000L;
    public static long DISBAND_TIME = 172800000L;
    protected final ConcurrentMap<Integer, Skill> _skills = new ConcurrentSkipListMap<>();
    protected final ConcurrentMap<Integer, RankPrivs> _privs = new ConcurrentSkipListMap<>();
    protected final ConcurrentMap<Integer, SubUnit> _subUnits = new ConcurrentSkipListMap<>();
    private final int _clanId;
    private final ClanWarehouse _warehouse;
    private final List<Clan> _atWarWith = new ArrayList<>();
    private final List<Clan> _underAttackFrom = new ArrayList<>();
    private int _allyId;
    private int _level;
    private int _hasCastle;
    private int _hasHideout;
    private int _crestId;
    private int _crestLargeId;
    private long _expelledMemberTime;
    private long _leavedAllyTime;
    private long _dissolvedAllyTime;
    private long _disbandEndTime;
    private long _disbandPenaltyTime;
    private int _whBonus = -1;
    private String _notice;
    private int _reputation;

    public Clan(final int clanId) {
        _clanId = clanId;
        InitializePrivs();
        (_warehouse = new ClanWarehouse(this)).restore();
    }

    public static Clan restore(final int clanId) {
        if (clanId == 0) {
            return null;
        }
        Clan clan = null;
        Connection con1 = null;
        PreparedStatement statement1 = null;
        ResultSet clanData = null;
        try {
            con1 = DatabaseFactory.getInstance().getConnection();
            statement1 = con1.prepareStatement("SELECT clan_level,hasCastle,hasHideout,ally_id,reputation_score,expelled_member,leaved_ally,dissolved_ally,warehouse,disband_end,disband_penalty FROM clan_data where clan_id=?");
            statement1.setInt(1, clanId);
            clanData = statement1.executeQuery();
            if (!clanData.next()) {
                LOGGER.warn("Clan " + clanId + " doesnt exists!");
                return null;
            }
            clan = new Clan(clanId);
            clan.setLevel(clanData.getInt("clan_level"));
            clan.setHasCastle(clanData.getInt("hasCastle"));
            clan.setHasHideout(clanData.getInt("hasHideout"));
            clan.setAllyId(clanData.getInt("ally_id"));
            clan._reputation = clanData.getInt("reputation_score");
            clan.setExpelledMemberTime(clanData.getLong("expelled_member") * 1000L);
            clan.setLeavedAllyTime(clanData.getLong("leaved_ally") * 1000L);
            clan.setDissolvedAllyTime(clanData.getLong("dissolved_ally") * 1000L);
            clan.setWhBonus(clanData.getInt("warehouse"));
            clan.setDisbandEndTime(clanData.getLong("disband_end") * 1000L);
            clan.setDisbandPenaltyTime(clanData.getLong("disband_penalty") * 1000L);
        } catch (Exception e) {
            LOGGER.error("Error while restoring clan!", e);
        } finally {
            DbUtils.closeQuietly(con1, statement1, clanData);
        }
        if (clan == null) {
            LOGGER.warn("Clan " + clanId + " does't exist");
            return null;
        }
        clan.restoreSkills();
        clan.restoreSubPledges();
        clan.getAllSubUnits().forEach(unit -> {
            unit.restore();
            unit.restoreSkills();
        });
        clan.restoreRankPrivs();
        clan.setCrestId(CrestCache.getInstance().getPledgeCrestId(clanId));
        clan.setCrestLargeId(CrestCache.getInstance().getPledgeCrestLargeId(clanId));
        return clan;
    }

    public static boolean isAcademy(final int pledgeType) {
        return pledgeType == -1;
    }

    public static boolean isRoyalGuard(final int pledgeType) {
        return pledgeType == 100 || pledgeType == 200;
    }

    public static boolean isOrderOfKnights(final int pledgeType) {
        return pledgeType == 1001 || pledgeType == 1002 || pledgeType == 2001 || pledgeType == 2002;
    }

    public int getClanId() {
        return _clanId;
    }

    public int getLeaderId() {
        return getLeaderId(SUBUNIT_MAIN_CLAN);
    }

    public UnitMember getLeader() {
        return getLeader(SUBUNIT_MAIN_CLAN);
    }

    public String getLeaderName() {
        return getLeaderName(SUBUNIT_MAIN_CLAN);
    }

    public String getName() {
        return getUnitName(SUBUNIT_MAIN_CLAN);
    }

    public UnitMember getAnyMember(final int id) {
        return getAllSubUnits().stream().map(unit -> unit.getUnitMember(id)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public UnitMember getAnyMember(final String name) {
        return getAllSubUnits().stream().map(unit -> unit.getUnitMember(name)).filter(Objects::nonNull).findFirst().orElse(null);
    }

    public int getAllSize() {
        return getAllSubUnits().stream().mapToInt(SubUnit::size).sum();
    }

    public String getUnitName(final int unitType) {
        if (unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)) {
            return "";
        }
        return getSubUnit(unitType).getName();
    }

    public String getLeaderName(final int unitType) {
        if (unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)) {
            return "";
        }
        return getSubUnit(unitType).getLeaderName();
    }

    public int getLeaderId(final int unitType) {
        if (unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)) {
            return 0;
        }
        return getSubUnit(unitType).getLeaderObjectId();
    }

    public UnitMember getLeader(final int unitType) {
        if (unitType == SUBUNIT_NONE || !_subUnits.containsKey(unitType)) {
            return null;
        }
        return getSubUnit(unitType).getLeader();
    }

    public void flush() {
        for (final UnitMember member : this) {
            removeClanMember(member.getObjectId());
        }
        _warehouse.writeLock();
        try {
            _warehouse.getItems().forEach(_warehouse::destroyItem);
        } finally {
            _warehouse.writeUnlock();
        }
        if (_hasCastle != 0) {
            ResidenceHolder.getInstance().getResidence(Castle.class, _hasCastle).changeOwner(null);
        }
    }

    public void removeClanMember(final int id) {
        if (id == getLeaderId(SUBUNIT_MAIN_CLAN)) {
            return;
        }
        getAllSubUnits().stream().filter(unit -> unit.isUnitMember(id)).findFirst().ifPresent(unit -> removeClanMember(unit.getType(), id));
    }

    public void removeClanMember(final int subUnitId, final int objectId) {
        final SubUnit subUnit = getSubUnit(subUnitId);
        if (subUnit == null) {
            return;
        }
        subUnit.removeUnitMember(objectId);
    }

    public List<UnitMember> getAllMembers() {
        final Collection<SubUnit> units = getAllSubUnits();
        int size = units.stream().mapToInt(SubUnit::size).sum();
        final List<UnitMember> members = new ArrayList<>(size);
        units.stream().map(SubUnit::getUnitMembers).forEach(members::addAll);
        return members;
    }

    public List<Player> getOnlineMembers(final int exclude) {
        final List<Player> result = new ArrayList<>(getAllSize() - 1);
        for (final UnitMember temp : this) {
            if (temp != null && temp.isOnline() && temp.getObjectId() != exclude) {
                result.add(temp.getPlayer());
            }
        }
        return result;
    }

    public int getAllyId() {
        return _allyId;
    }

    public void setAllyId(final int allyId) {
        _allyId = allyId;
    }

    public int getLevel() {
        return _level;
    }

    public void setLevel(final int level) {
        _level = level;
    }

    public int getCastle() {
        return _hasCastle;
    }

    public int getHasHideout() {
        return _hasHideout;
    }

    public void setHasHideout(final int hasHideout) {
        _hasHideout = hasHideout;
    }

    public int getResidenceId(final ResidenceType r) {
        switch (r) {
            case Castle: {
                return _hasCastle;
            }
            case ClanHall: {
                return _hasHideout;
            }
            default: {
                return 0;
            }
        }
    }

    public void setHasCastle(final int castle) {
        _hasCastle = castle;
    }

    public boolean isAnyMember(final int id) {
        return getAllSubUnits().stream().anyMatch(unit -> unit.isUnitMember(id));
    }

    public void updateClanInDB() {
        if (getLeaderId() == 0) {
            LOGGER.warn("updateClanInDB with empty LeaderId");
            Thread.dumpStack();
            return;
        }
        if (getClanId() == 0) {
            LOGGER.warn("updateClanInDB with empty ClanId");
            Thread.dumpStack();
            return;
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE clan_data SET ally_id=?,reputation_score=?,expelled_member=?,leaved_ally=?,dissolved_ally=?,clan_level=?,warehouse=?,disband_end=?,disband_penalty=? WHERE clan_id=?");
            statement.setInt(1, getAllyId());
            statement.setInt(2, getReputationScore());
            statement.setLong(3, getExpelledMemberTime() / 1000L);
            statement.setLong(4, getLeavedAllyTime() / 1000L);
            statement.setLong(5, getDissolvedAllyTime() / 1000L);
            statement.setInt(6, _level);
            statement.setInt(7, getWhBonus());
            statement.setInt(8, (int) (getDisbandEndTime() / 1000L));
            statement.setInt(9, (int) (getDisbandPenaltyTime() / 1000L));
            statement.setInt(10, getClanId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("error while updating clan '" + getClanId() + "' data in db");
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void store() {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("INSERT INTO clan_data (clan_id,clan_level,hasCastle,hasHideout,ally_id,expelled_member,leaved_ally,dissolved_ally) values (?,?,?,?,?,?,?,?)");
            statement.setInt(1, _clanId);
            statement.setInt(2, _level);
            statement.setInt(3, _hasCastle);
            statement.setInt(4, _hasHideout);
            statement.setInt(5, _allyId);
            statement.setLong(6, getExpelledMemberTime() / 1000L);
            statement.setLong(7, getLeavedAllyTime() / 1000L);
            statement.setLong(8, getDissolvedAllyTime() / 1000L);
            statement.execute();
            DbUtils.close(statement);
            final SubUnit mainSubUnit = _subUnits.get(0);
            statement = con.prepareStatement("INSERT INTO clan_subpledges (clan_id, type, leader_id, name) VALUES (?,?,?,?)");
            statement.setInt(1, _clanId);
            statement.setInt(2, mainSubUnit.getType());
            statement.setInt(3, mainSubUnit.getLeaderObjectId());
            statement.setString(4, mainSubUnit.getName());
            statement.execute();
            DbUtils.close(statement);
            statement = con.prepareStatement("UPDATE characters SET clanid=?,pledge_type=? WHERE obj_Id=?");
            statement.setInt(1, getClanId());
            statement.setInt(2, mainSubUnit.getType());
            statement.setInt(3, getLeaderId());
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("Exception: " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void broadcastToOnlineMembers(final IStaticPacket... packets) {
        for (final UnitMember member : this) {
            if (member.isOnline()) {
                member.getPlayer().sendPacket(packets);
            }
        }
    }

    public void broadcastToOnlineMembers(final L2GameServerPacket... packets) {
        for (final UnitMember member : this) {
            if (member.isOnline()) {
                member.getPlayer().sendPacket((IStaticPacket[]) packets);
            }
        }
    }

    public void broadcastToOtherOnlineMembers(final L2GameServerPacket packet, final Player player) {
        for (final UnitMember member : this) {
            if (member.isOnline() && member.getPlayer() != player) {
                member.getPlayer().sendPacket(packet);
            }
        }
    }

    @Override
    public String toString() {
        return getName();
    }

    public int getCrestId() {
        return _crestId;
    }

    public void setCrestId(final int newcrest) {
        _crestId = newcrest;
    }

    public boolean hasCrest() {
        return _crestId > 0;
    }

    public int getCrestLargeId() {
        return _crestLargeId;
    }

    public void setCrestLargeId(final int newcrest) {
        _crestLargeId = newcrest;
    }

    public boolean hasCrestLarge() {
        return _crestLargeId > 0;
    }

    public long getAdenaCount() {
        return _warehouse.getCountOfAdena();
    }

    public ClanWarehouse getWarehouse() {
        return _warehouse;
    }

    public int isAtWar() {
        if (_atWarWith != null && !_atWarWith.isEmpty()) {
            return 1;
        }
        return 0;
    }

    public int isAtWarOrUnderAttack() {
        if ((_atWarWith != null && !_atWarWith.isEmpty()) || (_underAttackFrom != null && !_underAttackFrom.isEmpty())) {
            return 1;
        }
        return 0;
    }

    public boolean isAtWarWith(final int id) {
        final Clan clan = ClanTable.getInstance().getClan(id);
        return _atWarWith != null && !_atWarWith.isEmpty() && _atWarWith.contains(clan);
    }

    public boolean isUnderAttackFrom(final int id) {
        final Clan clan = ClanTable.getInstance().getClan(id);
        return _underAttackFrom != null && !_underAttackFrom.isEmpty() && _underAttackFrom.contains(clan);
    }

    public void setEnemyClan(final Clan clan) {
        _atWarWith.add(clan);
    }

    public void deleteEnemyClan(final Clan clan) {
        _atWarWith.remove(clan);
    }

    public void setAttackerClan(final Clan clan) {
        _underAttackFrom.add(clan);
    }

    public void deleteAttackerClan(final Clan clan) {
        _underAttackFrom.remove(clan);
    }

    public List<Clan> getEnemyClans() {
        return _atWarWith;
    }

    public int getWarsCount() {
        return _atWarWith.size();
    }

    public List<Clan> getAttackerClans() {
        return _underAttackFrom;
    }

    public void broadcastClanStatus(final boolean updateList, final boolean needUserInfo, final boolean relation) {
        final List<L2GameServerPacket> listAll = updateList ? listAll() : null;
        final PledgeShowInfoUpdate update = new PledgeShowInfoUpdate(this);
        for (final UnitMember member : this) {
            if (member.isOnline()) {
                if (updateList) {
                    member.getPlayer().sendPacket(PledgeShowMemberListDeleteAll.STATIC);
                    member.getPlayer().sendPacket(listAll);
                }
                member.getPlayer().sendPacket(update);
                if (needUserInfo) {
                    member.getPlayer().broadcastCharInfo();
                }
                if (!relation) {
                    continue;
                }
                member.getPlayer().broadcastRelationChanged();
            }
        }
    }

    public Alliance getAlliance() {
        return (_allyId == 0) ? null : ClanTable.getInstance().getAlliance(_allyId);
    }

    public long getExpelledMemberTime() {
        return _expelledMemberTime;
    }

    public void setExpelledMemberTime(final long time) {
        _expelledMemberTime = time;
    }

    public void setExpelledMember() {
        _expelledMemberTime = System.currentTimeMillis();
        updateClanInDB();
    }

    public long getLeavedAllyTime() {
        return _leavedAllyTime;
    }

    public void setLeavedAllyTime(final long time) {
        _leavedAllyTime = time;
    }

    public void setLeavedAlly() {
        _leavedAllyTime = System.currentTimeMillis();
        updateClanInDB();
    }

    public long getDissolvedAllyTime() {
        return _dissolvedAllyTime;
    }

    public void setDissolvedAllyTime(final long time) {
        _dissolvedAllyTime = time;
    }

    public void setDissolvedAlly() {
        _dissolvedAllyTime = System.currentTimeMillis();
        updateClanInDB();
    }

    public boolean canInvite() {
        return System.currentTimeMillis() - _expelledMemberTime >= Config.EXPELLED_MEMBER_PENALTY;
    }

    public boolean canJoinAlly() {
        return System.currentTimeMillis() - _leavedAllyTime >= Config.LEAVED_ALLY_PENALTY;
    }

    public boolean canCreateAlly() {
        return System.currentTimeMillis() - _dissolvedAllyTime >= Config.DISSOLVED_ALLY_PENALTY;
    }

    public boolean canDisband() {
        return System.currentTimeMillis() > _disbandEndTime;
    }

    public int getRank() {
        final Clan[] clans = ClanTable.getInstance().getClans();
        Arrays.sort(clans, REPUTATION_COMPARATOR);
        final int place = 1;
        for (int i = 0; i < clans.length; ++i) {
            if (i == 100) {
                return 0;
            }
            final Clan clan = clans[i];
            if (clan == this) {
                return place + i;
            }
        }
        return 0;
    }

    public int getReputationScore() {
        return _reputation;
    }

    private void setReputationScore(final int rep) {
        if (_reputation >= 0 && rep < 0) {
            broadcastToOnlineMembers(Msg.SINCE_THE_CLAN_REPUTATION_SCORE_HAS_DROPPED_TO_0_OR_LOWER_YOUR_CLAN_SKILLS_WILL_BE_DE_ACTIVATED);
            for (final UnitMember member : this) {
                if (member.isOnline() && member.getPlayer() != null) {
                    disableSkills(member.getPlayer());
                }
            }
        } else if (_reputation < 0 && rep >= 0) {
            broadcastToOnlineMembers(Msg.THE_CLAN_SKILL_WILL_BE_ACTIVATED_BECAUSE_THE_CLANS_REPUTATION_SCORE_HAS_REACHED_TO_0_OR_HIGHER);
            for (final UnitMember member : this) {
                if (member.isOnline() && member.getPlayer() != null) {
                    enableSkills(member.getPlayer());
                }
            }
        }
        if (_reputation != rep) {
            _reputation = rep;
            broadcastToOnlineMembers(new PledgeShowInfoUpdate(this));
        }
        updateClanInDB();
    }

    public int incReputation(int inc, final boolean rate, final String source) {
        if (_level < Config.MIN_CLAN_LEVEL_FOR_REPUTATION) {
            return 0;
        }
        if (rate && Math.abs(inc) <= Config.RATE_CLAN_REP_SCORE_MAX_AFFECTED) {
            inc = (int) Math.round(inc * Config.RATE_CLAN_REP_SCORE);
        }
        setReputationScore(_reputation + inc);
        Log.add(getName() + "|" + inc + "|" + _reputation + "|" + source, "clan_reputation");
        return inc;
    }

    private void restoreSkills() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT skill_id,skill_level FROM clan_skills WHERE clan_id=?");
            statement.setInt(1, getClanId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int id = rset.getInt("skill_id");
                final int level = rset.getInt("skill_level");
                final Skill skill = SkillTable.getInstance().getInfo(id, level);
                _skills.put(skill.getId(), skill);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not restore clan skills: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public Collection<Skill> getSkills() {
        return _skills.values();
    }

    public final Skill[] getAllSkills() {
        if (_reputation < 0) {
            return Skill.EMPTY_ARRAY;
        }
        return _skills.values().toArray(new Skill[0]);
    }

    public Skill addSkill(final Skill newSkill, final boolean store) {
        Skill oldSkill = null;
        if (newSkill != null) {
            oldSkill = _skills.put(newSkill.getId(), newSkill);
            if (store) {
                Connection con = null;
                PreparedStatement statement = null;
                try {
                    con = DatabaseFactory.getInstance().getConnection();
                    if (oldSkill != null) {
                        statement = con.prepareStatement("UPDATE clan_skills SET skill_level=? WHERE skill_id=? AND clan_id=?");
                        statement.setInt(1, newSkill.getLevel());
                        statement.setInt(2, oldSkill.getId());
                        statement.setInt(3, getClanId());
                        statement.execute();
                    } else {
                        statement = con.prepareStatement("INSERT INTO clan_skills (clan_id,skill_id,skill_level) VALUES (?,?,?)");
                        statement.setInt(1, getClanId());
                        statement.setInt(2, newSkill.getId());
                        statement.setInt(3, newSkill.getLevel());
                        statement.execute();
                    }
                } catch (Exception e) {
                    LOGGER.warn("Error could not store char skills: " + e);
                    LOGGER.error("", e);
                } finally {
                    DbUtils.closeQuietly(con, statement);
                }
            }
            final PledgeSkillListAdd p = new PledgeSkillListAdd(newSkill.getId(), newSkill.getLevel());
            final PledgeSkillList p2 = new PledgeSkillList(this);
            for (final UnitMember temp : this) {
                if (temp.isOnline()) {
                    final Player player = temp.getPlayer();
                    if (player == null) {
                        continue;
                    }
                    addSkill(player, newSkill);
                    player.sendPacket(p, p2, new SkillList(player));
                    player.updateStats();
                }
            }
        }
        return oldSkill;
    }

    public void addSkillsQuietly(final Player player) {
        _skills.values().forEach(skill -> addSkill(player, skill));
        final SubUnit subUnit = getSubUnit(player.getPledgeType());
        if (subUnit != null) {
            subUnit.addSkillsQuietly(player);
        }
    }

    public void enableSkills(final Player player) {
        if (player.isOlyParticipant()) {
            return;
        }
        _skills.values().stream().filter(skill -> skill.getMinPledgeClass() <= player.getPledgeClass() || Config.CLAN_SKILLS_FOR_ALL).forEach(player::removeUnActiveSkill);
        final SubUnit subUnit = getSubUnit(player.getPledgeType());
        if (subUnit != null) {
            subUnit.enableSkills(player);
        }
    }

    public void disableSkills(final Player player) {
        _skills.values().forEach(player::addUnActiveSkill);
        final SubUnit subUnit = getSubUnit(player.getPledgeType());
        if (subUnit != null) {
            subUnit.disableSkills(player);
        }
    }

    private void addSkill(final Player player, final Skill skill) {
        if (skill.getMinPledgeClass() <= player.getPledgeClass() || Config.CLAN_SKILLS_FOR_ALL) {
            player.addSkill(skill, false);
            if (_reputation < 0 || player.isOlyParticipant()) {
                player.addUnActiveSkill(skill);
            }
        }
    }

    public void removeSkill(final int skill) {
        _skills.remove(skill);
        final PledgeSkillListAdd p = new PledgeSkillListAdd(skill, 0);
        for (final UnitMember temp : this) {
            final Player player = temp.getPlayer();
            if (player != null && player.isOnline()) {
                player.removeSkillById(skill);
                player.sendPacket(p, new SkillList(player));
            }
        }
    }

    public void broadcastSkillListToOnlineMembers() {
        for (final UnitMember temp : this) {
            final Player player = temp.getPlayer();
            if (player != null && player.isOnline()) {
                player.sendPacket(new PledgeSkillList(this));
                player.sendSkillList();
            }
        }
    }

    public int getAffiliationRank(final int pledgeType) {
        if (isAcademy(pledgeType)) {
            return 9;
        }
        if (isOrderOfKnights(pledgeType)) {
            return 8;
        }
        if (isRoyalGuard(pledgeType)) {
            return 7;
        }
        return 6;
    }

    public final SubUnit getSubUnit(final int pledgeType) {
        return _subUnits.get(pledgeType);
    }

    public final void addSubUnit(final SubUnit sp, final boolean updateDb) {
        _subUnits.put(sp.getType(), sp);
        if (updateDb) {
            broadcastToOnlineMembers(new PledgeReceiveSubPledgeCreated(sp));
            Connection con = null;
            PreparedStatement statement = null;
            try {
                con = DatabaseFactory.getInstance().getConnection();
                statement = con.prepareStatement("INSERT INTO `clan_subpledges` (clan_id,type,leader_id,name) VALUES (?,?,?,?)");
                statement.setInt(1, getClanId());
                statement.setInt(2, sp.getType());
                statement.setInt(3, sp.getLeaderObjectId());
                statement.setString(4, sp.getName());
                statement.execute();
            } catch (Exception e) {
                LOGGER.warn("Could not store clan Sub pledges: " + e);
                LOGGER.error("", e);
            } finally {
                DbUtils.closeQuietly(con, statement);
            }
        }
    }

    public int createSubPledge(final Player player, int pledgeType, final UnitMember leader, final String name) {
        final int temp = pledgeType;
        pledgeType = getAvailablePledgeTypes(pledgeType);
        if (pledgeType == -128) {
            if (temp == -1) {
                player.sendPacket(Msg.YOUR_CLAN_HAS_ALREADY_ESTABLISHED_A_CLAN_ACADEMY);
            } else {
                player.sendMessage("You can't create any more sub-units of this type");
            }
            return -128;
        }
        switch (pledgeType) {
            case 100:
            case 200: {
                if (getReputationScore() < 5000) {
                    player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                    return -128;
                }
                incReputation(-5000, false, "SubunitCreate");
                break;
            }
            case 1001:
            case 1002:
            case 2001:
            case 2002: {
                if (getReputationScore() < 10000) {
                    player.sendPacket(Msg.THE_CLAN_REPUTATION_SCORE_IS_TOO_LOW);
                    return -128;
                }
                incReputation(-10000, false, "SubunitCreate");
                break;
            }
        }
        addSubUnit(new SubUnit(this, pledgeType, leader, name), true);
        return pledgeType;
    }

    public int getAvailablePledgeTypes(int pledgeType) {
        if (pledgeType == 0) {
            return -128;
        }
        if (_subUnits.get(pledgeType) != null) {
            switch (pledgeType) {
                case -1: {
                    return -128;
                }
                case 100: {
                    pledgeType = getAvailablePledgeTypes(200);
                    break;
                }
                case 200: {
                    return -128;
                }
                case 1001: {
                    pledgeType = getAvailablePledgeTypes(1002);
                    break;
                }
                case 1002: {
                    pledgeType = getAvailablePledgeTypes(2001);
                    break;
                }
                case 2001: {
                    pledgeType = getAvailablePledgeTypes(2002);
                    break;
                }
                case 2002: {
                    return -128;
                }
            }
        }
        return pledgeType;
    }

    private void restoreSubPledges() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM clan_subpledges WHERE clan_id=?");
            statement.setInt(1, getClanId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int type = rset.getInt("type");
                final int leaderId = rset.getInt("leader_id");
                final String name = rset.getString("name");
                final SubUnit pledge = new SubUnit(this, type, leaderId, name);
                addSubUnit(pledge, false);
            }
        } catch (Exception e) {
            LOGGER.warn("Could not restore clan SubPledges: " + e, e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public int getSubPledgeLimit(final int pledgeType) {
        int limit;
        switch (_level) {
            case 0: {
                limit = Config.LIMIT_CLAN_LEVEL0;
                break;
            }
            case 1: {
                limit = Config.LIMIT_CLAN_LEVEL1;
                break;
            }
            case 2: {
                limit = Config.LIMIT_CLAN_LEVEL2;
                break;
            }
            case 3: {
                limit = Config.LIMIT_CLAN_LEVEL3;
                break;
            }
            default: {
                limit = Config.LIMIT_CLAN_LEVEL_4_AND_HIGH;
                break;
            }
        }
        switch (pledgeType) {
            case -1: {
                limit = Config.LIMIT_CLAN_ACADEMY;
                break;
            }
            case 100:
            case 200: {
                limit = Config.LIMIT_CLAN_HIGH_UNITS;
                break;
            }
            case 1001:
            case 1002:
            case 2001:
            case 2002: {
                limit = Config.LIMIT_CLAN_LOW_UNITS;
                break;
            }
        }
        return limit;
    }

    public int getUnitMembersSize(final int pledgeType) {
        if (pledgeType == -128 || !_subUnits.containsKey(pledgeType)) {
            return 0;
        }
        return getSubUnit(pledgeType).size();
    }

    private void restoreRankPrivs() {
        if (_privs == null) {
            InitializePrivs();
        }
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT privilleges,rank FROM clan_privs WHERE clan_id=?");
            statement.setInt(1, getClanId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int rank = rset.getInt("rank");
                final int privileges = rset.getInt("privilleges");
                final RankPrivs p = _privs.get(rank);
                if (p != null) {
                    p.setPrivs(privileges);
                } else {
                    LOGGER.warn("Invalid rank value (" + rank + "), please check clan_privs table");
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Could not restore clan privs by rank: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }

    public void InitializePrivs() {
        for (int i = 1; i <= 9; ++i) {
            _privs.put(i, new RankPrivs(i, 0, 0));
        }
    }

    public void updatePrivsForRank(final int rank) {
        for (final UnitMember member : this) {
            if (member.isOnline() && member.getPlayer() != null && member.getPlayer().getPowerGrade() == rank) {
                if (member.getPlayer().isClanLeader()) {
                    continue;
                }
                member.getPlayer().sendUserInfo();
            }
        }
    }

    public RankPrivs getRankPrivs(final int rank) {
        if (rank < 1 || rank > 9) {
            LOGGER.warn("Requested invalid rank value: " + rank);
            Thread.dumpStack();
            return null;
        }
        if (_privs.get(rank) == null) {
            LOGGER.warn("Request of rank before init: " + rank);
            Thread.dumpStack();
            setRankPrivs(rank, 0);
        }
        return _privs.get(rank);
    }

    public int countMembersByRank(final int rank) {
        int ret = 0;
        for (final UnitMember m : this) {
            if (m.getPowerGrade() == rank) {
                ++ret;
            }
        }
        return ret;
    }

    public void setRankPrivs(final int rank, final int privs) {
        if (rank < 1 || rank > 9) {
            LOGGER.warn("Requested set of invalid rank value: " + rank);
            Thread.dumpStack();
            return;
        }
        if (_privs.get(rank) != null) {
            _privs.get(rank).setPrivs(privs);
        } else {
            _privs.put(rank, new RankPrivs(rank, countMembersByRank(rank), privs));
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO clan_privs (clan_id,rank,privilleges) VALUES (?,?,?)");
            statement.setInt(1, getClanId());
            statement.setInt(2, rank);
            statement.setInt(3, privs);
            statement.execute();
        } catch (Exception e) {
            LOGGER.warn("Could not store clan privs for rank: " + e);
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public final Collection<RankPrivs> getAllRankPrivs() {
        return _privs.values();
    }

    public int getWhBonus() {
        return _whBonus;
    }

    public void setWhBonus(final int i) {
        if (_whBonus != -1) {
            mysql.set("UPDATE `clan_data` SET `warehouse`=? WHERE `clan_id`=?", i, getClanId());
        }
        _whBonus = i;
    }

    public final Collection<SubUnit> getAllSubUnits() {
        return _subUnits.values();
    }

    public List<L2GameServerPacket> listAll() {
        return getAllSubUnits().stream().map(unit -> new PledgeShowMemberListAll(this, unit)).collect(Collectors.toCollection(() -> new ArrayList<>(_subUnits.size())));
    }

    public String getNotice() {
        return _notice;
    }

    public void setNotice(final String notice) {
        _notice = notice;
    }

    public int getSkillLevel(final int id, final int def) {
        final Skill skill = _skills.get(id);
        return (skill == null) ? def : skill.getLevel();
    }

    public int getSkillLevel(final int id) {
        return getSkillLevel(id, -1);
    }

    @Override
    public Iterator<UnitMember> iterator() {
        final List<UnitMember> units = new ArrayList<>(_subUnits.size());

        _subUnits.values().stream().map(SubUnit::getUnitMembers).forEach(units::addAll);

        return Iterators.unmodifiableIterator(units.iterator());
    }

    public boolean isPlacedForDisband() {
        return _disbandEndTime != 0L;
    }

    public void placeForDisband() {
        _disbandEndTime = System.currentTimeMillis() + Config.CLAN_DISBAND_TIME;
        updateClanInDB();
    }

    public void unPlaceDisband() {
        _disbandEndTime = 0L;
        _disbandPenaltyTime = System.currentTimeMillis() + Config.CLAN_DISBAND_PENALTY;
        updateClanInDB();
    }

    public long getDisbandEndTime() {
        return _disbandEndTime;
    }

    public void setDisbandEndTime(final long disbandEndTime) {
        _disbandEndTime = disbandEndTime;
    }

    public long getDisbandPenaltyTime() {
        return _disbandPenaltyTime;
    }

    public void setDisbandPenaltyTime(final long disbandPenaltyTime) {
        _disbandPenaltyTime = disbandPenaltyTime;
    }

    private static class ClanReputationComparator implements Comparator<Clan> {
        @Override
        public int compare(final Clan o1, final Clan o2) {
            if (o1 == null || o2 == null) {
                return 0;
            }
            return o2.getReputationScore() - o1.getReputationScore();
        }
    }
}
