package ru.j2dev.gameserver.model.items.listeners;

import ru.j2dev.gameserver.listener.inventory.OnEquipListener;
import ru.j2dev.gameserver.model.Playable;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SkillCoolTime;
import ru.j2dev.gameserver.stats.Formulas;
import ru.j2dev.gameserver.tables.SkillTable;
import ru.j2dev.gameserver.templates.item.ItemTemplate;

public final class ItemSkillsListener implements OnEquipListener {
    private static final ItemSkillsListener _instance = new ItemSkillsListener();

    public static ItemSkillsListener getInstance() {
        return _instance;
    }

    @Override
    public void onUnequip(final int slot, final ItemInstance item, final Playable actor) {
        final Player player = (Player) actor;
        Skill[] itemSkills;
        Skill enchant4Skill;
        final ItemTemplate it = item.getTemplate();
        itemSkills = it.getAttachedSkills();
        enchant4Skill = it.getEnchant4Skill();
        player.removeTriggers(it);

        boolean ok = checkRingAndJewel(slot, player, it);
        if (itemSkills != null && itemSkills.length > 0) {
            for (final Skill itemSkill : itemSkills) {
                if(actor.isCastingNow() && actor.getCastingSkill() == itemSkill) {
                    actor.abortCast(true, true);
                }
                if (itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048) {
                    final int level = player.getSkillLevel(itemSkill.getId());
                    final int newlevel = level - 1;
                    if (newlevel > 0) {
                        player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
                    } else {
                        player.removeSkillById(itemSkill.getId());
                    }
                } else if (ok) {
                    player.removeSkill(itemSkill, false);
                }
            }
        }
        if (enchant4Skill != null) {
            player.removeSkill(enchant4Skill, false);
        }
        if (itemSkills != null && itemSkills.length > 0 || enchant4Skill != null) {
            player.sendSkillList();
            player.updateStats();
        }
    }

    private boolean checkRingAndJewel(int slot, Player player, ItemTemplate it) {
        boolean ok = true;
        if (slot == 2 && player.getInventory().getPaperdollItem(1) != null) {
            if (player.getInventory().getPaperdollItem(1).getItemId() == it.getItemId()) {
                ok = false;
            }
        }

        if (slot == 1 && player.getInventory().getPaperdollItem(2) != null) {
            if (player.getInventory().getPaperdollItem(2).getItemId() == it.getItemId()) {
                ok = false;
            }
        }

        if (slot == 5 && player.getInventory().getPaperdollItem(4) != null) {
            if (player.getInventory().getPaperdollItem(4).getItemId() == it.getItemId()) {
                ok = false;
            }
        }

        if (slot == 4 && player.getInventory().getPaperdollItem(5) != null) {
            if (player.getInventory().getPaperdollItem(5).getItemId() == it.getItemId()) {
                ok = false;
            }
        }
        return ok;
    }

    @Override
    public void onEquip(final int slot, final ItemInstance item, final Playable actor) {
        final Player player = (Player) actor;
        Skill[] itemSkills;
        Skill enchant4Skill = null;
        final ItemTemplate it = item.getTemplate();
        itemSkills = it.getAttachedSkills();
        if (item.getEnchantLevel() >= 4) {
            enchant4Skill = it.getEnchant4Skill();
        }
        if (it.getType2() == 0 && player.getGradePenalty() > 0) {
            return;
        }
        player.addTriggers(it);
        boolean needSendInfo = false;
        if (itemSkills.length > 0) {
            for (final Skill itemSkill : itemSkills) {
                if (itemSkill.getId() >= 26046 && itemSkill.getId() <= 26048) {
                    final int level = player.getSkillLevel(itemSkill.getId());
                    int newlevel;
                    if ((newlevel = level) > 0) {
                        if (SkillTable.getInstance().getInfo(itemSkill.getId(), level + 1) != null) {
                            newlevel = level + 1;
                        }
                    } else {
                        newlevel = 1;
                    }
                    if (newlevel != level) {
                        player.addSkill(SkillTable.getInstance().getInfo(itemSkill.getId(), newlevel), false);
                    }
                } else if (player.getSkillLevel(itemSkill.getId()) < itemSkill.getLevel()) {
                    player.addSkill(itemSkill, false);
                    if (itemSkill.isActive()) {
                        long reuseDelay = Formulas.calcSkillReuseDelay(player, itemSkill);
                        reuseDelay = Math.min(reuseDelay, 30000L);
                        if (reuseDelay > 0L && !player.isSkillDisabled(itemSkill)) {
                            player.disableSkill(itemSkill, reuseDelay);
                            needSendInfo = true;
                        }
                    }
                }
            }
        }
        if (enchant4Skill != null) {
            player.addSkill(enchant4Skill, false);
        }
        if (itemSkills.length > 0 || enchant4Skill != null) {
            player.sendSkillList();
            player.updateStats();
            if (needSendInfo) {
                player.sendPacket(new SkillCoolTime(player));
            }
        }
    }
}
