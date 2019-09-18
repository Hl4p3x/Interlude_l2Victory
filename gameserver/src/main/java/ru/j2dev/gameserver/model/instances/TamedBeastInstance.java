package ru.j2dev.gameserver.model.instances;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.lang.reference.HardReferences;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcInfo;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.concurrent.Future;

public final class TamedBeastInstance extends FeedableBeastInstance {
    private static final int MAX_DISTANCE_FROM_OWNER = 2000;
    private static final int MAX_DISTANCE_FOR_BUFF = 300;
    private static final int MAX_DURATION = 1200000;
    private static final int DURATION_CHECK_INTERVAL = 60000;
    private static final int DURATION_INCREASE_INTERVAL = 20000;
    private static final int BUFF_INTERVAL = 30000;
    private static final Skill[][] TAMED_SKILLS;

    static {
        (TAMED_SKILLS = new Skill[7][])[0] = new Skill[]{SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1045, 1), SkillTable.getInstance().getInfo(1086, 1), SkillTable.getInstance().getInfo(1217, 1), SkillTable.getInstance().getInfo(1240, 1), SkillTable.getInstance().getInfo(1268, 1)};
        TAMED_SKILLS[1] = new Skill[]{SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1160, 1), SkillTable.getInstance().getInfo(1204, 1)};
        TAMED_SKILLS[2] = new Skill[]{SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1045, 1), SkillTable.getInstance().getInfo(1086, 1), SkillTable.getInstance().getInfo(1217, 1), SkillTable.getInstance().getInfo(1240, 1)};
        TAMED_SKILLS[3] = new Skill[]{SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1204, 1)};
        TAMED_SKILLS[4] = new Skill[]{SkillTable.getInstance().getInfo(1044, 1), SkillTable.getInstance().getInfo(1045, 1), SkillTable.getInstance().getInfo(1086, 1), SkillTable.getInstance().getInfo(1217, 1), SkillTable.getInstance().getInfo(1240, 1), SkillTable.getInstance().getInfo(1268, 1)};
        TAMED_SKILLS[5] = new Skill[]{SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1204, 1)};
        TAMED_SKILLS[6] = new Skill[]{SkillTable.getInstance().getInfo(1013, 1), SkillTable.getInstance().getInfo(1048, 1), SkillTable.getInstance().getInfo(1059, 1), SkillTable.getInstance().getInfo(1078, 1), SkillTable.getInstance().getInfo(1085, 1), SkillTable.getInstance().getInfo(1204, 1)};
    }

    private HardReference<Player> _playerRef;
    private int _foodSkillId;
    private int _remainingTime;
    private Future<?> _durationCheckTask;
    private Future<?> _buffTask;
    private Skill[] _skills;

    public TamedBeastInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _playerRef = HardReferences.emptyRef();
        _remainingTime = MAX_DURATION;
        _durationCheckTask = null;
        _buffTask = null;
        _skills = Skill.EMPTY_ARRAY;
        _hasRandomWalk = false;
        _hasChatWindow = false;
        _hasRandomAnimation = false;
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return false;
    }

    private void onReceiveFood() {
        _remainingTime += DURATION_INCREASE_INTERVAL;
        if (_remainingTime > MAX_DURATION) {
            _remainingTime = MAX_DURATION;
        }
    }

    public int getRemainingTime() {
        return _remainingTime;
    }

    public void setRemainingTime(final int duration) {
        _remainingTime = duration;
    }

    public int getFoodType() {
        return _foodSkillId;
    }

    public void setFoodType(final int foodItemId) {
        if (foodItemId > 0) {
            _foodSkillId = foodItemId;
            if (_durationCheckTask != null) {
                _durationCheckTask.cancel(false);
            }
            _durationCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new CheckDuration(this), DURATION_CHECK_INTERVAL, DURATION_CHECK_INTERVAL);
        }
    }

    public void setTameType(final Player activeChar) {
        switch (getNpcId()) {
            case 16013: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_BUFFALO"));
                break;
            }
            case 16014: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_BUFFALO"));
                break;
            }
            case 16015: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_COUGAR"));
                break;
            }
            case 16016: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_COUGAR"));
                break;
            }
            case 16017: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_KOOKABURRA"));
                break;
            }
            case 16018: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_KOOKABURRA"));
                break;
            }
            case 16019: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_GRENDEL"));
                break;
            }
            case 16020: {
                setName(StringHolder.getInstance().getNotNull(activeChar, "TamedBeastInstance.NpcString.ALPEN_GRENDEL"));
                break;
            }
            default: {
                setName("");
                break;
            }
        }
        final Skill[] skills = TAMED_SKILLS[Rnd.get(TAMED_SKILLS.length)];
        _skills = skills.clone();
    }

    public void buffOwner() {
        if (!isInRange(getPlayer(), MAX_DISTANCE_FOR_BUFF)) {
            getAI().setIntention(CtrlIntention.AI_INTENTION_FOLLOW, getPlayer());
            return;
        }
        int delay = 0;
        for (final Skill skill : _skills) {
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
        if (owner != null && owner.getTrainedBeast() == this) {
            owner.setTrainedBeast(null);
        }
        _foodSkillId = 0;
        _remainingTime = 0;
    }

    @Override
    public Player getPlayer() {
        return _playerRef.get();
    }

    public void setOwner(final Player owner) {
        _playerRef = ((owner == null) ? HardReferences.emptyRef() : owner.getRef());
        if (owner != null) {
            setTitle(owner.getName());
            if (owner.getTrainedBeast() != null) {
                owner.getTrainedBeast().doDespawn();
            }
            owner.setTrainedBeast(this);
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

    public void despawnWithDelay(final int delay) {
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                doDespawn();
            }
        }, delay);
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
        if (owner != null && owner.getTrainedBeast() == this) {
            owner.setTrainedBeast(null);
        }
        setTarget(null);
        _foodSkillId = 0;
        _remainingTime = 0;
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

    private static class CheckDuration extends RunnableImpl {
        private final TamedBeastInstance _tamedBeast;

        CheckDuration(final TamedBeastInstance tamedBeast) {
            _tamedBeast = tamedBeast;
        }

        @Override
        public void runImpl() {
            final Player owner = _tamedBeast.getPlayer();
            if (owner == null || !owner.isOnline()) {
                _tamedBeast.doDespawn();
                return;
            }
            if (_tamedBeast.getDistance(owner) > MAX_DISTANCE_FROM_OWNER) {
                _tamedBeast.doDespawn();
                return;
            }
            final int foodTypeSkillId = _tamedBeast.getFoodType();
            _tamedBeast.setRemainingTime(_tamedBeast.getRemainingTime() - 60000);
            ItemInstance item = null;
            final int foodItemId = _tamedBeast.getItemIdBySkillId(foodTypeSkillId);
            if (foodItemId > 0) {
                item = owner.getInventory().getItemByItemId(foodItemId);
            }
            if (item != null && item.getCount() >= 1L) {
                _tamedBeast.onReceiveFood();
                owner.getInventory().destroyItem(item, 1L);
            } else if (_tamedBeast.getRemainingTime() < 900000) {
                _tamedBeast.setRemainingTime(-1);
            }
            if (_tamedBeast.getRemainingTime() <= 0) {
                _tamedBeast.doDespawn();
            }
        }
    }
}
