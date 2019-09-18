package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public final class TrainerInstance extends NpcInstance {
    public TrainerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        if(getTemplate().getHtmRoot() != null) {
            return getTemplate().getHtmRoot()+ pom +".htm";
        }
        return "trainer/" + pom + ".htm";
    }
}
