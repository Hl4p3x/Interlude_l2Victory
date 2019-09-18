package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class NpcNotSayInstance extends NpcInstance {
    public NpcNotSayInstance(final int objectID, final NpcTemplate template) {
        super(objectID, template);
        setHasChatWindow(false);
    }
}
