package ru.j2dev.gameserver.model.instances;

import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcInfo;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Future;

public final class AgathionInstance extends AgathionNpcInstance {

    private static final int MAX_DISTANCE_FROM_OWNER = 2000;
    private static final int MAX_DISTANCE_FOR_BUFF = 300;
    private static final int MAX_DURATION = 1200000;
    private static final int DURATION_CHECK_INTERVAL = 60000;
    private static final int DURATION_INCREASE_INTERVAL = 20000;
    private static final int BUFF_INTERVAL = 30000;

    private HardReference<Player> _playerRef;
    private Future<?> _durationCheckTask;
    private Future<?> _buffTask;
    private TIntObjectHashMap<Skill> _skills;
    public AgathionInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _playerRef = HardReferences.emptyRef();
        _durationCheckTask = null;
        _buffTask = null;
        _skills = getTemplate().getSkills();
    }
    public void buffOwner() {
        if (!isInRange(getPlayer(), MAX_DISTANCE_FOR_BUFF)) {
            getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getPlayer());
            return;
        }
        int delay = 0;
     //   Map<Integer, Skill> _skills = (Map<Integer, Skill>) _agathion.getTemplate().getSkills();
       // final TIntObjectHashMap<Skill> _skills = _agathion.getTemplate().getSkills();
        for (final Skill skill : _skills.valueCollection()) {
            if (getPlayer().getEffectList().getEffectsCountForSkill(skill.getId()) <= 0) {
                ThreadPoolManager.getInstance().schedule(new Buff(this, getPlayer(), skill), delay);
                delay = delay + skill.getHitTime() + 500;
            }
        }
    }


    @Override
    protected void onDeath(final Creature killer) {
        super.onDeath(killer);
        if (_durationCheckTask != null) {
            _durationCheckTask.cancel(false);
            _durationCheckTask = null;
        }
        if (_buffTask != null) {
            _buffTask.cancel(false);
            _buffTask = null;
        }
        final Player owner = getPlayer();
        if (owner != null && owner.getAgathion() == this) {
            owner.setAgathion(null);
        }
    }

    @Override
    public Player getPlayer() {
        return _playerRef.get();
    }

    public void setOwner(final Player owner) {
        _playerRef = ((owner == null) ? HardReferences.emptyRef() : owner.getRef());
        if (owner != null) {
            setTitle(owner.getName());
            setShowName(getParameter(SHOW_NAME, false));
            setTargetable(getParameter(TARGETABLE, false));
            if (owner.getAgathion() != null) {
                owner.getAgathion().doDespawn();
            }
            owner.setAgathion(this);
            for (final Player player : World.getAroundPlayers(this)) {
                player.sendPacket(new NpcInfo(this, player));
            }
            getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
            _buffTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
                @Override
                public void runImpl() {
                    buffOwner();
                }
            }, BUFF_INTERVAL, BUFF_INTERVAL);
        } else {
            doDespawn();
        }
    }


    public void doDespawn() {
        stopMove();
        if (_durationCheckTask != null) {
            _durationCheckTask.cancel(false);
            _durationCheckTask = null;
        }
        if (_buffTask != null) {
            _buffTask.cancel(false);
            _buffTask = null;
        }
        final Player owner = getPlayer();
        if (owner != null && owner.getAgathion() == this) {
            owner.setAgathion(null);
        }
        setTarget(null);
        onDecay();
    }

    public static class Buff extends RunnableImpl {
        private final NpcInstance _actor;
        private final Player _owner;
        private final Skill _skill;

        public Buff(final NpcInstance actor, final Player owner, final Skill skill) {
            _actor = actor;
            _owner = owner;
            _skill = skill;
        }

        @Override
        public void runImpl() {
            if (_actor != null) {
                _actor.doCast(_skill, _owner, true);
            }
        }
    }

}
