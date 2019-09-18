package ru.j2dev.gameserver.model.actor.listener;

import ru.j2dev.commons.listener.Listener;
import ru.j2dev.commons.listener.ListenerList;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.listener.actor.*;
import ru.j2dev.gameserver.listener.actor.ai.OnAiEventListener;
import ru.j2dev.gameserver.listener.actor.ai.OnAiIntentionListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;

public class CharListenerList extends ListenerList<Creature> {
    static final ListenerList<Creature> global = new ListenerList<>();

    protected final Creature actor;

    public CharListenerList(final Creature actor) {
        this.actor = actor;
    }

    public static boolean addGlobal(final Listener<Creature> listener) {
        return global.add(listener);
    }

    public static boolean removeGlobal(final Listener<Creature> listener) {
        return global.remove(listener);
    }

    public Creature getActor() {
        return actor;
    }

    public void onAiIntention(final CtrlIntention intention, final Object arg0, final Object arg1) {
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnAiIntentionListener.class::isInstance).forEach(listener -> ((OnAiIntentionListener) listener).onAiIntention(getActor(), intention, arg0, arg1));
        }
    }

    public void onAiEvent(final CtrlEvent evt, final Object[] args) {
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnAiEventListener.class::isInstance).forEach(listener -> ((OnAiEventListener) listener).onAiEvent(getActor(), evt, args));
        }
    }

    public void onAttack(final Creature target) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnAttackListener.class::isInstance).forEach(listener -> ((OnAttackListener) listener).onAttack(getActor(), target));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnAttackListener.class::isInstance).forEach(listener -> ((OnAttackListener) listener).onAttack(getActor(), target));
        }
    }

    public void onCreatureAttack(final Creature attacker) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnCreatureAttack.class::isInstance).forEach(listener -> ((OnCreatureAttack) listener).onCreatureAttack(getActor(), attacker));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnCreatureAttack.class::isInstance).forEach(listener -> ((OnCreatureAttack) listener).onCreatureAttack(getActor(), attacker));
        }
    }

    public void onCreatureAttacked(final Creature attacker) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnCreatureAttacked.class::isInstance).forEach(listener -> ((OnCreatureAttacked) listener).onCreatureAttacked(attacker, getActor()));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnCreatureAttacked.class::isInstance).forEach(listener -> ((OnCreatureAttacked) listener).onCreatureAttacked(attacker, getActor()));
        }
    }

    public void onMagicUse(final Skill skill, final Creature target, final boolean alt) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnMagicUseListener.class::isInstance).forEach(listener -> ((OnMagicUseListener) listener).onMagicUse(getActor(), skill, target, alt));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnMagicUseListener.class::isInstance).forEach(listener -> ((OnMagicUseListener) listener).onMagicUse(getActor(), skill, target, alt));
        }
    }

    public void onMagicHit(final Skill skill, final Creature caster) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnMagicHitListener.class::isInstance).forEach(listener -> ((OnMagicHitListener) listener).onMagicHit(getActor(), skill, caster));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnMagicHitListener.class::isInstance).forEach(listener -> ((OnMagicHitListener) listener).onMagicHit(getActor(), skill, caster));
        }
    }

    public void onDeath(final Creature killer) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnDeathListener.class::isInstance).forEach(listener -> ((OnDeathListener) listener).onDeath(getActor(), killer));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnDeathListener.class::isInstance).forEach(listener -> ((OnDeathListener) listener).onDeath(getActor(), killer));
        }
    }

    public void onKill(final Creature victim) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(listener -> OnKillListener.class.isInstance(listener) && !((OnKillListener) listener).ignorePetOrSummon()).forEach(listener -> ((OnKillListener) listener).onKill(getActor(), victim));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(listener -> OnKillListener.class.isInstance(listener) && !((OnKillListener) listener).ignorePetOrSummon()).forEach(listener -> ((OnKillListener) listener).onKill(getActor(), victim));
        }
    }

    public void onKillIgnorePetOrSummon(final Creature victim) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(listener -> OnKillListener.class.isInstance(listener) && ((OnKillListener) listener).ignorePetOrSummon()).forEach(listener -> ((OnKillListener) listener).onKill(getActor(), victim));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(listener -> OnKillListener.class.isInstance(listener) && ((OnKillListener) listener).ignorePetOrSummon()).forEach(listener -> ((OnKillListener) listener).onKill(getActor(), victim));
        }
    }

    public void onCurrentHpDamage(final double damage, final Creature attacker, final Skill skill) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnCurrentHpDamageListener.class::isInstance).forEach(listener -> ((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnCurrentHpDamageListener.class::isInstance).forEach(listener -> ((OnCurrentHpDamageListener) listener).onCurrentHpDamage(getActor(), damage, attacker, skill));
        }
    }

    public void onCurrentMpReduce(final double consumed, final Creature attacker) {
        if (!global.getListeners().isEmpty()) {
            global.getListeners().stream().filter(OnCurrentMpReduceListener.class::isInstance).forEach(listener -> ((OnCurrentMpReduceListener) listener).onCurrentMpReduce(getActor(), consumed, attacker));
        }
        if (!getListeners().isEmpty()) {
            getListeners().stream().filter(OnCurrentMpReduceListener.class::isInstance).forEach(listener -> ((OnCurrentMpReduceListener) listener).onCurrentMpReduce(getActor(), consumed, attacker));
        }
    }
}
