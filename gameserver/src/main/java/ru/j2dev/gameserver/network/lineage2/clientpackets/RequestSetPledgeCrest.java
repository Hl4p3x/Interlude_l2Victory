package ru.j2dev.gameserver.network.lineage2.clientpackets;

import ru.j2dev.gameserver.cache.CrestCache;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.pledge.Clan;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;

public class RequestSetPledgeCrest extends L2GameClientPacket {
    private int _length;
    private byte[] _data;

    @Override
    protected void readImpl() {
        _length = readD();
        if (_length == CrestCache.CREST_SIZE && _length == _buf.remaining()) {
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
        if ((activeChar.getClanPrivileges() & 0x80) == 0x80) {
            if (clan.isPlacedForDisband()) {
                activeChar.sendPacket(SystemMsg.DISPERSION_HAS_ALREADY_BEEN_REQUESTED);
                return;
            }
            if (clan.getLevel() < 3) {
                activeChar.sendPacket(Msg.CLAN_CREST_REGISTRATION_IS_ONLY_POSSIBLE_WHEN_CLANS_SKILL_LEVELS_ARE_ABOVE_3);
                return;
            }
            int crestId = 0;
            if (_data != null && CrestCache.isValidCrestData(_data, _length)) {
                crestId = CrestCache.getInstance().savePledgeCrest(clan.getClanId(), _data);
            } else if (clan.hasCrest()) {
                CrestCache.getInstance().removePledgeCrest(clan.getClanId());
            } else {
                LOGGER.warn("Character : {} tryed set wrong crest data check him!", activeChar.toString());
            }
            clan.setCrestId(crestId);
            clan.broadcastClanStatus(false, true, false);
        }
    }
}
