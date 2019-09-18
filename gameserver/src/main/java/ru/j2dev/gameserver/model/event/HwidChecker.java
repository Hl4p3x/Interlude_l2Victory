package ru.j2dev.gameserver.model.event;


import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


@HideAccess
@StringEncryption
public final class HwidChecker {
    private final boolean checkHWID;
    private List<String> list = Collections.emptyList();

    public HwidChecker(final boolean check) {
        checkHWID = check;
    }

    public final boolean canParticipate(final Player player) {
        // если проверять HardwareID не надо
        if (!checkHWID) {
            return true;
        }

        if (player == null || !player.isOnline()) {
            return false;
        }

        final String hwid = player.getNetConnection().getHwid();
        if (hwid == null) {
            return false;
        }

        // если новый список
        if (list.isEmpty()) {
            list = new CopyOnWriteArrayList<>();
            return list.add(hwid);
        }

        // уже есть такой
        return !list.contains(hwid) && list.add(hwid);

    }

    public final void clear() {
        if (!list.isEmpty()) {
            list.clear();
            list = Collections.emptyList();
        }
    }
}
