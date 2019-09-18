package services;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.CharacterVariablesDAO;
import ru.j2dev.gameserver.listener.script.OnInitScriptListener;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Functions;

import java.util.*;

public class ClanHelperService extends Functions implements OnInitScriptListener {
    private static final Logger log = LoggerFactory.getLogger(ClanHelperService.class);
    private static final String USED_HELPERS_IDXS_VAR_NAME = "@clanhelper.used";
    static boolean SERVICES_CLANHELPER_ENABLED = true;
    static String SERVICES_CLANHELPER_CONFIG = "1=2:2;2=2:3,[-200-10000,57-100500];3=2:4,[-200-20000]";
    private static boolean isEnabled;
    private static Map<Integer, ClanHelperEntry> helperEntries = Collections.emptyMap();

    @Override
    public void onInit() {
        if (Config.SERVICES_CLANHELPER_ENABLED) {
            final StringTokenizer cfgTok = new StringTokenizer(Config.SERVICES_CLANHELPER_CONFIG, ";");
            final Map<Integer, ClanHelperEntry> clanHelperEntries = new LinkedHashMap<>();
            while (cfgTok.hasMoreTokens()) {
                final String cfg = cfgTok.nextToken();
                final int splitIdx = cfg.indexOf(61);
                final int menuId = Integer.parseInt(cfg.substring(0, splitIdx));
                clanHelperEntries.put(menuId, ClanHelperEntry.parse(cfg.substring(splitIdx + 1)));
            }
            isEnabled = true;
            helperEntries = clanHelperEntries;
            log.info("ClanHelperService: Loaded {} entry(s).", clanHelperEntries.size());
        } else {
            isEnabled = false;
            helperEntries = Collections.emptyMap();
            log.info("ClanHelperService: Disabled");
        }
    }

    private boolean isClanHelperUsed(final Clan clan, final int helperId) {
        final String usedClanHelpers = CharacterVariablesDAO.getInstance().getVar(clan.getClanId(), "@clanhelper.used", "clan-var");
        if (usedClanHelpers == null || usedClanHelpers.isEmpty()) {
            return false;
        }
        return Arrays.stream(usedClanHelpers.split(";")).anyMatch(usedClanHelperIdText -> !usedClanHelperIdText.isEmpty() && helperId == Integer.parseInt(usedClanHelperIdText));
    }

    private void addUsedClanHelper(final Clan clan, final int helperId) {
        final String usedClanHelpers = StringUtils.defaultString(CharacterVariablesDAO.getInstance().getVar(clan.getClanId(), "@clanhelper.used", "clan-var"), "");
        CharacterVariablesDAO.getInstance().setVar(clan.getClanId(), "@clanhelper.used", "clan-var", String.format("%s;%d", usedClanHelpers, helperId), -1L);
    }

    public void get_clan_help(final String[] arg) {
        final Player player = getSelf();
        if (player == null) {
            return;
        }
        if (arg == null || arg.length < 1) {
            return;
        }
        final int helperMenuId = Integer.parseInt(arg[0]);
        if (!helperEntries.containsKey(helperMenuId)) {
            return;
        }
        final ClanHelperEntry helperEntry = helperEntries.get(helperMenuId);
        if (!isEnabled || !Config.SERVICES_CLANHELPER_ENABLED) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_disabled.htm"));
            return;
        }
        if (!player.isInPeaceZone()) {
            player.sendPacket(new NpcHtmlMessage(5).setFile("scripts/services/service_peace_zone.htm"));
            return;
        }
        final Clan clan = player.getClan();
        if (clan == null) {
            player.sendPacket(Msg.YOU_ARE_NOT_A_CLAN_MEMBER);
            return;
        }
        if (clan.getLeaderId() != player.getObjectId()) {
            player.sendPacket(Msg.ONLY_THE_CLAN_LEADER_IS_ENABLED);
            return;
        }
        if (isClanHelperUsed(clan, helperMenuId)) {
            player.sendMessage("Already used.");
            return;
        }
        final Map<String, Player> membersHwids = new HashMap<>();
        final List<Player> members = clan.getOnlineMembers(0);
        members.stream().filter(Player::isConnected).filter(member -> !member.isLogoutStarted()).forEach(member -> {
            final GameClient client = member.getNetConnection();
            if (client == null) {
                return;
            }
            if (client.getHwid() == null) {
                return;
            }
            if (client.getHwid().isEmpty()) {
                return;
            }
            membersHwids.put(client.getHwid(), member);
        });
        if (membersHwids.size() < helperEntry.getRequiredClanSize()) {
            player.sendMessage("Clan to small.");
            return;
        }
        addUsedClanHelper(clan, helperMenuId);
        boolean clanChanged = false;
        if (helperEntry.getRewardLevel() > 0 && helperEntry.getRewardLevel() >= clan.getLevel()) {
            clan.setLevel(helperEntry.getRewardLevel());
            clanChanged = true;
        }
        for (final Pair<Integer, Long> rewardItem : helperEntry.getRewardItems()) {
            if (rewardItem.getKey() < 0) {
                switch (rewardItem.getKey()) {
                    case -200: {
                        final int repAmount = rewardItem.getValue().intValue();
                        clan.incReputation(repAmount, false, "ClanHelperService");
                        player.sendPacket(new SystemMessage(1777).addNumber(repAmount));
                        clanChanged = true;
                        continue;
                    }
                    case -100: {
                        for (final Player member2 : membersHwids.values()) {
                            member2.addPcBangPoints(rewardItem.getValue().intValue(), false);
                        }
                    }
                }
            } else {
                membersHwids.values().forEach(member2 -> addItem(member2, rewardItem.getKey(), rewardItem.getValue()));
            }
        }
        if (clanChanged) {
            clan.updateClanInDB();
            clan.broadcastClanStatus(true, true, true);
        }
        player.sendMessage("Clan help successfully provided.");
    }

    private static class ClanHelperEntry {
        private final int _requiredClanSize;
        private final int _rewardLevel;
        private final List<Pair<Integer, Long>> _rewardItems;

        private ClanHelperEntry(final int requiredClanSize, final int rewardLevel, final List<Pair<Integer, Long>> rewardItems) {
            _requiredClanSize = requiredClanSize;
            _rewardLevel = rewardLevel;
            _rewardItems = rewardItems;
        }

        public ClanHelperEntry(final int requiredClanSize, final int rewardLevel) {
            _requiredClanSize = requiredClanSize;
            _rewardLevel = rewardLevel;
            _rewardItems = Collections.emptyList();
        }

        public static ClanHelperEntry parse(final String text) {
            final int reqEndIdx = text.indexOf(58);
            if (reqEndIdx <= 0) {
                throw new RuntimeException("Can't parse requirements of \"" + text + "\"");
            }
            final String requirementsText = text.substring(0, reqEndIdx).trim();
            final String rewardsText = text.substring(reqEndIdx + 1).trim();
            final int reqClanSizeEndIdx = requirementsText.indexOf(44);
            final int reqClanOnlineSize = Integer.parseInt((reqClanSizeEndIdx > 0) ? requirementsText.substring(0, reqClanSizeEndIdx).trim() : requirementsText);
            final int rewLvlEndIdx = rewardsText.indexOf(44, 0);
            if (rewLvlEndIdx <= 0) {
                final int rewClanLvl = Integer.parseInt(rewardsText);
                return new ClanHelperEntry(reqClanOnlineSize, rewClanLvl);
            }
            final int rewClanLvl = Integer.parseInt(rewardsText.substring(0, rewLvlEndIdx));
            String rewardItemsText = rewardsText.substring(rewLvlEndIdx + 1).trim();
            if (rewardItemsText.isEmpty()) {
                return new ClanHelperEntry(reqClanOnlineSize, rewClanLvl);
            }
            final List<Pair<Integer, Long>> rewardItems = new ArrayList<>();
            if (!rewardItemsText.startsWith(",")) {
                if (!rewardItemsText.startsWith("[") || !rewardItemsText.endsWith("]")) {
                    throw new RuntimeException("Can't parse reward of \"" + text + "\"");
                }
                rewardItemsText = rewardItemsText.substring(1, rewardItemsText.length() - 1).trim();
                for (String rewardItemText : rewardItemsText.split(",")) {
                    rewardItemText = rewardItemText.trim();
                    if (!rewardItemText.isEmpty()) {
                        final int rewItemIdCndSplitIdx = rewardItemText.indexOf(45, 1);
                        rewardItems.add(Pair.of(Integer.parseInt(rewardItemText.substring(0, rewItemIdCndSplitIdx).trim()), Long.parseLong(rewardItemText.substring(rewItemIdCndSplitIdx + 1).trim())));
                    }
                }
            }
            return new ClanHelperEntry(reqClanOnlineSize, rewClanLvl, rewardItems);
        }

        public int getRequiredClanSize() {
            return _requiredClanSize;
        }

        public int getRewardLevel() {
            return _rewardLevel;
        }

        public List<Pair<Integer, Long>> getRewardItems() {
            return _rewardItems;
        }
    }
}
