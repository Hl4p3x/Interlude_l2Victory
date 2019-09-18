package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillTargetType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MyTargetSelected;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcInfo;
import ru.j2dev.gameserver.taskmanager.EffectTaskManager;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.DeleteTask;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public final class TrapInstance extends NpcInstance {
    private final HardReference<? extends Creature> _ownerRef;
    private final Skill _skill;
    private ScheduledFuture<?> _targetTask;
    private ScheduledFuture<?> _destroyTask;
    private boolean _detected;

    public TrapInstance(final int objectId, final NpcTemplate template, final Creature owner, final Skill skill) {
        this(objectId, template, owner, skill, owner.getLoc());
    }

    public TrapInstance(final int objectId, final NpcTemplate template, final Creature owner, final Skill skill, final Location loc) {
        super(objectId, template);
        _ownerRef = owner.getRef();
        _skill = skill;
        setReflection(owner.getReflection());
        setLevel(owner.getLevel());
        setTitle(owner.getName());
        setLoc(loc);
    }

    @Override
    public boolean isTrap() {
        return true;
    }

    public Creature getOwner() {
        return _ownerRef.get();
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        _destroyTask = ThreadPoolManager.getInstance().schedule(new DeleteTask(this), 120000L);
        _targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new CastTask(this), 250L, 250L);
    }

    @Override
    public void broadcastCharInfo() {
        if (!isDetected()) {
            return;
        }
        super.broadcastCharInfo();
    }

    @Override
    protected void onDelete() {
        final Creature owner = getOwner();
        if (owner != null && owner.isPlayer()) {
            ((Player) owner).removeTrap(this);
        }
        if (_destroyTask != null) {
            _destroyTask.cancel(false);
        }
        _destroyTask = null;
        if (_targetTask != null) {
            _targetTask.cancel(false);
        }
        _targetTask = null;
        super.onDelete();
    }

    public boolean isDetected() {
        return _detected;
    }

    public void setDetected(final boolean detected) {
        _detected = detected;
    }

    @Override
    public int getPAtk(final Creature target) {
        final Creature owner = getOwner();
        return (owner == null) ? 0 : owner.getPAtk(target);
    }

    @Override
    public int getMAtk(final Creature target, final Skill skill) {
        final Creature owner = getOwner();
        return (owner == null) ? 0 : owner.getMAtk(target, skill);
    }

    @Override
    public boolean hasRandomAnimation() {
        return false;
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return false;
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        return false;
    }

    @Override
    public boolean isInvul() {
        return true;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

    @Override
    public boolean isLethalImmune() {
        return true;
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
    }

    @Override
    public void showChatWindow(final Player player, final String filename, final Object... replace) {
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
    }

    @Override
    public void onAction(final Player player, final boolean shift) {
        if (player.getTarget() != this) {
            player.setTarget(this);
            if (player.getTarget() == this) {
                player.sendPacket(new MyTargetSelected(getObjectId(), player.getLevel()));
            }
        }
        player.sendActionFailed();
    }

    @Override
    public List<L2GameServerPacket> addPacketList(final Player forPlayer, final Creature dropper) {
        if (!isDetected() && getOwner() != forPlayer) {
            return Collections.emptyList();
        }
        return Collections.singletonList(new NpcInfo(this, forPlayer));
    }

    private static class CastTask extends RunnableImpl {
        private final HardReference<NpcInstance> _trapRef;

        public CastTask(final TrapInstance trap) {
            _trapRef = trap.getRef();
        }

        @Override
        public void runImpl() {
            final TrapInstance trap = (TrapInstance) _trapRef.get();
            if (trap == null) {
                return;
            }
            final Creature owner = trap.getOwner();
            if (owner == null) {
                return;
            }
            for (final Creature target : trap.getAroundCharacters(200, 200)) {
                if (target != owner && trap._skill.checkTarget(owner, target, null, false, false) == null) {
                    final List<Creature> targets = new ArrayList<>();
                    if (trap._skill.getTargetType() != SkillTargetType.TARGET_AREA) {
                        targets.add(target);
                    } else {
                        for (final Creature t : trap.getAroundCharacters(trap._skill.getSkillRadius(), 128)) {
                            if (trap._skill.checkTarget(owner, t, null, false, false) == null) {
                                targets.add(target);
                            }
                        }
                    }
                    trap._skill.useSkill(trap, targets);
                    if (target.isPlayer()) {
                        target.sendMessage(new CustomMessage("common.Trap", target.getPlayer()));
                    }
                    trap.deleteMe();
                    break;
                }
            }
        }
    }
}
