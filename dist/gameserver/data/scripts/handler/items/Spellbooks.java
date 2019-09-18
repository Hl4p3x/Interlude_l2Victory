package handler.items;

import gnu.trove.set.hash.TIntHashSet;
import ru.j2dev.gameserver.data.xml.holder.SkillAcquireHolder;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.SkillLearn;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.MagicSkillUse;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.SkillTable;

import java.util.List;

public class Spellbooks extends ScriptItemHandler {
    private int[] _itemIds;

    public Spellbooks() {
        final TIntHashSet list = new TIntHashSet();
        final List<SkillLearn> l = SkillAcquireHolder.getInstance().getAllNormalSkillTreeWithForgottenScrolls();
        for (final SkillLearn learn : l) {
            list.add(learn.getItemId());
        }
        _itemIds = list.toArray();
    }

    @Override
    public boolean useItem(final Playable playable, final ItemInstance item, final boolean ctrl) {
        if (!playable.isPlayer()) {
            return false;
        }
        final Player player = (Player) playable;
        if (item.getCount() < 1L) {
            player.sendPacket(SystemMsg.INCORRECT_ITEM_COUNT);
            return false;
        }
        final List<SkillLearn> list = SkillAcquireHolder.getInstance().getSkillLearnListByItemId(player, item.getItemId());
        if (list.isEmpty()) {
            player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
            return false;
        }
        boolean alreadyHas = true;
        for (final SkillLearn learn : list) {
            if (player.getSkillLevel(learn.getId()) != learn.getLevel()) {
                alreadyHas = false;
                break;
            }
        }
        if (alreadyHas) {
            player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
            return false;
        }
        boolean wrongLvl = false;
        for (final SkillLearn learn2 : list) {
            if (player.getLevel() < learn2.getMinLevel()) {
                wrongLvl = true;
            }
        }
        if (wrongLvl) {
            player.sendPacket(new SystemMessage2(SystemMsg.S1_CANNOT_BE_USED_DUE_TO_UNSUITABLE_TERMS).addItemName(item.getItemId()));
            return false;
        }
        if (!player.consumeItem(item.getItemId(), 1L)) {
            return false;
        }
        for (final SkillLearn skillLearn : list) {
            final Skill skill = SkillTable.getInstance().getInfo(skillLearn.getId(), skillLearn.getLevel());
            if (skill == null) {
                continue;
            }
            player.sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));
            player.addSkill(skill, true);
        }
        player.updateStats();
        player.sendSkillList();
        player.broadcastPacket(new MagicSkillUse(player, player, 2790, 1, 1, 0L));
        return true;
    }

    @Override
    public int[] getItemIds() {
        return _itemIds;
    }
}
