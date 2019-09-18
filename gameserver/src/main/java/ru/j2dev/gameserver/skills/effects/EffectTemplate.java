package ru.j2dev.gameserver.skills.effects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Effect;
import ru.j2dev.gameserver.model.EffectList;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.StatTemplate;
import ru.j2dev.gameserver.stats.conditions.Condition;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public final class EffectTemplate extends StatTemplate {
    public static final EffectTemplate[] EMPTY_ARRAY = new EffectTemplate[0];
    public static final String NO_STACK = "none";
    public static final String HP_RECOVER_CAST = "HpRecoverCast";
    private static final Logger LOGGER = LoggerFactory.getLogger(EffectTemplate.class);

    public final double _value;
    public final int _count;
    public final long _period;
    public final EffectType _effectType;
    public final String _stackType;
    public final String _stackType2;
    public final int _stackOrder;
    public final int _displayId;
    public final int _displayLevel;
    public final boolean _applyOnCaster;
    public final boolean _applyOnSummon;
    public final boolean _cancelOnAction;
    public final boolean _isReflectable;
    public final AbnormalEffect _abnormalEffect;
    public final AbnormalEffect _abnormalEffect2;
    public final AbnormalEffect _abnormalEffect3;
    private final Boolean _isSaveable;
    private final Boolean _isCancelable;
    private final Boolean _isOffensive;
    private final StatsSet _paramSet;
    private final int _chance;
    public Condition _attachCond;

    public EffectTemplate(final StatsSet set) {
        _value = set.getDouble("value");
        _count = ((set.getInteger("count", 1) < 0) ? Integer.MAX_VALUE : set.getInteger("count", 1));
        _period = Math.min(Integer.MAX_VALUE, 1000 * ((set.getInteger("time", 1) < 0) ? Integer.MAX_VALUE : set.getInteger("time", 1)));
        _abnormalEffect = set.getEnum("abnormal", AbnormalEffect.class);
        _abnormalEffect2 = set.getEnum("abnormal2", AbnormalEffect.class);
        _abnormalEffect3 = set.getEnum("abnormal3", AbnormalEffect.class);
        _stackType = set.getString("stackType", "none");
        _stackType2 = set.getString("stackType2", "none");
        _stackOrder = set.getInteger("stackOrder", ("none".equals(_stackType) && "none".equals(_stackType2)) ? 1 : 0);
        _applyOnCaster = set.getBool("applyOnCaster", false);
        _applyOnSummon = set.getBool("applyOnSummon", false);
        _cancelOnAction = set.getBool("cancelOnAction", false);
        _isReflectable = set.getBool("isReflectable", true);
        _isSaveable = (set.isSet("isSaveable") ? set.getBool("isSaveable") : null);
        _isCancelable = (set.isSet("isCancelable") ? set.getBool("isCancelable") : null);
        _isOffensive = (set.isSet("isOffensive") ? set.getBool("isOffensive") : null);
        _displayId = set.getInteger("displayId", 0);
        _displayLevel = set.getInteger("displayLevel", 0);
        _effectType = set.getEnum("name", EffectType.class);
        _chance = set.getInteger("chance", Integer.MAX_VALUE);
        _paramSet = set;
    }


    public Effect getEffect(final Env env) {
        if (_attachCond != null && !_attachCond.test(env)) {
            return null;
        }
        try {
            return _effectType.makeEffect(env, this);
        } catch (Exception e) {
            LOGGER.error("", e);
            return null;
        }
    }

    public void attachCond(final Condition c) {
        _attachCond = c;
    }

    public int getCount() {
        return _count;
    }

    public long getPeriod() {
        return _period;
    }

    public EffectType getEffectType() {
        return _effectType;
    }

    public Effect getSameByStackType(final List<Effect> list) {
        return list.stream().filter(ef -> ef != null && EffectList.checkStackType(ef.getTemplate(), this)).findFirst().orElse(null);
    }

    public Effect getSameByStackType(final EffectList list) {
        return getSameByStackType(list.getAllEffects());
    }

    public Effect getSameByStackType(final Creature actor) {
        return getSameByStackType(actor.getEffectList().getAllEffects());
    }

    public StatsSet getParam() {
        return _paramSet;
    }

    public int chance(final int val) {
        return (_chance == Integer.MAX_VALUE) ? val : _chance;
    }

    public boolean isSaveable(final boolean def) {
        return (_isSaveable != null) ? _isSaveable : def;
    }

    public boolean isCancelable(final boolean def) {
        return (_isCancelable != null) ? _isCancelable : def;
    }

    public boolean isOffensive(final boolean def) {
        return (_isOffensive != null) ? _isOffensive : def;
    }
}
