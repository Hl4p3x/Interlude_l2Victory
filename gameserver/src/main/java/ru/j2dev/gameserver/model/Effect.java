package ru.j2dev.gameserver.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.listener.actor.OnAttackListener;
import ru.j2dev.gameserver.listener.actor.OnMagicUseListener;
import ru.j2dev.gameserver.network.lineage2.serverpackets.*;
import ru.j2dev.gameserver.skills.AbnormalEffect;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.stats.funcs.Func;
import ru.j2dev.gameserver.stats.funcs.FuncOwner;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.taskmanager.EffectTaskManager;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class Effect extends RunnableImpl implements Comparable<Effect>, FuncOwner {
    public static final Effect[] EMPTY_L2EFFECT_ARRAY = new Effect[0];
    protected static final Logger LOGGER = LoggerFactory.getLogger(Effect.class);
    public static int SUSPENDED = -1;
    public static int STARTING;
    public static int STARTED = 1;
    public static int ACTING = 2;
    public static int FINISHING = 3;
    public static int FINISHED = 4;

    protected final Creature _effector;
    protected final Creature _effected;
    protected final Skill _skill;
    protected final int _displayId;
    protected final int _displayLevel;
    protected final EffectTemplate _template;
    private final double _value;
    private final AtomicInteger _state = new AtomicInteger(STARTING);
    private final EEffectSlot _eEffSlot;
    private int _count;
    private long _period;
    private long _startTimeMillis;
    private long _duration;
    private boolean _inUse;
    private Effect _next;
    private boolean _active;
    private Future<?> _effectTask;
    private ActionDispelListener _listener;

    protected Effect(final Env env, final EffectTemplate template) {
        _skill = env.skill;
        _effector = env.character;
        _effected = env.target;
        _template = template;
        _value = template._value;
        _count = template.getCount();
        _period = template.getPeriod();
        _duration = _period * _count;
        _displayId = ((template._displayId != 0) ? template._displayId : _skill.getDisplayId());
        _displayLevel = ((template._displayLevel != 0) ? template._displayLevel : _skill.getDisplayLevel());
        if (_skill.isOffensive()) {
            _eEffSlot = EEffectSlot.EFFECT_SLOT_DEBUFF;
        } else {
            _eEffSlot = EEffectSlot.EFFECT_SLOT_NORMAL;
        }
    }

    public long getPeriod() {
        return _period;
    }

    public void setPeriod(final long time) {
        _period = time;
        _duration = _period * _count;
    }

    public int getCount() {
        return _count;
    }

    public void setCount(final int count) {
        _count = count;
        _duration = _period * _count;
    }

    public boolean isOneTime() {
        return _period == 0L;
    }

    public long getStartTime() {
        if (_startTimeMillis == 0L) {
            return System.currentTimeMillis();
        }
        return _startTimeMillis;
    }

    public long getTime() {
        return System.currentTimeMillis() - getStartTime();
    }

    public long getDuration() {
        return _duration;
    }

    public int getTimeLeft() {
        return (int) ((getDuration() - getTime()) / 1000L);
    }

    public boolean isTimeLeft() {
        return getDuration() - getTime() > 0L;
    }

    public boolean isInUse() {
        return _inUse;
    }

    public void setInUse(final boolean inUse) {
        _inUse = inUse;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(final boolean set) {
        _active = set;
    }

    public EffectTemplate getTemplate() {
        return _template;
    }

    public EEffectSlot getEffectSlot() {
        return _eEffSlot;
    }

    public String getStackType() {
        return getTemplate()._stackType;
    }

    public String getStackType2() {
        return getTemplate()._stackType2;
    }

    public boolean isStackTypeMatch(final String... params) {
        final String thisStackType = getStackType();
        final String thisStackType2 = getStackType2();
        for (String param : params) {
            if (param.equalsIgnoreCase(thisStackType)) {
                return true;
            }
            if (param.equalsIgnoreCase(thisStackType2)) {
                return true;
            }
        }
        return false;
    }

    public boolean isStackTypeMatch(final Effect param) {
        return isStackTypeMatch(param.getStackType()) || isStackTypeMatch(param.getStackType2());
    }

    public int getStackOrder() {
        return getTemplate()._stackOrder;
    }

    public Skill getSkill() {
        return _skill;
    }

    public Creature getEffector() {
        return _effector;
    }

    public Creature getEffected() {
        return _effected;
    }

    public double calc() {
        return _value;
    }

    public boolean isEnded() {
        return isFinished() || isFinishing();
    }

    public boolean isFinishing() {
        return getState() == FINISHING;
    }

    public boolean isFinished() {
        return getState() == FINISHED;
    }

    private int getState() {
        return _state.get();
    }

    private boolean setState(final int oldState, final int newState) {
        return _state.compareAndSet(oldState, newState);
    }

    public boolean checkCondition() {
        return true;
    }

    protected void onStart() {
        getEffected().addStatFuncs(getStatFuncs());
        getEffected().addTriggers(getTemplate());
        if (getTemplate()._abnormalEffect != AbnormalEffect.NULL) {
            getEffected().startAbnormalEffect(getTemplate()._abnormalEffect);
        } else if (getEffectType().getAbnormal() != null) {
            getEffected().startAbnormalEffect(getEffectType().getAbnormal());
        }
        if (getTemplate()._abnormalEffect2 != AbnormalEffect.NULL) {
            getEffected().startAbnormalEffect(getTemplate()._abnormalEffect2);
        }
        if (getTemplate()._abnormalEffect3 != AbnormalEffect.NULL) {
            getEffected().startAbnormalEffect(getTemplate()._abnormalEffect3);
        }
        if (_template._cancelOnAction) {
            getEffected().addListener(_listener = new ActionDispelListener());
        }
        if (getEffected().isPlayer() && !getSkill().canUseTeleport()) {
            getEffected().getPlayer().getPlayerAccess().UseTeleport = false;
        }
    }

    protected abstract boolean onActionTime();

    protected void onExit() {
        getEffected().removeStatsOwner(this);
        getEffected().removeTriggers(getTemplate());
        if (getTemplate()._abnormalEffect != AbnormalEffect.NULL) {
            getEffected().stopAbnormalEffect(getTemplate()._abnormalEffect);
        } else if (getEffectType().getAbnormal() != null) {
            getEffected().stopAbnormalEffect(getEffectType().getAbnormal());
        }
        if (getTemplate()._abnormalEffect2 != AbnormalEffect.NULL) {
            getEffected().stopAbnormalEffect(getTemplate()._abnormalEffect2);
        }
        if (getTemplate()._abnormalEffect3 != AbnormalEffect.NULL) {
            getEffected().stopAbnormalEffect(getTemplate()._abnormalEffect3);
        }
        if (_template._cancelOnAction) {
            getEffected().removeListener(_listener);
        }
        if (getEffected().isPlayer() && isStackTypeMatch("HpRecoverCast")) {
            getEffected().sendPacket(new ShortBuffStatusUpdate());
        }
        if (getEffected().isPlayer() && !getSkill().canUseTeleport() && !getEffected().getPlayer().getPlayerAccess().UseTeleport) {
            getEffected().getPlayer().getPlayerAccess().UseTeleport = true;
        }
    }

    private void stopEffectTask() {
        if (_effectTask != null) {
            _effectTask.cancel(false);
        }
    }

    private void startEffectTask() {
        if (_effectTask == null) {
            _startTimeMillis = System.currentTimeMillis();
            _effectTask = EffectTaskManager.getInstance().scheduleAtFixedRate(this, _period, _period);
        }
    }

    public final void schedule() {
        final Creature effected = getEffected();
        if (effected == null) {
            return;
        }
        if (!checkCondition()) {
            return;
        }
        getEffected().getEffectList().addEffect(this);
    }

    private void suspend() {
        if (setState(STARTING, SUSPENDED)) {
            startEffectTask();
        } else if (setState(STARTED, SUSPENDED) || setState(ACTING, SUSPENDED)) {
            synchronized (this) {
                if (isInUse()) {
                    setInUse(false);
                    setActive(false);
                    onExit();
                }
            }
            getEffected().getEffectList().removeEffect(this);
        }
    }

    public final void start() {
        if (setState(STARTING, STARTED)) {
            synchronized (this) {
                if (isInUse()) {
                    setActive(true);
                    onStart();
                    startEffectTask();
                }
            }
        }
        run();
    }

    @Override
    public final void runImpl() {
        if (setState(STARTED, ACTING)) {
            if (!getSkill().isHideStartMessage() && !getSkill().isToggle() && getEffected().getEffectList().getEffectsCountForSkill(getSkill().getId()) == 1) {
                getEffected().sendPacket(new SystemMessage(110).addSkillName(_displayId, _displayLevel));
            }
            if (getSkill().getSecondSkill() > 0) {
                SkillTable.getInstance().getInfo(getSkill().getSecondSkill(), 1).getEffects(_effector, _effected, false, false);
            }
            return;
        }
        if (getState() == SUSPENDED) {
            if (isTimeLeft()) {
                _count--;
                if (isTimeLeft()) {
                    return;
                }
            }
            exit();
            return;
        }
        if (getState() == ACTING && isTimeLeft()) {
            _count--;
            if ((!isActive() || onActionTime()) && isTimeLeft()) {
                return;
            }
        }
        if (setState(ACTING, FINISHING)) {
            setInUse(false);
        }
        if (setState(FINISHING, FINISHED)) {
            synchronized (this) {
                setActive(false);
                stopEffectTask();
                onExit();
            }
            final Effect next = getNext();
            if (next != null && next.setState(SUSPENDED, STARTING)) {
                next.schedule();
            }
            if (getSkill().getDelayedEffect() > 0) {
                SkillTable.getInstance().getInfo(getSkill().getDelayedEffect(), 1).getEffects(_effector, _effected, false, false);
            }
            final boolean msg = !isHidden() && getEffected().getEffectList().getEffectsCountForSkill(getSkill().getId()) == 1;
            getEffected().getEffectList().removeEffect(this);
            if (msg) {
                getEffected().sendPacket(new SystemMessage(92).addSkillName(_displayId, _displayLevel));
            }
        }
    }

    public void exit() {
        final Effect next = getNext();
        if (next != null) {
            next.exit();
        }
        removeNext();
        if (setState(STARTING, FINISHED)) {
            getEffected().getEffectList().removeEffect(this);
        } else if (setState(SUSPENDED, FINISHED)) {
            stopEffectTask();
        } else if (setState(STARTED, FINISHED) || setState(ACTING, FINISHED)) {
            synchronized (this) {
                if (isInUse()) {
                    setInUse(false);
                    setActive(false);
                    stopEffectTask();
                    onExit();
                }
            }
            getEffected().getEffectList().removeEffect(this);
        }
    }

    private boolean scheduleNext(final Effect e) {
        if (e == null || e.isEnded()) {
            return false;
        }
        final Effect next = getNext();
        if (next != null && !next.maybeScheduleNext(e)) {
            return false;
        }
        _next = e;
        return true;
    }

    public Effect getNext() {
        return _next;
    }

    private void removeNext() {
        _next = null;
    }

    public boolean maybeScheduleNext(final Effect newEffect) {
        if (newEffect.getStackOrder() < getStackOrder()) {
            if (newEffect.getTimeLeft() > getTimeLeft()) {
                newEffect.suspend();
                scheduleNext(newEffect);
            }
            return false;
        }
        if (newEffect.getTimeLeft() >= getTimeLeft()) {
            if (getNext() != null && getNext().getTimeLeft() > newEffect.getTimeLeft()) {
                newEffect.scheduleNext(getNext());
                removeNext();
            }
            exit();
        } else {
            suspend();
            newEffect.scheduleNext(this);
        }
        return true;
    }

    public Func[] getStatFuncs() {
        return getTemplate().getStatFuncs(this);
    }

    public void addIcon(final AbnormalStatusUpdate mi) {
        if (!isActive() || isHidden()) {
            return;
        }
        final int duration = _skill.isToggle() ? -1 : getTimeLeft();
        mi.addEffect(_displayId, _displayLevel, duration);
    }

    public void addPartySpelledIcon(final PartySpelled ps) {
        if (!isActive() || isHidden()) {
            return;
        }
        final int duration = _skill.isToggle() ? -1 : getTimeLeft();
        ps.addPartySpelledEffect(_displayId, _displayLevel, duration);
    }

    public void addOlympiadSpelledIcon(final Player player, final ExOlympiadSpelledInfo os) {
        if (!isActive() || isHidden()) {
            return;
        }
        final int duration = _skill.isToggle() ? -1 : getTimeLeft();
        os.addSpellRecivedPlayer(player);
        os.addEffect(_displayId, _displayLevel, duration);
    }

    protected int getLevel() {
        return _skill.getLevel();
    }

    public EffectType getEffectType() {
        return getTemplate()._effectType;
    }

    public boolean isHidden() {
        return _displayId < 0;
    }

    @Override
    public int compareTo(final Effect obj) {
        if (obj.equals(this)) {
            return 0;
        }
        return 1;
    }

    public boolean isSaveable() {
        return _template.isSaveable(getSkill().isSaveable()) && getTimeLeft() >= Config.ALT_SAVE_EFFECTS_REMAINING_TIME;
    }

    public int getDisplayId() {
        return _displayId;
    }

    public int getDisplayLevel() {
        return _displayLevel;
    }

    public boolean isCancelable() {
        return _template.isCancelable(getSkill().isCancelable());
    }

    @Override
    public String toString() {
        return "Skill: " + _skill + ", state: " + getState() + ", inUse: " + _inUse + ", active : " + _active;
    }

    @Override
    public boolean isFuncEnabled() {
        return isInUse();
    }

    @Override
    public boolean overrideLimits() {
        return false;
    }

    public boolean isOffensive() {
        return _template.isOffensive(getSkill().isOffensive());
    }

    public enum EEffectSlot {
        EFFECT_SLOT_NORMAL,
        EFFECT_SLOT_DEBUFF;

        public static final EEffectSlot[] VALUES = values();

    }

    private class ActionDispelListener implements OnAttackListener, OnMagicUseListener {
        @Override
        public void onMagicUse(final Creature actor, final Skill skill, final Creature target, final boolean alt) {
            exit();
        }

        @Override
        public void onAttack(final Creature attacker, final Creature target) {
            exit();
        }
    }
}
