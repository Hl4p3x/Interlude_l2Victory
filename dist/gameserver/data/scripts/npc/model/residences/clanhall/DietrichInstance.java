package npc.model.residences.clanhall;

import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class DietrichInstance extends _34BossMinionInstance {
    public DietrichInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String spawnChatSay() {
        return "Soldiers of Gustav, go forth and destroy the invaders!";
    }

    @Override
    public String teleChatSay() {
        return "Ah, the bitter taste of defeat... I fear my torments are not over...";
    }
}
