package services;

import bosses.*;
import org.apache.commons.lang3.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.NpcTemplateHolder;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.manager.RaidBossSpawnManager;
import ru.j2dev.gameserver.manager.RaidBossSpawnManager.Status;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Spawner;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.scripts.Functions;
import ru.j2dev.gameserver.templates.npc.NpcTemplate;
import ru.j2dev.gameserver.utils.TimeUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class BossStatusService extends Functions implements OnInitScriptListener, IVoicedCommandHandler {
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat(Config.SERVICES_BOSS_STATUS_FORMAT);
    private static BossStatusInfo[] _bossesInfo;
    private final String[] _commandList;

    public BossStatusService() {
        _commandList = new String[]{"boss_status", "epic"};
    }

    private static BossStatusInfo[] initBossesInfo() {
        final List<BossStatusInfo> result = new ArrayList<BossStatusInfo>() {
            {
                add(new EpicBossStatusInfo(29020, BaiumManager.getEpicBossState()));
                add(new EpicBossStatusInfo(29028, ValakasManager.getEpicBossState()));
                add(new EpicBossStatusInfo(FrintezzaManager.FRINTEZZA_NPC_ID, FrintezzaManager.getInstance().getEpicBossState()));
                add(new EpicBossStatusInfo(29068, AntharasManager.getEpicBossState()));
            }
        };
        for (final int raidBossNpcId : Config.SERVICES_BOSS_STATUS_ADDITIONAL_IDS) {
            result.add(new RaidBossStatusInfo(raidBossNpcId));
        }
        return result.toArray(new BossStatusInfo[0]);
    }

    private static BossStatusInfo[] getBossesInfo() {
        if (BossStatusService._bossesInfo == null) {
            return BossStatusService._bossesInfo = initBossesInfo();
        }
        return BossStatusService._bossesInfo;
    }

    private static String formatBossHtml(final Player player, final BossStatusInfo bossStatusInfo) {
        String bossHtml = StringHolder.getInstance().getNotNull(player, "scripts.services.BossStatusService." + bossStatusInfo.getStatus());
        final long respawnDate = bossStatusInfo.getRespawnDate();
        bossHtml = bossHtml.replace("%name%", bossStatusInfo.getName());
        bossHtml = bossHtml.replace("%npc_id%", String.valueOf(bossStatusInfo.getNpcId()));
        bossHtml = bossHtml.replace("%respawn_date%", (respawnDate > 0L) ? BossStatusService.DATE_FORMAT.format(respawnDate * 1000L) : "");
        return bossHtml;
    }

    @Override
    public boolean useVoicedCommand(final String command, final Player activeChar, final String target) {
        if (!Config.SERVICES_BOSS_STATUS_ENABLE) {
            return false;
        }
        if (_commandList[0].equalsIgnoreCase(command) || _commandList[1].equalsIgnoreCase(command)) {
            listBossStatusesWithMsg(activeChar);
            return true;
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        if (!Config.SERVICES_BOSS_STATUS_ENABLE) {
            return ArrayUtils.EMPTY_STRING_ARRAY;
        }
        return _commandList;
    }

    public void listBossStatusesWithMsg(final Player player) {
        if (!Config.SERVICES_BOSS_STATUS_ENABLE) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        final BossStatusInfo[] bossStatusInfos = getBossesInfo();
        final StringBuilder bossStatusInfoHtml = new StringBuilder();
        for (final BossStatusInfo bossStatusInfo : bossStatusInfos) {
            bossStatusInfoHtml.append(formatBossHtml(player, bossStatusInfo));
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, null);
        html.setFile("scripts/services/command_bossstatus.htm");
        html.replace("%list%", bossStatusInfoHtml.toString());
        html.replace("%current_date%", TimeUtils.toSimpleFormat(System.currentTimeMillis()));
        player.sendPacket(html);
    }

    public void listBossStatuses() {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        final NpcInstance npc = getNpc();
        if (!Config.SERVICES_BOSS_STATUS_ENABLE) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        final BossStatusInfo[] bossStatusInfos = getBossesInfo();
        final StringBuilder bossStatusInfoHtml = new StringBuilder();
        for (final BossStatusInfo bossStatusInfo : bossStatusInfos) {
            bossStatusInfoHtml.append(formatBossHtml(player, bossStatusInfo));
        }
        final NpcHtmlMessage html = new NpcHtmlMessage(player, npc);
        html.setFile("scripts/services/bossstatus.htm");
        html.replace("%list%", bossStatusInfoHtml.toString());
        html.replace("%current_date%", TimeUtils.toSimpleFormat(System.currentTimeMillis()));
        player.sendPacket(html);
    }

    @Override
    public void onInit() {
        if (Config.SERVICES_BOSS_STATUS_ENABLE) {
            VoicedCommandHandler.getInstance().registerVoicedCommandHandler(this);
        }
    }

    private abstract static class BossStatusInfo {
        private final int _bossNpcId;

        public BossStatusInfo(final int bossNpcId) {
            _bossNpcId = bossNpcId;
        }

        public int getNpcId() {
            return _bossNpcId;
        }

        public String getName() {
            final NpcTemplate bossNpcTemplate = NpcTemplateHolder.getInstance().getTemplate(getNpcId());
            if (bossNpcTemplate == null) {
                return "";
            }
            return bossNpcTemplate.getName();
        }

        public abstract BossStatus getStatus();

        public abstract long getRespawnDate();

        public enum BossStatus {
            ALIVE,
            DEAD,
            READY,
            RESPAWN
        }
    }

    public static final class EpicBossStatusInfo extends BossStatusInfo {
        private final EpicBossState _epicBossState;

        public EpicBossStatusInfo(final int bossNpcId, final EpicBossState epicBossState) {
            super(bossNpcId);
            _epicBossState = epicBossState;
        }

        @Override
        public BossStatus getStatus() {
            switch (_epicBossState.getState()) {
                case ALIVE: {
                    return BossStatus.ALIVE;
                }
                case NOTSPAWN: {
                    return BossStatus.READY;
                }
                case DEAD: {
                    return BossStatus.DEAD;
                }
                default: {
                    return BossStatus.RESPAWN;
                }
            }
        }

        @Override
        public long getRespawnDate() {
            return _epicBossState.getRespawnDate() / 1000L;
        }
    }

    public static final class RaidBossStatusInfo extends BossStatusInfo {
        public RaidBossStatusInfo(final int bossNpcId) {
            super(bossNpcId);
        }

        @Override
        public BossStatus getStatus() {
            if (RaidBossSpawnManager.getInstance().getRaidBossStatusId(getNpcId()) == Status.DEAD) {
                return BossStatus.DEAD;
            }
            final long respawnDate = getRespawnDate();
            if (respawnDate * 1000L > System.currentTimeMillis()) {
                return BossStatus.DEAD;
            }
            return BossStatus.ALIVE;
        }

        @Override
        public long getRespawnDate() {
            final Spawner spawner = RaidBossSpawnManager.getInstance().getSpawnTable().get(getNpcId());
            if (spawner == null) {
                return -1L;
            }
            return spawner.getRespawnTime();
        }
    }
}
