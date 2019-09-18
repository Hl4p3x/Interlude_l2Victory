package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.CrestCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class RequestExSetPledgeCrestLarge extends L2GameClientPacket {
    private int _length;
    private byte[] _data;

    @Override
    protected void readImpl() {
        _length = readD();
        if (_length == CrestCache.LARGE_CREST_SIZE && _length == _buf.remaining()) {
            readB(_data = new byte[_length]);
        }
    }

    @Override
    protected void runImpl() {
        final Player activeChar = getClient().getActiveChar();
        if (activeChar == null) {
            return;
        }
        final Clan clan = activeChar.getClan();
        if (clan == null) {
            return;
        }
        if ((activeChar.getClanPrivileges() & 0x80) == 0x80) {
            if (clan.isPlacedForDisband()) {
                activeChar.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
                return;
            }
            if (clan.getCastle() == 0 && clan.getHasHideout() == 0) {
                activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
                return;
            }
            int crestId = 0;
            if (_data != null && CrestCache.isValidCrestData(_data, _length)) {
                crestId = CrestCache.getInstance().savePledgeCrestLarge(clan.getClanId(), _data);
                activeChar.sendPacket(Msg.THE_CLANS_EMBLEM_WAS_SUCCESSFULLY_REGISTERED__ONLY_A_CLAN_THAT_OWNS_A_CLAN_HALL_OR_A_CASTLE_CAN_GET_THEIR_EMBLEM_DISPLAYED_ON_CLAN_RELATED_ITEMS);
            } else if (clan.hasCrestLarge()) {
                CrestCache.getInstance().removePledgeCrestLarge(clan.getClanId());
            } else {
                LOGGER.warn("Character : {} tryed set wrong crest data check him!", activeChar.toString());
            }
            clan.setCrestLargeId(crestId);
            clan.broadcastClanStatus(false, true, false);
        }
    }
}
