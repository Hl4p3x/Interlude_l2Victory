package ru.j2dev.gameserver.data.xml.holder;

import gnu.trove.map.TIntObjectMap;
import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.model.reward.RewardGroup;
import ru.j2dev.gameserver.model.reward.RewardList;
import ru.j2dev.gameserver.model.reward.RewardType;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.*;

public final class NpcTemplateHolder extends AbstractHolder {
    private final TIntObjectMap<NpcTemplate> _npcs = new TIntObjectHashMap<>();
    private Map<Integer, List<NpcTemplate>> _npcsByLevel;
    private NpcTemplate[] _allTemplates;
    private Map<String, NpcTemplate> _npcsNames;

    public static NpcTemplateHolder getInstance() {
        return LazyHolder.INSTANCE;
    }

    public void addTemplate(final NpcTemplate template) {
        if (_npcs.containsKey(template.npcId)) {
            warn("NPC redefined: " + template.npcId);
        }
        double lvlMod = LevelBonusHolder.getInstance().getLevelBonus(template.level);
        double conMod = BaseStatsBonusHolder.getInstance().getBaseStatsBonus(template.getBaseCON()).getCON();
        double menMod = BaseStatsBonusHolder.getInstance().getBaseStatsBonus(template.getBaseMEN()).getMEN();
        double strMod = BaseStatsBonusHolder.getInstance().getBaseStatsBonus(template.getBaseSTR()).getSTR();
        double intMod = BaseStatsBonusHolder.getInstance().getBaseStatsBonus(template.getBaseINT()).getINT();

        template.setBaseHpMax(template.getBaseHpMax() * conMod);
        template.setBaseMpMax(template.getBaseMpMax() * menMod);
        template.setBasePAtk((int) (template.getBasePAtk() * strMod * lvlMod));
        template.setBaseMAtk((int) (template.getBaseMAtk() * intMod * intMod * lvlMod * lvlMod));
        template.setBasePDef((int) (template.getBasePDef() * lvlMod));
        template.setBaseMDef((int) (template.getBaseMDef() * menMod * lvlMod));

        _npcs.put(template.npcId, template);
    }

    public NpcTemplate getTemplate(final int id) {
        final NpcTemplate npc = ArrayUtils.valid(_allTemplates, id);
        if (npc == null) {
            warn("Not defined npc id : " + id + ", or out of range!", new Exception());
            return null;
        }
        return _allTemplates[id];
    }

    public NpcTemplate getTemplateByName(final String name) {
        return _npcsNames.get(name.toLowerCase());
    }

    public List<NpcTemplate> getAllOfLevel(final int lvl) {
        return _npcsByLevel.get(lvl);
    }

    public Collection<NpcTemplate> getAll() {
        return _npcs.valueCollection();
    }

    private void buildFastLookupTable() {
        _npcsByLevel = new HashMap<>();
        _npcsNames = new HashMap<>();

        int highestId = 0;
        for (final int id : _npcs.keys()) {
            if (id > highestId) {
                highestId = id;
            }
        }

        _allTemplates = new NpcTemplate[highestId + 1];
        for (final NpcTemplate npcTemplate : _npcs.valueCollection()) {
            final int npcId = npcTemplate.getNpcId();

            _allTemplates[npcId] = npcTemplate;

            List<NpcTemplate> byLevel;
            if ((byLevel = _npcsByLevel.get(npcTemplate.level)) == null) {
                _npcsByLevel.put(npcId, byLevel = new ArrayList<>());
            }
            byLevel.add(npcTemplate);

            _npcsNames.put(npcTemplate.getName().toLowerCase(), npcTemplate);
        }
    }

    @Override
    protected void process() {
        buildFastLookupTable();
    }

    public void addEventDrop(final RewardList eventDrop) {
        for (final NpcTemplate npc : _allTemplates) {
            if (npc != null && !npc.getRewards().isEmpty()) {
                loop:
                for (final RewardList rl : npc.getRewards().values()) {
                    for (final RewardGroup rg : rl) {
                        if (!rg.isAdena()) {
                            npc.getRewards().put(RewardType.EVENT, eventDrop);
                            break loop;
                        }
                    }
                }
            }
        }
    }

    public void removeEventDrop() {
        for (final NpcTemplate npc : _allTemplates) {
            if (npc != null && !npc.getRewards().isEmpty()) {
                npc.getRewards().remove(RewardType.EVENT);
            }
        }
    }

    @Override
    public int size() {
        return _npcs.size();
    }

    @Override
    public void clear() {
        _npcsNames.clear();
        _npcs.clear();
    }

    private static class LazyHolder {
        private static final NpcTemplateHolder INSTANCE = new NpcTemplateHolder();
    }
}
