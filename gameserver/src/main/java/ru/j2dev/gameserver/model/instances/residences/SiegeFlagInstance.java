package ru.j2dev.gameserver.model.instances.residences;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.entity.events.objects.SiegeClanObject;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class SiegeFlagInstance extends NpcInstance {
    private SiegeClanObject _owner;
    private long _lastAnnouncedAttackedTime;

    public SiegeFlagInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        _lastAnnouncedAttackedTime = 0L;
        setHasChatWindow(false);
    }

    @Override
    public String getName() {
        return _owner.getClan().getName();
    }

    @Override
    public Clan getClan() {
        return _owner.getClan();
    }

    public void setClan(final SiegeClanObject owner) {
        _owner = owner;
    }

    @Override
    public String getTitle() {
        return "";
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        final Player player = attacker.getPlayer();
        if (player == null || isInvul()) {
            return false;
        }
        final Clan clan = player.getClan();
        return clan == null || _owner.getClan() != clan;
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        return true;
    }

    @Override
    protected void onDeath(final Creature killer) {
        _owner.setFlag(null);
        super.onDeath(killer);
    }

    @Override
    protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        if (System.currentTimeMillis() - _lastAnnouncedAttackedTime > 120000L) {
            _lastAnnouncedAttackedTime = System.currentTimeMillis();
            _owner.getClan().broadcastToOnlineMembers(SystemMsg.YOUR_BASE_IS_BEING_ATTACKED);
        }
        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }

    @Override
    public boolean hasRandomAnimation() {
        return false;
    }

    @Override
    public boolean isInvul() {
        return _isInvul;
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
    public boolean isHealBlocked() {
        return true;
    }

    @Override
    public boolean isEffectImmune() {
        return true;
    }
}
