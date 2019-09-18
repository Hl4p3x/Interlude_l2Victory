package events.bountyhunters;

import npc.model.QueenAntLarvaInstance;
import npc.model.SquashInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.util.Rnd;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.ItemTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.actor.OnDeathListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.listener.CharListenerList;
import ru.j2dev.gameserver.model.instances.*;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HuntersGuild extends Functions implements OnInitScriptListener, IVoicedCommandHandler {

    private static final String[] _commandList = {"gettask", "declinetask"};
    private static final Logger LOGGER = LoggerFactory.getLogger(HuntersGuild.class);

    private static boolean checkTarget(final NpcTemplate npc) {
        if (!npc.isInstanceOf(MonsterInstance.class)) {
            return false;
        }
        if (npc.rewardExp == 0) {
            return false;
        }
        if (npc.isInstanceOf(RaidBossInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(QueenAntLarvaInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(SquashInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(MinionInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(FestivalMonsterInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(TamedBeastInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(DeadManInstance.class)) {
            return false;
        }
        if (npc.isInstanceOf(ChestInstance.class)) {
            return false;
        }
        if (npc.title.contains("Quest Monster")) {
            return false;
        }
        return GameObjectsStorage.getByNpcId(npc.getNpcId()) != null;
    }

    private static void doReward(final Player player) {
        //if (!Config.EVENT_BOUNTY_HUNTERS_ENABLED) {
        //    return;
        //}
        final int rewardid = Integer.parseInt(player.getVar("bhRewardId"));
        final long rewardcount = Long.parseLong(player.getVar("bhRewardCount"));
        player.unsetVar("bhMonstersId");
        player.unsetVar("bhMonstersNeeded");
        player.unsetVar("bhMonstersKilled");
        player.unsetVar("bhRewardId");
        player.unsetVar("bhRewardCount");
        if (player.getVar("bhsuccess") != null) {
            player.setVar("bhsuccess", String.valueOf(Integer.parseInt(player.getVar("bhsuccess")) + 1), -1);
        } else {
            player.setVar("bhsuccess", "1", -1);
        }
        addItem(player, rewardid, rewardcount);
        show(new CustomMessage("scripts.events.bountyhunters.TaskCompleted", player).addNumber(rewardcount).addItemName(rewardid), player);
    }

    @Override
    public void onInit() {
        CharListenerList.addGlobal((OnDeathListener) (cha, killer) -> {
            //if (!Config.EVENT_BOUNTY_HUNTERS_ENABLED) {
            //    return;
            //}
            if (cha.isMonster() && !cha.isRaid() && killer != null && killer.getPlayer() != null && killer.getPlayer().getVar("bhMonstersId") != null && Integer.parseInt(killer.getPlayer().getVar("bhMonstersId")) == cha.getNpcId()) {
                final int count = Integer.parseInt(killer.getPlayer().getVar("bhMonstersKilled")) + 1;
                killer.getPlayer().setVar("bhMonstersKilled", String.valueOf(count), -1);
                final int needed = Integer.parseInt(killer.getPlayer().getVar("bhMonstersNeeded"));
                if (count >= needed) {
                    doReward(killer.getPlayer());
                } else {
                    sendMessage(new CustomMessage("scripts.events.bountyhunters.NotifyKill", killer.getPlayer()).addNumber(needed - count), killer.getPlayer());
                }
            }
        });
        //if (!Config.EVENT_BOUNTY_HUNTERS_ENABLED) {
        //    return;
        //}
        VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
        LOGGER.info("Loaded Event: Bounty Hunters Guild");
    }

    public void getTask(final Player player, final int id) {
        //if (!Config.EVENT_BOUNTY_HUNTERS_ENABLED) {
        //    return;
        //}
        final NpcTemplate target;
        double mod = 1.;
        if (id == 0) {
            final Collection<NpcTemplate> monsters = NpcTemplateHolder.getInstance().getAll();
            if (monsters.isEmpty()) {
                show(new CustomMessage("scripts.events.bountyhunters.NoTargets", player), player);
                return;
            }
            final List<NpcTemplate> targets = monsters.stream().filter(npcTemplate -> npcTemplate.level == player.getLevel()).filter(HuntersGuild::checkTarget).collect(Collectors.toList());
            if (targets.isEmpty()) {
                show(new CustomMessage("scripts.events.bountyhunters.NoTargets", player), player);
                return;
            }
            target = targets.get(Rnd.get(targets.size()));
        } else {
            target = NpcTemplateHolder.getInstance().getTemplate(id);
            if (target == null || !checkTarget(target)) {
                show(new CustomMessage("scripts.events.bountyhunters.WrongTarget", player), player);
                return;
            }
            if (player.getLevel() - target.level > 5) {
                show(new CustomMessage("scripts.events.bountyhunters.TooEasy", player), player);
                return;
            }
            mod = 0.5 * (10 + target.level - player.getLevel()) / 10.;
        }

        final int mobcount = target.level + Rnd.get(25, 50);
        player.setVar("bhMonstersId", String.valueOf(target.getNpcId()), -1);
        player.setVar("bhMonstersNeeded", String.valueOf(mobcount), -1);
        player.setVar("bhMonstersKilled", "0", -1);

        final int fails = player.getVar("bhfails") == null ? 0 : Integer.parseInt(player.getVar("bhfails")) * 5;
        final int success = player.getVar("bhsuccess") == null ? 0 : Integer.parseInt(player.getVar("bhsuccess")) * 5;

        final double reputation = Math.min(Math.max((100 + success - fails) / 100., .25), 2.) * mod;

        final long adenarewardvalue = Math.round((target.level * Math.max(Math.log(target.level), 1) * 10 + Math.max((target.level - 60) * 33, 0) + Math.max((target.level - 65) * 50, 0)) * target.rateHp * mobcount * Config.RATE_DROP_ADENA * player.getRateAdena() * reputation * .15);
        if (Rnd.chance(30)) // Адена, 30% случаев
        {
            player.setVar("bhRewardId", "57", -1);
            player.setVar("bhRewardCount", String.valueOf(adenarewardvalue), -1);
        } else { // Кристаллы, 70% случаев
            int crystal;
            if (target.level <= 39) {
                crystal = 1458; // D
            } else if (target.level <= 51) {
                crystal = 1459; // C
            } else if (target.level <= 60) {
                crystal = 1460; // B
            } else if (target.level <= 75) {
                crystal = 1461; // A
            } else {
                crystal = 1462; // S
            }
            player.setVar("bhRewardId", String.valueOf(crystal), -1);
            player.setVar("bhRewardCount", String.valueOf(adenarewardvalue / ItemTemplateHolder.getInstance().getTemplate(crystal).getReferencePrice()), -1);
        }
        show(new CustomMessage("scripts.events.bountyhunters.TaskGiven", player).addNumber(mobcount).addString(target.getName()), player);
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
        if (activeChar == null) { // || !Config.EVENT_BOUNTY_HUNTERS_ENABLED
            return false;
        }
        if (activeChar.getLevel() < 20) {
            sendMessage(new CustomMessage("scripts.events.bountyhunters.TooLowLevel", activeChar), activeChar);
            return true;
        }
        if (command.equalsIgnoreCase("gettask")) {
            if (activeChar.getVar("bhMonstersId") != null) {
                final int mobid = Integer.parseInt(activeChar.getVar("bhMonstersId"));
                final int mobcount = Integer.parseInt(activeChar.getVar("bhMonstersNeeded")) - Integer.parseInt(activeChar.getVar("bhMonstersKilled"));
                show(new CustomMessage("scripts.events.bountyhunters.TaskGiven", activeChar).addNumber(mobcount).addString(NpcTemplateHolder.getInstance().getTemplate(mobid).getName()), activeChar);
                return true;
            }
            int id = 0;
            if (target != null && target.trim().matches("[\\d]{1,9}")) {
                id = Integer.parseInt(target);
            }
            getTask(activeChar, id);
            return true;
        }
        if (command.equalsIgnoreCase("declinetask")) {
            if (activeChar.getVar("bhMonstersId") == null) {
                sendMessage(new CustomMessage("scripts.events.bountyhunters.NoTask", activeChar), activeChar);
                return true;
            }
            activeChar.unsetVar("bhMonstersId");
            activeChar.unsetVar("bhMonstersNeeded");
            activeChar.unsetVar("bhMonstersKilled");
            activeChar.unsetVar("bhRewardId");
            activeChar.unsetVar("bhRewardCount");
            if (activeChar.getVar("bhfails") != null) {
                activeChar.setVar("bhfails", String.valueOf(Integer.parseInt(activeChar.getVar("bhfails")) + 1), -1);
            } else {
                activeChar.setVar("bhfails", "1", -1);
            }
            show(new CustomMessage("scripts.events.bountyhunters.TaskCanceled", activeChar), activeChar);
            return true;
        }
        return false;
    }
}
