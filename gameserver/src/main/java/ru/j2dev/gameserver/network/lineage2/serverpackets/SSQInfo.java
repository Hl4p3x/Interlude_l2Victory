package ru.j2dev.gameserver.network.lineage2.serverpackets;

import ru.j2dev.gameserver.model.entity.SevenSigns;

public class SSQInfo extends L2GameServerPacket {
    private int _state;

    public SSQInfo() {
        _state = 0;
        final int compWinner = SevenSigns.getInstance().getCabalHighestScore();
        if (SevenSigns.getInstance().isSealValidationPeriod()) {
            if (compWinner == 2) {
                _state = 2;
            } else if (compWinner == 1) {
                _state = 1;
            }
        }
    }

    public SSQInfo(final int state) {
        _state = 0;
        _state = state;
    }

    @Override
    protected final void writeImpl() {
        writeC(0xf8);
        switch (_state) {
            case 1: {
                writeH(0x101);
                break;
            }
            case 2: {
                writeH(0x102);
                break;
            }
            default: {
                writeH(0x100);
                break;
            }
        }
    }
}
