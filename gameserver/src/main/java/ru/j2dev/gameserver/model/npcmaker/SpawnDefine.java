package ru.j2dev.gameserver.model.npcmaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.manager.NpcMakersManager;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.templates.StatsSet;
import ru.j2dev.gameserver.templates.npc.MinionData;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.npc.superPoint.SuperPoint;
import ru.j2dev.gameserver.utils.Location;

import java.util.*;

/**
 * @author: JunkyFunky
 * @date: 20.03.18 18:29
 */
public class SpawnDefine implements Cloneable {
    private static final Logger LOGGER = LoggerFactory.getLogger(SpawnDefine.class);
    public final NpcTemplate npc_template;
    public final int total;
    public final int respawn;
    public final int respawn_rand;
    public HashMap<Integer, Location> positions;
    public int chase_pc;
    public volatile int npc_count;
    private String dbname, dbsaving;
    private MultiValueSet<String> ai_params;
    private List<NpcInstance> npc_list;
    private DefaultMaker maker;
    private Set<MinionData> privates = Collections.emptySet();
    private int reflectionId;
    private boolean bossSpawnSet;
    private SuperPoint superPoint;

    public SpawnDefine(NpcTemplate npc_template, int total, int respawn, int respawn_rand, final SuperPoint superPoint, String ai_params, String privateStr, String dbn, String dbs, DefaultMaker maker, boolean bossSet) {
        this.npc_template = npc_template;
        this.total = total;
        this.respawn = respawn;
        this.respawn_rand = respawn_rand;
        this.maker = maker;
        this.superPoint = superPoint;
        npc_list = new ArrayList<>(total);
        dbname = dbn;
        dbsaving = dbs;
        bossSpawnSet = bossSet;

        if (ai_params != null && !ai_params.isEmpty()) {
            for (String param : ai_params.split(";")) {
                if (!param.isEmpty()) {
                    if (this.ai_params == null) {
                        this.ai_params = new StatsSet();
                    }
                    this.ai_params.set(param.split(":")[0], param.split(":")[1]);
                }
            }
        }

        if (privateStr != null && !privateStr.isEmpty()) {
            for (String priv : privateStr.split(";")) {
                if (priv != null && !priv.isEmpty()) {
                    String[] privateParams = priv.split(":");
                        if (privates.isEmpty()) {
                            privates = new HashSet<>();
                        }
                        privates.add(new MinionData(privateParams));
                }
            }
        }
    }

    public void respawn(NpcInstance npc, int respawn, int respawn_rand) {
        if (respawn == 0) {
            npc.refreshID();
            spawnNpc(npc, null);
        } else {
            long respawnDelay = respawn * 1000L + Rnd.get(-respawn_rand, respawn_rand) * 1000L;
            if (dbname != null && (dbsaving.contains("death_time") || bossSpawnSet)) {
                LOGGER.info("{} Schedule respawn: {}", this, new Date(System.currentTimeMillis() + respawnDelay));
                NpcMakersManager.getInstance().saveRespawn(dbname, System.currentTimeMillis() + respawnDelay, 0, 0, npc.getLoc());
            }
            RespawnManager.addRespawnNpc(npc, respawnDelay);
        }
    }

    public DefaultMaker getMaker() {
        return maker;
    }

    public void setMaker(DefaultMaker mk) {
        maker = mk;
    }

    public void despawn() {
        npc_list.forEach(npc -> {
            if (npc.isVisible()) {
                npc.deleteMe();
            } else {
                RespawnManager.cancelRespawn(npc);
            }
        });
        maker.atomicDecrease(npc_count);
        npc_count = 0;
    }

    public void addPosition(Location pos, int chance) {
        if (positions == null) {
            positions = new HashMap<>(1);
        }

        positions.put(chance, pos);
    }

    public Location getRandomPosInMyTerritory(NpcInstance npc) {
        return positions == null ? maker.getRandomPos(npc.getGeoIndex()) : getRandomPos();
    }

    public void spawn(int count, int respawn, int respawn_rand) {
        for (int i = 0; i < count; i++)
                try {
                    NpcInstance npc = getFreeNpc();

                    if (npc == null) {
                        NpcInstance tmp = npc_template.getNewInstance();

                        // Check if the Instance is a NpcInstance

                        npc = tmp;
                    }

                    npc_list.add(npc);

                    if (respawn == 0) {
                        RespawnData rd;
                        if (maker.debug) {
                            LOGGER.info(this + " respawn = 0 dbname=" + dbname);
                        }
                        if (dbname != null && (rd = NpcMakersManager.getInstance().getRespawnData(dbname)) != null) {
                            if (maker.debug) {
                                LOGGER.info(this + " respawnTime=" + rd.respawnTime + " currTime=" + System.currentTimeMillis());
                            }
                            NpcMakersManager.getInstance().removeRespawnData(rd);
                            if (rd.respawnTime > System.currentTimeMillis()) {
                                if (maker.debug) {
                                    LOGGER.info(this + " add respawn delay: " + (rd.respawnTime - System.currentTimeMillis()));
                                }
                                RespawnManager.addRespawnNpc(npc, rd.respawnTime - System.currentTimeMillis());
                            } else {
                                spawnNpc(npc, rd);
                            }
                        } else {
                            spawnNpc(npc, null);
                        }
                    } else {
                        RespawnManager.addRespawnNpc(npc, respawn * 1000L + Rnd.get(-respawn_rand, respawn_rand) * 1000L);
                    }
                } catch (Exception e) {
                    LOGGER.warn(this + " " + maker + " can't spawn " + e.getMessage());
                    e.printStackTrace();
                }
    }

    public void spawnNpc(NpcInstance npc, RespawnData rd) {
        Location loc;
        if (rd != null && (dbsaving.contains("pos") || bossSpawnSet)) {
            loc = rd.position;
        } else {
            loc = getRandomPosInMyTerritory(npc);
        }

        if (loc == null) {
            return;
        }

        // Set the HP and MP of the NpcInstance to the max
        if (rd != null && (dbsaving.contains("parameters") || bossSpawnSet)) {
            npc.setCurrentHpMp(rd.currentHp, rd.currentHp);
        } else {
            npc.setCurrentHpMp(npc.getMaxHp(), npc.getMaxMp());
        }
        if (npc.getParameters() == null) {
            npc.setParameters(ai_params);
        }

        if (chase_pc > 0) {
            if (npc.getParameter("MaxPursueRange", 0) == 0) {
                npc.setParameter("MaxPursueRange", chase_pc);
            }
        }

        npc.getEffectList().stopAllEffects();
        npc.removeStatsOwner(npc);
        npc.setSpawnDefine(this);
        npc.getMinionList().setMinionData(privates);

        // Set the heading of the NpcInstance (random heading if not defined)
        npc.setHeading(loc.h);

        // save npc_list points
        npc.setSpawnedLoc(loc);

        if (reflectionId != 0) {
            npc.setReflection(reflectionId);
        }

        npc.getAI().startAITask();

        npc.spawnMe(loc);
        if(npc.hasMinions()) {
            npc.getMinionList().spawnMinions();
        }
        if (maker.debug) {
            LOGGER.info(this+" spawn: "+npc+" "+loc+" npc_count="+npc_count);
        }
    }

    private Location getRandomPos() {
        int chance = Rnd.get(100);
        for (Map.Entry<Integer, Location> entry : positions.entrySet()) {
            if (chance < entry.getKey()) {
                return entry.getValue().clone();
            } else {
                chance -= entry.getKey();
            }
        }

        return null;
    }

    private NpcInstance getFreeNpc() {
        return npc_list.stream().filter(npc -> !npc.isVisible() && !RespawnManager.contains(npc)).findFirst().orElse(null);

    }

    public void sendScriptEvent(int eventId, Object arg1, Object arg2) {
        npc_list.stream().filter(GameObject::isVisible).forEach(npc -> npc.getAI().sendScriptEvent(npc, eventId, arg1, arg2));
    }

    public void save() {
        if (dbname != null && (dbsaving.contains("parameters") || dbsaving.contains("pos") || dbsaving.contains("death_time") || bossSpawnSet)) {
            for (NpcInstance npc : npc_list) {
                if (npc.isVisible() && npc.getMaxHp() != (int) npc.getCurrentHp()) {
                    NpcMakersManager.getInstance().saveRespawn(dbname, npc.isDead() && (dbsaving.contains("death_time") || bossSpawnSet) ? respawn * 1000L + Rnd.get(-respawn_rand, respawn_rand) * 1000L : 0, (int) npc.getCurrentHp(), (int) npc.getCurrentMp(), npc.getLoc());
                }
            }
        }
    }

    public SuperPoint getSuperPoint() {
        return superPoint;
    }

    public void setReflection(int refId) {
        reflectionId = refId;
    }

    public void setChasePc(int chasePc) {
        chase_pc = chasePc;
    }

    @Override
    public SpawnDefine clone() {
        try {
            SpawnDefine sd = (SpawnDefine) super.clone();
            sd.npc_list = new ArrayList<>();
            sd.maker = null;
            return sd;
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return "SpawnDefine[" + npc_template.getName() + ";id=" + npc_template.getNpcId() +";respawn=" + respawn+ ";hash=" + hashCode() + "]";
    }
}
