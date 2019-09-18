package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class MinionInstance extends MonsterInstance {
    private MonsterInstance _master;

    public MinionInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    public MonsterInstance getLeader() {
        return _master;
    }

    public void setLeader(final MonsterInstance leader) {
        _master = leader;
    }

    public boolean isRaidFighter() {
        return getLeader() != null && getLeader().isRaid();
    }

    @Override
    protected void onDeath(final Creature killer) {
        if (getLeader() != null) {
            getLeader().notifyMinionDied(this);
        }
        super.onDeath(killer);
    }

    @Override
    protected void onDecay() {
        decayMe();
        _spawnAnimation = 2;
    }

    @Override
    public boolean isFearImmune() {
        return isRaidFighter();
    }

    @Override
    public Location getSpawnedLoc() {
        return (getLeader() != null) ? getLeader().getLoc() : getLoc();
    }

    @Override
    public boolean canChampion() {
        return false;
    }

    @Override
    public boolean isMinion() {
        return true;
    }
}
