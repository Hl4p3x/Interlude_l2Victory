package ru.j2dev.gameserver.skills.effects;

import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.data.xml.holder.CubicHolder;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillLaunched;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.stats.Env;
import ru.j2dev.gameserver.templates.CubicTemplate;
import ru.j2dev.gameserver.templates.CubicTemplate.SkillInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;
import java.util.concurrent.Future;

public class EffectCubic extends Effect {
    private final CubicTemplate _template;
    private Future<?> _task;
    private long _reuse;

    public EffectCubic(final Env env, final EffectTemplate template) {
        super(env, template);
        _task = null;
        _reuse = 0L;
        _template = CubicHolder.getInstance().getTemplate(getTemplate().getParam().getInteger("cubicId"), getTemplate().getParam().getInteger("cubicLevel"));
    }

    private static boolean doHeal(final Player player, final SkillInfo info) {
        final Skill skill = info.getSkill();
        Creature target = null;
        if (player.getParty() == null) {
            if (!player.isCurrentHpFull() && !player.isDead()) {
                target = player;
            }
        } else {
            double currentHp = Double.MAX_VALUE;
            for (final Player member : player.getParty().getPartyMembers()) {
                if (member == null) {
                    continue;
                }
                if (!player.isInRange(member, info.getSkill().getCastRange()) || member.isCurrentHpFull() || member.isDead() || member.getCurrentHp() >= currentHp) {
                    continue;
                }
                currentHp = member.getCurrentHp();
                target = member;
            }
        }
        if (target == null) {
            return false;
        }
        final int chance = info.getChance((int) target.getCurrentHpPercents());
        if (!Rnd.chance(chance)) {
            return false;
        }
        final Creature aimTarget = target;
        player.broadcastPacket(new MagicSkillUse(player, aimTarget, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                final List<Creature> targets = new ArrayList<>(1);
                targets.add(aimTarget);
                player.broadcastPacket(new MagicSkillLaunched(player, skill, targets.stream().mapToInt(GameObject::getObjectId).toArray()));
                player.callSkill(skill, targets, false);
            }
        }, skill.getHitTime());
        return true;
    }

    private static boolean doAttack(final Player player, final SkillInfo info) {
        if (!Rnd.chance(info.getChance())) {
            return false;
        }
        final Creature target = getTarget(player, info);
        if (target == null) {
            return false;
        }
        final Skill skill = info.getSkill();
        player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                final List<Creature> targets = new ArrayList<>(1);
                targets.add(target);
                player.broadcastPacket(new MagicSkillLaunched(player, skill, targets.stream().mapToInt(GameObject::getObjectId).toArray()));
                player.callSkill(skill, targets, false);
                if (target.isNpc()) {
                    if (target.paralizeOnAttack(player)) {
                        if (Config.PARALIZE_ON_RAID_DIFF) {
                            player.paralizeMe(target);
                        }
                    } else {
                        final int damage = (skill.getEffectPoint() != 0) ? skill.getEffectPoint() : ((int) skill.getPower());
                        target.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, player, damage);
                    }
                }
            }
        }, skill.getHitTime());
        return true;
    }

    private static boolean doDebuff(final Player player, final SkillInfo info) {
        if (!Rnd.chance(info.getChance())) {
            return false;
        }
        final Creature target = getTarget(player, info);
        if (target == null) {
            return false;
        }
        final Skill skill = info.getSkill();
        player.broadcastPacket(new MagicSkillUse(player, target, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                final List<Creature> targets = new ArrayList<>(1);
                targets.add(target);
                player.broadcastPacket(new MagicSkillLaunched(player, skill, targets.stream().mapToInt(GameObject::getObjectId).toArray()));
                player.callSkill(skill, targets, false);
            }
        }, skill.getHitTime());
        return true;
    }

    private static boolean doCancel(final Player player, final SkillInfo info) {
        if (!Rnd.chance(info.getChance())) {
            return false;
        }
        boolean hasDebuff = false;
        for (final Effect e : player.getEffectList().getAllEffects()) {
            if (e.isOffensive() && e.isCancelable() && !e.getTemplate()._applyOnCaster) {
                hasDebuff = true;
                break;
            }
        }
        if (!hasDebuff) {
            return false;
        }
        final Skill skill = info.getSkill();
        player.broadcastPacket(new MagicSkillUse(player, player, skill.getDisplayId(), skill.getDisplayLevel(), skill.getHitTime(), 0L));
        ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
            @Override
            public void runImpl() {
                final List<Creature> targets = new ArrayList<>(1);
                targets.add(player);
                player.broadcastPacket(new MagicSkillLaunched(player, skill, targets.stream().mapToInt(GameObject::getObjectId).toArray()));
                player.callSkill(skill, targets, false);
            }
        }, skill.getHitTime());
        return true;
    }

    private static Creature getTarget(final Player owner, final SkillInfo info) {
        if (!owner.isInCombat()) {
            return null;
        }
        final GameObject object = owner.getTarget();
        if (object == null || !object.isCreature()) {
            return null;
        }
        final Creature target = (Creature) object;
        if (target.isDead()) {
            return null;
        }
        if (target.getCurrentHp() < info.getMinHp() && target.getCurrentHpPercents() < info.getMinHpPercent()) {
            return null;
        }
        if (target.isDoor() && !info.isCanAttackDoor()) {
            return null;
        }
        if (!owner.isInRangeZ(target, info.getSkill().getCastRange())) {
            return null;
        }
        final Player targetPlayer = target.getPlayer();
        if (targetPlayer != null && !targetPlayer.isInCombat()) {
            return null;
        }
        if (!target.isAutoAttackable(owner)) {
            return null;
        }
        return target;
    }

    @Override
    public void onStart() {
        super.onStart();
        final Player player = _effected.getPlayer();
        if (player == null) {
            return;
        }
        player.addCubic(this);
        _task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new ActionTask(), 1000L, 1000L);
    }

    @Override
    public void onExit() {
        super.onExit();
        final Player player = _effected.getPlayer();
        if (player == null) {
            return;
        }
        player.removeCubic(getId());
        _task.cancel(true);
        _task = null;
    }

    public void doAction(final Player player) {
        if (_reuse > System.currentTimeMillis()) {
            return;
        }
        boolean result = false;
        int chance = Rnd.get(1000);
        for (final Entry<Integer, List<SkillInfo>> entry : _template.getSkills()) {
            if ((chance -= entry.getKey()) < 0) {
                for (final SkillInfo skillInfo : entry.getValue()) {
                    switch (skillInfo.getActionType()) {
                        case ATTACK: {
                            result = doAttack(player, skillInfo);
                            continue;
                        }
                        case DEBUFF: {
                            result = doDebuff(player, skillInfo);
                            continue;
                        }
                        case HEAL: {
                            result = doHeal(player, skillInfo);
                            continue;
                        }
                        case CANCEL: {
                            result = doCancel(player, skillInfo);
                            continue;
                        }
                    }
                }
                break;
            }
        }
        if (result) {
            _reuse = System.currentTimeMillis() + _template.getDelay() * 1000L;
        }
    }

    @Override
    protected boolean onActionTime() {
        return false;
    }

    @Override
    public boolean isHidden() {
        return true;
    }

    @Override
    public boolean isCancelable() {
        return false;
    }

    public int getId() {
        return _template.getId();
    }

    private class ActionTask extends RunnableImpl {
        @Override
        public void runImpl() {
            if (!isActive()) {
                return;
            }
            final Player player = (_effected != null && _effected.isPlayer()) ? ((Player) _effected) : null;
            if (player == null) {
                return;
            }
            doAction(player);
        }
    }
}
