package npc.model;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlIntention;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.BossInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class OrfenInstance extends BossInstance {
    public static final Location nest = new Location(43728, 17220, -4342);
    public static final Location[] locs = {new Location(55024, 17368, -5412), new Location(53504, 21248, -5496), new Location(53248, 24576, -5272)};

    public OrfenInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void setTeleported(final boolean flag) {
        super.setTeleported(flag);
        final Location loc = flag ? nest : locs[Rnd.get(locs.length)];
        setSpawnedLoc(loc);
        getAggroList().clear(true);
        getAI().setIntention(CtrlIntention.AI_INTENTION_ACTIVE, null, null);
        teleToLocation(loc);
    }

    @Override
    protected void onSpawn() {
        super.onSpawn();
        setTeleported(false);
        broadcastPacketToOthers(new PlaySound(Type.MUSIC, "BS01_A", 1, 0, getLoc()));
    }

    @Override
    protected void onDeath(final Creature killer) {
        broadcastPacketToOthers(new PlaySound(Type.MUSIC, "BS02_D", 1, 0, getLoc()));
        super.onDeath(killer);
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        super.reduceCurrentHp(damage, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
        if (!isTeleported() && getCurrentHpPercents() <= 50.0) {
            setTeleported(true);
        }
    }
}
