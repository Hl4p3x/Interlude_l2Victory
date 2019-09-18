package ru.j2dev.gameserver.handler.usercommands;

import gnu.trove.map.hash.TIntObjectHashMap;
import ru.j2dev.commons.data.xml.AbstractHolder;
import ru.j2dev.gameserver.handler.usercommands.impl.*;

import java.util.Arrays;

public class UserCommandHandler extends AbstractHolder {
    private static final UserCommandHandler _instance = new UserCommandHandler();

    private final TIntObjectHashMap<IUserCommandHandler> _handlers = new TIntObjectHashMap<>();

    private UserCommandHandler() {
        registerUserCommandHandler(new ClanWarsList());
        registerUserCommandHandler(new ClanPenalty());
        registerUserCommandHandler(new CommandChannel());
        registerUserCommandHandler(new Escape());
        registerUserCommandHandler(new LocCommand());
        registerUserCommandHandler(new OlympiadStat());
        registerUserCommandHandler(new PartyInfo());
        registerUserCommandHandler(new InstanceZone());
        registerUserCommandHandler(new Time());
    }

    public static UserCommandHandler getInstance() {
        return _instance;
    }

    public void registerUserCommandHandler(final IUserCommandHandler handler) {
        final int[] userCommandList = handler.getUserCommandList();
        Arrays.stream(userCommandList).forEach(element -> _handlers.put(element, handler));
    }

    public IUserCommandHandler getUserCommandHandler(final int userCommand) {
        return _handlers.get(userCommand);
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
