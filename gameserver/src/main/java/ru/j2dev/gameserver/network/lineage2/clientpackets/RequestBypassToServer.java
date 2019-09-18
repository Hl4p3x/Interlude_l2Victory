package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.data.xml.holder.MultiSellHolder;
import ru.j2dev.gameserver.handler.admincommands.AdminCommandHandler;
import ru.j2dev.gameserver.handler.bbs.CommunityBoardManager;
import ru.j2dev.gameserver.handler.bbs.ICommunityBoardHandler;
import ru.j2dev.gameserver.handler.bypass.BypassHolder;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.manager.BypassManager.DecodedBypass;
import ru.j2dev.gameserver.model.GameObject;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadGameManager;
import ru.j2dev.gameserver.model.entity.olympiad.HeroManager;
import ru.j2dev.gameserver.model.instances.NpcInstance;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.scripts.Scripts;

import java.lang.reflect.Method;
import java.util.*;

public class RequestBypassToServer extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestBypassToServer.class);

    private DecodedBypass bp;
    private String _bypass;

    public RequestBypassToServer() {
        bp = null;
    }

    private static void comeHere(final GameClient client) {
        final GameObject obj = client.getActiveChar().getTarget();
        if (obj != null && obj.isNpc()) {
            final NpcInstance temp = (NpcInstance) obj;
            final Player activeChar = client.getActiveChar();
            temp.setTarget(activeChar);
            temp.moveToLocation(activeChar.getLoc(), 0, true);
        }
    }

    private static void playerHelp(final Player activeChar, final String path) {
        final NpcHtmlMessage html = new NpcHtmlMessage(5);
        html.setFile(path);
        activeChar.sendPacket(html);
    }

    @Override
    protected void readImpl() {
        _bypass = readS();
    }

    @Override
    protected void runImpl() {
        final GameClient client = getClient();
        final Player activeChar = client.getActiveChar();
        try {
            if (!_bypass.isEmpty()) {
                bp = client.decodeBypass(_bypass);
            }
            if (bp == null) {
                return;
            }
            if (activeChar == null) {
                if (Config.USE_SECOND_PASSWORD_AUTH && bp.bypass.startsWith("spa_")) {
                    Objects.requireNonNull(client.getSecondPasswordAuth()).getUI().handle(client, bp.bypass.substring(4));
                }
                return;
            }
            NpcInstance npc = activeChar.getLastNpc();
            final GameObject target = activeChar.getTarget();
            if (npc == null && target != null && target.isNpc()) {
                npc = (NpcInstance) target;
            }
            if (Config.LOG_REQUEST_BUYPASS) {
                LOGGER.info("Bypass calling : " + bp.bypass + " Caller :" + activeChar.toString());
            }
            if (bp.bypass.startsWith("admin_")) {
                AdminCommandHandler.getInstance().useAdminCommandHandler(activeChar, bp.bypass);
            } else if ("come_here".equals(bp.bypass) && activeChar.isGM()) {
                comeHere(getClient());
            } else if (bp.bypass.startsWith("player_help ")) {
                playerHelp(activeChar, bp.bypass.substring(12));
            } else if (bp.bypass.startsWith("htmbypass_")) {
                final String command = bp.bypass.substring(10).trim();
                final String word = command.split("\\s+")[0];
                final Pair<Object, Method> b = BypassHolder.getInstance().getBypass(word);
                if (b != null) {
                    b.getValue().invoke(b.getKey(), activeChar, npc, command.substring(word.length()).trim().split("\\s+"));
                }
            } else if (bp.bypass.startsWith("scripts_")) {
                final String command = bp.bypass.substring(8).trim();
                final String[] word = command.split("\\s+");
                final String[] args = command.substring(word[0].length()).trim().split("\\s+");
                final String[] path = word[0].split(":");
                if (path.length != 2) {
                    LOGGER.warn("Bad Script bypass!");
                    return;
                }
                Map<String, Object> variables = null;
                if (npc != null) {
                    variables = new HashMap<>(1);
                    variables.put("npc", npc.getRef());
                }
                if (word.length == 1) {
                    Scripts.getInstance().callScripts(activeChar, path[0], path[1], variables);
                } else {
                    Scripts.getInstance().callScripts(activeChar, path[0], path[1], new Object[]{args}, variables);
                }
            } else if (bp.bypass.startsWith("user_")) {
                final String command = bp.bypass.substring(5).trim();
                final String word2 = command.split("\\s+")[0];
                final String args2 = command.substring(word2.length()).trim();
                final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(word2);
                if (vch != null) {
                    vch.useVoicedCommand(word2, activeChar, args2);
                } else {
                    LOGGER.warn("Unknown voiced command '" + word2 + "'");
                }
            } else if (bp.bypass.startsWith("npc_")) {
                final int endOfId = bp.bypass.indexOf(95, 5);
                String id;
                if (endOfId > 0) {
                    id = bp.bypass.substring(4, endOfId);
                } else {
                    id = bp.bypass.substring(4);
                }
                final GameObject object = activeChar.getVisibleObject(Integer.parseInt(id));
                if (object != null && object.isNpc() && endOfId > 0 && object.isInActingRange(activeChar)) {
                    activeChar.setLastNpc((NpcInstance) object);
                    ((NpcInstance) object).onBypassFeedback(activeChar, bp.bypass.substring(endOfId + 1));
                }
            } else if (bp.bypass.startsWith("_olympiad?command=move_op_field&field=")) {
                if (!Config.OLY_SPECTATION_ALLOWED) {
                    return;
                }
                int stadium_id;
                try {
                    stadium_id = Integer.parseInt(bp.bypass.substring(38));
                    OlympiadGameManager.getInstance().watchCompetition(activeChar, stadium_id);
                } catch (Exception ex) {
                    LOGGER.warn("OlyObserver", ex);
                    ex.printStackTrace();
                }
            } else if (bp.bypass.startsWith("_diary")) {
                final String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
                final StringTokenizer st = new StringTokenizer(params, "&");
                final int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
                final int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
                HeroManager.getInstance().showHeroDiary(activeChar, heroclass, heropage);
            } else if (bp.bypass.startsWith("_match")) {
                final String params = bp.bypass.substring(bp.bypass.indexOf("?") + 1);
                final StringTokenizer st = new StringTokenizer(params, "&");
                final int heroclass = Integer.parseInt(st.nextToken().split("=")[1]);
                final int heropage = Integer.parseInt(st.nextToken().split("=")[1]);
                HeroManager.getInstance().showHistory(activeChar, heroclass, heropage);
            } else if (bp.bypass.startsWith("manor_menu_select?")) {
                final GameObject object2 = activeChar.getTarget();
                if (object2 != null && object2.isNpc()) {
                    ((NpcInstance) object2).onBypassFeedback(activeChar, bp.bypass);
                }
            } else if (bp.bypass.startsWith("multisell ")) {
                MultiSellHolder.getInstance().SeparateAndSend(Integer.parseInt(bp.bypass.substring(10)), activeChar, 0.0);
            } else if (bp.bypass.startsWith("Quest ")) {
                final String p = bp.bypass.substring(6).trim();
                final int idx = p.indexOf(32);
                if (idx < 0) {
                    activeChar.processQuestEvent(p, "", npc);
                } else {
                    activeChar.processQuestEvent(p.substring(0, idx), p.substring(idx).trim(), npc);
                }
            } else if (bp.bbs) {
                if (!Config.COMMUNITYBOARD_ENABLED) {
                    activeChar.sendPacket(new SystemMessage(938));
                } else {
                    final ICommunityBoardHandler communityBoardHandler = CommunityBoardManager.getInstance().getCommunityHandler(bp.bypass);
                    communityBoardHandler.onBypassCommand(activeChar, bp.bypass);
                }
            }  else if (bp.bypass.startsWith("lang")) {
                int val = Integer.parseInt(bp.bypass.substring(4));
                switch (val) {
                    case 1:
                        break;
                    case 2:
                        break;
                }
            }
        } catch (Exception e) {
            String st2 = "Bad RequestBypassToServer: " + bp.bypass;
            final GameObject target2 = (activeChar != null) ? activeChar.getTarget() : null;
            if (target2 != null && target2.isNpc()) {
                st2 = st2 + " via NPC #" + ((NpcInstance) target2).getNpcId();
            }
            LOGGER.error(st2, e);
        }
    }
}
