package events.Halloween;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.ai.DefaultAI;
import ru.j2dev.gameserver.idfactory.IdFactory;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillLaunched;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Location;

import java.util.concurrent.atomic.AtomicInteger;

public class PumpkinGhostAI extends DefaultAI {
    private static final int Firework = 2024;
    private static final String[] _song = {
            "PumpkinGhostAI.SongText1", "PumpkinGhostAI.SongText2",
            "PumpkinGhostAI.SongText3", "PumpkinGhostAI.SongText4",
            "PumpkinGhostAI.SongText5", "PumpkinGhostAI.SongText6",
            "PumpkinGhostAI.SongText7", "PumpkinGhostAI.SongText8",
            "PumpkinGhostAI.SongText9", "PumpkinGhostAI.SongText10",
            "PumpkinGhostAI.SongText11", "PumpkinGhostAI.SongText12",
            "PumpkinGhostAI.SongText13", "PumpkinGhostAI.SongText14",
            "PumpkinGhostAI.SongText15", "PumpkinGhostAI.SongText16",
            "PumpkinGhostAI.SongText17", "PumpkinGhostAI.SongText18",
            "PumpkinGhostAI.SongText19", "PumpkinGhostAI.SongText20",
            "PumpkinGhostAI.SongText21", "PumpkinGhostAI.SongText22",
            "PumpkinGhostAI.SongText23", "PumpkinGhostAI.SongText24",
            "PumpkinGhostAI.SongText25", "PumpkinGhostAI.SongText26",
            "PumpkinGhostAI.SongText27", "PumpkinGhostAI.SongText28",
            "PumpkinGhostAI.SongText29", "PumpkinGhostAI.SongText30",
            "PumpkinGhostAI.SongText31", "PumpkinGhostAI.SongText32",
            "PumpkinGhostAI.SongText33", "PumpkinGhostAI.SongText34",
            "PumpkinGhostAI.SongText35", "PumpkinGhostAI.SongText36",
            "PumpkinGhostAI.SongText37", "PumpkinGhostAI.SongText38",
            "PumpkinGhostAI.SongText39", "PumpkinGhostAI.SongText40",
            "PumpkinGhostAI.SongText41", "PumpkinGhostAI.SongText42",
            "PumpkinGhostAI.SongText43", "PumpkinGhostAI.SongText44",
            "PumpkinGhostAI.SongText45", "PumpkinGhostAI.SongText46",
            "PumpkinGhostAI.SongText47", "PumpkinGhostAI.SongText48"};
    private static final AtomicInteger _fraseIdx = new AtomicInteger(0);
    private final Location[] _flyPoints;
    private final int _chance;
    private final int[] _item_ids;
    private int _pointIdx;
    private long _lastTask;

    public PumpkinGhostAI(final NpcInstance actor, final Location[] points, final int idx, final int chance, final int[] item_ids) {
        super(actor);
        _lastTask = System.currentTimeMillis();
        _flyPoints = points;
        _pointIdx = idx + 1;
        if (_pointIdx >= _flyPoints.length) {
            _pointIdx = 0;
        }
        _chance = chance;
        _item_ids = item_ids;
    }

    @Override
    protected void onEvtSpawn() {
        final NpcInstance actor = getActor();
        if (actor == null) {
            return;
        }
        actor.setWalking();
        addTaskMove(_flyPoints[_pointIdx], false);
        super.onEvtSpawn();
    }

    @Override
    protected boolean thinkActive() {
        final NpcInstance actor = getActor();
        if (actor == null || actor.isDead()) {
            return true;
        }
        if (_def_think) {
            return doTask();
        }
        final long currentTime = System.currentTimeMillis();
        if (currentTime - _lastTask > 3000L) {
            if (Rnd.chance(30)) {
                actor.broadcastPacket(new MagicSkillUse(actor, actor, Firework, 1, 0, 0L), new MagicSkillLaunched(actor, getSkillInfo(2024, 1), actor.getObjectId()));
            } else if (_pointIdx == 0 && Rnd.chance(40)) {
                Functions.npcSayInRangeCustomMessage(getActor(), 300, _song[_fraseIdx.getAndIncrement() % _song.length]);
            } else if (Rnd.chance(_chance)) {
                final ItemInstance item = new ItemInstance(IdFactory.getInstance().getNextId(), _item_ids[Rnd.get(_item_ids.length)]);
                item.setCount(1L);
                item.dropToTheGround(actor, Location.coordsRandomize(actor.getLoc(), 10, 50));
            }
            if (isOutOfRange()) {
                addTaskMove(_flyPoints[_pointIdx], false);
            }
            _lastTask = currentTime;
        }
        return true;
    }

    private boolean isOutOfRange() {
        final NpcInstance actor = getActor();
        return actor != null && !actor.isDead() && actor.getLoc().distance(_flyPoints[_pointIdx]) > 512.0;
    }

    @Override
    protected void onEvtArrived() {
        _pointIdx++;
        if (_pointIdx >= _flyPoints.length) {
            _pointIdx = 0;
        }
        addTaskMove(_flyPoints[_pointIdx], false);
        super.onEvtArrived();
    }

    @Override
    protected void onEvtAttacked(final Creature attacker, final int damage) {
        if (attacker != null) {
            SkillTable.getInstance().getInfo(4515, 1).getEffects(attacker, attacker, false, false);
        }
        doTask();
    }

    @Override
    protected boolean randomAnimation() {
        return false;
    }

    @Override
    protected boolean randomWalk() {
        return false;
    }

    @Override
    public boolean isGlobalAI() {
        return true;
    }

}
