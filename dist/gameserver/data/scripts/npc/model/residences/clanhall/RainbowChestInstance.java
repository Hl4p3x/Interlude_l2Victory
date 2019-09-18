package npc.model.residences.clanhall;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class RainbowChestInstance extends MonsterInstance {
    private static final int ItemA = 8035;
    private static final int ItemB = 8036;
    private static final int ItemC = 8037;
    private static final int ItemD = 8038;
    private static final int ItemE = 8039;
    private static final int ItemF = 8040;
    private static final int ItemG = 8041;
    private static final int ItemH = 8042;
    private static final int ItemI = 8043;
    private static final int ItemK = 8045;
    private static final int ItemL = 8046;
    private static final int ItemN = 8047;
    private static final int ItemO = 8048;
    private static final int ItemP = 8049;
    private static final int ItemR = 8050;
    private static final int ItemS = 8051;
    private static final int ItemT = 8052;
    private static final int ItemU = 8053;
    private static final int ItemW = 8054;
    private static final int ItemY = 8055;

    public RainbowChestInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void reduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        if (attacker == null || !attacker.isPlayer() || attacker.getActiveWeaponInstance() != null || skill != null || isDot) {
            return;
        }
        super.reduceCurrentHp(getMaxHp() * 0.2, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    @Override
    public void onDeath(final Creature k) {
        super.onDeath(k);
        final Player killer = k.getPlayer();
        if (killer == null) {
            return;
        }
        for (int count = 1 + Rnd.get(2), i = 0; i < count; ++i) {
            final int chance = Rnd.get(100);
            if (chance <= 5) {
                dropItem(killer, 8035, 1L);
            } else if (chance > 5 && chance <= 10) {
                dropItem(killer, 8036, 1L);
            } else if (chance > 10 && chance <= 15) {
                dropItem(killer, 8037, 1L);
            } else if (chance > 15 && chance <= 20) {
                dropItem(killer, 8038, 1L);
            } else if (chance > 20 && chance <= 25) {
                dropItem(killer, 8039, 1L);
            } else if (chance > 25 && chance <= 30) {
                dropItem(killer, 8040, 1L);
            } else if (chance > 30 && chance <= 35) {
                dropItem(killer, 8041, 1L);
            } else if (chance > 35 && chance <= 40) {
                dropItem(killer, 8042, 1L);
            } else if (chance > 40 && chance <= 45) {
                dropItem(killer, 8043, 1L);
            } else if (chance > 45 && chance <= 50) {
                dropItem(killer, 8045, 1L);
            } else if (chance > 50 && chance <= 55) {
                dropItem(killer, 8046, 1L);
            } else if (chance > 55 && chance <= 60) {
                dropItem(killer, 8047, 1L);
            } else if (chance > 60 && chance <= 65) {
                dropItem(killer, 8048, 1L);
            } else if (chance > 65 && chance <= 70) {
                dropItem(killer, 8049, 1L);
            } else if (chance > 70 && chance <= 75) {
                dropItem(killer, 8050, 1L);
            } else if (chance > 75 && chance <= 80) {
                dropItem(killer, 8051, 1L);
            } else if (chance > 80 && chance <= 85) {
                dropItem(killer, 8052, 1L);
            } else if (chance > 85 && chance <= 90) {
                dropItem(killer, 8053, 1L);
            } else if (chance > 90 && chance <= 95) {
                dropItem(killer, 8054, 1L);
            } else if (chance > 95) {
                dropItem(killer, 8055, 1L);
            }
        }
    }
}
