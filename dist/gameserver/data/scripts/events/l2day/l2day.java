package events.l2day;

import ru.j2dev.gameserver.model.reward.RewardData;

import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class l2day extends LettersCollection {
    private static final int BSOE = 3958;
    private static final int BSOR = 3959;
    private static final int GUIDANCE = 3926;
    private static final int WHISPER = 3927;
    private static final int FOCUS = 3928;
    private static final int ACUMEN = 3929;
    private static final int HASTE = 3930;
    private static final int AGILITY = 3931;
    private static final int EMPOWER = 3932;
    private static final int MIGHT = 3933;
    private static final int WINDWALK = 3934;
    private static final int SHIELD = 3935;
    private static final int ENCH_WPN_D = 955;
    private static final int ENCH_WPN_C = 951;
    private static final int ENCH_WPN_B = 947;
    private static final int ENCH_WPN_A = 729;
    private static final int RABBIT_EARS = 8947;
    private static final int FEATHERED_HAT = 8950;
    private static final int FAIRY_ANTENNAE = 8949;
    private static final int ARTISANS_GOOGLES = 8951;
    private static final int LITTLE_ANGEL_WING = 8948;
    private static final int RING_OF_ANT_QUIEEN = 6660;
    private static final int EARRING_OF_ORFEN = 6661;
    private static final int RING_OF_CORE = 6662;
    private static final int FRINTEZZA_NECKLACE = 8191;

    static {
        _name = "l2day";
        _msgStarted = "scripts.events.l2day.AnnounceEventStarted";
        _msgEnded = "scripts.events.l2day.AnnounceEventStoped";
        _words.put("LineageII", new Integer[][]{{L, 1}, {I, 1}, {N, 1}, {E, 2}, {A, 1}, {G, 1}, {II, 1}});
        _rewards.put("LineageII", new RewardData[]{new RewardData(GUIDANCE, 3L, 3L, 85000.0), new RewardData(WHISPER, 3L, 3L, 85000.0), new RewardData(FOCUS, 3L, 3L, 85000.0), new RewardData(ACUMEN, 3L, 3L, 85000.0), new RewardData(HASTE, 3L, 3L, 85000.0), new RewardData(AGILITY, 3L, 3L, 85000.0), new RewardData(EMPOWER, 3L, 3L, 85000.0), new RewardData(MIGHT, 3L, 3L, 85000.0), new RewardData(WINDWALK, 3L, 3L, 85000.0), new RewardData(SHIELD, 3L, 3L, 85000.0), new RewardData(BSOE, 1L, 1L, 50000.0), new RewardData(BSOR, 1L, 1L, 50000.0), new RewardData(ENCH_WPN_C, 3L, 3L, 14000.0), new RewardData(ENCH_WPN_B, 2L, 2L, 7000.0), new RewardData(ENCH_WPN_A, 1L, 1L, 7000.0), new RewardData(RABBIT_EARS, 1L, 1L, 5000.0), new RewardData(FEATHERED_HAT, 1L, 1L, 5000.0), new RewardData(FAIRY_ANTENNAE, 1L, 1L, 5000.0), new RewardData(RING_OF_ANT_QUIEEN, 1L, 1L, 100.0), new RewardData(RING_OF_CORE, 1L, 1L, 100.0)});
        _words.put("Throne", new Integer[][]{{T, 1}, {H, 1}, {R, 1}, {O, 1}, {N, 1}, {E, 1}});
        _rewards.put("Throne", new RewardData[]{new RewardData(GUIDANCE, 3L, 3L, 85000.0), new RewardData(WHISPER, 3L, 3L, 85000.0), new RewardData(FOCUS, 3L, 3L, 85000.0), new RewardData(ACUMEN, 3L, 3L, 85000.0), new RewardData(HASTE, 3L, 3L, 85000.0), new RewardData(AGILITY, 3L, 3L, 85000.0), new RewardData(EMPOWER, 3L, 3L, 85000.0), new RewardData(MIGHT, 3L, 3L, 85000.0), new RewardData(WINDWALK, 3L, 3L, 85000.0), new RewardData(SHIELD, 3L, 3L, 85000.0), new RewardData(BSOE, 1L, 1L, 50000.0), new RewardData(BSOR, 1L, 1L, 50000.0), new RewardData(ENCH_WPN_D, 4L, 4L, 16000.0), new RewardData(ENCH_WPN_C, 3L, 3L, 11000.0), new RewardData(ENCH_WPN_B, 2L, 2L, 6000.0), new RewardData(ARTISANS_GOOGLES, 1L, 1L, 6000.0), new RewardData(LITTLE_ANGEL_WING, 1L, 1L, 5000.0), new RewardData(RING_OF_ANT_QUIEEN, 1L, 1L, 100.0), new RewardData(RING_OF_CORE, 1L, 1L, 100.0)});
        _words.put("NCSoft", new Integer[][]{{N, 1}, {C, 1}, {S, 1}, {O, 1}, {F, 1}, {T, 1}});
        _rewards.put("NCSoft", new RewardData[]{new RewardData(GUIDANCE, 3L, 3L, 85000.0), new RewardData(WHISPER, 3L, 3L, 85000.0), new RewardData(FOCUS, 3L, 3L, 85000.0), new RewardData(ACUMEN, 3L, 3L, 85000.0), new RewardData(HASTE, 3L, 3L, 85000.0), new RewardData(AGILITY, 3L, 3L, 85000.0), new RewardData(EMPOWER, 3L, 3L, 85000.0), new RewardData(MIGHT, 3L, 3L, 85000.0), new RewardData(WINDWALK, 3L, 3L, 85000.0), new RewardData(SHIELD, 3L, 3L, 85000.0), new RewardData(BSOE, 1L, 1L, 50000.0), new RewardData(BSOR, 1L, 1L, 50000.0), new RewardData(ENCH_WPN_D, 4L, 4L, 16000.0), new RewardData(ENCH_WPN_C, 3L, 3L, 11000.0), new RewardData(ENCH_WPN_B, 2L, 2L, 6000.0), new RewardData(ARTISANS_GOOGLES, 1L, 1L, 6000.0), new RewardData(LITTLE_ANGEL_WING, 1L, 1L, 5000.0), new RewardData(RING_OF_ANT_QUIEEN, 1L, 1L, 100.0), new RewardData(RING_OF_CORE, 1L, 1L, 100.0)});
        final int DROP_MULT = 3;
        final Map<Integer, Integer> temp = _words.values().stream().flatMap(Arrays::stream).collect(Collectors.toMap(i -> i[0], i -> i[1], (a, b) -> a + b));
        letters = new int[temp.size()][2];
        int j = 0;
        for (final Entry<Integer, Integer> e : temp.entrySet()) {
            letters[j++] = new int[]{e.getKey(), e.getValue() * DROP_MULT};
        }
    }
}
