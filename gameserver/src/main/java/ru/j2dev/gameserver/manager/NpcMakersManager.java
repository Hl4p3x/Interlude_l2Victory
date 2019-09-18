package ru.j2dev.gameserver.manager;

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.SuperPointHolder;
import ru.j2dev.gameserver.data.xml.holder.TerritoryHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Territory;
import ru.j2dev.gameserver.model.npcmaker.DefaultMaker;
import ru.j2dev.gameserver.model.npcmaker.RespawnData;
import ru.j2dev.gameserver.model.npcmaker.SpawnDefine;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.templates.npc.superPoint.SuperPoint;
import ru.j2dev.gameserver.utils.Location;

import java.io.File;
import java.lang.reflect.Constructor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by JunkyFunky
 * on 20.03.2018 20:59
 * group j2dev
 */
public class NpcMakersManager {
    private static final Logger LOGGER = LoggerFactory.getLogger(NpcMakersManager.class);
    private static final NpcMakersManager _instance = new NpcMakersManager();
    private Map<String, RespawnData> _respawnData;

    private Map<String, DefaultMaker> _npcMakers = new HashMap<>();

    public static NpcMakersManager getInstance() {
        return _instance;
    }

    public static boolean set(String query) {
        Connection con = null;
        Statement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.createStatement();
            statement.executeUpdate(query);
        } catch (Exception e) {
            LOGGER.warn("Could not execute update '" + query + "': " + e);
            Thread.dumpStack();
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        return true;
    }

    public void load() {
        loadRespawnData();
        loadMakersXml();
    }

    public void saveRespawn(String dbname, long respawnTime, int hp, int mp, Location pos) {
        set("REPLACE INTO npcmaker_respawn VALUES('" + dbname + "'," + respawnTime + "," + hp + "," + mp + "," + pos.getX() + "," + pos.getY() + "," + pos.getZ() + ")");
    }

    public RespawnData getRespawnData(String name) {
        return _respawnData.get(name);
    }

    private void loadMakersXml() {
        try {
            final SAXBuilder reader = new SAXBuilder();
            final Document document = reader.build(new File("data/xml/npcmakers/makers.xml"));
            final Element root = document.getRootElement();
            for (Element node : root.getChildren()) {
                if ("npcmaker_ex".equalsIgnoreCase(node.getName())) {
                    String name = node.getAttributeValue("name");
                    String ai = node.getAttributeValue("ai");
                    String ai_parameters = node.getAttributeValue("ai_parameters");
                    String terr = node.getAttributeValue("territory");
                    String ban_terr = node.getAttributeValue("banned_territory");
                    int maximum_npc = Integer.parseInt(node.getAttributeValue("maximum_npc"));

                    Constructor<?> constructor = null;
                    try {
                        if (!ai.equals("default_maker")) {
                            constructor = Scripts.getInstance().getClasses().get("npc.maker." + ai).getConstructors()[0];
                        }
                    } catch (Exception e) {
                        LOGGER.warn("can't find npcmaker ai: " + ai + " use DefaultMake. " + e);
                    }

                    DefaultMaker defaultMaker = null;

                    if (constructor != null) {
                        try {
                            defaultMaker = (DefaultMaker) constructor.newInstance(maximum_npc, name);
                        } catch (Exception e) {
                            LOGGER.warn("can't create npcmaker ai: " + ai + " " + e);
                            e.printStackTrace();
                        }
                    }

                    if (defaultMaker == null) {
                        defaultMaker = new DefaultMaker(maximum_npc, name);
                    }


                    if (ai_parameters != null && !ai_parameters.isEmpty()) {
                        for (String param : ai_parameters.split(";")) {
                            if (!param.isEmpty()) {
                                defaultMaker.setParameter(param.split(":")[0], param.split(":")[1]);
                            }
                        }
                    }

                    if (terr != null) {
                        for (String terr_name : terr.split(";")) {
                            if (!terr_name.isEmpty()) {
                                Territory territory = TerritoryHolder.getInstance().getTerritoryByName(terr_name);
                                if (territory == null) {
                                    LOGGER.warn("maker " + ai + " territory: " + terr_name + " not found.");
                                } else {
                                    defaultMaker.addTerritory(territory);
                                }
                            }
                        }
                    }

                    if (ban_terr != null) {
                        for (String terr_name : ban_terr.split(";")) {
                            if (!terr_name.isEmpty()) {
                                Territory territory = TerritoryHolder.getInstance().getTerritoryByName(terr_name);
                                if (territory == null) {
                                    LOGGER.warn("maker " + ai + " banned territory: " + terr_name + " not found.");
                                } else {
                                    defaultMaker.addBannedTerritory(territory);
                                }
                            }
                        }
                    }
                    for (Element npcElement : node.getChildren()) {
                        if ("npc".equalsIgnoreCase(npcElement.getName())) {
                            int npcId = Integer.parseInt(npcElement.getAttributeValue("npcId"));
                            int total = Integer.parseInt(npcElement.getAttributeValue("total"));
                            int npcmaker_respawn;
                            try{
                                npcmaker_respawn = Integer.parseInt(npcElement.getAttributeValue("respawn"));
                            } catch (Exception ex) {
                                npcmaker_respawn = 0;
                            }
                            int respawn_rand;
                            try{
                                respawn_rand = Integer.parseInt(npcElement.getAttributeValue("respawn_random"));
                            } catch (Exception ex) {
                                respawn_rand = 0;
                            }
                            int is_chase_pc;
                            try{
                                is_chase_pc = Integer.parseInt(npcElement.getAttributeValue("is_chase_pc"));
                            } catch (Exception ex) {
                                is_chase_pc = 0;
                            }

                            String pos = npcElement.getAttributeValue("pos");
                            String npc_ai_params = npcElement.getAttributeValue("ai_parameters");
                            String privates = npcElement.getAttributeValue("privates");
                            String superpoint = npcElement.getAttributeValue("superpoint");
                            String dbname = npcElement.getAttributeValue("dbname");
                            String dbsaving = npcElement.getAttributeValue("dbsaving");
                            boolean rbSpawnSet;
                            try {
                                rbSpawnSet = npcElement.getAttributeValue("boss_respawn_set").equalsIgnoreCase("yes");
                            } catch (Exception ex) {
                                rbSpawnSet = false;
                            }
                            NpcTemplate template;
                            SuperPoint superPointRoute = SuperPointHolder.getInstance().getSuperPointsByName(superpoint);

                            if (npcId > 0 && (template = NpcTemplateHolder.getInstance().getTemplate(npcId)) != null) {
                                SpawnDefine spawnDefine = new SpawnDefine(template, total, npcmaker_respawn, respawn_rand, superPointRoute, npc_ai_params, privates, dbname, dbsaving, defaultMaker, rbSpawnSet);
                                if (is_chase_pc > 0)
                                    spawnDefine.setChasePc(is_chase_pc);

                                if (!"anyware".equalsIgnoreCase(pos)) {
                                    spawnDefine.addPosition(Location.parseLoc(pos), 100);
                                }

                                defaultMaker.addSpawnDefine(spawnDefine);
                            } else
                                LOGGER.warn("npc template id: " + npcId + " not found!");
                        }
                    }

                    _npcMakers.put(name, defaultMaker);
                }
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        }

        LOGGER.info("loaded " + _npcMakers.size() + " npc makers.");
        startNpcMakers();
    }

    private void startNpcMakers() {
        _npcMakers.values().forEach(DefaultMaker::onStart);
    }

    private void loadRespawnData() {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        _respawnData = new ConcurrentHashMap<>();
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM `npcmaker_respawn`");
            rset = statement.executeQuery();

            while (rset.next()) {
                if (rset.getLong("respawn_time") > 0 && rset.getLong("respawn_time") < System.currentTimeMillis()) {
                    continue;
                }
                _respawnData.put(rset.getString("dbname"), new RespawnData(rset.getString("dbname"), rset.getLong("respawn_time"), rset.getInt("hp"), rset.getInt("mp"), rset.getInt("x"), rset.getInt("y"), rset.getInt("z")));
            }
            set("DELETE FROM npcmaker_respawn WHERE respawn_time < " + System.currentTimeMillis());
        } catch (Exception e) {
            LOGGER.warn("can't load npcmaker_respawn data: " + e);
            e.printStackTrace();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }

        LOGGER.info("Loaded " + _respawnData.size() + " scheduled respawns.");
    }

    public void removeRespawnData(RespawnData rd) {
        _respawnData.remove(rd.dbname);
    }
}
