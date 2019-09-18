package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.lang.ArrayUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.cache.ItemInfoCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.handler.voicecommands.VoicedCommandHandler;
import ru.j2dev.gameserver.manager.PetitionManager;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.model.chat.ChatFilters;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatFilter;
import ru.j2dev.gameserver.model.chat.chatfilter.ChatMsg;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.matching.MatchingRoom;
import ru.j2dev.gameserver.network.lineage2.components.ChatType;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ActionFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.Say2;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SystemMessage;
import ru.j2dev.gameserver.utils.Log;
import ru.j2dev.gameserver.utils.MapUtils;
import ru.j2dev.gameserver.utils.Strings;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

public class Say2C extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(Say2C.class);
    private static final Pattern EX_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+[\\s]+\tID=([0-9]+)[\\s]+\tColor=[0-9]+[\\s]+\tUnderline=[0-9]+[\\s]+\tTitle=\u001b(.[^\u001b]*)[^\b]");
    private static final Pattern SKIP_ITEM_LINK_PATTERN = Pattern.compile("[\b]\tType=[0-9]+(.[^\b]*)[\b]");

    private String _text;
    private ChatType _type;
    private String _target;

    private static void shout(final Player activeChar, final Say2 cs) {
        final int rx = MapUtils.regionX(activeChar);
        final int ry = MapUtils.regionY(activeChar);
        final int offset = Config.SHOUT_OFFSET;
        GameObjectsStorage.getPlayers().stream().filter(player -> player != activeChar && activeChar.getReflection() == player.getReflection() && !player.isBlockAll()).filter(player -> !player.isInBlockList(activeChar)).forEach(player -> {
            final int tx = MapUtils.regionX(player);
            final int ty = MapUtils.regionY(player);
            if ((tx < rx - offset || tx > rx + offset || ty < ry - offset || ty > ry + offset) && !activeChar.isInRangeZ(player, Config.CHAT_RANGE)) {
                return;
            }
            player.sendPacket(cs);
        });
    }

    private static void announce(final Player activeChar, final Say2 cs) {
        GameObjectsStorage.getPlayers().stream().filter(player -> player != activeChar && activeChar.getReflection() == player.getReflection() && !player.isBlockAll()).filter(player -> !player.isInBlockList(activeChar)).forEach(player -> player.sendPacket(cs));
    }

    @Override
    protected void readImpl() {
        _text = readS(Config.CHAT_MESSAGE_MAX_LEN);
        _type = ArrayUtils.valid(ChatType.VALUES, readD());
        _target = ((_type == ChatType.TELL) ? readS(Config.CNAME_MAXLEN) : null);
    }

    @SuppressWarnings("StringConcatenationInLoop")
    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        if (_type == null || _text == null || _text.length() == 0) {
            activeChar.sendActionFailed();
            return;
        }
        _text = _text.replaceAll("\\\\n", "\n");
        if (_text.contains("\n")) {
            final String[] lines = _text.split("\n");
            _text = "";
            IntStream.range(0, lines.length).forEach(i -> {
                lines[i] = lines[i].trim();
                if (lines[i].length() != 0) {
                    if (_text.length() > 0) {
                        _text += "\n  >";
                    }
                    _text += lines[i];
                }
            });
        }

        if (_text.isEmpty()) {
            LOGGER.warn(activeChar.getName() + ": sending empty text. Possible packet hack!");
            activeChar.sendActionFailed();
            return;
        }
        if (_text.startsWith(".")) {
            final String fullcmd = _text.substring(1).trim();
            final String command = fullcmd.split("\\s+")[0];
            final String args = fullcmd.substring(command.length()).trim();
            if (command.length() > 0) {
                final IVoicedCommandHandler vch = VoicedCommandHandler.getInstance().getVoicedCommandHandler(command);
                if (vch != null) {
                    vch.useVoicedCommand(command, activeChar, args);
                    return;
                }
            }
            activeChar.sendMessage(new CustomMessage("common.command404", activeChar));
            return;
        }
        final Player receiver = (_target == null) ? null : World.getPlayer(_target);
        final long currentTimeMillis = System.currentTimeMillis();
        if (!activeChar.getPlayerAccess().CanAnnounce) {
            for (final ChatFilter f : ChatFilters.getInstance().getFilters()) {
                if (f.isMatch(activeChar, _type, _text, receiver)) {
                    switch (f.getAction()) {
                        case ChatFilter.ACTION_BAN_CHAT: {
                            activeChar.updateNoChannel(Integer.parseInt(f.getValue()) * 1000L);
                            break;
                        }
                        case ChatFilter.ACTION_WARN_MSG: {
                            activeChar.sendMessage(new CustomMessage(f.getValue(), activeChar));
                            return;
                        }
                        case ChatFilter.ACTION_REPLACE_MSG: {
                            _text = f.getValue();
                            break;
                        }
                        case ChatFilter.ACTION_REDIRECT_MSG: {
                            _type = ChatType.valueOf(f.getValue());
                            break;
                        }
                        default:
                            break;
                    }
                }
            }
        }
        if (activeChar.getNoChannel() > 0L && ArrayUtils.contains(Config.BAN_CHANNEL_LIST, _type)) {
            if (activeChar.getNoChannelRemained() > 0L) {
                final long timeRemained = activeChar.getNoChannelRemained() / 60000L;
                activeChar.sendMessage(new CustomMessage("common.ChatBanned", activeChar).addNumber(timeRemained));
                return;
            }
            activeChar.updateNoChannel(0L);
        }
        if (_text.isEmpty()) {
            return;
        }
        Matcher m = EX_ITEM_LINK_PATTERN.matcher(_text);
        while (m.find()) {
            final int objectId = Integer.parseInt(m.group(1));
            final ItemInstance item = activeChar.getInventory().getItemByObjectId(objectId);
            if (item == null) {
                activeChar.sendActionFailed();
                break;
            }
            ItemInfoCache.getInstance().put(item);
        }
        final String translit = activeChar.getVar("translit");
        if (translit != null) {
            m = SKIP_ITEM_LINK_PATTERN.matcher(_text);
            final StringBuilder sb = new StringBuilder();
            int end = 0;
            while (m.find()) {
                sb.append(Strings.fromTranslit(_text.substring(end, end = m.start()), "tl".equals(translit) ? 1 : 2));
                sb.append(_text, end, end = m.end());
            }
            _text = sb.append(Strings.fromTranslit(_text.substring(end, _text.length()), "tl".equals(translit) ? 1 : 2)).toString();
        }
        Say2 cs = new Say2(activeChar.getObjectId(), _type, activeChar.getName(), _text);
        switch (_type) {
            case TELL: {
                if (receiver != null && receiver.isInOfflineMode()) {
                    activeChar.sendMessage("The person is in offline trade mode.");
                    activeChar.sendActionFailed();
                    break;
                }
                if (receiver != null && !receiver.isInBlockList(activeChar) && !receiver.isBlockAll()) {
                    if (!receiver.getMessageRefusal()) {
                        if (activeChar.getAntiFlood().canTell(receiver.getObjectId(), _text)) {
                            receiver.sendPacket(cs);
                        }
                        cs = new Say2(activeChar.getObjectId(), _type, "->" + receiver.getName(), _text);
                        activeChar.sendPacket(cs);
                        break;
                    }
                    activeChar.sendPacket(Msg.THE_PERSON_IS_IN_A_MESSAGE_REFUSAL_MODE);
                    break;
                } else {
                    if (receiver == null) {
                        activeChar.sendPacket(new SystemMessage(3).addString(_target), ActionFail.STATIC);
                        break;
                    }
                    activeChar.sendPacket(Msg.YOU_HAVE_BEEN_BLOCKED_FROM_THE_CONTACT_YOU_SELECTED, ActionFail.STATIC);
                    break;
                }
            }
            case SHOUT: {
                if (activeChar.isCursedWeaponEquipped()) {
                    activeChar.sendMessage(new CustomMessage("SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON", activeChar));
                    return;
                }
                if (activeChar.isInObserverMode()) {
                    activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
                    return;
                }
                if (!activeChar.isGM() && !activeChar.getAntiFlood().canShout(_text)) {
                    activeChar.sendMessage("Shout chat is allowed once per 5 seconds.");
                    return;
                }
                if (Config.GLOBAL_SHOUT && activeChar.getLevel() > Config.GLOBAL_SHOUT_MIN_LEVEL && activeChar.getPvpKills() >= Config.GLOBAL_SHOUT_MIN_PVP_COUNT) {
                    announce(activeChar, cs);
                } else {
                    shout(activeChar, cs);
                }
                activeChar.sendPacket(cs);
                break;
            }
            case TRADE: {
                if (activeChar.isCursedWeaponEquipped()) {
                    activeChar.sendMessage(new CustomMessage("SHOUT_AND_TRADE_CHATING_CANNOT_BE_USED_SHILE_POSSESSING_A_CURSED_WEAPON", activeChar));
                    return;
                }
                if (activeChar.isInObserverMode()) {
                    activeChar.sendPacket(Msg.YOU_CANNOT_CHAT_LOCALLY_WHILE_OBSERVING);
                    return;
                }
                if (!activeChar.isGM() && !activeChar.getAntiFlood().canTrade(_text)) {
                    activeChar.sendMessage("Trade chat is allowed once per 5 seconds.");
                    return;
                }
                if (Config.GLOBAL_TRADE_CHAT && activeChar.getLevel() > Config.GLOBAL_TRADE_CHAT_MIN_LEVEL && activeChar.getPvpKills() >= Config.GLOBAL_TRADE_MIN_PVP_COUNT) {
                    announce(activeChar, cs);
                } else {
                    shout(activeChar, cs);
                }
                activeChar.sendPacket(cs);
                break;
            }
            case ALL: {
                if (activeChar.isCursedWeaponEquipped()) {
                    cs = new Say2(activeChar.getObjectId(), _type, activeChar.getTransformationName(), _text);
                }
                List<Player> list;
                list = World.getAroundPlayers(activeChar);
                if (list != null) {
                    for (final Player player : list) {
                        if (player != activeChar && player.getReflection() == activeChar.getReflection() && !player.isBlockAll()) {
                            if (player.isInBlockList(activeChar)) {
                                continue;
                            }
                            player.sendPacket(cs);
                        }
                    }
                }
                activeChar.sendPacket(cs);
                break;
            }
            case CLAN: {
                if (activeChar.getClan() != null) {
                    activeChar.getClan().broadcastToOnlineMembers(cs);
                    break;
                }
                break;
            }
            case ALLIANCE: {
                if (activeChar.getClan() != null && activeChar.getClan().getAlliance() != null) {
                    activeChar.getClan().getAlliance().broadcastToOnlineMembers(cs);
                    break;
                }
                break;
            }
            case PARTY: {
                if (activeChar.isInParty()) {
                    activeChar.getParty().broadCast(cs);
                    break;
                }
                break;
            }
            case PARTY_ROOM: {
                final MatchingRoom r = activeChar.getMatchingRoom();
                if (r != null && r.getType() == MatchingRoom.PARTY_MATCHING) {
                    r.broadCast(cs);
                    break;
                }
                break;
            }
            case COMMANDCHANNEL_ALL: {
                if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel()) {
                    activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
                    return;
                }
                if (activeChar.getParty().getCommandChannel().getChannelLeader() == activeChar) {
                    activeChar.getParty().getCommandChannel().broadCast(cs);
                    break;
                }
                activeChar.sendPacket(Msg.ONLY_CHANNEL_OPENER_CAN_GIVE_ALL_COMMAND);
                break;
            }
            case COMMANDCHANNEL_COMMANDER: {
                if (!activeChar.isInParty() || !activeChar.getParty().isInCommandChannel()) {
                    activeChar.sendPacket(Msg.YOU_DO_NOT_HAVE_AUTHORITY_TO_USE_THE_COMMAND_CHANNEL);
                    return;
                }
                if (activeChar.getParty().isLeader(activeChar)) {
                    activeChar.getParty().getCommandChannel().broadcastToChannelPartyLeaders(cs);
                    break;
                }
                activeChar.sendPacket(Msg.ONLY_A_PARTY_LEADER_CAN_ACCESS_THE_COMMAND_CHANNEL);
                break;
            }
            case HERO_VOICE: {
                if (!activeChar.isHero() && !activeChar.getPlayerAccess().CanAnnounce) {
                    break;
                }
                if (!activeChar.getPlayerAccess().CanAnnounce && !activeChar.getAntiFlood().canHero(_text)) {
                    activeChar.sendMessage("Hero chat is allowed once per 10 seconds.");
                    return;
                }
                for (final Player player2 : GameObjectsStorage.getPlayers()) {
                    if (!player2.isInBlockList(activeChar) && !player2.isBlockAll()) {
                        player2.sendPacket(cs);
                    }
                }
                break;
            }
            case PETITION_PLAYER:
            case PETITION_GM: {
                if (!PetitionManager.getInstance().isPlayerInConsultation(activeChar)) {
                    activeChar.sendPacket(new SystemMessage(745));
                    return;
                }
                PetitionManager.getInstance().sendActivePetitionMessage(activeChar, _text);
                break;
            }
            case BATTLEFIELD: {
                if (activeChar.getBattlefieldChatId() == 0) {
                    return;
                }
                for (final Player player2 : GameObjectsStorage.getPlayers()) {
                    if (!player2.isInBlockList(activeChar) && !player2.isBlockAll() && player2.getBattlefieldChatId() == activeChar.getBattlefieldChatId()) {
                        player2.sendPacket(cs);
                    }
                }
                break;
            }
            case MPCC_ROOM: {
                final MatchingRoom r2 = activeChar.getMatchingRoom();
                if (r2 != null && r2.getType() == MatchingRoom.CC_MATCHING) {
                    r2.broadCast(cs);
                    break;
                }
                break;
            }
            default: {
                LOGGER.warn("Character " + activeChar.getName() + " used unknown chat type: " + _type.ordinal() + ".");
                break;
            }
        }
        Log.LogChat(_type.name(), activeChar.getName(), _target, _text, 0);
        activeChar.getMessageBucket().addLast(new ChatMsg(_type, (receiver == null) ? 0 : receiver.getObjectId(), _text.hashCode(), (int) (currentTimeMillis / 1000L)));
    }
}
