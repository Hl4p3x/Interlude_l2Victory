package ru.j2dev.gameserver.model.instances.residences;

import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.entity.events.impl.SiegeEvent;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Set;

public abstract class SiegeToggleNpcInstance extends NpcInstance {
    private NpcInstance _fakeInstance;
    private int _maxHp;

    public SiegeToggleNpcInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
        setHasChatWindow(false);
    }

    public void setZoneList(final Set<String> set) {
    }

    public void register(final Spawner spawn) {
    }

    public void initFake(final int fakeNpcId) {
        (_fakeInstance = NpcTemplateHolder.getInstance().getTemplate(fakeNpcId).getNewInstance()).setCurrentHpMp(1.0, _fakeInstance.getMaxMp());
        _fakeInstance.setHasChatWindow(false);
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }

    public abstract void onDeathImpl(final Creature p0);

    @Override
    protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        setCurrentHp(Math.max(getCurrentHp() - damage, 0.0), false);
        if (getCurrentHp() < 0.5) {
            doDie(attacker);
            onDeathImpl(attacker);
            decayMe();
            _fakeInstance.spawnMe(getLoc());
        }
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        if (attacker == null) {
            return false;
        }
        final Player player = attacker.getPlayer();
        if (player == null) {
            return false;
        }
        final SiegeEvent<?, ?> siegeEvent = getEvent(SiegeEvent.class);
        return siegeEvent != null && siegeEvent.isInProgress();
    }

    @Override
    public boolean isAttackable(final Creature attacker) {
        return isAutoAttackable(attacker);
    }

    @Override
    public boolean isInvul() {
        return false;
    }

    @Override
    public boolean hasRandomAnimation() {
        return false;
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

    public void decayFake() {
        _fakeInstance.decayMe();
    }

    @Override
    public int getMaxHp() {
        return _maxHp;
    }

    public void setMaxHp(final int maxHp) {
        _maxHp = maxHp;
    }

    @Override
    protected void onDecay() {
        decayMe();
        _spawnAnimation = 2;
    }

    @Override
    public Clan getClan() {
        return null;
    }
}
