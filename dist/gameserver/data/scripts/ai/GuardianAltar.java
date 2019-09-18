package ai;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.SimpleSpawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.utils.Location;

import java.util.List;

public class GuardianAltar extends DefaultAI {
    private static final int DarkShamanVarangka = 18808;

    public GuardianAltar(final NpcInstance actor) {
        super(actor);
        actor.setIsInvul(true);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null) {
            return;
        }
        final Player player = attacker.getPlayer();
        if (Rnd.chance(40) && player.getInventory().destroyItemByItemId(14848, 1L)) {
            final List<NpcInstance> around = actor.getAroundNpc(1500, 300);
            if (around != null && !around.isEmpty()) {
                for (final NpcInstance npc : around) {
                    if (npc.getNpcId() == 18808) {
                        Functions.npcSay(actor, "I can sense the presence of Dark Shaman already!");
                        return;
                    }
                }
            }
            try {
                final SimpleSpawner sp = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(18808));
                sp.setLoc(Location.findPointToStay(actor, 400, 420));
                final NpcInstance npc = sp.doSpawn(true);
                if (attacker.isPet() || attacker.isSummon()) {
                    npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
                }
                npc.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (Rnd.chance(5)) {
            final List<NpcInstance> around = actor.getAroundNpc(1000, 300);
            if (around != null && !around.isEmpty()) {
                for (final NpcInstance npc : around) {
                    if (npc.getNpcId() == 22702) {
                        return;
                    }
                }
            }
            for (int i = 0; i < 2; ++i) {
                try {
                    final SimpleSpawner sp2 = new SimpleSpawner(NpcTemplateHolder.getInstance().getTemplate(22702));
                    sp2.setLoc(Location.findPointToStay(actor, 150, 160));
                    final NpcInstance npc2 = sp2.doSpawn(true);
                    if (attacker.isPet() || attacker.isSummon()) {
                        npc2.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker, Rnd.get(2, 100));
                    }
                    npc2.getAI().notifyEvent(CtrlEvent.EVT_AGGRESSION, attacker.getPlayer(), Rnd.get(1, 100));
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }
}
