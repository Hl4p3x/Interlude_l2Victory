package services;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.base.Race;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.tables.SkillTable;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SupportMagic extends Functions implements OnInitScriptListener {
    private static final Logger LOG = LoggerFactory.getLogger(SupportMagic.class);
    private static final File NEWBIE_BUFFS_XML_FILE = new File(Config.DATAPACK_ROOT, "data/newbie_buffs.xml");
    private static List<NewbieBuffsList> NEWBIE_BUFFS;

    public static void doSupportMagic(final NpcInstance npc, final Player player, final boolean servitor) {
        if (player.isCursedWeaponEquipped()) {
            return;
        }
        for (final NewbieBuffsList newbieBuffsList : SupportMagic.NEWBIE_BUFFS) {
            if (newbieBuffsList.getType().canUse(player)) {
                final int lvl = player.getLevel();
                if (lvl < newbieBuffsList.getMinLevel()) {
                    show("default/newbie_nosupport_min.htm", player, npc, "%min_level%", newbieBuffsList.getMinLevel());
                    return;
                }
                if (lvl > newbieBuffsList.getMaxLevel()) {
                    show("default/newbie_nosupport_max.htm", player, npc, "%max_level%", newbieBuffsList.getMaxLevel());
                    return;
                }
                newbieBuffsList.apply(npc, player);
            }
        }
    }

    private static NewbieBuffsList parseNewbieBuffsList(final NewbieBuffsListType type, final Node newbieBuffsListNode) {
        int listMinLevel;
        final int listMaxLevel = listMinLevel = Integer.parseInt(newbieBuffsListNode.getAttributes().getNamedItem("max_level").getNodeValue());
        final List<Pair<Integer, Skill>> buffsList = new ArrayList<>();
        for (Node newbieBuffsListEntryNode = newbieBuffsListNode.getFirstChild(); newbieBuffsListEntryNode != null; newbieBuffsListEntryNode = newbieBuffsListEntryNode.getNextSibling()) {
            if ("buff".equalsIgnoreCase(newbieBuffsListEntryNode.getNodeName())) {
                final NamedNodeMap buffAttrs = newbieBuffsListEntryNode.getAttributes();
                final int skillId = Integer.parseInt(buffAttrs.getNamedItem("skill_id").getNodeValue());
                final int skillLevel = Integer.parseInt(buffAttrs.getNamedItem("skill_level").getNodeValue());
                final int minLevel = Integer.parseInt(buffAttrs.getNamedItem("min_level").getNodeValue());
                if (minLevel < listMinLevel) {
                    listMinLevel = minLevel;
                }
                final Pair<Integer, Skill> newbieBuffPair = Pair.of(minLevel, SkillTable.getInstance().getInfo(skillId, skillLevel));
                buffsList.add(newbieBuffPair);
            }
        }
        return new NewbieBuffsList(type, listMinLevel, listMaxLevel, buffsList.toArray(new Pair[buffsList.size()]));
    }

    private static List<NewbieBuffsList> parseNewbieBuffsDocument(final Document newbieBuffsDoc) {
        final List<NewbieBuffsList> result = new ArrayList<>();
        for (Node newbieBuffsListRoot = newbieBuffsDoc.getFirstChild(); newbieBuffsListRoot != null; newbieBuffsListRoot = newbieBuffsListRoot.getNextSibling()) {
            if ("list".equalsIgnoreCase(newbieBuffsListRoot.getNodeName())) {
                for (Node newbieBuffsListNode = newbieBuffsListRoot.getFirstChild(); newbieBuffsListNode != null; newbieBuffsListNode = newbieBuffsListNode.getNextSibling()) {
                    if ("warrior".equalsIgnoreCase(newbieBuffsListNode.getNodeName())) {
                        final NewbieBuffsList warriorBuffsList = parseNewbieBuffsList(NewbieBuffsListType.WARRIOR, newbieBuffsListNode);
                        result.add(warriorBuffsList);
                    } else if ("mage".equalsIgnoreCase(newbieBuffsListNode.getNodeName())) {
                        final NewbieBuffsList mageBuffsList = parseNewbieBuffsList(NewbieBuffsListType.MAGE, newbieBuffsListNode);
                        result.add(mageBuffsList);
                    }
                }
            }
        }
        return Collections.unmodifiableList(result);
    }

    private static List<NewbieBuffsList> parseNewbieBuffs(final File newbieBuffsXmlFile) {
        Document newbieBuffsDoc;
        try {
            final DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(false);
            factory.setIgnoringComments(true);
            newbieBuffsDoc = factory.newDocumentBuilder().parse(newbieBuffsXmlFile);
        } catch (Exception e) {
            SupportMagic.LOG.error("Error loading file " + newbieBuffsXmlFile, e);
            return Collections.emptyList();
        }
        try {
            final List<NewbieBuffsList> result = parseNewbieBuffsDocument(newbieBuffsDoc);
            int loadBuffsCnt = 0;
            for (final NewbieBuffsList nbl : result) {
                loadBuffsCnt += nbl.getBuffs().length;
            }
            SupportMagic.LOG.info("SupportMagic: Loaded " + loadBuffsCnt + " newbie buff(s).");
            return result;
        } catch (Exception e) {
            SupportMagic.LOG.error("Error in file " + newbieBuffsXmlFile, e);
            return Collections.emptyList();
        }
    }

    public void getSupportMagic() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        doSupportMagic(npc, player, false);
    }

    public void getSupportServitorMagic() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        doSupportMagic(npc, player, true);
    }

    public void getProtectionBlessing() {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player.getKarma() > 0) {
            return;
        }
        if (player.getLevel() > 39 || player.getClassId().getLevel() >= 3) {
            show("default/newbie_blessing_no.htm", player, npc);
            return;
        }
        npc.doCast(SkillTable.getInstance().getInfo(5182, 1), player, true);
    }

    @Override
    public void onInit() {
        SupportMagic.NEWBIE_BUFFS = parseNewbieBuffs(SupportMagic.NEWBIE_BUFFS_XML_FILE);
    }

    private enum NewbieBuffsListType {
        WARRIOR {
            @Override
            public boolean canUse(final Player player) {
                return !player.isMageClass() || player.getTemplate().race == Race.orc;
            }
        },
        MAGE {
            @Override
            public boolean canUse(final Player player) {
                return player.isMageClass() && player.getTemplate().race != Race.orc;
            }
        };

        public abstract boolean canUse(final Player p0);
    }

    private static class NewbieBuffsList {
        private final NewbieBuffsListType _type;
        private final int _minLevel;
        private final int _maxLevel;
        private final Pair<Integer, Skill>[] _buffs;

        private NewbieBuffsList(final NewbieBuffsListType type, final int minLevel, final int maxLevel, final Pair<Integer, Skill>[] buffs) {
            _type = type;
            _minLevel = minLevel;
            _maxLevel = maxLevel;
            _buffs = buffs;
        }

        public NewbieBuffsListType getType() {
            return _type;
        }

        public int getMinLevel() {
            return _minLevel;
        }

        public int getMaxLevel() {
            return _maxLevel;
        }

        public Pair<Integer, Skill>[] getBuffs() {
            return _buffs;
        }

        public void apply(final Creature buffer, final Creature target) {
            final int lvl = target.getLevel();
            for (final Pair<Integer, Skill> newbieBuffPair : getBuffs()) {
                if (lvl >= newbieBuffPair.getKey()) {
                    final Skill newbieBuffSkill = newbieBuffPair.getValue();
                    buffer.broadcastPacket(new MagicSkillUse(buffer, target, newbieBuffSkill, 0, 0L));
                    buffer.callSkill(newbieBuffSkill, Collections.singletonList(target), true);
                }
            }
        }
    }
}
