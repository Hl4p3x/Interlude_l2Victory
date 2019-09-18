package handler.usercommands;

import ru.j2dev.gameserver.handler.usercommands.IUserCommandHandler;
import ru.j2dev.gameserver.handler.usercommands.UserCommandHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;

public abstract class ScriptUserCommand implements IUserCommandHandler, OnInitScriptListener {
    @Override
    public void onInit() {
        UserCommandHandler.getInstance().registerUserCommandHandler(this);
    }

}
