package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.model.Player;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@HideAccess
@StringEncryption
public final class IpChecker {
    private final boolean checkIP;
    private List<String> list = Collections.emptyList();

    public IpChecker(final boolean check) {
        checkIP = check;
    }

    public final boolean canParticipate(final Player player) {
        if (!checkIP) {
            return true;
        }
        if (player == null || !player.isOnline()) {
            return false;
        }

        final String ip = player.getIP();

        if (ip == null) {
            return false;
        }

        if ("?.?.?.?".equals(ip)) {
            return false;
        }

        // если новый список
        if (list.isEmpty()) {
            list = new CopyOnWriteArrayList<>();
            return list.add(ip);
        }

        // уже есть такой
        return !list.contains(ip) && list.add(ip);
    }

    public final void clear() {
        if (!list.isEmpty()) {
            list.clear();
            list = Collections.emptyList();
        }
    }
}

