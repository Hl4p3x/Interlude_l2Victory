package npc.model;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public final class GvGBossInstance extends MonsterInstance {
    public GvGBossInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void showChatWindow(final Player player, final int val, final Object... arg) {
    }

    @Override
    public void showChatWindow(final Player player, final String filename, final Object... replace) {
    }

    @Override
    public boolean canChampion() {
        return false;
    }

    @Override
    public boolean isFearImmune() {
        return true;
    }

    @Override
    public boolean isParalyzeImmune() {
        return true;
    }

    @Override
    public boolean isLethalImmune() {
        return true;
    }
}
