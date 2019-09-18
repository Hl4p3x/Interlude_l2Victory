package services;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.reference.HardReference;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.actor.OnKillListener;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Creature;
import ru.j2dev.gameserver.model.EffectList;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Skill;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.actor.listener.PlayerListenerList;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.scripts.Scripts;
import ru.j2dev.gameserver.tables.SkillTable;

public class BotCheckService extends Functions implements OnInitScriptListener {
    private static final Logger LOG = LoggerFactory.getLogger(BotCheckService.class);
    private static final String MONSTER_KILL_VAR_NAME = "BCSMonKillCnt";
    private static final BotCheckKillListener BOT_CHECK_KILL_LISTENER = new BotCheckKillListener();
    private static final BotCheckVoicedCommandHandler BOT_CHECK_VOICED_COMMAND_HANDLER = new BotCheckVoicedCommandHandler();

    private static int getMonterKillCounter(final Player player) {
        return player.getVarInt("BCSMonKillCnt", 0);
    }

    private static void setMonsterKillCounter(final Player player, final int monsterKillCounter) {
        if (monsterKillCounter > 0) {
            player.setVar("BCSMonKillCnt", monsterKillCounter, -1L);
        } else {
            player.unsetVar("BCSMonKillCnt");
        }
    }

    private static boolean checkBotCheckThreshold(final Player player, final int monsterKillCount) {
        if (monsterKillCount < Config.CAPTCHA_MONSTER_KILLS_THRESHOLD) {
            return false;
        }
        ThreadPoolManager.getInstance().execute(new BotCheckTask(player));
        return true;
    }

    private static void botCheckPlayer(final Player player) {
        final Skill penaltySkill = SkillTable.getInstance().getInfo(Config.CAPTCHA_PENALTY_SKILL_ID, Config.CAPTCHA_PENALTY_SKILL_LEVEL);
        penaltySkill.getEffects(player, player, false, false, 0L, 0.0, false);
        Scripts.getInstance().callScripts(player, "Util", "RequestCapcha", new Object[]{"services.BotCheckService:captchaCheckSucceed", player.getObjectId(), 30});
    }

    private static void playerCheckSucceed(final Player player) {
        setMonsterKillCounter(player, 0);
        final Skill penaltySkill = SkillTable.getInstance().getInfo(Config.CAPTCHA_PENALTY_SKILL_ID, Config.CAPTCHA_PENALTY_SKILL_LEVEL);
        final EffectList playerEffectList = player.getEffectList();
        playerEffectList.stopEffect(penaltySkill);
    }

    private static void onMonsterKill(final Player player) {
        final int monsterKillCounter = getMonterKillCounter(player);
        if (!checkBotCheckThreshold(player, monsterKillCounter)) {
            setMonsterKillCounter(player, monsterKillCounter + 1);
        }
    }

    public void captchaCheckSucceed() {
        final Player player = getSelf();
        if (!Config.SERVICE_CAPTCHA_BOT_CHECK || player == null) {
            return;
        }
        playerCheckSucceed(player);
    }

    @Override
    public void onInit() {
        if (Config.SERVICE_CAPTCHA_BOT_CHECK) {
            VoicedCommandHandler.getInstance().registerVoicedCommandHandler(BotCheckService.BOT_CHECK_VOICED_COMMAND_HANDLER);
            PlayerListenerList.addGlobal(BotCheckService.BOT_CHECK_KILL_LISTENER);
        }
    }

    private static class BotCheckKillListener implements OnKillListener {
        @Override
        public void onKill(final Creature actor, final Creature victim) {
            if (!Config.SERVICE_CAPTCHA_BOT_CHECK || !actor.isPlayer() || !victim.isMonster() || actor.isInZone(ZoneType.peace_zone) || actor.isInZone(ZoneType.fun) || actor.isInZone(ZoneType.battle_zone) || actor.isInZone(ZoneType.RESIDENCE)) {
                return;
            }
            onMonsterKill(actor.getPlayer());
        }

        @Override
        public boolean ignorePetOrSummon() {
            return true;
        }
    }

    private static class BotCheckVoicedCommandHandler implements IVoicedCommandHandler {
        private final String[] _commandList;

        private BotCheckVoicedCommandHandler() {
            _commandList = new String[]{"check_ololo_bots"};
        }

        @Override
        public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
            if (!Config.SERVICE_CAPTCHA_BOT_CHECK) {
                return false;
            }
            if (_commandList[0].equalsIgnoreCase(command)) {
                checkBotCheckThreshold(activeChar, getMonterKillCounter(activeChar));
                return true;
            }
            return false;
        }

        @Override
        public String[] getVoicedCommandList() {
            if (!Config.SERVICE_CAPTCHA_BOT_CHECK) {
                return ArrayUtils.EMPTY_STRING_ARRAY;
            }
            return _commandList;
        }
    }

    private static class BotCheckTask extends RunnableImpl {
        private final HardReference<Player> _playerRef;

        private BotCheckTask(final Player player) {
            _playerRef = player.getRef();
        }

        @Override
        public void runImpl() {
            final Player player = _playerRef.get();
            if (player == null) {
                return;
            }
            botCheckPlayer(player);
        }
    }
}
