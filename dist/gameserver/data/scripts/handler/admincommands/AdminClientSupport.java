package handler.admincommands;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.item.ItemTemplate;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.ItemFunctions;

public class AdminClientSupport extends ScriptAdminCommand {
    private static final Logger LOGGER = LoggerFactory.getLogger(AdminClientSupport.class);

    @Override
    public boolean useAdminCommand(final Enum comm, final String[] wordList, final String fullString, final Player player) {
        final Commands c = (Commands) comm;
        GameObject target = player.getTarget();
        switch (c) {
            case admin_setskill: {
                if (wordList.length != 3) {
                    return false;
                }
                if (!player.getPlayerAccess().CanEditChar) {
                    return false;
                }
                if (target == null || !target.isPlayer()) {
                    return false;
                }
                try {
                    final Skill skill = SkillTable.getInstance().getInfo(Integer.parseInt(wordList[1]), Integer.parseInt(wordList[2]));
                    target.getPlayer().addSkill(skill, true);
                    target.getPlayer().sendPacket(new SystemMessage2(SystemMsg.YOU_HAVE_EARNED_S1_SKILL).addSkillName(skill.getId(), skill.getLevel()));
                    break;
                } catch (NumberFormatException e) {
                    LOGGER.info("AdminClientSupport:useAdminCommand(Enum,String[],String,L2Player): " + e, e);
                    return false;
                }
            }
            case admin_summon: {
                if (wordList.length != 3) {
                    return false;
                }
                if (!player.getPlayerAccess().CanEditChar) {
                    return false;
                }
                try {
                    final int id = Integer.parseInt(wordList[1]);
                    final long count = Long.parseLong(wordList[2]);
                    if (id >= 1000000) {
                        if (target == null) {
                            target = player;
                        }
                        final NpcTemplate template = NpcTemplateHolder.getInstance().getTemplate(id - 1000000);
                        for (int i = 0; i < count; ++i) {
                            final NpcInstance npc = template.getNewInstance();
                            npc.setSpawnedLoc(target.getLoc());
                            npc.setCurrentHpMp((double) npc.getMaxHp(), (double) npc.getMaxMp(), true);
                            npc.spawnMe(npc.getSpawnedLoc());
                        }
                    } else {
                        if (target == null) {
                            target = player;
                        }
                        if (!target.isPlayer()) {
                            return false;
                        }
                        final ItemTemplate template2 = ItemTemplateHolder.getInstance().getTemplate(id);
                        if (template2 == null) {
                            return false;
                        }
                        if (template2.isStackable()) {
                            final ItemInstance item = ItemFunctions.createItem(id);
                            item.setCount(count);
                            target.getPlayer().getInventory().addItem(item);
                            target.getPlayer().sendPacket(SystemMessage2.obtainItems(item));
                        } else {
                            for (int i = 0; i < count; ++i) {
                                final ItemInstance item2 = ItemFunctions.createItem(id);
                                target.getPlayer().getInventory().addItem(item2);
                                target.getPlayer().sendPacket(SystemMessage2.obtainItems(item2));
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    LOGGER.info("AdminClientSupport:useAdminCommand(Enum,String[],String,L2Player): " + e, e);
                    return false;
                }
                break;
            }
        }
        return true;
    }

    @Override
    public Enum[] getAdminCommandEnum() {
        return Commands.values();
    }

    public enum Commands {
        admin_setskill,
        admin_summon
    }
}
