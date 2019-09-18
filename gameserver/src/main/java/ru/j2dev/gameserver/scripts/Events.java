package ru.j2dev.gameserver.scripts;

import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.scripts.Scripts.ScriptClassAndMethod;
import ru.j2dev.gameserver.utils.Strings;

public final class Events {

    public static boolean onAction(final Player player, final GameObject obj, final boolean shift) {
        if (!shift) {
            ScriptClassAndMethod handler = Scripts.onAction.get(obj.getL2ClassShortName());
            if (handler == null && obj.isDoor()) {
                handler = Scripts.onAction.get("DoorInstance");
            }
            return handler != null && Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[]{player, obj}));
        }
        if (player.getVarB("noShift")) {
            return false;
        }
        ScriptClassAndMethod handler = Scripts.onActionShift.get(obj.getL2ClassShortName());
        if (handler == null && obj.isNpc()) {
            handler = Scripts.onActionShift.get("NpcInstance");
        }
        if (handler == null && obj.isPet()) {
            handler = Scripts.onActionShift.get("PetInstance");
        }
        return handler != null && Strings.parseBoolean(Scripts.getInstance().callScripts(player, handler.className, handler.methodName, new Object[]{player, obj}));
    }
}
