package ru.j2dev.gameserver.ai;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.model.items.attachment.FlagItemAttachment;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;

public class PlayerAI extends PlayableAI {
    public PlayerAI(final Player actor) {
        super(actor);
    }

    @Override
    protected void onIntentionRest() {
        changeIntention(CtrlIntention.AI_INTENTION_REST, null, null);
        setAttackTarget(null);
        clientStopMoving();
    }

    @Override
    protected void onIntentionActive() {
        clearNextAction();
        changeIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
    }

    @Override
    public void onIntentionInteract(final GameObject object) {
        final Player actor = getActor();
        if (actor.getSittingTask()) {
            setNextAction(NextAction.INTERACT, object, null, false, false);
            return;
        }
        if (actor.isSitting()) {
            actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
            clientActionFailed();
            return;
        }
        super.onIntentionInteract(object);
    }

    @Override
    public void onIntentionPickUp(final GameObject object) {
        final Player actor = getActor();
        if (actor.getSittingTask()) {
            setNextAction(NextAction.PICKUP, object, null, false, false);
            return;
        }
        if (actor.isSitting()) {
            actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
            clientActionFailed();
            return;
        }
        super.onIntentionPickUp(object);
    }

    @Override
    protected void thinkAttack(final boolean checkRange) {
        final Player actor = getActor();
        if (actor.isInFlyingTransform()) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            return;
        }
        final FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
        if (attachment != null && !attachment.canAttack(actor)) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }
        if (actor.isFrozen()) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
            return;
        }
        super.thinkAttack(checkRange);
    }

    @Override
    protected void thinkCast(final boolean checkRange) {
        final Player actor = getActor();
        final FlagItemAttachment attachment = actor.getActiveWeaponFlagAttachment();
        if (attachment != null && !attachment.canCast(actor, _skill)) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendActionFailed();
            return;
        }
        if (actor.isFrozen()) {
            setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
            actor.sendPacket(SystemMsg.YOU_CANNOT_MOVE_WHILE_FROZEN, ActionFail.STATIC);
            return;
        }
        super.thinkCast(checkRange);
    }

    @Override
    public void Attack(final GameObject target, final boolean forceUse, final boolean dontMove) {
        final Player actor = getActor();
        if (actor.isInFlyingTransform()) {
            actor.sendActionFailed();
            return;
        }
        if (System.currentTimeMillis() - actor.getLastAttackPacket() < Config.ATTACK_PACKET_DELAY) {
            actor.sendActionFailed();
            return;
        }
        actor.setLastAttackPacket();
        if (actor.getSittingTask()) {
            setNextAction(NextAction.ATTACK, target, null, forceUse, false);
            return;
        }
        if (actor.isSitting()) {
            actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
            clientActionFailed();
            return;
        }
        super.Attack(target, forceUse, dontMove);
    }

    @Override
    public void Cast(final Skill skill, final Creature target, final boolean forceUse, final boolean dontMove) {
        final Player actor = getActor();
        if (!skill.altUse() && !skill.isToggle() && (skill.getSkillType() != SkillType.CRAFT || !Config.ALLOW_TALK_WHILE_SITTING)) {
            if (actor.getSittingTask()) {
                setNextAction(NextAction.CAST, skill, target, forceUse, dontMove);
                clientActionFailed();
                return;
            }
            if (skill.getSkillType() == SkillType.SUMMON && actor.getPrivateStoreType() != Player.STORE_PRIVATE_NONE) {
                actor.sendPacket(Msg.YOU_CANNOT_SUMMON_DURING_A_TRADE_OR_WHILE_USING_THE_PRIVATE_SHOPS);
                clientActionFailed();
                return;
            }
            if (actor.isSitting()) {
                if (skill.getSkillType() == SkillType.TRANSFORMATION) {
                    actor.sendPacket(Msg.YOU_CANNOT_TRANSFORM_WHILE_SITTING);
                } else {
                    actor.sendPacket(Msg.YOU_CANNOT_MOVE_WHILE_SITTING);
                }
                clientActionFailed();
                return;
            }
        }
        super.Cast(skill, target, forceUse, dontMove);
    }

    @Override
    public Player getActor() {
        return (Player) super.getActor();
    }
}
