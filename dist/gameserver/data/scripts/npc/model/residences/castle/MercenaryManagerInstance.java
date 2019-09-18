package npc.model.residences.castle;

import ru.j2dev.gameserver.model.instances.MerchantInstance;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class MercenaryManagerInstance extends MerchantInstance {
    public MercenaryManagerInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public boolean canInteractWithKarmaPlayer() {
        return true;
    }
}
