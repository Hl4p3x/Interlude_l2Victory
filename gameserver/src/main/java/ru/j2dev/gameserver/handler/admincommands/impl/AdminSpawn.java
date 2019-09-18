package ru.j2dev.gameserver.handler.admincommands.impl;

import gnu.trove.list.TIntList;
import gnu.trove.list.array.TIntArrayList;
import org.jdom2.*;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ai.CharacterAI;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.admincommands.AdminCommandHandler;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.manager.RaidBossSpawnManager;
import ru.j2dev.gameserver.model.*;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.taskmanager.SpawnTaskManager;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;
import ru.j2dev.gameserver.utils.NpcUtils;

import java.io.*;
import java.lang.reflect.Constructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AdminSpawn implements IAdminCommandHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminSpawn.class);

    @Override
    public boolean useAdminCommand(final Enum<?> comm, final String[] wordList, final String fullString, final Player activeChar) {
        final Commands command = (Commands) comm;
        if (!activeChar.getPlayerAccess().CanEditNPC) {
            return false;
        }
        switch (command) {
            case admin_show_spawns: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns.htm"));
                break;
            }
            case admin_spawn_index: {
                try {
                    final String val = fullString.substring(18);
                    activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/spawns/" + val + ".htm"));
                } catch (StringIndexOutOfBoundsException ignored) {
                }
                break;
            }
            case admin_spawn1: {
                final StringTokenizer st = new StringTokenizer(fullString, " ");
                try {
                    st.nextToken();
                    final String id = st.nextToken();
                    int mobCount = 1;
                    if (st.hasMoreTokens()) {
                        mobCount = Integer.parseInt(st.nextToken());
                    }
                    spawnMonster(activeChar, id, 0, mobCount);
                } catch (Exception ignored) {
                }
                break;
            }
            case admin_spawn:
            case admin_spawn_monster: {
                final StringTokenizer st = new StringTokenizer(fullString, " ");
                try {
                    st.nextToken();
                    final String id = st.nextToken();
                    int respawnTime = 30;
                    int mobCount2 = 1;
                    if (st.hasMoreTokens()) {
                        mobCount2 = Integer.parseInt(st.nextToken());
                    }
                    if (st.hasMoreTokens()) {
                        respawnTime = Integer.parseInt(st.nextToken());
                    }
                    spawnMonster(activeChar, id, respawnTime, mobCount2);
                } catch (Exception ignored) {
                }
                break;
            }
            case admin_setai: {
                if (activeChar.getTarget() == null || !activeChar.getTarget().isNpc()) {
                    activeChar.sendMessage("Please select target NPC or mob.");
                    return false;
                }
                final StringTokenizer st = new StringTokenizer(fullString, " ");
                st.nextToken();
                if (!st.hasMoreTokens()) {
                    activeChar.sendMessage("Please specify AI name.");
                    return false;
                }
                final String aiName = st.nextToken();
                final NpcInstance target = (NpcInstance) activeChar.getTarget();
                Constructor<?> aiConstructor = null;
                try {
                    if (!"npc".equalsIgnoreCase(aiName)) {
                        aiConstructor = Class.forName("ru.j2dev.gameserver.ai." + aiName).getConstructors()[0];
                    }
                } catch (Exception e) {
                    try {
                        aiConstructor = Scripts.getInstance().getClasses().get("ai." + aiName).getConstructors()[0];
                    } catch (Exception e2) {
                        activeChar.sendMessage("This type AI not found.");
                        return false;
                    }
                }
                if (aiConstructor != null) {
                    try {
                        target.setAI((CharacterAI) aiConstructor.newInstance(target));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    target.getAI().startAITask();
                    break;
                }
                break;
            }
            case admin_setaiparam: {
                if (activeChar.getTarget() == null || !activeChar.getTarget().isNpc()) {
                    activeChar.sendMessage("Please select target NPC or mob.");
                    return false;
                }
                final StringTokenizer st = new StringTokenizer(fullString, " ");
                st.nextToken();
                if (!st.hasMoreTokens()) {
                    activeChar.sendMessage("Please specify AI parameter name.");
                    activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
                    return false;
                }
                final String paramName = st.nextToken();
                if (!st.hasMoreTokens()) {
                    activeChar.sendMessage("Please specify AI parameter value.");
                    activeChar.sendMessage("USAGE: //setaiparam <param> <value>");
                    return false;
                }
                final String paramValue = st.nextToken();
                final NpcInstance target = (NpcInstance) activeChar.getTarget();
                target.setParameter(paramName, paramValue);
                target.decayMe();
                target.spawnMe();
                activeChar.sendMessage("AI parameter " + paramName + " succesfully setted to " + paramValue);
                break;
            }
            case admin_dumpparams: {
                if (activeChar.getTarget() == null || !activeChar.getTarget().isNpc()) {
                    activeChar.sendMessage("Please select target NPC or mob.");
                    return false;
                }
                final NpcInstance target = (NpcInstance) activeChar.getTarget();
                final MultiValueSet<String> set = target.getParameters();
                if (!set.isEmpty()) {
                    System.out.println("Dump of Parameters:\r\n" + set);
                    break;
                }
                System.out.println("Parameters is empty.");
                break;
            }
            case admin_setheading: {
                final GameObject obj = activeChar.getTarget();
                if (!obj.isNpc()) {
                    activeChar.sendMessage("Target is incorrect!");
                    return false;
                }
                final NpcInstance npc = (NpcInstance) obj;
                npc.setHeading(activeChar.getHeading());
                npc.decayMe();
                npc.spawnMe();
                activeChar.sendMessage("New heading : " + activeChar.getHeading());
                final Spawner spawn = npc.getSpawn();
                if (spawn == null) {
                    activeChar.sendMessage("Spawn for this npc == null!");
                    return false;
                }
                break;
            }
            case admin_spawn_loc_xml: {
                if (wordList.length > 2) {
                    try {
                        final int sideHLen = Math.max(16, Integer.parseInt(wordList[1])) / 2;
                        final int x0 = activeChar.getLoc().getX() - sideHLen;
                        final int y0 = activeChar.getLoc().getY() - sideHLen;
                        final int x2 = activeChar.getLoc().getX() + sideHLen;
                        final int y2 = activeChar.getLoc().getY() + sideHLen;
                        final int zmin = activeChar.getLoc().getZ();
                        final int zmax = activeChar.getLoc().getZ() + 128;
                        Functions.sendDebugMessage(activeChar, "Spawn location saved to custom.xml");
                        try {
                            final SAXBuilder builder = new SAXBuilder();
                            final File xmlFile = new File(Config.DATAPACK_ROOT, "data/xml/spawn/custom.xml");

                            final Document doc = builder.build(xmlFile);
                            final Element rootElement = doc.getRootElement();

                            final Element spawmElement = new Element("spawn");
                            spawmElement.setAttribute(new Attribute("event_name", "[custom_spawn]"));
                            spawmElement.setAttribute(new Attribute("name", "[" + wordList[2] + "]"));

                            rootElement.addContent(spawmElement);
                            final Element meshElement = new Element("mesh");
                            for (int i = 1; i <= 4; i++) {
                                int x = 0;
                                switch (i) {
                                    case 1:
                                        x = x0;
                                        break;
                                    case 2:
                                        x = x2;
                                        break;
                                    case 3:
                                        x = x2;
                                        break;
                                    case 4:
                                        x = x0;
                                }
                                int y = 0;
                                switch (i) {
                                    case 1:
                                        y = y0;
                                        break;
                                    case 2:
                                        y = y0;
                                        break;
                                    case 3:
                                        y = y2;
                                        break;
                                    case 4:
                                        y = y2;
                                }
                                final Element vertexElement1 = new Element("vertex");
                                vertexElement1.setAttribute(new Attribute("x", String.valueOf(x)));
                                vertexElement1.setAttribute(new Attribute("y", String.valueOf(y)));
                                vertexElement1.setAttribute(new Attribute("minz", String.valueOf(zmin)));
                                vertexElement1.setAttribute(new Attribute("maxz", String.valueOf(zmax)));
                                meshElement.addContent(vertexElement1);
                            }
                            spawmElement.addContent(meshElement);

                            final Element npcElement = new Element("npc");

                            npcElement.setAttribute(new Attribute("id", String.valueOf(wordList[2])));
                            npcElement.setAttribute(new Attribute("count", "1"));
                            npcElement.setAttribute(new Attribute("respawn", "60"));
                            spawmElement.addContent(npcElement);

                            final XMLOutputter xmlOutput = new XMLOutputter();

                            xmlOutput.setFormat(Format.getPrettyFormat());
                            xmlOutput.output(doc, new FileWriter(xmlFile));
                        } catch (IOException | JDOMException io) {
                            io.printStackTrace();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                    break;
                }
                activeChar.sendMessage("usage: //spawn_loc_xml <side_len> <npc_id>");
                break;
            }
            case admin_spawnline: {
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/linespawn.htm"));
                break;
            }
            case admin_linespawn_startloc: {
                activeChar.setVar("startLocLine", activeChar.getLoc().toXYZHString(), -1);
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/linespawn.htm"));
                break;
            }
            case admin_linespawn_endloc: {
                activeChar.setVar("endLocLine", activeChar.getLoc().toXYZHString(), -1);
                activeChar.sendPacket(new NpcHtmlMessage(5).setFile("admin/linespawn.htm"));
                break;
            }
            case admin_linespawn: {
                final Location startLoc = Location.parseLoc(activeChar.getVar("startLocLine"));
                final Location endLoc = Location.parseLoc(activeChar.getVar("endLocLine"));
                //final String[] listNpc = wordList[1].replace(" ", "").split(",");
                final int[] npcList = loadNpcLineArray();
                saveLineSpawnToXml(activeChar, getLinePoints(npcList, startLoc, endLoc));
                break;
            }
            case admin_generate_loc: {
                if (wordList.length < 2) {
                    activeChar.sendMessage("Incorrect argument count!");
                    return false;
                }
                final int id2 = Integer.parseInt(wordList[1]);
                int id3 = 0;
                if (wordList.length > 2) {
                    id3 = Integer.parseInt(wordList[2]);
                }
                int min_x = Integer.MIN_VALUE;
                int min_y = Integer.MIN_VALUE;
                int min_z = Integer.MIN_VALUE;
                int max_x = Integer.MAX_VALUE;
                int max_y = Integer.MAX_VALUE;
                int max_z = Integer.MAX_VALUE;
                String name = "";
                for (final NpcInstance _npc : World.getAroundNpc(activeChar)) {
                    if (_npc.getNpcId() == id2 || _npc.getNpcId() == id3) {
                        name = _npc.getName();
                        min_x = Math.min(min_x, _npc.getX());
                        min_y = Math.min(min_y, _npc.getY());
                        min_z = Math.min(min_z, _npc.getZ());
                        max_x = Math.max(max_x, _npc.getX());
                        max_y = Math.max(max_y, _npc.getY());
                        max_z = Math.max(max_z, _npc.getZ());
                    }
                }
                min_x -= 500;
                min_y -= 500;
                max_x += 500;
                max_y += 500;
                System.out.println("(0,'" + name + "'," + min_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
                System.out.println("(0,'" + name + "'," + min_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
                System.out.println("(0,'" + name + "'," + max_x + "," + max_y + "," + min_z + "," + max_z + ",0),");
                System.out.println("(0,'" + name + "'," + max_x + "," + min_y + "," + min_z + "," + max_z + ",0),");
                System.out.println("delete from spawnlist where npc_templateid in (" + id2 + ", " + id3 + ") and locx <= " + min_x + " and locy <= " + min_y + " and locz <= " + min_z + " and locx >= " + max_x + " and locy >= " + max_y + " and locz >= " + max_z + ";");
                break;
            }
            case admin_dumpspawntasks: {
                System.out.println(SpawnTaskManager.getInstance());
                break;
            }
            case admin_dumpspawn: {
                final StringTokenizer st = new StringTokenizer(fullString, " ");
                try {
                    st.nextToken();
                    final String id4 = st.nextToken();
                    final int respawnTime2 = 30;
                    final int mobCount3 = 1;
                    spawnMonster(activeChar, id4, respawnTime2, mobCount3);
                    try {
                        new File("dumps").mkdir();
                        final File f = new File("dumps/spawndump.txt");
                        if (!f.exists()) {
                            f.createNewFile();
                        }
                        final FileWriter writer = new FileWriter(f, true);
                        writer.write("<spawn count=\"1\" respawn=\"60\" respawn_random=\"0\" period_of_day=\"none\">\n\t<point x=\"" + activeChar.getLoc().x + "\" y=\"" + activeChar.getLoc().y + "\" z=\"" + activeChar.getLoc().z + "\" h=\"" + activeChar.getLoc().h + "\" />\n\t<npc id=\"" + Integer.parseInt(id4) + "\" /><!--" + NpcTemplateHolder.getInstance().getTemplate(Integer.parseInt(id4)).getName() + "-->\n</spawn>\n");
                        writer.close();
                    } catch (Exception ignored) {
                    }
                } catch (Exception ignored) {
                }
                break;
            }
            case admin_chess_spawn: {
                final Location loc = activeChar.getLoc();
                spawnChessMobs(8, 20001, loc);
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    private void spawnChessMobs(int cell, final int npcId, final Location startLoc) {
        cell /= 2;
        Location[][] a = new Location[cell][cell];

        for(int i = 0; i < cell; i++) {
            for(int j = 0; j < cell; j++) {
                a[i][j] = startLoc;
                if((i+j) % (int) Math.sqrt(cell) == 0) {
                    a[i][j].x -= 50;
                    a[i][j].y -= 50;

                }
                else if((i+j) % 2 == 0) {
                    a[i][j].x += 50;
                } else {
                    a[i][j].y += 50;
                }
                NpcUtils.spawnSingle(npcId, a[i][j]);
            }
        }

    }

    private int[] loadNpcLineArray() {
        TIntList result = new TIntArrayList();
        final File f = new File("./data/npclinespawnarray.txt");
        int i = 0;
        try(LineNumberReader lnr = new LineNumberReader(new BufferedReader(new FileReader(f)))) {
            String line;
            while ((line = lnr.readLine()) != null) {
                result.add(Integer.parseInt(line));
                i++;
            }
            result.sort();
            LOGGER.info("LineSpawn loaded "+i+ "npcId's count from txt file");
        } catch (final FileNotFoundException e) {
            LOGGER.warn("npclinespawnarray.txt is missing in data folder");
        } catch (final NullPointerException npe) {
            LOGGER.warn("NPE on loading npclinespawnarray.txt", npe.getLocalizedMessage());
        } catch (final Exception e) {
            LOGGER.error("npclinespawnarray.txt Error in line " + i + " :" + e);
        }

        return result.toArray();
    }

    private void spawnMonster(final Player activeChar, String monsterId, final int respawnTime, final int mobCount) {
        GameObject target = activeChar.getTarget();
        if (target == null) {
            target = activeChar;
        }
        final Pattern pattern = Pattern.compile("[0-9]*");
        final Matcher regexp = pattern.matcher(monsterId);
        NpcTemplate template;
        if (regexp.matches()) {
            final int monsterTemplate = Integer.parseInt(monsterId);
            template = NpcTemplateHolder.getInstance().getTemplate(monsterTemplate);
        } else {
            monsterId = monsterId.replace('_', ' ');
            template = NpcTemplateHolder.getInstance().getTemplateByName(monsterId);
        }
        if (template == null) {
            activeChar.sendMessage("Incorrect monster template.");
            return;
        }
        try {
            final SimpleSpawner spawn = new SimpleSpawner(template);
            spawn.setLoc(target.getLoc());
            spawn.setAmount(mobCount);
            spawn.setHeading(activeChar.getHeading());
            spawn.setRespawnDelay(respawnTime);
            spawn.setReflection(activeChar.getReflection());
            if (RaidBossSpawnManager.getInstance().isDefined(template.getNpcId())) {
                activeChar.sendMessage("Raid Boss " + template.name + " already spawned.");
            } else {
                spawn.init();
                if (respawnTime == 0) {
                    spawn.stopRespawn();
                }
                activeChar.sendMessage("Created " + template.name + " on " + target.getObjectId() + ".");
            }

            if (Config.SAVE_ADMIN_SPAWN) {
                try {
                    final SAXBuilder builder = new SAXBuilder();
                    final File xmlFile = new File(Config.DATAPACK_ROOT, "data/xml/spawn/custom.xml");

                    final Document doc = builder.build(xmlFile);
                    final Element rootElement = doc.getRootElement();

                    final Element spawmElement = new Element("spawn");
                    spawmElement.addContent(new Comment(activeChar.getName() + " spawned : " + template.getName() + " on Loc : " + target.getLoc()));
                    spawmElement.setAttribute(new Attribute("event_name", "[custom_spawn]"));
                    spawmElement.setAttribute(new Attribute("name", "[" + template.getName() + "]"));

                    rootElement.addContent(spawmElement);

                    final Element npcElement = new Element("npc");

                    npcElement.setAttribute(new Attribute("id", String.valueOf(template.getNpcId())));
                    npcElement.setAttribute(new Attribute("count", "1"));
                    npcElement.setAttribute(new Attribute("respawn", "60"));
                    npcElement.setAttribute(new Attribute("pos", String.valueOf(target.getX()) + " " + String.valueOf(target.getY()) + " " + String.valueOf(target.getZ()) + " " + String.valueOf(target.getHeading())));
                    spawmElement.addContent(npcElement);

                    final XMLOutputter xmlOutput = new XMLOutputter();

                    // display nice nice
                    xmlOutput.setFormat(Format.getPrettyFormat());
                    xmlOutput.output(doc, new FileWriter(xmlFile));

                    // xmlOutput.output(doc, System.out);
                    activeChar.sendMessage("Spawn saved to custom.xml");
                } catch (IOException | JDOMException io) {
                    io.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            activeChar.sendMessage("Target is not ingame.");
        }
    }

    private void saveLineSpawnToXml(final Player activeChar, final Map<Location, String> locationMap) {
        final Pattern pattern = Pattern.compile("[0-9]*");
        for(Map.Entry<Location, String> npcLoc : locationMap.entrySet()) {
            final Location location = npcLoc.getKey();
            String monsterId = npcLoc.getValue();
            monsterId = monsterId.trim();
            final Matcher regexp = pattern.matcher(monsterId);
            NpcTemplate template;
            if (regexp.matches()) {
                final int monsterTemplate = Integer.parseInt(monsterId);
                template = NpcTemplateHolder.getInstance().getTemplate(monsterTemplate);
            } else {
                monsterId = monsterId.replace('_', ' ');
                template = NpcTemplateHolder.getInstance().getTemplateByName(monsterId);
            }
            if (template == null) {
                return;
            }
            try {
                final SAXBuilder builder = new SAXBuilder();
                final File xmlFile = new File(Config.DATAPACK_ROOT, "data/xml/spawn/custom.xml");

                final Document doc = builder.build(xmlFile);
                final Element rootElement = doc.getRootElement();

                final Element spawmElement = new Element("spawn");
                spawmElement.setAttribute(new Attribute("event_name", "[custom_npc_spawn]"));
                spawmElement.setAttribute(new Attribute("name", "[" + template.getName() + "]"));

                rootElement.addContent(spawmElement);

                final Element npcElement = new Element("npc");

                npcElement.setAttribute(new Attribute("id", String.valueOf(template.getNpcId())));
                npcElement.setAttribute(new Attribute("count", "1"));
                npcElement.setAttribute(new Attribute("respawn", "60"));
                npcElement.setAttribute(new Attribute("pos", location.toXYZHString()));
                Comment comment = new Comment("Added on : "+LocalDateTime.now().toString()+" in zones : "+activeChar.getZonesNames());
                spawmElement.addContent(npcElement);
                spawmElement.addContent(comment);


                final XMLOutputter xmlOutput = new XMLOutputter();

                // display nice nice
                xmlOutput.setFormat(Format.getPrettyFormat());
                xmlOutput.output(doc, new FileWriter(xmlFile));
            } catch (IOException | JDOMException io) {
                io.printStackTrace();
            }
        }
        AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, "admin_reload_spawn");

    }

    private Map<Location, String> getLinePoints(final int[] npcIds, final Location startLoc, final Location endLoc) {
        int i = 0;
        Map<Location, String> npcLocs = new HashMap<>();
        for (int npcId : npcIds) {
            final Location loc = new Location(startLoc.x, startLoc.y, startLoc.z, startLoc.h);
            loc.x += ((endLoc.x - startLoc.x) / npcIds.length) * i;
            loc.y += ((endLoc.y - startLoc.y) / npcIds.length) * i;
            i++;
            npcLocs.put(loc, String.valueOf(npcId));
        }
        return npcLocs;
    }

    private enum Commands {
        admin_show_spawns,
        admin_spawn,
        admin_spawn_monster,
        admin_spawn_index,
        admin_spawn1,
        admin_setheading,
        admin_setai,
        admin_setaiparam,
        admin_dumpparams,
        admin_spawn_loc_xml,
        admin_generate_loc,
        admin_dumpspawntasks,
        admin_chess_spawn,
        admin_spawnline,
        admin_linespawn_endloc,
        admin_linespawn_startloc,
        admin_linespawn,
        admin_dumpspawn
    }
}
