package ru.j2dev.gameserver.network.lineage2.serverpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.model.entity.residence.ClanHall;
import ru.j2dev.gameserver.model.entity.residence.ResidenceFunction;

public class AgitDecoInfo extends L2GameServerPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(AgitDecoInfo.class);
    private static int[] _buff = {0, 1, 1, 1, 2, 2, 2, 2, 2, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2};
    private static int[] _itCr8 = {0, 1, 2, 2};

    private int _id;
    private int hp_recovery;
    private int mp_recovery;
    private int exp_recovery;
    private int teleport;
    private int curtains;
    private int itemCreate;
    private int support;
    private int platform;

    public AgitDecoInfo(final ClanHall clanHall) {
        _id = clanHall.getId();
        hp_recovery = getHpRecovery(clanHall.isFunctionActive(ResidenceFunction.RESTORE_HP) ? clanHall.getFunction(ResidenceFunction.RESTORE_HP).getLevel() : 0);
        mp_recovery = getMpRecovery(clanHall.isFunctionActive(ResidenceFunction.RESTORE_MP) ? clanHall.getFunction(ResidenceFunction.RESTORE_MP).getLevel() : 0);
        exp_recovery = getExpRecovery(clanHall.isFunctionActive(ResidenceFunction.RESTORE_EXP) ? clanHall.getFunction(ResidenceFunction.RESTORE_EXP).getLevel() : 0);
        teleport = clanHall.isFunctionActive(ResidenceFunction.TELEPORT) ? clanHall.getFunction(ResidenceFunction.TELEPORT).getLevel() : 0;
        curtains = clanHall.isFunctionActive(ResidenceFunction.CURTAIN) ? clanHall.getFunction(ResidenceFunction.CURTAIN).getLevel() : 0;
        itemCreate = clanHall.isFunctionActive(ResidenceFunction.ITEM_CREATE) ? _itCr8[clanHall.getFunction(ResidenceFunction.ITEM_CREATE).getLevel()] : 0;
        support = clanHall.isFunctionActive(ResidenceFunction.SUPPORT) ? _buff[clanHall.getFunction(ResidenceFunction.SUPPORT).getLevel()] : 0;
        platform = clanHall.isFunctionActive(ResidenceFunction.PLATFORM) ? clanHall.getFunction(ResidenceFunction.PLATFORM).getLevel() : 0;
    }

    private static int getHpRecovery(final int percent) {
        switch (percent) {
            case 0: {
                return 0;
            }
            case 20:
            case 40:
            case 80:
            case 120:
            case 140: {
                return 1;
            }
            case 160:
            case 180:
            case 200:
            case 220:
            case 240:
            case 260:
            case 280:
            case 300: {
                return 2;
            }
            default: {
                LOGGER.warn("Unsupported percent " + percent + " in hp recovery");
                return 0;
            }
        }
    }

    private static int getMpRecovery(final int percent) {
        switch (percent) {
            case 0: {
                return 0;
            }
            case 5:
            case 10:
            case 15:
            case 20: {
                return 1;
            }
            case 25:
            case 30:
            case 35:
            case 40:
            case 45:
            case 50: {
                return 2;
            }
            default: {
                LOGGER.warn("Unsupported percent " + percent + " in mp recovery");
                return 0;
            }
        }
    }

    private static int getExpRecovery(final int percent) {
        switch (percent) {
            case 0: {
                return 0;
            }
            case 5:
            case 10:
            case 15:
            case 20: {
                return 1;
            }
            case 25:
            case 30:
            case 35:
            case 40:
            case 45:
            case 50: {
                return 2;
            }
            default: {
                LOGGER.warn("Unsupported percent " + percent + " in exp recovery");
                return 0;
            }
        }
    }

    @Override
    protected final void writeImpl() {
        writeC(0xf7);
        writeD(_id);
        writeC(hp_recovery);
        writeC(mp_recovery);
        writeC(mp_recovery);
        writeC(exp_recovery);
        writeC(teleport);
        writeC(0);
        writeC(curtains);
        writeC(itemCreate);
        writeC(support);
        writeC(support);
        writeC(platform);
        writeC(itemCreate);
        writeD(0);
        writeD(0);
    }
}
