package handler.items;

import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.manager.MapRegionManager;
import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.ChestInstance;
import ru.j2dev.gameserver.model.instances.MinionInstance;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.RaidBossInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.mapregion.DomainArea;

public class Seed extends ScriptItemHandler {
    private static int[] _itemIds;

    public Seed() {
        _itemIds = new int[Manor.getInstance().getAllSeeds().size()];
        int id = 0;
        for (final Integer s : Manor.getInstance().getAllSeeds().keySet()) {
            _itemIds[id++] = s;
        }
    }

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (playable == null || !playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        if (playable.getTarget() == null) {
            player.sendActionFailed();
            return false;
        }
        if (!player.getTarget().isMonster() || player.getTarget() instanceof RaidBossInstance || (player.getTarget() instanceof MinionInstance && ((MinionInstance) player.getTarget()).getLeader() instanceof RaidBossInstance) || player.getTarget() instanceof ChestInstance || (((MonsterInstance) playable.getTarget()).getChampion() > 0 && !item.isAltSeed())) {
            player.sendPacket(SystemMsg.THE_TARGET_IS_UNAVAILABLE_FOR_SEEDING);
            return false;
        }
        final MonsterInstance target = (MonsterInstance) playable.getTarget();
        if (target == null) {
            player.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        if (target.isDead()) {
            player.sendPacket(Msg.INVALID_TARGET);
            return false;
        }
        if (target.isSeeded()) {
            player.sendPacket(SystemMsg.THE_SEED_HAS_BEEN_SOWN);
            return false;
        }
        final int seedId = item.getItemId();
        if (seedId == 0 || player.getInventory().getItemByItemId(item.getItemId()) == null) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return false;
        }
        final DomainArea domain = MapRegionManager.getInstance().getRegionData(DomainArea.class, player);
        final int castleId = (domain == null) ? 0 : domain.getId();
        if (Manor.getInstance().getCastleIdForSeed(seedId) != castleId) {
            player.sendPacket(SystemMsg.THIS_SEED_MAY_NOT_BE_SOWN_HERE);
            return false;
        }
        final Skill skill = SkillTable.getInstance().getInfo(2097, 1);
        if (skill == null) {
            player.sendActionFailed();
            return false;
        }
        if (skill.checkCondition(player, target, false, false, true)) {
            player.setUseSeed(seedId);
            player.getAI().Cast(skill, target);
        }
        return true;
    }

    @Override
    public final int[] getItemIds() {
        return _itemIds;
    }
}
