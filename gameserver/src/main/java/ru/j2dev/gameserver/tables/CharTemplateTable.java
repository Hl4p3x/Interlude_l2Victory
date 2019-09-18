package ru.j2dev.gameserver.tables;

import org.apache.commons.io.FilenameUtils;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import ru.j2dev.commons.data.xml.helpers.SimpleDTDEntityResolver;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.ShortCut;
import ru.j2dev.gameserver.model.base.ClassId;
import ru.j2dev.gameserver.model.base.Experience;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.templates.PlayerTemplate;
import ru.j2dev.gameserver.templates.StatsSet;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.io.FileInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class CharTemplateTable {
    private static final Logger LOGGER = LoggerFactory.getLogger(CharTemplateTable.class);
    private final Map<Integer, PlayerTemplate> _templates;
    private Map<ClassId, List<ShortCut>> _shortCuts;

    private CharTemplateTable() {
        _templates = new HashMap<>();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT * FROM class_list, char_templates WHERE class_list.id = char_templates.classId ORDER BY class_list.id");
            rset = statement.executeQuery();
            while (rset.next()) {
                final StatsSet set = new StatsSet();
                final ClassId classId = ClassId.VALUES[rset.getInt("class_list.id")];
                set.set("classId", rset.getInt("class_list.id"));
                set.set("className", rset.getString("char_templates.className"));
                set.set("raceId", rset.getInt("char_templates.RaceId"));
                set.set("baseSTR", rset.getInt("char_templates.STR"));
                set.set("baseCON", rset.getInt("char_templates.CON"));
                set.set("baseDEX", rset.getInt("char_templates.DEX"));
                set.set("baseINT", rset.getInt("char_templates._INT"));
                set.set("baseWIT", rset.getInt("char_templates.WIT"));
                set.set("baseMEN", rset.getInt("char_templates.MEN"));
                set.set("baseHpMax", 0);
                set.set("baseMpMax", 0);
                set.set("baseCpMax", 0);
                set.set("baseHpReg", 0.01);
                set.set("baseCpReg", 0.01);
                set.set("baseMpReg", 0.01);
                set.set("basePAtk", rset.getInt("char_templates.p_atk"));
                set.set("basePDef", rset.getInt("char_templates.p_def"));
                set.set("baseMAtk", rset.getInt("char_templates.m_atk"));
                set.set("baseMDef", 41);
                set.set("basePAtkSpd", rset.getInt("char_templates.p_spd"));
                set.set("baseMAtkSpd", classId.isMage() ? Config.BASE_MAGE_CAST_SPEED : Config.BASE_WARRIOR_CAST_SPEED);
                set.set("baseCritRate", rset.getInt("char_templates.critical"));
                set.set("baseWalkSpd", rset.getInt("char_templates.walk_spd"));
                set.set("baseRunSpd", rset.getInt("char_templates.run_spd"));
                set.set("baseShldDef", 0);
                set.set("baseShldRate", 0);
                switch (set.getInteger("raceId")) {
                    case 3: {
                        set.set("baseAtkRange", 25);
                        break;
                    }
                    default: {
                        set.set("baseAtkRange", 20);
                        break;
                    }
                }
                set.set("baseExp", Experience.getExpForLevel(rset.getInt("char_templates.level")));
                set.set("spawnX", rset.getInt("char_templates.x"));
                set.set("spawnY", rset.getInt("char_templates.y"));
                set.set("spawnZ", rset.getInt("char_templates.z"));
                set.set("isMale", true);
                set.set("collision_radius", rset.getDouble("char_templates.m_col_r"));
                set.set("collision_height", rset.getDouble("char_templates.m_col_h"));
                PlayerTemplate ct = new PlayerTemplate(set);
                for (int x = 1; x < 15; ++x) {
                    if (rset.getInt("char_templates.items" + x) != 0) {
                        ct.addItem(rset.getInt("char_templates.items" + x));
                    }
                }
                _templates.put(ct.classId.getId(), ct);
                set.set("isMale", false);
                set.set("collision_radius", rset.getDouble("char_templates.f_col_r"));
                set.set("collision_height", rset.getDouble("char_templates.f_col_h"));
                ct = new PlayerTemplate(set);
                for (int x = 1; x < 15; ++x) {
                    final int itemId = rset.getInt("char_templates.items" + x);
                    if (itemId != 0) {
                        ct.addItem(itemId);
                    }
                }
                _templates.put(ct.classId.getId() | 0x100, ct);
            }
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        LOGGER.info("CharTemplateTable: Loaded " + _templates.size() + " Character Templates.");
        _shortCuts = parseShortCuts(new File(Config.DATAPACK_ROOT, "data/xml/others/character_shortcuts.xml"));
    }

    public static CharTemplateTable getInstance() {
        return LazyHolder.INSTANCE;
    }

    private static class LazyHolder {
        protected static final CharTemplateTable INSTANCE = new CharTemplateTable();
    }

    private static Map<ClassId, List<ShortCut>> parseShortCuts(final File file) {
        final Map<ClassId, List<ShortCut>> result = new HashMap<>();
        if (!file.exists()) {
            LOGGER.warn("File " + file.getAbsolutePath() + " not exists");
            return Collections.emptyMap();
        }
        final SAXBuilder reader = new SAXBuilder();
        reader.setErrorHandler(new ErrorHandler() {
            @Override
            public void warning(final SAXParseException exception) {
                LOGGER.warn("File: " + file.getName() + ":" + exception.getLineNumber() + " warning: " + exception.getMessage());
            }

            @Override
            public void error(final SAXParseException exception) {
                LOGGER.error("File: " + file.getName() + ":" + exception.getLineNumber() + " error: " + exception.getMessage());
            }

            @Override
            public void fatalError(final SAXParseException exception) {
                LOGGER.error("File: " + file.getName() + ":" + exception.getLineNumber() + " fatal: " + exception.getMessage());
            }
        });
        reader.setEntityResolver(new SimpleDTDEntityResolver(new File(file.getParentFile(), FilenameUtils.removeExtension(file.getName()) + ".dtd")));
        try (FileInputStream fis = new FileInputStream(file)) {
            final Document document = reader.build(fis);
            final Element rootElement = document.getRootElement();
            for (Element listElement : rootElement.getChildren()) {
                if ("shortcut".equals(listElement.getName())) {
                    ClassId classId = null;
                    final String classIdStr = listElement.getAttributeValue("classId");
                    if (classIdStr != null) {
                        classId = ClassId.valueOf(classIdStr);
                    }
                    final int slot = Integer.parseInt(listElement.getAttributeValue("slot", "0"));
                    final int page = Integer.parseInt(listElement.getAttributeValue("page", "0"));
                    final String shortCutType = listElement.getAttributeValue("type");
                    int type;
                    if ("ITEM".equalsIgnoreCase(shortCutType) || "TYPE_ITEM".equalsIgnoreCase(shortCutType)) {
                        type = 1;
                    } else if ("SKILL".equalsIgnoreCase(shortCutType) || "TYPE_SKILL".equalsIgnoreCase(shortCutType)) {
                        type = 2;
                    } else {
                        if (!"ACTION".equalsIgnoreCase(shortCutType) && !"TYPE_ACTION".equalsIgnoreCase(shortCutType)) {
                            throw new RuntimeException("Unknown short cut type");
                        }
                        type = 3;
                    }
                    final int id = Integer.parseInt(listElement.getAttributeValue("id", "0"));
                    final int level = Integer.parseInt(listElement.getAttributeValue("level", "-1"));
                    final int characterType = Integer.parseInt(listElement.getAttributeValue("characterType", "1"));
                    final ShortCut shortCut = new ShortCut(slot, page, type, id, level, characterType);
                    if (classId == null) {
                        for (final ClassId cId : ClassId.VALUES) {
                            List<ShortCut> shortCuts = result.computeIfAbsent(cId, k -> new ArrayList<>());
                            shortCuts.add(shortCut);
                        }
                    } else {
                        List<ShortCut> shortCuts2 = result.computeIfAbsent(classId, k -> new ArrayList<>());
                        shortCuts2.add(shortCut);
                    }
                }
            }
        } catch (Exception e) {
            LOGGER.warn("Exception: " + e, e);
        }
        return result;
    }

    public PlayerTemplate getTemplate(final ClassId classId, final boolean female) {
        return getTemplate(classId.getId(), female);
    }

    public PlayerTemplate getTemplate(final int classId, final boolean female) {
        int key = classId;
        if (female) {
            key |= 0x100;
        }
        return _templates.get(key);
    }

    public List<ShortCut> getShortCuts(final ClassId classId) {
        final List<ShortCut> result = _shortCuts.get(classId);
        if (result == null) {
            return Collections.emptyList();
        }
        return result;
    }

    public List<ShortCut> getShortCuts(final Player player) {
        return getShortCuts(player.getClassId());
    }

}