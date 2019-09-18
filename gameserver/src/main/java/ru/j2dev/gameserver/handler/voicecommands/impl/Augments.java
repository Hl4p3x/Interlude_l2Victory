package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.gameserver.data.xml.holder.OptionDataHolder;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.stats.triggers.TriggerInfo;
import ru.j2dev.gameserver.templates.OptionDataTemplate;

public class Augments implements IVoicedCommandHandler {
    private final String[] _commandList;

    public Augments() {
        _commandList = new String[]{"aug", "augments"};
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player player, final String args) {
        for (int slot = 0; slot < 17; ++slot) {
            final ItemInstance item = player.getInventory().getPaperdollItem(slot);
            if (item != null) {
                if (item.isAugmented()) {
                    final StringBuilder info = new StringBuilder(30);
                    info.append("<<Detail augments info>>");
                    info.append("\n");
                    info.append(item.getName()).append(" with enchant level ").append(item.getEnchantLevel()).append(" have augment:");
                    info.append("\n");
                    info.append("Option id 1 : ").append(item.getVariationStat1());
                    info.append("\n");
                    info.append("Option id 2 : ").append(item.getVariationStat2());
                    getInfo(info, item.getVariationStat1());
                    getInfo(info, item.getVariationStat2());
                    player.sendMessage(info.toString());
                }
            }
        }
        return true;
    }

    private void getInfo(final StringBuilder info, final int id) {
        final OptionDataTemplate template = OptionDataHolder.getInstance().getTemplate(id);
        if (template != null) {
            if (!template.getSkills().isEmpty()) {
                for (final Skill s : template.getSkills()) {
                    info.append(" ");
                    info.append("\n");
                    info.append("Skill name: ").append(s.getName()).append(" (id: ").append(s.getId()).append(") - level ").append(s.getLevel());
                    info.append("\n");
                }
            }
            if (!template.getTriggerList().isEmpty()) {
                for (final TriggerInfo t : template.getTriggerList()) {
                    info.append("\n");
                    info.append("Chance skill id: ").append(t.id);
                    info.append(" - level ").append(t.level);
                    info.append("\n");
                    info.append("Activation type ").append(t.getType());
                    info.append("\n");
                    info.append("Activation chance : ").append(t.getChance()).append("%");
                }
            }
        }
    }
}
