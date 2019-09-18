package ru.j2dev.gameserver.data.xml.parser;

import org.jdom2.Element;
import ru.j2dev.commons.data.xml.AbstractFileParser;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.EnchantSkillHolder;

import java.io.File;
import java.util.Arrays;

public class EnchantSkillParser extends AbstractFileParser<EnchantSkillHolder> {

    private EnchantSkillParser() {
        super(EnchantSkillHolder.getInstance());
    }

    public static EnchantSkillParser getInstance() {
        return LazyHolder.INSTANCE;
    }

    @Override
    public File getXMLFile() {
        return new File(Config.DATAPACK_ROOT, "data/xml/skill_tree/skill_enchant_data.xml");
    }

    @Override
    protected void readData(final EnchantSkillHolder holder, final Element rootElement) {
        rootElement.getChildren("skill").forEach(skillElement -> {
            final int skillId = Integer.parseInt(skillElement.getAttributeValue("id"));
            skillElement.getChildren("route").forEach(skillRouteElement -> {
                final int routeId = Integer.parseInt(skillRouteElement.getAttributeValue("id"));
                skillRouteElement.getChildren("enchant").forEach(skillEnchantElement -> {
                    final int enchantLevel = Integer.parseInt(skillEnchantElement.getAttributeValue("level"));
                    final int skillLevel = Integer.parseInt(skillEnchantElement.getAttributeValue("skillLvl"));
                    final long exp = Long.parseLong(skillEnchantElement.getAttributeValue("exp"));
                    final int sp = Integer.parseInt(skillEnchantElement.getAttributeValue("sp"));
                    final String chancesVal = skillEnchantElement.getAttributeValue("chances");
                    final String[] chancesValArr = chancesVal.split("\\s+");
                    final int[] chances = Arrays.stream(chancesValArr).mapToInt(Integer::parseInt).toArray();
                    final String neededItemIdVal = skillEnchantElement.getAttributeValue("neededItemId");
                    final int neededItemId = (neededItemIdVal != null) ? Integer.parseInt(neededItemIdVal) : 0;
                    final String neededItemCntVal = skillEnchantElement.getAttributeValue("neededItemCount");
                    final int neededItemCnt = (neededItemCntVal != null) ? Integer.parseInt(neededItemCntVal) : 0;
                    holder.addEnchantSkill(skillId, skillLevel, enchantLevel, routeId, exp, sp, chances, neededItemId, neededItemCnt);
                });
            });
        });
    }

    private static class LazyHolder {
        protected static final EnchantSkillParser INSTANCE = new EnchantSkillParser();
    }
}
