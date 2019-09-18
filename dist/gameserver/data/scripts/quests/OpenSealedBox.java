package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.QuestState;

import java.util.HashMap;
import java.util.Map;

public class OpenSealedBox {
    public static final int[] counts = {1, 5, 10};
    private static final RewardGroup[] rewardgroups = {new RewardAdena(), new RewardRes1(), new RewardRes2(), new RewardEnchants(), new RewardParts()};

    private final QuestState st;
    private final Map<Integer, Long> rewards;
    private String result;
    private int takecount;

    public OpenSealedBox(final QuestState st, final int count) {
        result = "";
        takecount = 0;
        rewards = new HashMap<>();
        this.st = st;
        if (count < 1) {
            return;
        }
        takecount = count;
        if (st.getQuestItemsCount(7255) < count) {
            result = ((count == 1) ? "I don't see a box... Come back when you find one!" : "I don't see enougth boxes... Come back when you find enougth!");
            return;
        }
        int not_disintegrated = 0;
        for (int i = 0; i < count; ++i) {
            not_disintegrated += Rnd.get(2);
        }
        if (not_disintegrated == 0) {
            result = ((count == 1) ? "I'm so sorry! The box just disintegrated!" : "I'm so sorry! The boxes just disintegrated!");
            return;
        }
        for (int i = 0; i < not_disintegrated; ++i) {
            rewardgroups[Rnd.get(rewardgroups.length)].apply(rewards);
        }
        if (rewards.size() == 0) {
            result = ((count == 1) ? "Hmm. The box is empty." : "Hmm. All boxes is empty.");
            return;
        }
        result = "Wow! Something came out of it!";
    }

    public String apply() {
        if (takecount > 0) {
            if (rewards.size() > 0 && !canGiveReward()) {
                return "You haven't enougth free slots in your inventory.";
            }
            st.takeItems(7255, (long) takecount);
            rewards.keySet().forEach(itemId -> st.giveItems(itemId, rewards.get(itemId), false));
        }
        rewards.clear();
        return result;
    }

    private boolean canGiveReward() {
        int FreeInvSlots = st.getPlayer().getInventoryLimit() - st.getPlayer().getInventory().getSize();
        for (final Integer itemId : rewards.keySet()) {
            final ItemInstance item = st.getPlayer().getInventory().getItemByItemId(itemId);
            if (item == null || !item.isStackable()) {
                --FreeInvSlots;
            }
        }
        return FreeInvSlots > 0;
    }

    public abstract static class RewardGroup {
        protected static void putReward(final Map<Integer, Long> rewards, final int item_id, long count) {
            if (rewards.containsKey(item_id)) {
                count += rewards.remove(item_id);
            }
            rewards.put(item_id, count);
        }

        public abstract void apply(final Map<Integer, Long> p0);
    }

    public static class RewardAdena extends RewardGroup {
        @Override
        public void apply(final Map<Integer, Long> rewards) {
            RewardGroup.putReward(rewards, 57, 10000L);
        }
    }

    public static class RewardRes1 extends RewardGroup {
        @Override
        public void apply(final Map<Integer, Long> rewards) {
            if (Rnd.chance(84.8)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 43) {
                    RewardGroup.putReward(rewards, 1884, 42L);
                } else if (i1 < 66) {
                    RewardGroup.putReward(rewards, 1895, 36L);
                } else if (i1 < 184) {
                    RewardGroup.putReward(rewards, 1876, 4L);
                } else if (i1 < 250) {
                    RewardGroup.putReward(rewards, 1881, 6L);
                } else if (i1 < 287) {
                    RewardGroup.putReward(rewards, 5549, 8L);
                } else if (i1 < 484) {
                    RewardGroup.putReward(rewards, 1874, 1L);
                } else if (i1 < 681) {
                    RewardGroup.putReward(rewards, 1889, 1L);
                } else if (i1 < 799) {
                    RewardGroup.putReward(rewards, 1877, 1L);
                } else if (i1 < 902) {
                    RewardGroup.putReward(rewards, 1894, 1L);
                } else {
                    RewardGroup.putReward(rewards, 4043, 1L);
                }
            }
            if (Rnd.chance(32.3)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 335) {
                    RewardGroup.putReward(rewards, 1888, 1L);
                } else if (i1 < 556) {
                    RewardGroup.putReward(rewards, 4040, 1L);
                } else if (i1 < 725) {
                    RewardGroup.putReward(rewards, 1890, 1L);
                } else if (i1 < 872) {
                    RewardGroup.putReward(rewards, 5550, 1L);
                } else if (i1 < 962) {
                    RewardGroup.putReward(rewards, 1893, 1L);
                } else if (i1 < 986) {
                    RewardGroup.putReward(rewards, 4046, 1L);
                } else {
                    RewardGroup.putReward(rewards, 4048, 1L);
                }
            }
        }
    }

    public static class RewardRes2 extends RewardGroup {
        @Override
        public void apply(final Map<Integer, Long> rewards) {
            if (Rnd.chance(84.7)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 148) {
                    RewardGroup.putReward(rewards, 1878, 8L);
                } else if (i1 < 175) {
                    RewardGroup.putReward(rewards, 1882, 24L);
                } else if (i1 < 273) {
                    RewardGroup.putReward(rewards, 1879, 4L);
                } else if (i1 < 322) {
                    RewardGroup.putReward(rewards, 1880, 6L);
                } else if (i1 < 357) {
                    RewardGroup.putReward(rewards, 1885, 6L);
                } else if (i1 < 554) {
                    RewardGroup.putReward(rewards, 1875, 1L);
                } else if (i1 < 685) {
                    RewardGroup.putReward(rewards, 1883, 1L);
                } else if (i1 < 803) {
                    RewardGroup.putReward(rewards, 5220, 1L);
                } else if (i1 < 901) {
                    RewardGroup.putReward(rewards, 4039, 1L);
                } else {
                    RewardGroup.putReward(rewards, 4044, 1L);
                }
            }
            if (Rnd.chance(25.1)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 350) {
                    RewardGroup.putReward(rewards, 1887, 1L);
                } else if (i1 < 587) {
                    RewardGroup.putReward(rewards, 4042, 1L);
                } else if (i1 < 798) {
                    RewardGroup.putReward(rewards, 1886, 1L);
                } else if (i1 < 922) {
                    RewardGroup.putReward(rewards, 4041, 1L);
                } else if (i1 < 966) {
                    RewardGroup.putReward(rewards, 1892, 1L);
                } else if (i1 < 996) {
                    RewardGroup.putReward(rewards, 1891, 1L);
                } else {
                    RewardGroup.putReward(rewards, 4047, 1L);
                }
            }
        }
    }

    public static class RewardEnchants extends RewardGroup {
        @Override
        public void apply(final Map<Integer, Long> rewards) {
            if (Rnd.chance(3.1)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 223) {
                    RewardGroup.putReward(rewards, 730, 1L);
                } else if (i1 < 893) {
                    RewardGroup.putReward(rewards, 948, 1L);
                } else {
                    RewardGroup.putReward(rewards, 960, 1L);
                }
            }
            if (Rnd.chance(0.5)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 202) {
                    RewardGroup.putReward(rewards, 729, 1L);
                } else if (i1 < 928) {
                    RewardGroup.putReward(rewards, 947, 1L);
                } else {
                    RewardGroup.putReward(rewards, 959, 1L);
                }
            }
        }
    }

    public static class RewardParts extends RewardGroup {
        @Override
        public void apply(final Map<Integer, Long> rewards) {
            if (Rnd.chance(32.9)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 88) {
                    RewardGroup.putReward(rewards, 6698, 1L);
                } else if (i1 < 185) {
                    RewardGroup.putReward(rewards, 6699, 1L);
                } else if (i1 < 238) {
                    RewardGroup.putReward(rewards, 6700, 1L);
                } else if (i1 < 262) {
                    RewardGroup.putReward(rewards, 6701, 1L);
                } else if (i1 < 292) {
                    RewardGroup.putReward(rewards, 6702, 1L);
                } else if (i1 < 356) {
                    RewardGroup.putReward(rewards, 6703, 1L);
                } else if (i1 < 420) {
                    RewardGroup.putReward(rewards, 6704, 1L);
                } else if (i1 < 482) {
                    RewardGroup.putReward(rewards, 6705, 1L);
                } else if (i1 < 554) {
                    RewardGroup.putReward(rewards, 6706, 1L);
                } else if (i1 < 576) {
                    RewardGroup.putReward(rewards, 6707, 1L);
                } else if (i1 < 640) {
                    RewardGroup.putReward(rewards, 6708, 1L);
                } else if (i1 < 704) {
                    RewardGroup.putReward(rewards, 6709, 1L);
                } else if (i1 < 777) {
                    RewardGroup.putReward(rewards, 6710, 1L);
                } else if (i1 < 799) {
                    RewardGroup.putReward(rewards, 6711, 1L);
                } else if (i1 < 863) {
                    RewardGroup.putReward(rewards, 6712, 1L);
                } else if (i1 < 927) {
                    RewardGroup.putReward(rewards, 6713, 1L);
                } else {
                    RewardGroup.putReward(rewards, 6714, 1L);
                }
            }
            if (Rnd.chance(5.4)) {
                final int i1 = Rnd.get(1000);
                if (i1 < 100) {
                    RewardGroup.putReward(rewards, 6688, 1L);
                } else if (i1 < 198) {
                    RewardGroup.putReward(rewards, 6689, 1L);
                } else if (i1 < 298) {
                    RewardGroup.putReward(rewards, 6690, 1L);
                } else if (i1 < 398) {
                    RewardGroup.putReward(rewards, 6691, 1L);
                } else if (i1 < 499) {
                    RewardGroup.putReward(rewards, 7579, 1L);
                } else if (i1 < 601) {
                    RewardGroup.putReward(rewards, 6693, 1L);
                } else if (i1 < 703) {
                    RewardGroup.putReward(rewards, 6694, 1L);
                } else if (i1 < 801) {
                    RewardGroup.putReward(rewards, 6695, 1L);
                } else if (i1 < 902) {
                    RewardGroup.putReward(rewards, 6696, 1L);
                } else {
                    RewardGroup.putReward(rewards, 6697, 1L);
                }
            }
        }
    }
}
