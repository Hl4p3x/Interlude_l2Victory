package ru.j2dev.gameserver.handler.voicecommands.impl;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Functions;

public class Services extends Functions implements IVoicedCommandHandler {
    private static final String[] _voicedCommands = {"autoloot", "xpfreez", "ru", "en"};

    @Override
    public boolean useVoicedCommand(String command, final Player activeChar, final String target) {
        command = command.intern();
        if (command.startsWith("autoloot") && target != null && target.length() > 1) {
            autoLoot(activeChar, target.startsWith("on"), target.startsWith("adena"));
            return true;
        }
        if (command.startsWith("xpfreez")) {
            if (target.startsWith("on")) {
                activeChar.setVar("NoExp", "1", -1L);
                activeChar.sendMessage("Exp freezed.");
                return true;
            }
            if (target.startsWith("off")) {
                activeChar.unsetVar("NoExp");
                activeChar.sendMessage("Exp normal.");
                return true;
            }
        } else {
            if (command.startsWith("ru")) {
                activeChar.setVar("lang@", "ru", -1L);
                return true;
            }
            if (command.startsWith("en")) {
                activeChar.setVar("lang@", "en", -1L);
                return true;
            }
        }
        return false;
    }

    public void autoLoot(final Player player, final boolean on, final boolean adena) {
        if (on && !adena) {
            player.setAutoLoot(on);
            if (Config.AUTO_LOOT_HERBS) {
                player.setAutoLootHerbs(on);
            }
            player.sendMessage("Autolooting all.");
        } else if (adena) {
            player.setAutoLoot(false);
            player.setAutoLootAdena(adena);
            player.sendMessage("Autolooting adena only.");
        } else {
            player.setAutoLootAdena(false);
            player.setAutoLoot(false);
            player.setAutoLootHerbs(false);
            player.sendMessage("Autolooting off.");
        }
    }

    @Override
    public String[] getVoicedCommandList() {
        return _voicedCommands;
    }
}
