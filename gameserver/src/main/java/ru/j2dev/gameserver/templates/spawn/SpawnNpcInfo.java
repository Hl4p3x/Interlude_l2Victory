package ru.j2dev.gameserver.templates.spawn;

import ru.j2dev.commons.collections.MultiValueSet;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.Location;

public class SpawnNpcInfo {
    private final int _npcId;
    private final NpcTemplate _template;
    private final int _max;
    private final MultiValueSet<String> _parameters;
    private Location _spawnLoc;

    public SpawnNpcInfo(final int npcId, final int max, final MultiValueSet<String> set) {
        _npcId = npcId;
        _template = NpcTemplateHolder.getInstance().getTemplate(npcId);
        _max = max;
        _parameters = set;
    }

    public int getNpcId() {
        return _npcId;
    }

    public Location getSpawnLoc() {
        return _spawnLoc;
    }

    public NpcTemplate getTemplate() {
        return _template;
    }

    public int getMax() {
        return _max;
    }

    public MultiValueSet<String> getParameters() {
        return _parameters;
    }
}
