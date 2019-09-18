package ai;

import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.CtrlEvent;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ExShowScreenMessage.ScreenMessageAlign;
import ru.j2dev.gameserver.tables.SkillTable;

import java.util.List;

public class SeducedInvestigator extends Fighter {
    private final int[] _allowedTargets;
    private long _reuse;

    public SeducedInvestigator(final NpcInstance actor) {
        super(actor);
        _allowedTargets = new int[]{25659, 25660, 25661, 25662, 25663, 25664};
        _reuse = 0L;
        actor.startImmobilized();
        actor.startHealBlocked();
        AI_TASK_ACTIVE_DELAY = 5000L;
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor.isDead()) {
            return false;
        }
        final List<NpcInstance> around = actor.getAroundNpc(1000, 300);
        if (around != null && !around.isEmpty()) {
            for (final NpcInstance npc : around) {
                if (ArrayUtils.contains(_allowedTargets, npc.getNpcId())) {
                    actor.getAI().notifyEvent(CtrlEvent.EVT_ATTACKED, npc, 300);
                }
            }
        }
        if (Rnd.chance(0.1) && _reuse + 30000L < System.currentTimeMillis()) {
            final List<Player> players = World.getAroundPlayers(actor, 500, 200);
            if (players == null || players.size() < 1) {
                return false;
            }
            final Player player = players.get(Rnd.get(players.size()));
            if (player.getReflectionId() == actor.getReflectionId()) {
                _reuse = System.currentTimeMillis();
                final int[] buffs = {5970, 5971, 5972, 5973};
                switch (actor.getNpcId()) {
                    case 36562:
                        actor.doCast(SkillTable.getInstance().getInfo(buffs[0], 1), player, true);
                        break;
                    case 36563:
                        actor.doCast(SkillTable.getInstance().getInfo(buffs[1], 1), player, true);
                        break;
                    case 36564:
                        actor.doCast(SkillTable.getInstance().getInfo(buffs[2], 1), player, true);
                        break;
                    default:
                        actor.doCast(SkillTable.getInstance().getInfo(buffs[3], 1), player, true);
                        break;
                }
            }
        }
        return true;
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final NpcInstance actor = getActor();
        final Reflection r = actor.getReflection();
        final List<Player> players = r.getPlayers();
        for (final Player p : players) {
            p.sendPacket(new ExShowScreenMessage("The Investigator has been killed. The mission is failed.", 3000, ScreenMessageAlign.TOP_CENTER, true));
        }
        r.startCollapseTimer(5000L);
        super.onEvtDead(killer);
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        final NpcInstance actor = getActor();
        if (attacker == null) {
            return;
        }
        if (attacker.isPlayable()) {
            return;
        }
        if (attacker.getNpcId() == 25659 || attacker.getNpcId() == 25660 || attacker.getNpcId() == 25661) {
            actor.getAggroList().addDamageHate(attacker, 0, 20);
        }
        super.onEvtAttacked(attacker, damage);
    }

    @Override
    protected void onEvtAggression(final Creature target, final int aggro) {
        if (target.isPlayer() || target.isPet() || target.isSummon()) {
            return;
        }
        super.onEvtAggression(target, aggro);
    }

    @Override
    public boolean checkAggression(final Creature target) {
        return !target.isPlayable() && super.checkAggression(target);
    }
}
