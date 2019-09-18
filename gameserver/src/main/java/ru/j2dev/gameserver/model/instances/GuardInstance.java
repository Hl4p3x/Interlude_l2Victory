package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class GuardInstance extends NpcInstance {
    public GuardInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public boolean isAutoAttackable(final Creature attacker) {
        return (attacker.isMonster() && ((MonsterInstance) attacker).isAggressive()) || (attacker.isPlayable() && attacker.getKarma() > 0);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        return "guard/" + pom + ".htm";
    }

    @Override
    public boolean isInvul() {
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
    protected void onReduceCurrentHp(final double damage, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp) {
        getAggroList().addDamageHate(attacker, (int) damage, 0);
        super.onReduceCurrentHp(damage, attacker, skill, awake, standUp, directHp);
    }
}
