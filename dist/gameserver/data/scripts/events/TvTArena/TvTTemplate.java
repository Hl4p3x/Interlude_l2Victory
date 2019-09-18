package events.TvTArena;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Announcements;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.listener.zone.OnZoneEnterLeaveListener;
import ru.j2dev.gameserver.manager.ReflectionManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.base.TeamType;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Revive;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.PositionUtils;

import java.util.ArrayList;
import java.util.List;
//TODO переписать это уёбище, убрать даблкод
public abstract class TvTTemplate extends Functions {
    private static final int ITEM_ID = 4357;
    private static final String ITEM_NAME = "Silver Shilen";
    private static final int LENGTH_TEAM = 12;
    private static final boolean ALLOW_CLAN_SKILL = true;
    private static boolean ALLOW_BUFFS;
    private static boolean ALLOW_HERO_SKILL;

    protected int _managerId;
    protected String _className;
    protected int _creatorId;
    protected NpcInstance _manager;
    protected int _status;
    protected int _CharacterFound;
    protected int _price;
    protected int _team1count;
    protected int _team2count;
    protected int _team1min;
    protected int _team1max;
    protected int _team2min;
    protected int _team2max;
    protected int _timeToStart;
    protected boolean _timeOutTask;
    protected List<Location> _team1points;
    protected List<Location> _team2points;
    protected List<Integer> _team1list;
    protected List<Integer> _team2list;
    protected List<Integer> _team1live;
    protected List<Integer> _team2live;
    protected Zone _zone;
    protected ZoneListener _zoneListener;

    public TvTTemplate() {
        _status = 0;
        _CharacterFound = 0;
        _price = 10000;
        _team1count = 1;
        _team2count = 1;
        _team1min = 1;
        _team1max = 85;
        _team2min = 1;
        _team2max = 85;
        _timeToStart = 10;
    }

    protected abstract void onInit();

    public void template_stop() {
        if (_status <= 0) {
            return;
        }
        sayToAll("\u0411\u043e\u0439 \u043f\u0440\u0435\u0440\u0432\u0430\u043d \u043f\u043e \u0442\u0435\u0445\u043d\u0438\u0447\u0435\u0441\u043a\u0438\u043c \u043f\u0440\u0438\u0447\u0438\u043d\u0430\u043c, \u0441\u0442\u0430\u0432\u043a\u0438 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u044b");
        unParalyzeTeams();
        ressurectPlayers();
        returnItemToTeams();
        healPlayers();
        removeBuff();
        teleportPlayersToSavedCoords();
        clearTeams();
        _status = 0;
        _timeOutTask = false;
    }

    public void template_create1(final Player player) {
        if (_status > 0) {
            show("\u0414\u043e\u0436\u0434\u0438\u0442\u0435\u0441\u044c \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0431\u043e\u044f", player);
            return;
        }
        if (player.getTeam() != TeamType.NONE) {
            show("\u0412\u044b \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u044b", player);
            return;
        }
        show("scripts/events/TvTArena/" + _managerId + "-1.htm", player);
    }

    public void template_register(final Player player) {
        if (_status == 0) {
            show("\u0411\u043e\u0439 \u043d\u0430 \u0434\u0430\u043d\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u043d\u0435 \u0441\u043e\u0437\u0434\u0430\u043d", player);
            return;
        }
        if (_status > 1) {
            show("\u0414\u043e\u0436\u0434\u0438\u0442\u0435\u0441\u044c \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0431\u043e\u044f", player);
            return;
        }
        if (player.getTeam() != TeamType.NONE) {
            show("\u0412\u044b \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u044b", player);
            return;
        }
        show("scripts/events/TvTArena/" + _managerId + "-3.htm", player);
    }

    public void template_check1(final Player player, final NpcInstance manager, final String[] var) {
        if (var.length != 8) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435", player);
            return;
        }
        if (_status > 0) {
            show("\u0414\u043e\u0436\u0434\u0438\u0442\u0435\u0441\u044c \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0431\u043e\u044f", player);
            return;
        }
        if (manager == null || !manager.isNpc()) {
            show("Hacker? :) " + manager, player);
            return;
        }
        _manager = manager;
        try {
            _price = Integer.valueOf(var[0]);
            _team1count = Integer.valueOf(var[1]);
            _team2count = Integer.valueOf(var[2]);
            _team1min = Integer.valueOf(var[3]);
            _team1max = Integer.valueOf(var[4]);
            _team2min = Integer.valueOf(var[5]);
            _team2max = Integer.valueOf(var[6]);
            _timeToStart = Integer.valueOf(var[7]);
        } catch (Exception e) {
            show("\u041d\u0435\u043a\u043e\u0440\u0440\u0435\u043a\u0442\u043d\u044b\u0435 \u0434\u0430\u043d\u043d\u044b\u0435", player);
            return;
        }
        if (_price < 1 || _price > 500) {
            show("\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u0430\u044f \u0441\u0442\u0430\u0432\u043a\u0430", player);
            return;
        }
        if (_team1count < 1 || _team1count > TvTTemplate.LENGTH_TEAM || _team2count < 1 || _team2count > TvTTemplate.LENGTH_TEAM) {
            show("\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u044b\u0439 \u0440\u0430\u0437\u043c\u0435\u0440 \u043a\u043e\u043c\u0430\u043d\u0434\u044b", player);
            return;
        }
        if (_team1min < 1 || _team1min > 86 || _team2min < 1 || _team2min > 86 || _team1max < 1 || _team1max > 86 || _team2max < 1 || _team2max > 86 || _team1min > _team1max || _team2min > _team2max) {
            show("\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u044b\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c", player);
            return;
        }
        if (player.getLevel() < _team1min || player.getLevel() > _team1max) {
            show("\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u044b\u0439 \u0443\u0440\u043e\u0432\u0435\u043d\u044c", player);
            return;
        }
        if (_timeToStart < 1 || _timeToStart > 10) {
            show("\u041d\u0435\u043f\u0440\u0430\u0432\u0438\u043b\u044c\u043d\u043e\u0435 \u0432\u0440\u0435\u043c\u044f", player);
            return;
        }
        if (getItemCount(player, TvTTemplate.ITEM_ID) < _price) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        _creatorId = player.getObjectId();
        removeItem(player, TvTTemplate.ITEM_ID, (long) _price);
        player.setTeam(TeamType.BLUE);
        _status = 1;
        _team1list.clear();
        _team2list.clear();
        _team1live.clear();
        _team2live.clear();
        _team1list.add(player.getObjectId());
        sayToAll(player.getName() + " \u0441\u043e\u0437\u0434\u0430\u043b \u0431\u043e\u0439 " + _team1count + "\u0445" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, \u0441\u0442\u0430\u0432\u043a\u0430 " + _price + " " + TvTTemplate.ITEM_NAME + ", \u043d\u0430\u0447\u0430\u043b\u043e \u0447\u0435\u0440\u0435\u0437 " + _timeToStart + " \u043c\u0438\u043d");
        executeTask("events.TvTArena." + _className, "announce", new Object[0], 60000L);
    }

    public void template_register_check(final Player player) {
        if (_status == 0) {
            show("\u0411\u043e\u0439 \u043d\u0430 \u0434\u0430\u043d\u043d\u044b\u0439 \u043c\u043e\u043c\u0435\u043d\u0442 \u043d\u0435 \u0441\u043e\u0437\u0434\u0430\u043d", player);
            return;
        }
        if (_status > 1) {
            show("\u0414\u043e\u0436\u0434\u0438\u0442\u0435\u0441\u044c \u043e\u043a\u043e\u043d\u0447\u0430\u043d\u0438\u044f \u0431\u043e\u044f", player);
            return;
        }
        if (_team1list.contains(player.getObjectId()) || _team2list.contains(player.getObjectId())) {
            show("\u0412\u044b \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u044b", player);
            return;
        }
        if (player.getTeam() != TeamType.NONE) {
            show("\u0412\u044b \u0443\u0436\u0435 \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043d\u044b", player);
            return;
        }
        if (getItemCount(player, TvTTemplate.ITEM_ID) < _price) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return;
        }
        final int size1 = _team1list.size();
        final int size2 = _team2list.size();
        if (size1 > size2) {
            String t;
            if (tryRegister(2, player) != null && (t = tryRegister(1, player)) != null) {
                show(t, player);
            }
        } else if (size1 < size2) {
            String t;
            if (tryRegister(1, player) != null && (t = tryRegister(2, player)) != null) {
                show(t, player);
            }
        } else {
            final int team = Rnd.get(1, 2);
            String t2;
            if (tryRegister(team, player) != null && (t2 = tryRegister((team == 1) ? 2 : 1, player)) != null) {
                show(t2, player);
            }
        }
    }

    private String tryRegister(final int team, final Player player) {
        if (team == 1) {
            if (player.getLevel() < _team1min || player.getLevel() > _team1max) {
                return "\u0412\u044b \u043d\u0435 \u043f\u043e\u0434\u0445\u043e\u0434\u0438\u0442\u0435 \u043f\u043e \u0443\u0440\u043e\u0432\u043d\u044e";
            }
            if (_team1list.size() >= _team1count) {
                return "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 1 \u043f\u0435\u0440\u0435\u043f\u043e\u043b\u043d\u0435\u043d\u0430";
            }
            doRegister(1, player);
            return null;
        } else {
            if (player.getLevel() < _team2min || player.getLevel() > _team2max) {
                return "\u0412\u044b \u043d\u0435 \u043f\u043e\u0434\u0445\u043e\u0434\u0438\u0442\u0435 \u043f\u043e \u0443\u0440\u043e\u0432\u043d\u044e";
            }
            if (_team2list.size() >= _team2count) {
                return "\u041a\u043e\u043c\u0430\u043d\u0434\u0430 2 \u043f\u0435\u0440\u0435\u043f\u043e\u043b\u043d\u0435\u043d\u0430";
            }
            doRegister(2, player);
            return null;
        }
    }

    private void doRegister(final int team, final Player player) {
        removeItem(player, TvTTemplate.ITEM_ID, (long) _price);
        if (team == 1) {
            _team1list.add(player.getObjectId());
            player.setTeam(TeamType.BLUE);
            sayToAll(player.getName() + " \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043b\u0441\u044f \u0437\u0430 1 \u043a\u043e\u043c\u0430\u043d\u0434\u0443");
        } else {
            _team2list.add(player.getObjectId());
            player.setTeam(TeamType.RED);
            sayToAll(player.getName() + " \u0437\u0430\u0440\u0435\u0433\u0438\u0441\u0442\u0440\u0438\u0440\u043e\u0432\u0430\u043b\u0441\u044f \u0437\u0430 2 \u043a\u043e\u043c\u0430\u043d\u0434\u0443");
        }
        if (_team1list.size() >= _team1count && _team2list.size() >= _team2count) {
            sayToAll("\u041a\u043e\u043c\u0430\u043d\u0434\u044b \u0433\u043e\u0442\u043e\u0432\u044b, \u0441\u0442\u0430\u0440\u0442 \u0447\u0435\u0440\u0435\u0437 1 \u043c\u0438\u043d\u0443\u0442\u0443.");
            _timeToStart = 1;
        }
    }

    public void template_announce() {
        final Player creator = GameObjectsStorage.getPlayer(_creatorId);
        if (_status != 1 || creator == null) {
            return;
        }
        if (_timeToStart > 1) {
            --_timeToStart;
            sayToAll(creator.getName() + " \u0441\u043e\u0437\u0434\u0430\u043b \u0431\u043e\u0439 " + _team1count + "\u0445" + _team2count + ", " + _team1min + "-" + _team1max + "lv vs " + _team2min + "-" + _team2max + "lv, \u0441\u0442\u0430\u0432\u043a\u0430 " + _price + " " + TvTTemplate.ITEM_NAME + ", \u043d\u0430\u0447\u0430\u043b\u043e \u0447\u0435\u0440\u0435\u0437 " + _timeToStart + " \u043c\u0438\u043d");
            executeTask("events.TvTArena." + _className, "announce", new Object[0], 60000L);
        } else if (_team2list.size() > 0) {
            sayToAll("\u041f\u043e\u0434\u0433\u043e\u0442\u043e\u0432\u043a\u0430 \u043a \u0431\u043e\u044e");
            executeTask("events.TvTArena." + _className, "prepare", new Object[0], 5000L);
        } else {
            sayToAll("\u0411\u043e\u0439 \u043d\u0435 \u0441\u043e\u0441\u0442\u043e\u044f\u043b\u0441\u044f, \u043d\u0435\u0442 \u043f\u0440\u043e\u0442\u0438\u0432\u043d\u0438\u043a\u043e\u0432");
            _status = 0;
            returnItemToTeams();
            clearTeams();
        }
    }

    public void template_prepare() {
        if (_status != 1) {
            return;
        }
        _status = 2;
        for (final Player player : getPlayers(_team1list)) {
            if (!player.isDead()) {
                _team1live.add(player.getObjectId());
            }
        }
        for (final Player player : getPlayers(_team2list)) {
            if (!player.isDead()) {
                _team2live.add(player.getObjectId());
            }
        }
        if (!checkTeams()) {
            return;
        }
        saveBackCoords();
        clearArena();
        ressurectPlayers();
        removeBuff();
        healPlayers();
        paralyzeTeams();
        teleportTeamsToArena();
        sayToAll("\u0411\u043e\u0439 \u043d\u0430\u0447\u043d\u0435\u0442\u0441\u044f \u0447\u0435\u0440\u0435\u0437 30 \u0441\u0435\u043a\u0443\u043d\u0434");
        executeTask("events.TvTArena." + _className, "start", new Object[0], 30000L);
    }

    public void template_start() {
        if (_status != 2) {
            return;
        }
        if (!checkTeams()) {
            return;
        }
        sayToAll("Go!!!");
        unParalyzeTeams();
        _status = 3;
        executeTask("events.TvTArena." + _className, "timeOut", new Object[0], 180000L);
        _timeOutTask = true;
    }

    public void clearArena() {
        _zone.getObjects().stream().filter(obj -> obj != null && obj.isPlayable()).forEach(obj -> obj.teleToLocation(_zone.getSpawn()));
    }

    public boolean checkTeams() {
        if (_team1live.isEmpty()) {
            teamHasLost(1);
            return false;
        }
        if (_team2live.isEmpty()) {
            teamHasLost(2);
            return false;
        }
        return true;
    }

    public void saveBackCoords() {
        for (final Player player : getPlayers(_team1list)) {
            player.setVar("TvTArena_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflectionId(), -1L);
        }
        for (final Player player : getPlayers(_team2list)) {
            player.setVar("TvTArena_backCoords", player.getX() + " " + player.getY() + " " + player.getZ() + " " + player.getReflectionId(), -1L);
        }
    }

    public void teleportPlayersToSavedCoords() {
        for (final Player player : getPlayers(_team1list)) {
            try {
                final String var = player.getVar("TvTArena_backCoords");
                if (var == null || "".equals(var)) {
                    continue;
                }
                final String[] coords = var.split(" ");
                if (coords.length != 4) {
                    continue;
                }
                player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
                player.unsetVar("TvTArena_backCoords");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        for (final Player player : getPlayers(_team2list)) {
            try {
                final String var = player.getVar("TvTArena_backCoords");
                if (var == null || "".equals(var)) {
                    continue;
                }
                final String[] coords = var.split(" ");
                if (coords.length != 4) {
                    continue;
                }
                player.teleToLocation(Integer.parseInt(coords[0]), Integer.parseInt(coords[1]), Integer.parseInt(coords[2]), Integer.parseInt(coords[3]));
                player.unsetVar("TvTArena_backCoords");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void healPlayers() {
        for (final Player player : getPlayers(_team1list)) {
            player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
            player.setCurrentCp((double) player.getMaxCp());
        }
        for (final Player player : getPlayers(_team2list)) {
            player.setCurrentHpMp((double) player.getMaxHp(), (double) player.getMaxMp());
            player.setCurrentCp((double) player.getMaxCp());
        }
    }

    public void ressurectPlayers() {
        for (final Player player : getPlayers(_team1list)) {
            if (player.isDead()) {
                player.restoreExp();
                player.setCurrentHp((double) player.getMaxHp(), true);
                player.setCurrentMp((double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.broadcastPacket(new Revive(player));
            }
        }
        for (final Player player : getPlayers(_team2list)) {
            if (player.isDead()) {
                player.restoreExp();
                player.setCurrentHp((double) player.getMaxHp(), true);
                player.setCurrentMp((double) player.getMaxMp());
                player.setCurrentCp((double) player.getMaxCp());
                player.broadcastPacket(new Revive(player));
            }
        }
    }

    private void removeBuffFromPlayerList(final List<Integer> players) {
        for (final Player player : getPlayers(players)) {
            if (player != null) {
                try {
                    if (player.isCastingNow()) {
                        player.abortCast(true, true);
                    }
                    if (!TvTTemplate.ALLOW_CLAN_SKILL && player.getClan() != null) {
                        for (final Skill skill : player.getClan().getAllSkills()) {
                            player.removeSkill(skill, false);
                        }
                    }
                    if (!TvTTemplate.ALLOW_HERO_SKILL && player.isHero()) {
                        HeroManager.removeSkills(player);
                    }
                    if (!TvTTemplate.ALLOW_BUFFS) {
                        player.getEffectList().stopAllEffects();
                        if (player.getPet() != null) {
                            final Summon summon = player.getPet();
                            summon.getEffectList().stopAllEffects();
                            if (summon.isPet()) {
                                summon.unSummon();
                            }
                        }
                        if (player.getAgathionId() > 0) {
                            player.setAgathion(0);
                        }
                    }
                    player.sendSkillList();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void removeBuff() {
        removeBuffFromPlayerList(_team1list);
        removeBuffFromPlayerList(_team2list);
    }

    private void backBuffToPlayerList(final List<Integer> players) {
        for (final Player player : getPlayers(players)) {
            if (player == null) {
                continue;
            }
            try {
                player.getEffectList().stopAllEffects();
                if (!TvTTemplate.ALLOW_CLAN_SKILL && player.getClan() != null) {
                    for (final Skill skill : player.getClan().getAllSkills()) {
                        if (skill.getMinPledgeClass() <= player.getPledgeClass() || Config.CLAN_SKILLS_FOR_ALL) {
                            player.addSkill(skill, false);
                        }
                    }
                }
                if (!TvTTemplate.ALLOW_HERO_SKILL && player.isHero()) {
                    HeroManager.addSkills(player);
                }
                player.sendSkillList();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void backBuff() {
        backBuffToPlayerList(_team1list);
        backBuffToPlayerList(_team2list);
    }

    private void paralyzeTeamList(final List<Integer> players) {
        final Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
        for (final Player player : getPlayers(players)) {
            player.getEffectList().stopEffect(1411);
            revengeSkill.getEffects(player, player, false, false);
            if (player.getPet() != null) {
                revengeSkill.getEffects(player, player.getPet(), false, false);
            }
        }
    }

    public void paralyzeTeams() {
        paralyzeTeamList(_team1list);
        paralyzeTeamList(_team2list);
    }

    public void unParalyzeTeams() {
        for (final Player player : getPlayers(_team1list)) {
            player.getEffectList().stopEffect(4515);
            if (player.getPet() != null) {
                player.getPet().getEffectList().stopEffect(4515);
            }
            player.leaveParty();
        }
        for (final Player player : getPlayers(_team2list)) {
            player.getEffectList().stopEffect(4515);
            if (player.getPet() != null) {
                player.getPet().getEffectList().stopEffect(4515);
            }
            player.leaveParty();
        }
    }

    public void teleportTeamsToArena() {
        Integer n = 0;
        for (final Player player : getPlayers(_team1live)) {
            unRide(player);
            unSummonPet(player, true);
            player.teleToLocation(_team1points.get(n), ReflectionManager.DEFAULT);
            ++n;
        }
        n = 0;
        for (final Player player : getPlayers(_team2live)) {
            unRide(player);
            unSummonPet(player, true);
            player.teleToLocation(_team2points.get(n), ReflectionManager.DEFAULT);
            ++n;
        }
    }

    public boolean playerHasLost(final Player player) {
        if (player.getTeam() == TeamType.BLUE) {
            _team1live.remove(player.getObjectId());
        } else {
            _team2live.remove(player.getObjectId());
        }
        final Skill revengeSkill = SkillTable.getInstance().getInfo(4515, 1);
        revengeSkill.getEffects(player, player, false, false);
        return !checkTeams();
    }

    public void teamHasLost(final Integer team_id) {
        if (team_id == 1) {
            sayToAll("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 2 \u043f\u043e\u0431\u0435\u0434\u0438\u043b\u0430");
            payItemToTeam(2);
        } else {
            sayToAll("\u041a\u043e\u043c\u0430\u043d\u0434\u0430 1 \u043f\u043e\u0431\u0435\u0434\u0438\u043b\u0430");
            payItemToTeam(1);
        }
        unParalyzeTeams();
        backBuff();
        teleportPlayersToSavedCoords();
        ressurectPlayers();
        healPlayers();
        clearTeams();
        _status = 0;
        _timeOutTask = false;
    }

    public void template_timeOut() {
        if (_timeOutTask && _status == 3) {
            sayToAll("\u0412\u0440\u0435\u043c\u044f \u0438\u0441\u0442\u0435\u043a\u043b\u043e, \u043d\u0438\u0447\u044c\u044f!");
            returnItemToTeams();
            unParalyzeTeams();
            backBuff();
            teleportPlayersToSavedCoords();
            ressurectPlayers();
            healPlayers();
            clearTeams();
            _status = 0;
            _timeOutTask = false;
        }
    }

    public void payItemToTeam(final Integer team_id) {
        if (team_id == 1) {
            for (final Player player : getPlayers(_team1list)) {
                addItem(player, TvTTemplate.ITEM_ID, (long) (_price + _team2list.size() * _price / _team1list.size()));
            }
        } else {
            for (final Player player : getPlayers(_team2list)) {
                addItem(player, TvTTemplate.ITEM_ID, (long) (_price + _team2list.size() * _price / _team1list.size()));
            }
        }
    }

    public void returnItemToTeams() {
        for (final Player player : getPlayers(_team1list)) {
            addItem(player, TvTTemplate.ITEM_ID, (long) _price);
        }
        for (final Player player : getPlayers(_team2list)) {
            addItem(player, TvTTemplate.ITEM_ID, (long) _price);
        }
    }

    public void clearTeams() {
        for (final Player player : getPlayers(_team1list)) {
            player.setTeam(TeamType.NONE);
        }
        for (final Player player : getPlayers(_team2list)) {
            player.setTeam(TeamType.NONE);
        }
        _team1list.clear();
        _team2list.clear();
        _team1live.clear();
        _team2live.clear();
    }

    public void onDeath(final Creature self, final Creature killer) {
        if (_status >= 2 && self.isPlayer() && self.getTeam() != TeamType.NONE && (_team1list.contains(self.getObjectId()) || _team2list.contains(self.getObjectId()))) {
            final Player player = self.getPlayer();
            final Player kplayer = killer.getPlayer();
            if (kplayer != null) {
                sayToAll(kplayer.getName() + " \u0443\u0431\u0438\u043b " + player.getName());
                if (player.getTeam() == kplayer.getTeam() || (!_team1list.contains(kplayer.getObjectId()) && !_team2list.contains(kplayer.getObjectId()))) {
                    sayToAll("\u041d\u0430\u0440\u0443\u0448\u0435\u043d\u0438\u0435 \u043f\u0440\u0430\u0432\u0438\u043b, \u0438\u0433\u0440\u043e\u043a " + kplayer.getName() + " \u043e\u0448\u0442\u0440\u0430\u0444\u043e\u0432\u0430\u043d \u043d\u0430 " + _price + " " + TvTTemplate.ITEM_NAME);
                    removeItem(kplayer, TvTTemplate.ITEM_ID, (long) _price);
                }
                playerHasLost(player);
            } else {
                sayToAll(player.getName() + " \u0443\u0431\u0438\u0442");
                playerHasLost(player);
            }
        }
    }

    public void onPlayerExit(final Player player) {
        if (player != null && _status > 0 && player.getTeam() != TeamType.NONE && (_team1list.contains(player.getObjectId()) || _team2list.contains(player.getObjectId()))) {
            switch (_status) {
                case 1: {
                    removePlayer(player);
                    sayToAll(player.getName() + " \u0434\u0438\u0441\u043a\u0432\u0430\u043b\u0438\u0444\u0438\u0446\u0438\u0440\u043e\u0432\u0430\u043d");
                    if (player.getObjectId() == (_creatorId)) {
                        sayToAll("\u0411\u043e\u0439 \u043f\u0440\u0435\u0440\u0432\u0430\u043d, \u0441\u0442\u0430\u0432\u043a\u0438 \u0432\u043e\u0437\u0432\u0440\u0430\u0449\u0435\u043d\u044b");
                        returnItemToTeams();
                        backBuff();
                        teleportPlayersToSavedCoords();
                        unParalyzeTeams();
                        ressurectPlayers();
                        healPlayers();
                        clearTeams();
                        unParalyzeTeams();
                        clearTeams();
                        _status = 0;
                        _timeOutTask = false;
                        break;
                    }
                    break;
                }
                case 2: {
                    removePlayer(player);
                    sayToAll(player.getName() + " \u0434\u0438\u0441\u043a\u0432\u0430\u043b\u0438\u0444\u0438\u0446\u0438\u0440\u043e\u0432\u0430\u043d");
                    checkTeams();
                    break;
                }
                case 3: {
                    removePlayer(player);
                    sayToAll(player.getName() + " \u0434\u0438\u0441\u043a\u0432\u0430\u043b\u0438\u0444\u0438\u0446\u0438\u0440\u043e\u0432\u0430\u043d");
                    checkTeams();
                    break;
                }
            }
        }
    }

    public void onTeleport(final Player player) {
        if (player != null && _status > 1 && player.getTeam() != TeamType.NONE && player.isInZone(_zone)) {
            onPlayerExit(player);
        }
    }

    private void removePlayer(final Player player) {
        if (player != null) {
            _team1list.remove(player.getObjectId());
            _team2list.remove(player.getObjectId());
            _team1live.remove(player.getObjectId());
            _team2live.remove(player.getObjectId());
            player.setTeam(TeamType.NONE);
        }
    }

    private List<Player> getPlayers(final List<Integer> list) {
        final List<Player> result = new ArrayList<>();
        for (final int storeId : list) {
            final Player player = GameObjectsStorage.getPlayer(storeId);
            if (player != null) {
                result.add(player);
            }
        }
        return result;
    }

    public void sayToAll(final String text) {
        Announcements.getInstance().announceToAll(_manager.getName() + ": " + text, ChatType.CRITICAL_ANNOUNCE);
    }

    public class ZoneListener implements OnZoneEnterLeaveListener {
        @Override
        public void onZoneEnter(final Zone zone, final Creature cha) {
            final Player player = cha.getPlayer();
            if (_status >= 2 && player != null && !_team1list.contains(player.getObjectId()) && !_team2list.contains(player.getObjectId())) {
                ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, _zone.getSpawn()), 3000L);
            }
        }

        @Override
        public void onZoneLeave(final Zone zone, final Creature cha) {
            final Player player = cha.getPlayer();
            if (_status >= 2 && player != null && player.getTeam() != TeamType.NONE && (_team1list.contains(player.getObjectId()) || _team2list.contains(player.getObjectId()))) {
                final double angle = PositionUtils.convertHeadingToDegree(cha.getHeading());
                final double radian = Math.toRadians(angle - 90.0);
                final int x = (int) (cha.getX() + 50.0 * Math.sin(radian));
                final int y = (int) (cha.getY() - 50.0 * Math.cos(radian));
                final int z = cha.getZ();
                ThreadPoolManager.getInstance().schedule(new TeleportTask(cha, new Location(x, y, z)), 3000L);
            }
        }
    }

    public class TeleportTask extends RunnableImpl {
        Location loc;
        Creature target;

        public TeleportTask(final Creature target, final Location loc) {
            this.target = target;
            this.loc = loc;
            target.block();
        }

        @Override
        public void runImpl() {
            target.unblock();
            target.teleToLocation(loc);
        }
    }
}
