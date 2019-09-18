package handler.admincommands;

import ru.j2dev.gameserver.handler.admincommands.AdminCommandHandler;
import ru.j2dev.gameserver.handler.admincommands.IAdminCommandHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;

public abstract class ScriptAdminCommand implements IAdminCommandHandler, OnInitScriptListener {
    @Override
    public void onInit() {
        AdminCommandHandler.getInstance().registerAdminCommandHandler(this);
    }

}
