package npc.model;

import quests._111_ElrokianHuntersProof;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.model.quest.QuestState;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class AsamahInstance extends NpcInstance {
    private static final int ElrokianTrap = 8763;
    private static final int TrapStone = 8764;

    public AsamahInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!canBypassCheck(player, this)) {
            return;
        }
        switch (command) {
            case "buyTrap": {
                String htmltext;
                final QuestState ElrokianHuntersProof = player.getQuestState(_111_ElrokianHuntersProof.class);
                if (player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && Functions.getItemCount(player, 57) > 1000000L) {
                    if (Functions.getItemCount(player, 8763) > 0L) {
                        htmltext = getNpcId() + "-alreadyhave.htm";
                    } else {
                        Functions.removeItem(player, 57, 1000000L);
                        Functions.addItem(player, 8763, 1L);
                        htmltext = getNpcId() + "-given.htm";
                    }
                } else {
                    htmltext = getNpcId() + "-cant.htm";
                }
                showChatWindow(player, "default/" + htmltext);
                break;
            }
            case "buyStones": {
                String htmltext;
                final QuestState ElrokianHuntersProof = player.getQuestState(_111_ElrokianHuntersProof.class);
                if (player.getLevel() >= 75 && ElrokianHuntersProof != null && ElrokianHuntersProof.isCompleted() && Functions.getItemCount(player, 57) > 1000000L) {
                    Functions.removeItem(player, 57, 1000000L);
                    Functions.addItem(player, 8764, 100L);
                    htmltext = getNpcId() + "-given.htm";
                } else {
                    htmltext = getNpcId() + "-cant.htm";
                }
                showChatWindow(player, "default/" + htmltext);
                break;
            }
            default:
                super.onBypassFeedback(player, command);
                break;
        }
    }
}
