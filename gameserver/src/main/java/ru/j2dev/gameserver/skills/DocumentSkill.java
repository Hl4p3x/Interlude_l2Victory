package ru.j2dev.gameserver.skills;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.j2dev.gameserver.data.xml.holder.EnchantSkillHolder;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Skill.SkillType;
import ru.j2dev.gameserver.stats.conditions.Condition;
import ru.j2dev.gameserver.templates.SkillEnchant;
import ru.j2dev.gameserver.templates.StatsSet;

import java.io.File;
import java.util.*;
import java.util.Map.Entry;

@Deprecated
public final class DocumentSkill extends DocumentBase {
    private static final Logger LOGGER = LoggerFactory.getLogger(DocumentSkill.class);
    private static final String SKILL_ENCHANT_NODE_NAME = "enchant";
    private static final Comparator<Integer> INTEGER_KEY_ASC_COMPARATOR = Comparator.comparingInt(o -> o);

    protected final Map<String, Map<Integer, Object>> tables;
    private final Set<String> usedTables;
    private final List<Skill> skillsInFile;
    private SkillLoad currentSkill;

    DocumentSkill(final File file) {
        super(file);
        tables = new LinkedHashMap<>();
        currentSkill = null;
        usedTables = new HashSet<>();
        skillsInFile = new LinkedList<>();
    }

    protected void resetTable() {
        if (!usedTables.isEmpty()) {
            for (final String table : tables.keySet()) {
                if (!usedTables.contains(table)) {
                    LOGGER.warn("Unused table " + table + " for skill " + currentSkill.id);
                }
            }
        }
        usedTables.clear();
        tables.clear();
    }

    private void setCurrentSkill(final SkillLoad skill) {
        currentSkill = skill;
    }

    protected List<Skill> getSkills() {
        return skillsInFile;
    }

    @Override
    protected Object getTableValue(final String name) {
        final Map<Integer, Object> values = tables.get(name);
        if (values == null) {
            LOGGER.error("No table " + name + " for skill " + currentSkill.id);
            return 0;
        }
        if (!values.containsKey(currentSkill.currentLevel)) {
            LOGGER.error("No value in table " + name + " for skill " + currentSkill.id + " at level " + currentSkill.currentLevel);
            return 0;
        }
        usedTables.add(name);
        return values.get(currentSkill.currentLevel);
    }

    @Override
    protected Object getTableValue(final String name, final int level) {
        final Map<Integer, Object> values = tables.get(name);
        if (values == null) {
            LOGGER.error("No table " + name + " for skill " + currentSkill.id);
            return 0;
        }
        if (!values.containsKey(level)) {
            LOGGER.error("No value in table " + name + " for skill " + currentSkill.id + " at level " + level);
            return 0;
        }
        usedTables.add(name);
        return values.get(level);
    }

    @Override
    protected void parseDocument(final Document doc) {
        for (Node n = doc.getFirstChild(); n != null; n = n.getNextSibling()) {
            if ("list".equalsIgnoreCase(n.getNodeName())) {
                for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling()) {
                    if ("skill".equalsIgnoreCase(d.getNodeName())) {
                        parseSkill(d);
                        skillsInFile.addAll(currentSkill.skills);
                        resetTable();
                    }
                }
            } else if ("skill".equalsIgnoreCase(n.getNodeName())) {
                parseSkill(n);
                skillsInFile.addAll(currentSkill.skills);
            }
        }
    }

    private void loadTable(final Node tableNode, final int skillLevelOffset, final int levels) {
        final NamedNodeMap tableNodeAttrs = tableNode.getAttributes();
        final String tableName = tableNodeAttrs.getNamedItem("name").getNodeValue();
        final Object[] tableContent = fillTableToSize(parseTable(tableNode), levels);
        Map<Integer, Object> globalTableLevels = tables.computeIfAbsent(tableName, k -> new TreeMap<>(INTEGER_KEY_ASC_COMPARATOR));
        for (int tblContIdx = 0; tblContIdx < tableContent.length; ++tblContIdx) {
            final int skillLvl = skillLevelOffset + tblContIdx;
            if (globalTableLevels.containsKey(skillLvl)) {
                LOGGER.error("Duplicate skill level " + skillLvl + " in table " + tableName + " in skill " + currentSkill.id);
                return;
            }
            globalTableLevels.put(skillLvl, tableContent[tblContIdx]);
        }
    }

    protected void parseSkill(final Node n) {
        final NamedNodeMap attrs = n.getAttributes();
        final int skillId = Integer.parseInt(attrs.getNamedItem("id").getNodeValue());
        final String skillName = attrs.getNamedItem("name").getNodeValue();
        final int skillBaseLevels = Integer.parseInt(attrs.getNamedItem("levels").getNodeValue());
        setCurrentSkill(new SkillLoad(skillId, skillName));
        final List<Integer> skillLevelsList = new ArrayList<>();
        try {
            for (int skillLvl = 1; skillLvl <= skillBaseLevels; ++skillLvl) {
                skillLevelsList.add(skillLvl);
            }
            final Node skillRootNode = n.cloneNode(true);
            for (int skillChildNodeIdx = 0, skillChildNodesLen = skillRootNode.getChildNodes().getLength(); skillChildNodeIdx < skillChildNodesLen; ++skillChildNodeIdx) {
                int skillEnchantLevels;
                final Node skillEnchRootNode = skillRootNode.getChildNodes().item(skillChildNodeIdx);
                final String skillEnchNodeName = skillEnchRootNode.getNodeName();
                if (skillEnchNodeName.startsWith("enchant")) {
                    int skillCurrEnchantRoute;
                    try {
                        skillCurrEnchantRoute = Integer.parseInt(skillEnchNodeName.substring("enchant".length()));
                    } catch (NumberFormatException nfe) {
                        LOGGER.error("Wrong enchant " + skillEnchNodeName + " in skill " + skillId);
                        break;
                    }
                    final int skillEnchRouteFirstSkillLevel = EnchantSkillHolder.getInstance().getFirstSkillLevelOf(skillId, skillCurrEnchantRoute);
                    final Node skillEnchLevelsNode = skillEnchRootNode.getAttributes().getNamedItem("levels");
                    if (skillEnchLevelsNode != null) {
                        skillEnchantLevels = Integer.parseInt(skillEnchLevelsNode.getNodeValue());
                    } else {
                        LOGGER.warn("Skill " + skillId + " have no enchant levels in route " + skillCurrEnchantRoute + ".");
                        skillEnchantLevels = EnchantSkillHolder.getInstance().getMaxEnchantLevelOf(skillId);
                    }
                    final int skillRouteMaxEnchantLevel = EnchantSkillHolder.getInstance().getMaxEnchantLevelOf(skillId);
                    if (skillEnchantLevels != skillRouteMaxEnchantLevel) {
                        LOGGER.warn("Unknown enchant levels " + skillEnchantLevels + " for skill " + skillId + ". Actual " + skillRouteMaxEnchantLevel);
                    }
                    for (int skillEnchantLevel = 1; skillEnchantLevel <= skillEnchantLevels; ++skillEnchantLevel) {
                        final SkillEnchant skillEnchant = EnchantSkillHolder.getInstance().getSkillEnchant(skillId, skillCurrEnchantRoute, skillEnchantLevel);
                        if (skillEnchant == null) {
                            LOGGER.error("No enchant level " + skillEnchantLevel + " in route " + skillCurrEnchantRoute + " for skill " + skillId);
                            break;
                        }
                        skillLevelsList.add(skillEnchant.getSkillLevel());
                    }
                    for (Node skillEnchNode = skillEnchRootNode.getFirstChild(); skillEnchNode != null; skillEnchNode = skillEnchNode.getNextSibling()) {
                        if ("table".equalsIgnoreCase(skillEnchNode.getNodeName())) {
                            loadTable(skillEnchNode, skillEnchRouteFirstSkillLevel, skillEnchantLevels);
                        } else if (skillEnchNode.getNodeType() == 1) {
                            LOGGER.error("Unknown element of enchant \"" + skillEnchNode.getNodeName() + "\" in skill " + skillId);
                        }
                    }
                }
            }
            for (Node skillTableNode = n.getFirstChild(); skillTableNode != null; skillTableNode = skillTableNode.getNextSibling()) {
                if ("table".equalsIgnoreCase(skillTableNode.getNodeName())) {
                    loadTable(skillTableNode, 1, skillBaseLevels);
                }
            }
            for (final Entry<String, Map<Integer, Object>> tableEntry : tables.entrySet()) {
                final Map<Integer, Object> table = tableEntry.getValue();
                final Object baseEnchantValue = table.get(skillBaseLevels);
                for (final Integer skillLevel : skillLevelsList) {
                    if (skillLevel > skillBaseLevels && !table.containsKey(skillLevel)) {
                        table.put(skillLevel, baseEnchantValue);
                    }
                }
            }
            for (final Integer skillLevel2 : skillLevelsList) {
                final StatsSet currLevelStatSet = new StatsSet();
                currLevelStatSet.set("skill_id", currentSkill.id);
                currLevelStatSet.set("level", skillLevel2);
                currLevelStatSet.set("name", currentSkill.name);
                currLevelStatSet.set("base_level", skillBaseLevels);
                currentSkill.sets.put(skillLevel2, currLevelStatSet);
            }
            for (final Integer skillLevel2 : skillLevelsList) {
                for (Node skillSetNode = n.getFirstChild(); skillSetNode != null; skillSetNode = skillSetNode.getNextSibling()) {
                    if ("set".equalsIgnoreCase(skillSetNode.getNodeName())) {
                        final StatsSet skillCurrLevelSet = currentSkill.sets.get(skillLevel2);
                        currentSkill.currentLevel = skillLevel2;
                        parseBeanSet(skillSetNode, skillCurrLevelSet, skillLevel2);
                    }
                }
            }
            for (final StatsSet currStatsSet : currentSkill.sets.values()) {
                final Skill newSkill = currStatsSet.getEnum("skillType", SkillType.class).makeSkill(currStatsSet);
                currentSkill.currentSkills.put(newSkill.getLevel(), newSkill);
            }
            for (final Integer skillLevel2 : skillLevelsList) {
                currentSkill.currentLevel = skillLevel2;
                final Skill currSkill = currentSkill.currentSkills.get(skillLevel2);
                if (currSkill == null) {
                    LOGGER.error("Undefined skill id " + skillId + " level " + skillLevel2);
                    return;
                }
                currSkill.setDisplayLevel(skillLevel2);
                for (Node skillNode = n.getFirstChild(); skillNode != null; skillNode = skillNode.getNextSibling()) {
                    final String skillNodeName = skillNode.getNodeName();
                    if ("cond".equalsIgnoreCase(skillNodeName)) {
                        final Condition condition = parseCondition(skillNode.getFirstChild());
                        if (condition != null) {
                            final Node sysMsgIdAttr = skillNode.getAttributes().getNamedItem("msgId");
                            if (sysMsgIdAttr != null) {
                                final int sysMsgId = parseNumber(sysMsgIdAttr.getNodeValue()).intValue();
                                condition.setSystemMsg(sysMsgId);
                            }
                            currSkill.attach(condition);
                        }
                    } else if ("for".equalsIgnoreCase(skillNodeName)) {
                        parseTemplate(skillNode, currSkill);
                    } else if ("triggers".equalsIgnoreCase(skillNodeName)) {
                        parseTrigger(skillNode, currSkill);
                    }
                }
            }
            currentSkill.skills.addAll(currentSkill.currentSkills.values());
        } catch (Exception e) {
            LOGGER.error("Error loading skill " + skillId, e);
        }
    }

    protected Object[] parseTable(final Node n) {
        final NamedNodeMap attrs = n.getAttributes();
        final String name = attrs.getNamedItem("name").getNodeValue();
        if (name.charAt(0) != '#') {
            throw new IllegalArgumentException("Table name must start with #");
        }
        final StringTokenizer data = new StringTokenizer(n.getFirstChild().getNodeValue());
        final List<String> array = new ArrayList<>();
        while (data.hasMoreTokens()) {
            array.add(data.nextToken());
        }
        return array.toArray(new Object[0]);
    }

    private Object[] fillTableToSize(Object[] table, final int size) {
        if (table.length < size) {
            final Object[] ret = new Object[size];
            System.arraycopy(table, 0, ret, 0, table.length);
            table = ret;
        }
        for (int j = 1; j < size; ++j) {
            if (table[j] == null) {
                table[j] = table[j - 1];
            }
        }
        return table;
    }

    public class SkillLoad {
        public final int id;
        public final String name;
        public final Map<Integer, StatsSet> sets;
        public final List<Skill> skills;
        public final Map<Integer, Skill> currentSkills;
        public int currentLevel;

        public SkillLoad(final int id_, final String name_) {
            id = id_;
            name = name_;
            sets = new TreeMap<>(INTEGER_KEY_ASC_COMPARATOR);
            skills = new ArrayList<>();
            currentSkills = new TreeMap<>(INTEGER_KEY_ASC_COMPARATOR);
        }
    }
}
