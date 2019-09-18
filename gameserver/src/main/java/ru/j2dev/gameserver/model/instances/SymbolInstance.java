package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.taskmanager.EffectTaskManager;
import ru.j2dev.gameserver.taskmanager.tasks.objecttasks.DeleteTask;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;

public class SymbolInstance extends NpcInstance {
    private final Creature _owner;
    private final Skill _skill;
    private ScheduledFuture<?> _targetTask;
    private ScheduledFuture<?> _destroyTask;

    public SymbolInstance(final int objectId, final NpcTemplate template, final Creature owner, final Skill skill) {
        super(objectId, template);
        _owner = owner;
        _skill = skill;
        setReflection(owner.getReflection());
        setLevel(owner.getLevel());
        setTitle(owner.getName());
    }

    public Creature getOwner() {
        return _owner;
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        _destroyTask = ThreadPoolManager.getInstance().schedule(new DeleteTask(this), 120000L);
        _targetTask = EffectTaskManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
            @Override
            public void runImpl() {
                getAroundCharacters(200, 200).stream().filter(target -> _skill.checkTarget(_owner, target, null, false, false) == null).forEach(target -> {
                    final List<Creature> targets = new ArrayList<>();
                    if (!_skill.isAoE()) {
                        targets.add(target);
                    } else {
                        getAroundCharacters(_skill.getSkillRadius(), 128).stream().filter(t -> _skill.checkTarget(_owner, t, null, false, false) == null).map(t -> target).forEach(targets::add);
                    }
                    _skill.useSkill(SymbolInstance.this, targets);
                });
            }
        }, 1000L, Rnd.get(4000L, 7000L));
    }

    @Override
    protected void onDelete() {
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
        player.sendActionFailed();
    }

    @Override
    public Clan getClan() {
        return null;
    }
}
