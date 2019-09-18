package ru.j2dev.gameserver.handler.admincommands;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.handler.admincommands.impl.*;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.utils.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

public class AdminCommandHandler extends AbstractHolder {
    private static final AdminCommandHandler _instance = new AdminCommandHandler();

    private final Map<String, IAdminCommandHandler> _handlers = new HashMap<>();

    private AdminCommandHandler() {
        registerAdminCommandHandler(new AdminAdmin());
        registerAdminCommandHandler(new AdminAnnouncements());
        registerAdminCommandHandler(new AdminBan());
        registerAdminCommandHandler(new AdminCamera());
        registerAdminCommandHandler(new AdminCancel());
        registerAdminCommandHandler(new AdminChangeAccessLevel());
        registerAdminCommandHandler(new AdminClanHall());
        registerAdminCommandHandler(new AdminCreateItem());
        registerAdminCommandHandler(new AdminCursedWeapons());
        registerAdminCommandHandler(new AdminDelete());
        registerAdminCommandHandler(new AdminDisconnect());
        registerAdminCommandHandler(new AdminDoorControl());
        registerAdminCommandHandler(new AdminEditChar());
        registerAdminCommandHandler(new AdminEffects());
        registerAdminCommandHandler(new AdminEnchant());
        registerAdminCommandHandler(new AdminEvents());
        registerAdminCommandHandler(new AdminGeodata());
        registerAdminCommandHandler(new AdminGm());
        registerAdminCommandHandler(new AdminGmChat());
        registerAdminCommandHandler(new AdminHeal());
        registerAdminCommandHandler(new AdminHelpPage());
        registerAdminCommandHandler(new AdminInstance());
        registerAdminCommandHandler(new AdminIP());
        registerAdminCommandHandler(new AdminLevel());
        registerAdminCommandHandler(new AdminMammon());
        registerAdminCommandHandler(new AdminManor());
        registerAdminCommandHandler(new AdminMenu());
        registerAdminCommandHandler(new AdminMonsterRace());
        registerAdminCommandHandler(new AdminMove());
        registerAdminCommandHandler(new AdminNochannel());
        registerAdminCommandHandler(new AdminOlympiad());
        registerAdminCommandHandler(new AdminPetition());
        registerAdminCommandHandler(new AdminPledge());
        registerAdminCommandHandler(new AdminPolymorph());
        registerAdminCommandHandler(new AdminQuests());
        registerAdminCommandHandler(new AdminReload());
        registerAdminCommandHandler(new AdminRepairChar());
        registerAdminCommandHandler(new AdminRes());
        registerAdminCommandHandler(new AdminRide());
        registerAdminCommandHandler(new AdminServer());
        registerAdminCommandHandler(new AdminShop());
        registerAdminCommandHandler(new AdminShutdown());
        registerAdminCommandHandler(new AdminSkill());
        registerAdminCommandHandler(new AdminScripts());
        registerAdminCommandHandler(new AdminSpawn());
        registerAdminCommandHandler(new AdminSS());
        registerAdminCommandHandler(new AdminTarget());
        registerAdminCommandHandler(new AdminTeleport());
        registerAdminCommandHandler(new AdminZone());
        registerAdminCommandHandler(new AdminKill());
        registerAdminCommandHandler(new AdminTest());
    }

    public static AdminCommandHandler getInstance() {
        return _instance;
    }

    public void registerAdminCommandHandler(final IAdminCommandHandler handler) {
        Stream.of(handler.getAdminCommandEnum()).forEach(e -> _handlers.put(e.toString().toLowerCase(), handler));
    }

    public IAdminCommandHandler getAdminCommandHandler(final String adminCommand) {
        String command = adminCommand;
        if (adminCommand.contains(" ")) {
            command = adminCommand.substring(0, adminCommand.indexOf(" "));
        }
        return _handlers.get(command);
    }

    public void useAdminCommandHandler(final Player activeChar, final String adminCommand) {
        if (!activeChar.isGM() && !activeChar.getPlayerAccess().CanUseGMCommand) {
            activeChar.sendMessage(new CustomMessage("l2p.gameserver.clientpackets.SendBypassBuildCmd.NoCommandOrAccess", activeChar).addString(adminCommand));
            return;
        }
        final String[] wordList = adminCommand.split(" ");
        final IAdminCommandHandler handler = _handlers.get(wordList[0]);
        if (handler != null) {
            boolean success = false;
            try {
                success = Stream.of(handler.getAdminCommandEnum()).filter(e -> e.toString().equalsIgnoreCase(wordList[0])).findFirst().filter(e -> handler.useAdminCommand(e, wordList, adminCommand, activeChar)).isPresent();
            } catch (Exception e2) {
                error("", e2);
            }
            Log.LogCommand(activeChar, activeChar.getTarget(), adminCommand, success);
        }
    }

    @Override
    public void process() {
    }

    @Override
    public int size() {
        return _handlers.size();
    }

    @Override
    public void clear() {
        _handlers.clear();
    }

    public Set<String> getAllCommands() {
        return _handlers.keySet();
    }
}
