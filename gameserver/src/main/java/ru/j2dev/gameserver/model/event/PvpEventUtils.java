package ru.j2dev.gameserver.model.event;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;

import java.util.Arrays;
import java.util.Comparator;

@HideAccess
@StringEncryption
public class PvpEventUtils {
    static final Comparator<PvpEventPlayerInfo> SCORE_COMPARATOR = (o1, o2) -> {
        if (o1 == null || o2 == null) {
            return 0;
        }
        return o2.getKillsCount() - o1.getKillsCount();
    };

    public static PvpEventPlayerInfo[] sortAndTrimPlayerInfos(final PvpEventPlayerInfo[] initial) {
        return sortAndTrimPlayerInfos(initial, 9);
    }

    static PvpEventPlayerInfo[] sortAndTrimPlayerInfos(final PvpEventPlayerInfo[] initial, final int limit) {
        // this is just classic insert sort
        // If u can find better sort for max 20-30 units, rewrite this... :)
        int max, index = 0;
        PvpEventPlayerInfo pom;
        for (int i = 0; i < initial.length; i++) {
            max = initial[i].getKillsCount();
            for (int j = i; j < initial.length; j++) {
                if (initial[j].getKillsCount() >= max) {
                    max = initial[j].getKillsCount();
                    index = j;

                }
            }
            pom = initial[i];
            initial[i] = initial[index];
            initial[index] = pom;
        }

        return Arrays.copyOfRange(initial, 0, Math.min(initial.length, limit));
    }
}
