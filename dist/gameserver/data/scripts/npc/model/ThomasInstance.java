package npc.model;

import events.SavingSnowman.SavingSnowman;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class ThomasInstance extends MonsterInstance {
    public ThomasInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void reduceCurrentHp(double i, final Creature attacker, final Skill skill, final boolean awake, final boolean standUp, final boolean directHp, final boolean canReflect, final boolean transferDamage, final boolean isDot, final boolean sendMessage) {
        i = 10.0;
        if (attacker.getActiveWeaponInstance() != null) {
            switch (attacker.getActiveWeaponInstance().getItemId()) {
                case 4202:
                case 5133:
                case 5817:
                case 7058:
                case 8350: {
                    i = 100.0;
                    break;
                }
                default: {
                    i = 10.0;
                    break;
                }
            }
        }
        super.reduceCurrentHp(i, attacker, skill, awake, standUp, directHp, canReflect, transferDamage, isDot, sendMessage);
    }

    @Override
    protected void onDeath(final Creature killer) {
        Creature topdam = getAggroList().getTopDamager();
        if (topdam == null) {
            topdam = killer;
        }
        SavingSnowman.freeSnowman(topdam);
        super.onDeath(killer);
    }

    @Override
    public boolean canChampion() {
        return false;
    }
}
