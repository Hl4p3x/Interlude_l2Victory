package ru.j2dev.gameserver.skills.skillclasses;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.Manor;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.StatsSet;

import java.util.List;

public class Sowing extends Skill {
    public Sowing(final StatsSet set) {
        super(set);
    }

    @Override
    public void useSkill(final Creature activeChar, final List<Creature> targets) {
        if (!activeChar.isPlayer()) {
            return;
        }
        final Player player = (Player) activeChar;
        final int seedId = player.getUseSeed();
        final boolean altSeed = ItemTemplateHolder.getInstance().getTemplate(seedId).isAltSeed();
        if (!player.getInventory().destroyItemByItemId(seedId, 1L)) {
            activeChar.sendActionFailed();
            return;
        }
        player.sendPacket(SystemMessage2.removeItems(seedId, 1L));
        for (final Creature target : targets) {
            if (target != null) {
                final MonsterInstance monster = (MonsterInstance) target;
                if (monster.isSeeded()) {
                    continue;
                }
                double SuccessRate = Config.MANOR_SOWING_BASIC_SUCCESS;
                final double diffPlayerTarget = Math.abs(activeChar.getLevel() - target.getLevel());
                final double diffSeedTarget = Math.abs(Manor.getInstance().getSeedLevel(seedId) - target.getLevel());
                if (diffPlayerTarget > Config.MANOR_DIFF_PLAYER_TARGET) {
                    SuccessRate -= (diffPlayerTarget - Config.MANOR_DIFF_PLAYER_TARGET) * Config.MANOR_DIFF_PLAYER_TARGET_PENALTY;
                }
                if (diffSeedTarget > Config.MANOR_DIFF_SEED_TARGET) {
                    SuccessRate -= (diffSeedTarget - Config.MANOR_DIFF_SEED_TARGET) * Config.MANOR_DIFF_SEED_TARGET_PENALTY;
                }
                if (altSeed) {
                    SuccessRate *= Config.MANOR_SOWING_ALT_BASIC_SUCCESS / Config.MANOR_SOWING_BASIC_SUCCESS;
                }
                if (SuccessRate < 1.0) {
                    SuccessRate = 1.0;
                }
                if (player.isGM()) {
                    activeChar.sendMessage(new CustomMessage("l2p.gameserver.skills.skillclasses.Sowing.Chance", player).addNumber((long) SuccessRate));
                }
                if (Rnd.chance(SuccessRate) && monster.setSeeded(player, seedId, altSeed)) {
                    activeChar.sendPacket(Msg.THE_SEED_WAS_SUCCESSFULLY_SOWN);
                } else {
                    activeChar.sendPacket(Msg.THE_SEED_WAS_NOT_SOWN);
                }
            }
        }
    }
}
