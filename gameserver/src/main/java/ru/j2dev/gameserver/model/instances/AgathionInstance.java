package ru.j2dev.gameserver.model.instances;

import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcInfo;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.stats.Stats;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;

import static ru.j2dev.gameserver.ai.CtrlIntention.AI_INTENTION_IDLE;

public final class AgathionInstance extends AgathionNpcInstance {

    private static final int MAX_DISTANCE_FOR_BUFF = 1200;
    private static final int BUFF_INTERVAL = 10000;
    private int _interval;
    private Player owner;
    private HardReference<Player> _playerRef;
    private Future<?> _durationCheckTask;
    private Future<?> _buffTask;
    private Future<?> _restoreHpTask;
    private Future<?> _restoreMpTask;
    private Future<?> _restoreCpTask;
    private TIntObjectHashMap<Skill> _skills;
    private int _remainingTime;
    private int _unsummonskill;

    private int _restoreHpId;
    private int _restoreMpId;
    private int _restoreCpId;
    private int _restoreHpPercent;
    private int _restoreMpPercent;
    private int _restoreCpPercent;

    public AgathionInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _playerRef = HardReferences.emptyRef();
        _durationCheckTask = null;
        _buffTask = null;
        owner = getPlayer();
        _skills = getTemplate().getSkills();
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
            setFollowMode(true);
            getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);

            _buffTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new RunnableImpl() {
                @Override
                public void runImpl() {
                    buffOwner();
                }
            }, getBuffInterval(), getBuffInterval());

        } else {
            doDespawn();
        }
    }

    private void buffOwner() {
        if (!isInRange(getPlayer(), MAX_DISTANCE_FOR_BUFF)) {
            getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
            return;
        }

        int totalBuffsAvailable = 0;
        for (Skill skill : _skills.valueCollection()) {
            // if the skill is a buff, check if the owner has it already [  owner.getEffect(L2Skill skill) ]
            if (skill.getSkillType() == Skill.SkillType.BUFF)
                totalBuffsAvailable++;
        }
        // start the buff tasks
        _buffTask = ThreadPoolManager.getInstance().schedule(new CheckOwnerBuffs(this, totalBuffsAvailable), getBuffInterval());

    }


    private int get_restoreHpPercent()
    {   return _restoreHpPercent;}

    private int get_restoreMpPercent(){
        return _restoreMpPercent;
    }
    private int get_restoreCpPercent(){
        return _restoreCpPercent;
    }

    public void set_restoreHpPercent(final int restoreHpPercent)
    {
        _restoreHpPercent = restoreHpPercent;
    }

    public void set_restoreMpPercent(final int restoreMpPercent)
    {
        _restoreMpPercent = restoreMpPercent;
    }

    public void set_restoreCpPercent(final int restoreCpPercent)
    {
        _restoreCpPercent = restoreCpPercent;
    }

    private int get_restoreHpId()
    {
        return _restoreHpId;
    }

    public void set_restoreHpId(final int restoreHpId)
    {
        _restoreHpId = restoreHpId;
    }
    private int get_restoreMpId()
    {
        return _restoreMpId;
    }

    public void set_restoreMpId(final int restoreMpId)
    {
        _restoreMpId = restoreMpId;
    }
    private int get_restoreCpId()
    {
        return _restoreCpId;
    }

    public void set_restoreCpId(final int restoreCpId)
    {
        _restoreCpId = restoreCpId;
    }



    private int getBuffInterval() {
        return _interval;
    }

    public void setBuffInterval(final int interval) {
        _interval = interval;
    }

    public int getRemainingTime() {
        return _remainingTime;
    }

    public void setRemainingTime(final int duration) {
        _remainingTime = duration;
    }

    private int getUnsummonSkill() {
        return _unsummonskill;
    }

    private void setUnsummonSkill(final int unsummonskill) {
        _unsummonskill = unsummonskill;
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
            TIntObjectHashMap<Skill> _skills = owner.getAgathion().getTemplate().getSkills();

            for (final Skill skill : _skills.valueCollection()) {
                owner.getEffectList().stopEffect(skill);
            }
            owner.setAgathion(null);
            owner.removeSkill(getUnsummonSkill(), true);
            owner.sendSkillList();
        }
    }

    @Override
    public Player getPlayer() {
        return _playerRef.get();
    }

    public void addUnsummon(Player player, final int skill) {
        final Skill sk = SkillTable.getInstance().getInfo(skill, 1);
        setUnsummonSkill(skill);
        player.addSkill(sk, false);
        player.sendSkillList();
    }

    public void setFollowMode(final boolean state) {
        final Player owner = getPlayer();
        if (state) {
            if (getAI().getIntention() == CtrlIntention.AI_INTENTION_ACTIVE) {
                getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, owner);
            }
        } else if (getAI().getIntention() == CtrlIntention.AI_INTENTION_FOLLOW) {
            getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE);
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
            TIntObjectHashMap<Skill> _skills = owner.getAgathion().getTemplate().getSkills();

            for (final Skill skill : _skills.valueCollection()) {
                owner.getEffectList().stopEffect(skill);
            }
            owner.setAgathion(null);
            owner.removeSkill(getUnsummonSkill(), true);
            owner.sendSkillList();
        }
        _remainingTime = 0;

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

    private class CheckOwnerBuffs extends RunnableImpl {
        private AgathionInstance _agathionBuff;
        private int _numBuffs;

        CheckOwnerBuffs(AgathionInstance agathionBuff, int numBuffs) {
            _agathionBuff = agathionBuff;
            _numBuffs = numBuffs;
        }

        public void runImpl() {
            final Player owner = _agathionBuff.getPlayer();

            // check if the owner is no longer around...if so, despawn
            if (owner == null || !owner.isOnline()) {
                doDespawn();
                return;
            }
            if (_agathionBuff.getDistance(owner) > 1200) {
                _agathionBuff.doDespawn();
                return;
            }
            // if the owner is dead, do nothing...
            if (owner.isDead())
                return;
            // if the tamed beast is currently casting a spell, do not interfere (do not attempt to cast anything new yet).
            if (isCastingNow())
                return;

            int totalBuffsOnOwner = 0;
            int i = 0;
            int rand = Rnd.get(_numBuffs);
            Skill skill = null;

            for (Skill skills : _skills.valueCollection()) {
                if (skills.getSkillType() == Skill.SkillType.BUFF) {
                    if (i++ == rand)
                        skill = skills;
                    if (owner.getEffectList().getEffectsBySkill(skills) != null)
                        totalBuffsOnOwner++;
                }
            }
            if (skill != null) {
                if (owner.getEffectList().getEffectsBySkill(skill) != null && skill.hasEffects()) {
                    return;
                } else if (skill.hasEffects()) {
                    _agathionBuff.doCast(skill, owner, true);
                } else
                return;
            }

            getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, _agathionBuff.getPlayer());
        }
    }

}
