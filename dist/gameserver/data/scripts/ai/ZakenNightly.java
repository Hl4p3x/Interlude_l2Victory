package ai;

import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameTimeController;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.ai.Fighter;
import ru.j2dev.gameserver.listener.game.OnDayNightChangeListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.entity.Reflection;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound;
import ru.j2dev.gameserver.network.lineage2.serverpackets.PlaySound.Type;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.utils.Location;

import java.util.stream.IntStream;

public class ZakenNightly extends Fighter {
    private static final int doll_blader_b = 29023;
    private static final int vale_master_b = 29024;
    private static final int pirates_zombie_captain_b = 29026;
    private static final int pirates_zombie_b = 29027;
    private static final Location[] _locations = {new Location(53950, 219860, -3496), new Location(55980, 219820, -3496), new Location(54950, 218790, -3496), new Location(55970, 217770, -3496), new Location(53930, 217760, -3496), new Location(55970, 217770, -3224), new Location(55980, 219920, -3224), new Location(54960, 218790, -3224), new Location(53950, 219860, -3224), new Location(53930, 217760, -3224), new Location(55970, 217770, -2952), new Location(55980, 219920, -2952), new Location(54960, 218790, -2952), new Location(53950, 219860, -2952), new Location(53930, 217760, -2952)};
    private final long _teleportSelfReuse;
    private final NpcInstance actor;
    private long _teleportSelfTimer;
    private int _stage;

    public ZakenNightly(final NpcInstance actor) {
        super(actor);
        _teleportSelfTimer = 0L;
        _teleportSelfReuse = 30000L;
        this.actor = getActor();
        _stage = 0;
        MAX_PURSUE_RANGE = Integer.MAX_VALUE;
        GameTimeController.getInstance().addListener(new onDayNightChange(actor));
    }

    @Override
    protected void thinkAttack() {
        if (Config.ZAKEN_ENABLE_TELEPORT && _teleportSelfTimer + _teleportSelfReuse < System.currentTimeMillis()) {
            _teleportSelfTimer = System.currentTimeMillis();
            if (Rnd.chance(20)) {
                actor.doCast(SkillTable.getInstance().getInfo(4222, 1), actor, false);
                ThreadPoolManager.getInstance().schedule(new RunnableImpl() {
                    @Override
                    public void runImpl() {
                        actor.teleToLocation(_locations[Rnd.get(_locations.length)]);
                        actor.getAggroList().clear(true);
                    }
                }, 500L);
            }
        }
        final double actor_hp_precent = actor.getCurrentHpPercents();
        final Reflection r = actor.getReflection();
        switch (_stage) {
            case 0: {
                if (actor_hp_precent < 90.0) {
                    r.addSpawnWithoutRespawn(pirates_zombie_captain_b, actor.getLoc(), 300);
                    _stage++;
                    break;
                }
                break;
            }
            case 1: {
                if (actor_hp_precent < 80.0) {
                    r.addSpawnWithoutRespawn(doll_blader_b, actor.getLoc(), 300);
                    _stage++;
                    break;
                }
                break;
            }
            case 2: {
                if (actor_hp_precent < 70.0) {
                    r.addSpawnWithoutRespawn(vale_master_b, actor.getLoc(), 300);
                    r.addSpawnWithoutRespawn(vale_master_b, actor.getLoc(), 300);
                    _stage++;
                    break;
                }
                break;
            }
            case 3: {
                if (actor_hp_precent < 60.0) {
                    IntStream.range(0, 5).forEach(i -> r.addSpawnWithoutRespawn(pirates_zombie_b, actor.getLoc(), 300));
                    _stage++;
                    break;
                }
                break;
            }
            case 4: {
                if (actor_hp_precent < 50.0) {
                    IntStream.range(0, 5).forEach(i -> {
                        r.addSpawnWithoutRespawn(doll_blader_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(pirates_zombie_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(vale_master_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(pirates_zombie_captain_b, actor.getLoc(), 300);
                    });
                    _stage++;
                    break;
                }
                break;
            }
            case 5: {
                if (actor_hp_precent < 40.0) {
                    IntStream.range(0, 6).forEach(i -> {
                        r.addSpawnWithoutRespawn(doll_blader_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(pirates_zombie_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(vale_master_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(pirates_zombie_captain_b, actor.getLoc(), 300);
                    });
                    _stage++;
                    break;
                }
                break;
            }
            case 6: {
                if (actor_hp_precent < 30.0) {
                    IntStream.range(0, 7).forEach(i -> {
                        r.addSpawnWithoutRespawn(doll_blader_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(pirates_zombie_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(vale_master_b, actor.getLoc(), 300);
                        r.addSpawnWithoutRespawn(pirates_zombie_captain_b, actor.getLoc(), 300);
                    });
                    _stage++;
                    break;
                }
                break;
            }
        }
        super.thinkAttack();
    }

    @Override
    protected void onEvtDead(final Creature killer) {
        final Reflection r = actor.getReflection();
        r.setReenterTime(System.currentTimeMillis());
        actor.broadcastPacket(new PlaySound(Type.MUSIC, "BS02_D", 1, actor.getObjectId(), actor.getLoc()));
        super.onEvtDead(killer);
    }

    @Override
    protected void teleportHome() {
    }

    private static class onDayNightChange implements OnDayNightChangeListener {
        private final HardReference<NpcInstance> _actRef;

        onDayNightChange(final NpcInstance actor) {
            _actRef = actor.getRef();
        }

        @Override
        public void onDay() {
            final NpcInstance zaken = _actRef.get();
            zaken.doCast(SkillTable.getInstance().getInfo(4223, 1), zaken, false);
        }

        @Override
        public void onNight() {
            final NpcInstance zaken = _actRef.get();
            zaken.doCast(SkillTable.getInstance().getInfo(4224, 1), zaken, false);
        }
    }
}
