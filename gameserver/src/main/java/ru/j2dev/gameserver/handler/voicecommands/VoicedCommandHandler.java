package ru.j2dev.gameserver.handler.voicecommands;

import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.handler.voicecommands.impl.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class VoicedCommandHandler extends AbstractHolder {
    private static final VoicedCommandHandler _instance = new VoicedCommandHandler();

    private final Map<String, IVoicedCommandHandler> _handlers = new HashMap<>();

    private VoicedCommandHandler() {
        registerVoicedCommandHandler(new Offline());
        registerVoicedCommandHandler(new Online());
        registerVoicedCommandHandler(new ServerInfo());
        registerVoicedCommandHandler(new Wedding());
        registerVoicedCommandHandler(new Services());
        registerVoicedCommandHandler(new WhoAmI());
        registerVoicedCommandHandler(new Help());
        registerVoicedCommandHandler(new InstanceZone());
        registerVoicedCommandHandler(new Relog());
        if (Config.ALT_ALLOW_MENU_COMMAND) {
            registerVoicedCommandHandler(new Cfg());
        }
        registerVoicedCommandHandler(new CWHPrivileges());
        registerVoicedCommandHandler(new Augments());
        registerVoicedCommandHandler(new Relocate());
        registerVoicedCommandHandler(new ItemRemaining());
        registerVoicedCommandHandler(new Banking());
    }

    public static VoicedCommandHandler getInstance() {
        return VoicedCommandHandler._instance;
    }

    public void registerVoicedCommandHandler(final IVoicedCommandHandler handler) {
        final String[] voicedCommandList = handler.getVoicedCommandList();
        Arrays.stream(voicedCommandList).forEach(element -> _handlers.put(element, handler));
    }

    public IVoicedCommandHandler getVoicedCommandHandler(final String voicedCommand) {
        String command = voicedCommand;
        if (voicedCommand.contains(" ")) {
            command = voicedCommand.substring(0, voicedCommand.indexOf(" "));
        }
        return _handlers.get(command);
    }

    @Override
    public int size() {
        return _handlers.size();
    }

    @Override
    public void clear() {
        _handlers.clear();
    }
}