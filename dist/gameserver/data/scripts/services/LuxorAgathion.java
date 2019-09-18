package services;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.scripts.Functions;

public class LuxorAgathion extends Functions {
    private static final int[][] INGRIDIENTS = {{6471, 25}, {5094, 50}, {9814, 4}, {9816, 5}, {9817, 5}, {9815, 3}, {57, 7500000}};
    private static final int OldAgathion = 10408;
    private static final int ShadowPurpleVikingCirclet = 10315;
    private static final int ShadowGoldenVikingCirclet = 10321;
    private static final int[] ANGEL_BRACELET_IDS = {10320, 10316, 10317, 10318, 10319};
    private static final int[] DEVIL_BRACELET_IDS = {10326, 10322, 10323, 10324, 10325};
    private static final int SUCCESS_RATE = 60;
    private static final int RARE_RATE = 5;

    public void angelAgathion() {
        agathion(LuxorAgathion.ANGEL_BRACELET_IDS, 1);
    }

    public void devilAgathion() {
        agathion(LuxorAgathion.DEVIL_BRACELET_IDS, 2);
    }

    private void agathion(final int[] braceletes, final int type) {
        final Player player = getSelf();
        final NpcInstance npc = getNpc();
        if (player == null || npc == null) {
            return;
        }
        for (final int[] ingridient : LuxorAgathion.INGRIDIENTS) {
            if (getItemCount(player, ingridient[0]) < ingridient[1]) {
                show("merchant/30098-2.htm", player, npc);
                return;
            }
        }
        for (final int[] ingridient : LuxorAgathion.INGRIDIENTS) {
            removeItem(player, ingridient[0], (long) ingridient[1]);
        }
        if (!Rnd.chance(LuxorAgathion.SUCCESS_RATE)) {
            addItem(player, 10408, 1L);
            if (type == 1) {
                addItem(player, 10315, 1L);
            } else {
                addItem(player, 10321, 1L);
            }
            show("merchant/30098-3.htm", player, npc);
            return;
        }
        addItem(player, braceletes[Rnd.chance(LuxorAgathion.RARE_RATE) ? 0 : Rnd.get(1, braceletes.length - 1)], 1L);
        show("merchant/30098-4.htm", player, npc);
    }
}
