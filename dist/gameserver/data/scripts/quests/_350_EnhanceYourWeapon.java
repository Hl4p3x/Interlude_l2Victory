package quests;

import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.SoulCrystalHolder;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.MonsterInstance;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.quest.Quest;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage2;
import ru.j2dev.gameserver.templates.SoulCrystal;
import ru.j2dev.gameserver.templates.npc.AbsorbInfo;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class _350_EnhanceYourWeapon extends Quest {
    private static final int RED_SOUL_CRYSTAL0_ID = 4629;
    private static final int GREEN_SOUL_CRYSTAL0_ID = 4640;
    private static final int BLUE_SOUL_CRYSTAL0_ID = 4651;
    private static final int Jurek = 30115;
    private static final int Gideon = 30194;
    private static final int Winonin = 30856;

    public _350_EnhanceYourWeapon() {
        super(false);
        addStartNpc(30115);
        addStartNpc(30194);
        addStartNpc(30856);
        for (final NpcTemplate template : NpcTemplateHolder.getInstance().getAll()) {
            if (template != null && !template.getAbsorbInfo().isEmpty()) {
                addKillId(template.npcId);
            }
        }
    }

    

    @Override
    public String onEvent(final String event, final QuestState st, final NpcInstance npc) {
        if ("30115-04.htm".equalsIgnoreCase(event) || "30194-04.htm".equalsIgnoreCase(event) || "30856-04.htm".equalsIgnoreCase(event)) {
            st.setCond(1);
            st.setState(2);
            st.playSound("ItemSound.quest_accept");
        }
        if ("30115-09.htm".equalsIgnoreCase(event) || "30194-09.htm".equalsIgnoreCase(event) || "30856-09.htm".equalsIgnoreCase(event)) {
            st.giveItems(4629, 1L);
        }
        if ("30115-10.htm".equalsIgnoreCase(event) || "30194-10.htm".equalsIgnoreCase(event) || "30856-10.htm".equalsIgnoreCase(event)) {
            st.giveItems(4640, 1L);
        }
        if ("30115-11.htm".equalsIgnoreCase(event) || "30194-11.htm".equalsIgnoreCase(event) || "30856-11.htm".equalsIgnoreCase(event)) {
            st.giveItems(4651, 1L);
        }
        if ("exit.htm".equalsIgnoreCase(event)) {
            st.exitCurrentQuest(true);
        }
        return event;
    }

    @Override
    public String onTalk(final NpcInstance npc, final QuestState st) {
        final String npcId = str((long) npc.getNpcId());
        String htmltext;
        final int id = st.getState();
        if (st.getQuestItemsCount(4629) == 0L && st.getQuestItemsCount(4640) == 0L && st.getQuestItemsCount(4651) == 0L) {
            if (id == 1) {
                htmltext = npcId + "-01.htm";
            } else {
                htmltext = npcId + "-21.htm";
            }
        } else {
            if (id == 1) {
                st.setCond(1);
                st.setState(2);
            }
            htmltext = npcId + "-03.htm";
        }
        return htmltext;
    }

    @Override
    public String onKill(final NpcInstance npc, final QuestState qs) {
        final Player player = qs.getPlayer();
        if (player == null || !npc.isMonster()) {
            return null;
        }
        List<PlayerResult> list;
        if (player.getParty() == null) {
            list = new ArrayList<>(1);
            list.add(new PlayerResult(player));
        } else {
            list = new ArrayList<>(player.getParty().getMemberCount());
            list.add(new PlayerResult(player));
            for (final Player m : player.getParty().getPartyMembers()) {
                if (m != player && m.isInRange(npc.getLoc(), (long) Config.ALT_PARTY_DISTRIBUTION_RANGE)) {
                    list.add(new PlayerResult(m));
                }
            }
        }
        for (final AbsorbInfo info : npc.getTemplate().getAbsorbInfo()) {
            calcAbsorb(list, (MonsterInstance) npc, info);
        }
        for (final PlayerResult r : list) {
            r.send();
        }
        return null;
    }

    private void calcAbsorb(final List<PlayerResult> players, final MonsterInstance npc, final AbsorbInfo info) {
        int memberSize;
        List<PlayerResult> targets;
        switch (info.getAbsorbType()) {
            case LAST_HIT: {
                targets = Collections.singletonList(players.get(0));
                break;
            }
            case PARTY_ALL: {
                targets = players;
                break;
            }
            case PARTY_RANDOM: {
                memberSize = players.size();
                if (memberSize == 1) {
                    targets = Collections.singletonList(players.get(0));
                    break;
                }
                final int size = Rnd.get(memberSize);
                targets = new ArrayList<>(size);
                final List<PlayerResult> temp = new ArrayList<>(players);
                Collections.shuffle(temp);
                for (int i = 0; i < size; ++i) {
                    targets.add(temp.get(i));
                }
                break;
            }
            case PARTY_ONE: {
                memberSize = players.size();
                if (memberSize == 1) {
                    targets = Collections.singletonList(players.get(0));
                    break;
                }
                final int rnd = Rnd.get(memberSize);
                targets = Collections.singletonList(players.get(rnd));
                break;
            }
            default: {
                return;
            }
        }
        for (final PlayerResult target : targets) {
            if (target != null) {
                if (target.getMessage() == SystemMsg.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL) {
                    continue;
                }
                final Player targetPlayer = target.getPlayer();
                if (info.isSkill() && !npc.isAbsorbed(targetPlayer)) {
                    continue;
                }
                if (targetPlayer.getQuestState(_350_EnhanceYourWeapon.class) == null) {
                    continue;
                }
                boolean resonate = false;
                SoulCrystal soulCrystal = null;
                final List<ItemInstance> items = targetPlayer.getInventory().getItems();
                for (final ItemInstance item : items) {
                    final SoulCrystal crystal = SoulCrystalHolder.getInstance().getCrystal(item.getItemId());
                    if (crystal != null) {
                        target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
                        if (soulCrystal != null) {
                            target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_CAUSED_RESONATION_AND_FAILED_AT_ABSORBING_A_SOUL);
                            resonate = true;
                            break;
                        }
                        soulCrystal = crystal;
                    }
                }
                if (resonate) {
                    continue;
                }
                if (soulCrystal == null) {
                    continue;
                }
                if (!info.canAbsorb(soulCrystal.getLevel() + 1)) {
                    target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_IS_REFUSING_TO_ABSORB_THE_SOUL);
                } else {
                    int nextItemId = 0;
                    if (info.getCursedChance() > 0 && soulCrystal.getCursedNextItemId() > 0) {
                        nextItemId = (Rnd.chance(info.getCursedChance()) ? soulCrystal.getCursedNextItemId() : 0);
                    }
                    if (nextItemId == 0) {
                        nextItemId = (Rnd.chance(info.getChance()) ? soulCrystal.getNextItemId() : 0);
                    }
                    if (nextItemId == 0) {
                        target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
                    } else if (targetPlayer.consumeItem(soulCrystal.getItemId(), 1L)) {
                        targetPlayer.getInventory().addItem(nextItemId, 1L);
                        targetPlayer.sendPacket(SystemMessage2.obtainItems(nextItemId, 1L, 0));
                        target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_SUCCEEDED_IN_ABSORBING_A_SOUL);
                    } else {
                        target.setMessage(SystemMsg.THE_SOUL_CRYSTAL_WAS_NOT_ABLE_TO_ABSORB_THE_SOUL);
                    }
                }
            }
        }
    }

    private static class PlayerResult {
        private final Player _player;
        private SystemMsg _message;

        public PlayerResult(final Player player) {
            _player = player;
        }

        public Player getPlayer() {
            return _player;
        }

        public SystemMsg getMessage() {
            return _message;
        }

        public void setMessage(final SystemMsg message) {
            _message = message;
        }

        public void send() {
            if (_message != null) {
                _player.sendPacket(_message);
            }
        }
    }
}
