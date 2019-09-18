package ru.j2dev.gameserver.skills;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.NaturalOrderComparator;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Skill;

import java.io.File;
import java.util.*;

public class SkillsEngine {
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillsEngine.class);
    private static final SkillsEngine _instance = new SkillsEngine();

    public static SkillsEngine getInstance() {
        return _instance;
    }

    public List<Skill> loadSkills(final File file) {
        if (file == null) {
            LOGGER.warn("SkillsEngine: File not found!");
            return null;
        }
        //LOGGER.info("Loading skills from " + file.getName() + " ...");
        final DocumentSkill doc = new DocumentSkill(file);
        doc.parse();
        return doc.getSkills();
    }

    public Map<Integer, Map<Integer, Skill>> loadAllSkills() {
        final File dir = new File(Config.DATAPACK_ROOT, "data/xml/stats/skills");
        if (!dir.exists()) {
            LOGGER.info("Dir " + dir.getAbsolutePath() + " not exists");
            return Collections.emptyMap();
        }
        final File[] files = dir.listFiles(pathname -> pathname.getName().endsWith(".xml"));
        final Map<Integer, Map<Integer, Skill>> result = new HashMap<>();
        if (files != null) {
            Arrays.sort(files, NaturalOrderComparator.FILE_NAME_COMPARATOR);
            int maxId = 0;
            int maxLvl = 0;
            for (final File file : files) {
                final List<Skill> skills = loadSkills(file);
                if (skills != null) {
                    for (final Skill skill : skills) {
                        final int skillId = skill.getId();
                        final int skillLevel = skill.getLevel();
                        Map<Integer, Skill> skillLevels = result.computeIfAbsent(skillId, k -> new HashMap<>());
                        skillLevels.put(skillLevel, skill);
                        if (skill.getId() > maxId) {
                            maxId = skill.getId();
                        }
                        if (skill.getLevel() > maxLvl) {
                            maxLvl = skill.getLevel();
                        }
                    }
                }
            }
            LOGGER.info("SkillsEngine: Loaded " + result.size() + " skill templates from XML files. Max id: " + maxId + ", max level: " + maxLvl);

        }
        return result;
    }
}



