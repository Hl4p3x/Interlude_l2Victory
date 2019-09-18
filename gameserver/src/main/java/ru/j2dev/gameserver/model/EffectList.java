package ru.j2dev.gameserver.model;

import gnu.trove.list.array.TIntArrayList;
import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.skills.EffectType;
import ru.j2dev.gameserver.skills.effects.EffectTemplate;
import ru.j2dev.gameserver.skills.skillclasses.Transformation;
import ru.j2dev.gameserver.stats.funcs.FuncTemplate;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class EffectList {
    public static final int NONE_SLOT_TYPE = -1;
    public static final int BUFF_SLOT_TYPE = 0;
    public static final int MUSIC_SLOT_TYPE = 1;
    public static final int TRIGGER_SLOT_TYPE = 2;
    public static final int DEBUFF_SLOT_TYPE = 3;
    public static final int DEBUFF_LIMIT = 8;
    public static final int MUSIC_LIMIT = 12;
    public static final int TRIGGER_LIMIT = 12;
    private final Creature _actor;
    private final Lock lock = new ReentrantLock();
    private List<Effect> _effects;

    public EffectList(final Creature owner) {
        _actor = owner;
    }

    public static int getSlotType(final Effect e) {
        if (e.getSkill().isPassive() || e.getSkill().isSlotNone() || e.getSkill().isToggle() || e.getSkill() instanceof Transformation || e.isStackTypeMatch("HpRecoverCast") || e.getEffectType() == EffectType.Cubic) {
            return NONE_SLOT_TYPE;
        }
        if (e.getSkill().isOffensive()) {
            return DEBUFF_SLOT_TYPE;
        }
        if (e.getSkill().isTrigger()) {
            return MUSIC_SLOT_TYPE;
        }
        return BUFF_SLOT_TYPE;
    }

    public static boolean checkStackType(final EffectTemplate ef1, final EffectTemplate ef2) {
        return (!"none".equals(ef1._stackType) && ef1._stackType.equalsIgnoreCase(ef2._stackType)) || (!"none".equals(ef1._stackType) && ef1._stackType.equalsIgnoreCase(ef2._stackType2)) || (!"none".equals(ef1._stackType2) && ef1._stackType2.equalsIgnoreCase(ef2._stackType)) || (!"none".equals(ef1._stackType2) && ef1._stackType2.equalsIgnoreCase(ef2._stackType2));
    }

    public int getEffectsCountForSkill(final int skill_id) {
        if (isEmpty()) {
            return 0;
        }
        return (int) _effects.stream().filter(e -> e.getSkill().getId() == skill_id).count();
    }

    public Effect getEffectByType(final EffectType et) {
        if (isEmpty()) {
            return null;
        }
        return _effects.stream().filter(e -> e.getEffectType() == et).findFirst().orElse(null);
    }

    public List<Effect> getEffectsBySkill(final Skill skill) {
        if (skill == null) {
            return null;
        }
        return getEffectsBySkillId(skill.getId());
    }

    public int getActiveMusicCount(final int skillId) {
        if (isEmpty()) {
            return 0;
        }
        return (int) _effects.stream().filter(e -> Config.ALT_ADDITIONAL_DANCE_SONG_MANA_CONSUME && e.getSkill().isMusic() && e.getSkill().getId() != skillId && e.getTimeLeft() > Config.ALT_MUSIC_COST_GUARD_INTERVAL).count();
    }

    public List<Effect> getEffectsBySkillId(final int skillId) {
        if (isEmpty()) {
            return null;
        }
        final List<Effect> list = _effects.stream().filter(e -> e.getSkill().getId() == skillId).collect(Collectors.toCollection(() -> new ArrayList<>(2)));
        return list.isEmpty() ? null : list;
    }

    public Effect getEffectByIndexAndType(final int skillId, final EffectType type) {
        if (isEmpty()) {
            return null;
        }
        return _effects.stream().filter(e -> e.getSkill().getId() == skillId && e.getEffectType() == type).findFirst().orElse(null);
    }

    public Effect getEffectByStackType(final String type) {
        if (isEmpty()) {
            return null;
        }
        return _effects.stream().filter(e -> e.getStackType().equals(type)).findFirst().orElse(null);
    }

    public boolean containEffectFromSkills(final int... skillIds) {
        if (isEmpty()) {
            return false;
        }
        return _effects.stream().mapToInt(e -> e.getSkill().getId()).anyMatch(skillId -> ArrayUtils.contains(skillIds, skillId));
    }

    public List<Effect> getAllEffects() {
        if (isEmpty()) {
            return Collections.emptyList();
        }
        return new ArrayList<>(_effects);
    }

    public boolean isEmpty() {
        return _effects == null || _effects.isEmpty();
    }

    public Effect[] getAllFirstEffects() {
        if (isEmpty()) {
            return Effect.EMPTY_L2EFFECT_ARRAY;
        }
        final LinkedHashMap<Skill, Effect> map = new LinkedHashMap<>();
        _effects.stream().filter(e -> !map.containsKey(e.getSkill())).forEach(e -> map.put(e.getSkill(), e));
        return map.values().toArray(new Effect[0]);
    }

    private void checkSlotLimit(final Effect newEffect) {
        if (_effects == null) {
            return;
        }
        final int slotType = getSlotType(newEffect);
        if (slotType == -1) {
            return;
        }
        int size = 0;
        final TIntArrayList skillIds = new TIntArrayList();
        for (final Effect e : _effects) {
            if (e.isInUse()) {
                if (e.getSkill().equals(newEffect.getSkill())) {
                    return;
                }
                if (skillIds.contains(e.getSkill().getId())) {
                    continue;
                }
                final int subType = getSlotType(e);
                if (subType != slotType) {
                    continue;
                }
                ++size;
                skillIds.add(e.getSkill().getId());
            }
        }
        int limit = 0;
        switch (slotType) {
            case BUFF_SLOT_TYPE: {
                limit = _actor.getBuffLimit();
                break;
            }
            case DEBUFF_SLOT_TYPE: {
                limit = Config.ALT_DEBUFF_LIMIT;
                break;
            }
            case TRIGGER_SLOT_TYPE: {
                limit = TRIGGER_LIMIT + Config.ALT_TRIGGER_SLOT_ADDER;
                break;
            }
        }
        if (size < limit) {
            return;
        }
        int skillId = _effects.stream().filter(e2 -> e2.isInUse() && getSlotType(e2) == slotType).findFirst().map(e2 -> e2.getSkill().getId()).orElse(0);
        if (skillId != 0) {
            stopEffect(skillId);
        }
    }

    public void addEffect(final Effect effect) {
        final double hp = _actor.getCurrentHp();
        final double mp = _actor.getCurrentMp();
        final double cp = _actor.getCurrentCp();
        final String stackType = effect.getStackType();
        boolean add;
        final HashSet<Skill> removed = new HashSet<>();
        lock.lock();
        try {
            if (_effects == null) {
                _effects = new CopyOnWriteArrayList<>();
            }
            if ("none".equals(stackType)) {
                for (final Effect e : _effects) {
                    if (!e.isInUse()) {
                        continue;
                    }
                    if (e.getSkill().getId() != effect.getSkill().getId() || e.getEffectType() != effect.getEffectType() || !"none".equals(e.getStackType())) {
                        continue;
                    }
                    if (effect.getTimeLeft() <= e.getTimeLeft()) {
                        return;
                    }
                    removed.add(e.getSkill());
                    e.exit();
                }
            } else {
                for (final Effect e : _effects) {
                    if (!e.isInUse()) {
                        continue;
                    }
                    if (!checkStackType(e.getTemplate(), effect.getTemplate())) {
                        continue;
                    }
                    if (e.getSkill().getId() == effect.getSkill().getId() && e.getEffectType() != effect.getEffectType()) {
                        break;
                    }
                    if (e.getStackOrder() == -1) {
                        return;
                    }
                    if (!e.maybeScheduleNext(effect)) {
                        return;
                    }
                    removed.add(e.getSkill());
                }
            }
            checkSlotLimit(effect);
            if (add = _effects.add(effect)) {
                effect.setInUse(true);
            }
        } finally {
            lock.unlock();
        }
        if (!add) {
            return;
        }
        if (!removed.isEmpty()) {
            removed.forEach(s -> effect.getEffected().sendPacket(new SystemMessage(92).addSkillName(s.getDisplayId(), s.getDisplayLevel())));
        }
        effect.start();
        for (final FuncTemplate ft : effect.getTemplate().getAttachedFuncs()) {
            switch (ft.getStat()) {
                case MAX_HP:
                    _actor.setCurrentHp(hp, false);
                    break;
                case MAX_MP:
                    _actor.setCurrentMp(mp);
                    break;
                case MAX_CP:
                    _actor.setCurrentCp(cp);
                    break;
            }
        }
        _actor.updateStats();
        _actor.updateEffectIcons();
    }

    public void removeEffect(final Effect effect) {
        if (effect == null) {
            return;
        }
        boolean remove;
        lock.lock();
        try {
            if (_effects == null) {
                return;
            }
            if (!(remove = _effects.remove(effect))) {
                return;
            }
        } finally {
            lock.unlock();
        }
        if (!remove) {
            return;
        }
        _actor.updateStats();
        _actor.updateEffectIcons();
    }

    public void stopAllEffects() {
        if (isEmpty()) {
            return;
        }
        lock.lock();
        try {
            _effects.forEach(Effect::exit);
        } finally {
            lock.unlock();
        }
        _actor.updateStats();
        _actor.updateEffectIcons();
    }

    public void stopEffect(final int skillId) {
        if (isEmpty()) {
            return;
        }
        _effects.stream().filter(e -> e.getSkill().getId() == skillId).forEach(Effect::exit);
    }

    public void stopEffect(final Skill skill) {
        if (skill != null) {
            stopEffect(skill.getId());
        }
    }

    public void stopEffectByDisplayId(final int skillId) {
        if (isEmpty()) {
            return;
        }
        _effects.stream().filter(e -> e.getSkill().getDisplayId() == skillId).forEach(Effect::exit);
    }

    public void stopEffects(final EffectType type) {
        if (isEmpty()) {
            return;
        }
        _effects.stream().filter(e -> e.getEffectType() == type).forEach(Effect::exit);
    }

    public void stopAllSkillEffects(final EffectType type) {
        if (isEmpty()) {
            return;
        }
        _effects.stream().filter(e -> e.getEffectType() == type).mapToInt(e -> e.getSkill().getId()).forEach(this::stopEffect);
    }
}
