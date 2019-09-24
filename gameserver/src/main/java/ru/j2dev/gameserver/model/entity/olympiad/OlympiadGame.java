package ru.j2dev.gameserver.model.entity.olympiad;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import gnu.trove.map.hash.TIntIntHashMap;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.olympiad.NoblessManager.NobleRecord;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.skills.TimeStamp;
import ru.j2dev.gameserver.skills.effects.EffectCubic;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.HtmlUtils;
import ru.j2dev.gameserver.utils.Location;

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;

@HideAccess
@StringEncryption
public class OlympiadGame {
    private static final double RESTORE_MOD = 0.8;
    private final OlympiadStadium _olympiadStadium;
    private final OlympiadGameType _type;
    OlympiadPlayer[] _olympiadPlayers;
    private long _start_time;
    private OlympiadGameState _state;
    private ScheduledFuture<?> _currentTask;

    public OlympiadGame(final OlympiadGameType type, final OlympiadStadium olympiadStadium) {
        _type = type;
        _olympiadStadium = olympiadStadium;
        _state = null;
        _start_time = 0L;
    }

    private static int CalcPoints(final int points) {
        return Math.max(1, (Math.min(50, points) - 1) / 5 + 1);
    }

    public static SystemMessage checkPlayer(final Player player) {
        if (!player.isNoble()) {
            return new SystemMessage(1501).addName(player);
        }
        if (player.isInDuel()) {
            return new SystemMessage(1599);
        }
        if (player.getBaseClassId() != player.getClassId().getId() || player.getClassId().getLevel() < 4) {
            return new SystemMessage(1500).addName(player);
        }
        if (player.getInventoryLimit() * 0.8 <= player.getInventory().getSize()) {
            return new SystemMessage(1691).addName(player);
        }
        if (player.isCursedWeaponEquipped()) {
            return new SystemMessage(1857).addName(player).addItemName(player.getCursedWeaponEquippedId());
        }
        if (NoblessManager.getInstance().getPointsOf(player.getObjectId()) < 1) {
            return new SystemMessage(1983).addString(new CustomMessage("THE_REQUEST_CANNOT_BE_COMPLETED_BECAUSE_THE_REQUIREMENTS_ARE_NOT_MET_IN_ORDER_TO_PARTICIPATE_IN", player, new Object[0]).toString());
        }
        return null;
    }

    public OlympiadGameState getState() {
        return _state;
    }

    public void setState(final OlympiadGameState state) {
        if (_state == OlympiadGameState.STAND_BY && state == OlympiadGameState.PLAYING) {
            _start_time = System.currentTimeMillis();
        }
        _state = state;
    }

    public OlympiadGameType getType() {
        return _type;
    }

    public OlympiadStadium getStadium() {
        return _olympiadStadium;
    }

    public void setPlayers(final OlympiadPlayer[] olympiadPlayers) {
        _olympiadPlayers = olympiadPlayers;
        Arrays.stream(_olympiadPlayers).forEach(participant -> Arrays.stream(participant.getPlayers()).forEach(player -> player.setOlyParticipant(participant)));
    }

    public void start() {
        Arrays.stream(_olympiadPlayers).forEach(OlympiadPlayer::onStart);
    }

    private void prepareParticipantsForReturn() {
        Arrays.stream(_olympiadPlayers).flatMap(part -> Arrays.stream(part.getPlayers())).forEach(player -> {
            try {
                if (player.getClan() != null) {
                    player.getClan().enableSkills(player);
                }
                for (final Effect e : player.getEffectList().getAllFirstEffects()) {
                    if (!(e instanceof EffectCubic) || e.getSkill().getTargetType() != SkillTargetType.TARGET_SELF || !e.getSkill().isToggle()) {
                        e.exit();
                    }
                }
                Arrays.stream(Config.OLY_RESTRICTED_SKILL_IDS).filter(player::isUnActiveSkill).mapToObj(player::getKnownSkill).filter(Objects::nonNull).forEach(player::removeUnActiveSkill);
                if (player.isDead()) {
                    player.broadcastPacket(new Revive(player));
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), true);
                    player.setCurrentCp(player.getMaxCp());
                } else {
                    player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp(), false);
                    player.setCurrentCp(player.getMaxCp());
                }
                final Collection<TimeStamp> reuse = player.getSkillReuses();
                reuse.stream().map(ts -> SkillTable.getInstance().getInfo(ts.getId(), ts.getLevel())).forEach(player::enableSkill);
                if (player.isHero()) {
                    HeroManager.addSkills(player);
                }
                player.sendPacket(new ExOlympiadMode(0), new SkillList(player), new SkillCoolTime(player));
                player.updateStats();
                player.updateEffectIcons();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void prepareParticipantsForCompetition() {
        for (final OlympiadPlayer part : _olympiadPlayers) {
            for (final Player player : part.getPlayers()) {
                try {
                    boolean update = false;
                    for (final Effect e : player.getEffectList().getAllFirstEffects()) {
                        if (!(e instanceof EffectCubic) || e.getSkill().getTargetType() != SkillTargetType.TARGET_SELF) {
                            e.exit();
                            update = true;
                        }
                    }
                    final Collection<TimeStamp> reuse = player.getSkillReuses();
                    for (final TimeStamp ts : reuse) {
                        final Skill skill = SkillTable.getInstance().getInfo(ts.getId(), ts.getLevel());
                        player.enableSkill(skill);
                        update = true;
                    }
                    if (update) {
                        player.sendPacket(new SkillCoolTime(player));
                        player.updateStats();
                        player.updateEffectIcons();
                    }
                    if (Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ARMOR >= 0) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantArmorLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ARMOR));
                    }
                    if (Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_PHYS >= 0) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantWeaponPhysLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_PHYS));
                    }
                    if (Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_MAGE >= 0) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantWeaponMageLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_WEAPON_MAGE));
                    }
                    if (Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ACCESSORY >= 0) {
                        player.sendMessage(new CustomMessage("l2p.gameserver.model.entity.OlympiadGame.Competition.EnchantAccessoryLevelLimited", player, Config.OLY_LIMIT_ENCHANT_STAT_LEVEL_ACCESSORY));
                    }
                    if (player.getClan() != null) {
                        player.getClan().disableSkills(player);
                    }
                    Arrays.stream(Config.OLY_RESTRICTED_SKILL_IDS).mapToObj(player::getKnownSkill).filter(Objects::nonNull).forEach(player::addUnActiveSkill);
                    if (player.isHero()) {
                        HeroManager.removeSkills(player);
                    }
                    if (player.isCastingNow()) {
                        player.abortCast(true, false);
                    }
                    if (player.isMounted()) {
                        player.setMount(0, 0, 0);
                    }
                    if (player.getPet() != null) {
                        final Summon summon = player.getPet();
                        if (summon.isPet()) {
                            summon.unSummon();
                        } else {
                            summon.getEffectList().stopAllEffects();
                        }
                    }

                    player.sendSkillList();
                    final ItemInstance wpn = player.getInventory().getPaperdollItem(7);
                    if (wpn != null && wpn.isHeroWeapon()) {
                        player.getInventory().unEquipItem(wpn);
                        player.abortAttack(true, true);
                        player.refreshExpertisePenalty();
                    }
                    final Set<Integer> activeSoulShots = player.getAutoSoulShot();
                    activeSoulShots.forEach(itemId -> {
                        player.removeAutoSoulShot(itemId);
                        player.sendPacket(new ExAutoSoulShot(itemId, false));
                    });
                    final ItemInstance weapon = player.getActiveWeaponInstance();
                    if (weapon != null) {
                        weapon.setChargedSpiritshot(0);
                        weapon.setChargedSoulshot(0);
                    }
                    restoreHPCPMP();
                    player.broadcastUserInfo(true);
                    if (getType() != OlympiadGameType.TEAM_CLASS_FREE) {
                        if (player.getParty() != null) {
                            player.getParty().removePartyMember(player, false);
                        }
                    } else {
                        boolean upp = false;
                        if (player.getParty() != null) {
                            if (player.getParty().getPartyMembers().size() != part.getPlayers().length) {
                                upp = true;
                            } else {
                                for (final Player pm0 : player.getParty().getPartyMembers()) {
                                    boolean contains = false;
                                    for (final Player pm2 : part.getPlayers()) {
                                        if (pm0 == pm2) {
                                            contains = true;
                                        }
                                    }
                                    if (!contains) {
                                        upp = true;
                                    }
                                }
                            }
                        } else {
                            upp = true;
                        }
                        if (upp) {
                            final Player[] party_r = part.getPlayers();
                            if (party_r[0].getParty() != null) {
                                party_r[0].getParty().removePartyMember(party_r[0], false);
                            }
                            final Party party = new Party(party_r[0], 0);
                            party_r[0].setParty(party);
                            if (party_r[1].isInParty()) {
                                party_r[1].getParty().removePartyMember(party_r[1], false);
                            }
                            party.addPartyMember(party_r[1]);
                            if (party_r[2].isInParty()) {
                                party_r[2].getParty().removePartyMember(party_r[2], false);
                            }
                            party.addPartyMember(party_r[2]);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public void applyBuffs() {
        Arrays.stream(_olympiadPlayers).flatMap(part -> Arrays.stream(part.getPlayers())).forEach(player -> {
            try {
                final TIntIntHashMap buffs = Config.OLY_BUFFS.get(player.getActiveClassId());
                Arrays.stream(buffs.keys()).mapToObj(skillId -> SkillTable.getInstance().getInfo(skillId, buffs.get(skillId))).forEach(buff -> buff.getEffects(player, player, false, false));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void broadcastClassId() {
        sendClassNames(0, 1);
        sendClassNames(1, 0);
    }

    private void sendClassNames(final int team1, final int team2) {
        Arrays.stream(_olympiadPlayers[team1].getPlayers()).forEach(player -> Arrays.stream(_olympiadPlayers[team2].getPlayers()).forEach(player1 -> {
            player.sendPacket(getOpponentClassName(player, player1));
            player.sendPacket(getOpponentOlyStats(player, player1));
        }));
    }

    private L2GameServerPacket getOpponentClassName(final Player player, final Player player1) {
        return new Say2(player.getObjectId(), ChatType.CRITICAL_ANNOUNCE, player.isLangRus() ? "Помощник олимпиады" : "Olympiad Helper", player.isLangRus() ? "Против вас " : "You opponent" + HtmlUtils.makeClassNameFString(player, player1.getActiveClassId()));

    }

    private L2GameServerPacket getOpponentOlyStats(final Player player, final Player player1) {
        final NobleRecord nr = NoblessManager.getInstance().getNobleRecord(player1.getObjectId());
        return new Say2(player.getObjectId(), ChatType.CRITICAL_ANNOUNCE, player.isLangRus() ? "Помощник олимпиады" : "Olympiad Helper", player1.getName() + " Stats :" + nr.points_current + " win/lose :" + nr.comp_win + "/" + nr.comp_loose);

    }

    public void restoreHPCPMP() {
        Arrays.stream(_olympiadPlayers).flatMap(part -> Arrays.stream(part.getPlayers())).forEach(player -> {
            try {
                player.setCurrentHpMp(player.getMaxHp(), player.getMaxMp());
                player.setCurrentCp(player.getMaxCp());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void finish() {
        Arrays.stream(_olympiadPlayers).forEach(participant -> {
            Arrays.stream(participant.getPlayers()).forEach(player -> {
                if (player.getMaxCp() * 0.8 > player.getCurrentCp()) {
                    player.setCurrentCp(player.getMaxCp() * 0.8);
                }
                if (player.getMaxHp() * 0.8 > player.getCurrentHp()) {
                    player.setCurrentHp(player.getMaxHp() * 0.8, false);
                }
                if (player.getMaxMp() * 0.8 > player.getCurrentMp()) {
                    player.setCurrentMp(player.getMaxMp() * 0.8);
                }
            });
            participant.onFinish();
        });
    }

    private int getParticipantsMinPoint() {
        int pmin = Integer.MAX_VALUE;
        for (final OlympiadPlayer olympiadPlayer : _olympiadPlayers) {
            for (final Player player : olympiadPlayer.getPlayers()) {
                if (player != null) {
                    final int ppoint = NoblessManager.getInstance().getPointsOf(player.getObjectId());
                    if (ppoint < pmin) {
                        pmin = ppoint;
                    }
                }
            }
        }
        return pmin;
    }

    private void processPoints(final OlympiadPlayer winn, final OlympiadPlayer loose, final boolean tie) {
        processPoints(winn, loose, tie, false);
    }

    private void processPoints(final OlympiadPlayer winn, final OlympiadPlayer loose, final boolean tie, final boolean looserDisconnected) {
        if (!looserDisconnected) {
            broadcastPacket(tie ? Msg.THE_GAME_ENDED_IN_A_TIE : new SystemMessage(1497).addString(winn.getName()), true, true);
        }
        long comp_spend_time = 0L;
        if (_start_time > 0L) {
            comp_spend_time = Math.min(Config.OLYMPIAD_COMPETITION_TIME, System.currentTimeMillis() - _start_time) / 1000L;
        }
        final Player[] loose_arr = loose.getPlayers();
        final Player[] winn_arr = winn.getPlayers();
        int loosed_points_sum = 0;
        int looser_sum = 0;
        int winner_sum = 0;
        for (Player aLoose_arr : loose_arr) {
            try {
                if (aLoose_arr != null) {
                    looser_sum += NoblessManager.getInstance().getPointsOf(aLoose_arr.getObjectId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        for (Player aWinn_arr : winn_arr) {
            try {
                if (aWinn_arr != null) {
                    winner_sum += NoblessManager.getInstance().getPointsOf(aWinn_arr.getObjectId());
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        final int min_points = Math.max(0, Math.min(winner_sum, looser_sum));
        for (int j = 0; j < loose_arr.length; ++j) {
            try {
                final Player lp2 = loose_arr[j];
                if (lp2 != null) {
                    if (NoblessManager.getInstance() != null) {
                        final int curr_points = NoblessManager.getInstance().getPointsOf(lp2.getObjectId());
                        final int loose_points = Math.max(1, (int) (min_points * Config.OLY_LOOSE_POINTS_MUL));
                        final int looser_points = Math.max(0, curr_points - loose_points);
                        loosed_points_sum += loose_points;
                        final NobleRecord lnr = NoblessManager.getInstance().getNobleRecord(lp2.getObjectId());
                        lnr.points_current = looser_points;
                        ++lnr.comp_loose;
                        ++lnr.comp_done;
                        switch (getType()) {
                            case CLASS_FREE: {
                                ++lnr.class_free_cnt;
                                break;
                            }
                            case CLASS_INDIVIDUAL: {
                                ++lnr.class_based_cnt;
                                break;
                            }
                            case TEAM_CLASS_FREE: {
                                ++lnr.team_cnt;
                                break;
                            }
                        }
                        NoblessManager.getInstance().SaveNobleRecord(lnr);
                        lp2.sendPacket(new SystemMessage(1658).addName(lp2).addNumber(loose_points));
                        Arrays.stream(lp2.getAllQuestsStates()).filter(QuestState::isStarted).forEach(qs -> qs.getQuest().notifyOlympiadResult(qs, getType(), false));
                        lp2.getEffectList().getAllEffects().stream().filter(e -> e != null && e.isCancelable()).forEach(Effect::exit);
                        lp2.sendChanges();
                        lp2.updateEffectIcons();
                        OlympiadGameManager.getInstance().addCompetitionResult(OlympiadSystemManager.getInstance().getCurrentSeason(), NoblessManager.getInstance().getNobleRecord(winn_arr[j].getObjectId()), loose_points, NoblessManager.getInstance().getNobleRecord(lp2.getObjectId()), loose_points, getType(), tie, looserDisconnected, comp_spend_time);
                        lp2.getListeners().onOlyCompetitionCompleted(this, false);
                    }
                }
            } catch (Exception ex2) {
                ex2.printStackTrace();
            }
        }
        if (!looserDisconnected) {
            final int points = loosed_points_sum / loose_arr.length;
            for (int k = 0; k < winn_arr.length; ++k) {
                try {
                    final Player wp2 = winn_arr[k];
                    final int win_points = Math.max(0, NoblessManager.getInstance().getPointsOf(wp2.getObjectId()) + points);
                    NoblessManager.getInstance().setPointsOf(wp2.getObjectId(), win_points);
                    final NobleRecord wnr = NoblessManager.getInstance().getNobleRecord(wp2.getObjectId());
                    wnr.points_current = win_points;
                    ++wnr.comp_win;
                    ++wnr.comp_done;
                    switch (getType()) {
                        case CLASS_FREE: {
                            ++wnr.class_free_cnt;
                            break;
                        }
                        case CLASS_INDIVIDUAL: {
                            ++wnr.class_based_cnt;
                            break;
                        }
                        case TEAM_CLASS_FREE: {
                            ++wnr.team_cnt;
                            break;
                        }
                    }
                    NoblessManager.getInstance().SaveNobleRecord(wnr);
                    wp2.sendPacket(new SystemMessage(1657).addName(wp2).addNumber(points));
                    Arrays.stream(wp2.getAllQuestsStates()).filter(QuestState::isStarted).forEach(qs2 -> qs2.getQuest().notifyOlympiadResult(qs2, getType(), !tie));
                    wp2.getEffectList().getAllEffects().stream().filter(e2 -> e2 != null && e2.isCancelable()).forEach(Effect::exit);
                    wp2.sendChanges();
                    wp2.updateEffectIcons();
                    int rvicnt = 0;
                    switch (getType()) {
                        case CLASS_FREE: {
                            rvicnt = Config.OLY_VICTORY_CFREE_RITEMCNT;
                            break;
                        }
                        case CLASS_INDIVIDUAL: {
                            rvicnt = Config.OLY_VICTORY_CBASE_RITEMCNT;
                            break;
                        }
                        case TEAM_CLASS_FREE: {
                            rvicnt = Config.OLY_VICTORY_3TEAM_RITEMCNT;
                            break;
                        }
                    }
                    if (rvicnt > 0) {
                        wp2.getInventory().addItem(Config.OLY_VICTORY_RITEMID, rvicnt);
                        wp2.sendPacket(SystemMessage2.obtainItems(Config.OLY_VICTORY_RITEMID, rvicnt, 0));
                    }
                    OlympiadGameManager.getInstance().addCompetitionResult(OlympiadSystemManager.getInstance().getCurrentSeason(), NoblessManager.getInstance().getNobleRecord(wp2.getObjectId()), points, NoblessManager.getInstance().getNobleRecord(loose_arr[k].getObjectId()), points, getType(), tie, looserDisconnected, comp_spend_time);
                    wp2.getListeners().onOlyCompetitionCompleted(this, !tie);
                } catch (Exception ex3) {
                    ex3.printStackTrace();
                }
            }
        }
    }

    public boolean ValidateParticipants() {
        boolean cancel = Arrays.stream(_olympiadPlayers).anyMatch(participant -> !participant.validateThis());
        if (cancel) {
            cancelTask();
            OlympiadGameManager.getInstance().FinishCompetition(this);
            return true;
        }
        return false;
    }

    public synchronized void ValidateWinner() {
        if (getState() == OlympiadGameState.INIT) {
            broadcastPacket(Msg.THE_GAME_HAS_BEEN_CANCELLED_BECAUSE_THE_OTHER_PARTY_ENDS_THE_GAME, true, false);
            if (!_olympiadPlayers[0].isAlive()) {
                processPoints(_olympiadPlayers[1], _olympiadPlayers[0], false, true);
            } else if (!_olympiadPlayers[1].isAlive()) {
                processPoints(_olympiadPlayers[0], _olympiadPlayers[1], false, true);
            }
            cancelTask();
            OlympiadGameManager.getInstance().FinishCompetition(this);
            return;
        }
        if (getState() != OlympiadGameState.FINISH && (!_olympiadPlayers[0].isAlive() || !_olympiadPlayers[1].isAlive())) {
            cancelTask();
            setState(OlympiadGameState.FINISH);
            OlympiadGameManager.getInstance().scheduleFinishCompetition(this, 20, 100L);
        }
        if (getState() == OlympiadGameState.FINISH) {
            if (!_olympiadPlayers[0].isAlive()) {
                processPoints(_olympiadPlayers[1], _olympiadPlayers[0], false);
            } else if (!_olympiadPlayers[1].isAlive()) {
                processPoints(_olympiadPlayers[0], _olympiadPlayers[1], false);
            } else {
                final double dmg0 = _olympiadPlayers[0].getTotalDamage();
                final double dmg2 = _olympiadPlayers[1].getTotalDamage();
                if (dmg0 < dmg2) {
                    processPoints(_olympiadPlayers[0], _olympiadPlayers[1], false);
                } else if (dmg0 > dmg2) {
                    processPoints(_olympiadPlayers[1], _olympiadPlayers[0], false);
                } else {
                    processPoints(_olympiadPlayers[0], _olympiadPlayers[1], true);
                }
            }
            Arrays.stream(_olympiadPlayers).flatMap(participant -> Arrays.stream(participant.getPlayers())).forEach(player -> {
                if (player.isDead()) {
                    player.doRevive(100.0);
                }
                player.block();
                player.sendPacket(new ExOlympiadMode(0));
            });
            broadcastPacket(new ExOlympiadMode(3), false, true);
        }
    }

    public void teleportParticipantsOnStadium() {
        prepareParticipantsForCompetition();
        Arrays.stream(_olympiadPlayers).forEach(participant -> Arrays.stream(participant.getPlayers()).forEach(player -> {
            try {
                if (player != null) {
                    final Location loc = Location.findAroundPosition(getStadium().getLocForParticipant(participant), 0, 32);
                    player.setVar("backCoords", player.getLoc().toXYZString(), -1L);
                    player.teleToLocation(loc, _olympiadStadium);
                    player.sendPacket(new ExOlympiadMode(participant.getSide()));
                    if (getType() == OlympiadGameType.TEAM_CLASS_FREE) {
                        player.setTeam((participant.getSide() == 1) ? TeamType.BLUE : TeamType.RED);
                    }
                    final Summon summon = player.getPet();
                    if (summon != null) {
                        if (summon.isPet()) {
                            summon.unSummon();
                        } else {
                            summon.teleToLocation(loc, _olympiadStadium);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }));
    }

    public void teleportParticipantsBack() {
        prepareParticipantsForReturn();
        Arrays.stream(_olympiadPlayers).flatMap(participant -> Arrays.stream(participant.getPlayers())).forEach(player -> {
            try {
                if (player != null && player.getVar("backCoords") != null) {
                    final Location loc = Location.parseLoc(player.getVar("backCoords"));
                    player.unsetVar("backCoords");
                    player.sendPacket(new ExOlympiadMode(0));
                    if (player.isBlocked()) {
                        player.unblock();
                    }
                    if (getType() == OlympiadGameType.TEAM_CLASS_FREE) {
                        player.setTeam(TeamType.NONE);
                    }
                    player.setReflection(0);
                    player.teleToLocation(loc);
                    final Summon summon = player.getPet();
                    if (summon != null) {
                        if (summon.isPet()) {
                            summon.unSummon();
                        } else {
                            summon.setReflection(0);
                            summon.teleToLocation(loc);
                        }
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    public void broadcastEverybodyOlympiadUserInfo() {
        Arrays.stream(_olympiadPlayers).flatMap(participant -> Arrays.stream(participant.getPlayers())).filter(Objects::nonNull).forEach(player -> {
            final ExOlympiadUserInfo oui = new ExOlympiadUserInfo(player);
            broadcastPacket(oui, true, true);
            player.broadcastRelationChanged();
        });
    }

    public void broadcastEverybodyEffectIcons() {
        Arrays.stream(_olympiadPlayers).flatMap(participant -> Arrays.stream(participant.getPlayers())).forEach(player -> broadcastEffectIcons(player, player.getEffectList().getAllFirstEffects()));
    }

    public void broadcastEffectIcons(final Player player, final Effect[] effects) {
        final ExOlympiadSpelledInfo osi = new ExOlympiadSpelledInfo();
        Arrays.stream(effects).filter(effect -> effect != null && effect.isInUse()).forEach(effect -> effect.addOlympiadSpelledIcon(player, osi));
        if (getState() == OlympiadGameState.PLAYING) {
            broadcastPacket(osi, true, true);
        } else {
            player.getOlyParticipant().sendPacket(osi);
        }
    }

    public void broadcastPacket(final L2GameServerPacket gsp, final boolean toParticipants, final boolean toObservers) {
        if (getState() != null) {
            if (getState() == OlympiadGameState.INIT && toParticipants) {
                Arrays.stream(_olympiadPlayers).flatMap(participant -> Arrays.stream(participant.getPlayers())).forEach(player -> player.sendPacket(gsp));
            } else {
                getStadium().getPlayers().stream().filter(player2 -> (toParticipants && player2.isOlyParticipant()) || (toObservers && player2.isOlyObserver())).forEach(player2 -> player2.sendPacket(gsp));
            }
        }
    }

    public synchronized void scheduleTask(final Runnable task, final long delay) {
        _currentTask = ThreadPoolManager.getInstance().schedule(task, delay);
    }

    public synchronized void cancelTask() {
        if (_currentTask != null) {
            _currentTask.cancel(false);
            _currentTask = null;
        }
    }

    public OlympiadPlayer[] getGamePlayers() {
        return _olympiadPlayers;
    }
}
