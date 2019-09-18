package ru.j2dev.gameserver.tables;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.SkillAcquireHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.base.AcquireType;
import ru.j2dev.gameserver.model.base.EnchantSkillLearn;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SkillTreeTable {
    public static final int NORMAL_ENCHANT_COST_MULTIPLIER = 1;
    public static final int NORMAL_ENCHANT_BOOK = 6622;
    public static final Map<Integer, List<EnchantSkillLearn>> _enchant = new ConcurrentHashMap<>();
    private static final Logger LOGGER = LoggerFactory.getLogger(SkillTreeTable.class);
    private static SkillTreeTable _instance;

    private SkillTreeTable() {
        LOGGER.info("SkillTreeTable: Loaded " + _enchant.size() + " enchanted skills.");
    }

    public static void getInstance() {
        if (_instance == null) {
            _instance = new SkillTreeTable();
        }
    }

    public static void checkSkill(final Player player, final Skill skill) {
        final SkillLearn learnBase = SkillAcquireHolder.getInstance().getSkillLearn(player, player.getClassId(), skill.getId(), 1, AcquireType.NORMAL);
        if (learnBase == null) {
            return;
        }
        if (learnBase.getMinLevel() >= player.getLevel() + Config.ALT_REMOVE_SKILLS_ON_DELEVEL) {
            player.removeSkill(skill, true);
        }
    }

    private static int levelWithoutEnchant(final Skill skill) {
        return (skill.getDisplayLevel() > 100) ? skill.getBaseLevel() : skill.getLevel();
    }

    public static int isEnchantable(final Skill skill) {
        final List<EnchantSkillLearn> enchants = _enchant.get(skill.getId());
        if (enchants == null) {
            return 0;
        }
        for (final EnchantSkillLearn e : enchants) {
            if (e.getBaseLevel() <= skill.getLevel()) {
                return 1;
            }
        }
        return 0;
    }

    public static void unload() {
        if (_instance != null) {
            _instance = null;
        }
        _enchant.clear();
    }
}
