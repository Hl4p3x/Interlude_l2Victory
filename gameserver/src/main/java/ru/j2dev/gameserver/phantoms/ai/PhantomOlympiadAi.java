package ru.j2dev.gameserver.phantoms.ai;


import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.phantoms.model.Phantom;

/**
 * Created by JunkyFunky
 * on 20.05.2018 20:12
 * group j2dev
 */
public class PhantomOlympiadAi extends AbstractPhantomAi {

    public PhantomOlympiadAi(Phantom actor) {
        super(actor);
    }

    @Override
    public void runImpl() {

    }

    @Override
    protected void onEvtAttacked(Creature attacker, int damage) {
        if(attacker.isPlayable()) {
            actor.getMemory().getAggroList().addDamageHate(attacker, damage, damage);
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    public PhantomAiType getType() {
        return PhantomAiType.OLYMPIAD;
    }
}
