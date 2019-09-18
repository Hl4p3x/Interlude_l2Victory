package ru.j2dev.gameserver.model.instances;

import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.manager.games.FishingChampionShipManager;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

public class FishermanInstance extends MerchantInstance {
    public FishermanInstance(final int objectId, final NpcTemplate template) {
        super(objectId, template);
    }

    @Override
    public String getHtmlPath(final int npcId, final int val, final Player player) {
        String pom;
        if (val == 0) {
            pom = "" + npcId;
        } else {
            pom = npcId + "-" + val;
        }
        return "fisherman/" + pom + ".htm";
    }

    @Override
    public void onBypassFeedback(final Player player, final String command) {
        if (!NpcInstance.canBypassCheck(player, this)) {
            return;
        }
        if ("FishingSkillList".equalsIgnoreCase(command)) {
            NpcInstance.showFishingSkillList(player);
        } else if (command.startsWith("FishingChampionship") && Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
            FishingChampionShipManager.getInstance().showChampScreen(player, this);
        } else if (command.startsWith("FishingReward") && Config.ALT_FISH_CHAMPIONSHIP_ENABLED) {
            FishingChampionShipManager.getInstance().getReward(player);
        } else {
            super.onBypassFeedback(player, command);
        }
    }
}
