package ru.j2dev.gameserver.templates;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.Zone.ZoneTarget;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class ZoneTemplate {
    private final String _name;
    private final ZoneType _type;
    private final Territory _territory;
    private final boolean _isEnabled;
    private final boolean _isDefault;
    private final List<Location> _restartPoints;
    private final List<Location> _PKrestartPoints;
    private final long _restartTime;
    private final int _enteringMessageId;
    private final int _leavingMessageId;
    private final Race _affectRace;
    private final ZoneTarget _target;
    private final Skill _skill;
    private final int _skillProb;
    private final int _initialDelay;
    private final int _unitTick;
    private final int _randomTick;
    private final int _damageMessageId;
    private final int _damageOnHP;
    private final int _damageOnMP;
    private final double _moveBonus;
    private final double _regenBonusHP;
    private final double _regenBonusMP;
    private final int _eventId;
    private final String[] _blockedActions;
    private final int _index;
    private final int _taxById;
    private final StatsSet _params;

    @SuppressWarnings("unchecked")
    public ZoneTemplate(final StatsSet set) {
        _name = set.getString("name");
        _type = ZoneType.valueOf(set.getString("type"));
        _territory = (Territory) set.get("territory");
        _enteringMessageId = set.getInteger("entering_message_no", 0);
        _leavingMessageId = set.getInteger("leaving_message_no", 0);
        _target = ZoneTarget.valueOf(set.getString("target", "pc"));
        _affectRace = ("all".equals(set.getString("affect_race", "all")) ? null : Race.valueOf(set.getString("affect_race")));
        String s = set.getString("skill_name", null);
        Skill skill = null;
        if (s != null) {
            final String[] sk = s.split("[\\s,;]+");
            skill = SkillTable.getInstance().getInfo(Integer.parseInt(sk[0]), Integer.parseInt(sk[1]));
        }
        _skill = skill;
        _skillProb = set.getInteger("skill_prob", 100);
        _initialDelay = set.getInteger("initial_delay", 1);
        _unitTick = set.getInteger("unit_tick", 1);
        _randomTick = set.getInteger("random_time", 0);
        _moveBonus = set.getDouble("move_bonus", 0.0);
        _regenBonusHP = set.getDouble("hp_regen_bonus", 0.0);
        _regenBonusMP = set.getDouble("mp_regen_bonus", 0.0);
        _damageOnHP = set.getInteger("damage_on_hp", 0);
        _damageOnMP = set.getInteger("damage_on_mp", 0);
        _damageMessageId = set.getInteger("message_no", 0);
        _eventId = set.getInteger("eventId", 0);
        _isEnabled = set.getBool("enabled", true);
        _isDefault = set.getBool("default", true);
        _restartPoints = (List<Location>) set.get("restart_points");
        _PKrestartPoints = (List<Location>) set.get("PKrestart_points");
        _restartTime = set.getLong("restart_time", 0L);
        s = (String) set.get("blocked_actions");
        if (s != null) {
            _blockedActions = s.split("[\\s,;]+");
        } else {
            _blockedActions = null;
        }
        _index = set.getInteger("index", 0);
        _taxById = set.getInteger("taxById", 0);
        _params = set;
    }

    public boolean isEnabled() {
        return _isEnabled;
    }

    public boolean isDefault() {
        return _isDefault;
    }

    public String getName() {
        return _name;
    }

    public ZoneType getType() {
        return _type;
    }

    public Territory getTerritory() {
        return _territory;
    }

    public int getEnteringMessageId() {
        return _enteringMessageId;
    }

    public int getLeavingMessageId() {
        return _leavingMessageId;
    }

    public Skill getZoneSkill() {
        return _skill;
    }

    public int getSkillProb() {
        return _skillProb;
    }

    public int getInitialDelay() {
        return _initialDelay;
    }

    public int getUnitTick() {
        return _unitTick;
    }

    public int getRandomTick() {
        return _randomTick;
    }

    public ZoneTarget getZoneTarget() {
        return _target;
    }

    public Race getAffectRace() {
        return _affectRace;
    }

    public String[] getBlockedActions() {
        return _blockedActions;
    }

    public int getDamageMessageId() {
        return _damageMessageId;
    }

    public int getDamageOnHP() {
        return _damageOnHP;
    }

    public int getDamageOnMP() {
        return _damageOnMP;
    }

    public double getMoveBonus() {
        return _moveBonus;
    }

    public double getRegenBonusHP() {
        return _regenBonusHP;
    }

    public double getRegenBonusMP() {
        return _regenBonusMP;
    }

    public long getRestartTime() {
        return _restartTime;
    }

    public List<Location> getRestartPoints() {
        return _restartPoints;
    }

    public List<Location> getPKRestartPoints() {
        return _PKrestartPoints;
    }

    public int getIndex() {
        return _index;
    }

    public int getTaxById() {
        return _taxById;
    }

    public int getEventId() {
        return _eventId;
    }

    public MultiValueSet<String> getParams() {
        return _params.clone();
    }
}
