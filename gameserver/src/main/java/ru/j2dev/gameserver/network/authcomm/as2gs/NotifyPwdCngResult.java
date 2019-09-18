package ru.j2dev.gameserver.network.authcomm.as2gs;

import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.World;
import ru.j2dev.gameserver.network.authcomm.ReceivablePacket;

public class NotifyPwdCngResult extends ReceivablePacket {
    public static final int RESULT_OK = 1;
    public static final int RESULT_WRONG_OLD_PASSWORD = 2;
    public static final int RESULT_WRONG_NEW_PASSWORD = 3;
    public static final int RESULT_WRONG_ACCOUNT = 4;
    private int _requestor_oid;
    private int _result;

    @Override
    protected void readImpl() {
        _requestor_oid = readD();
        _result = readD();
    }

    @Override
    protected void runImpl() {
        final Player player = World.getPlayer(_requestor_oid);
        if (player == null) {
            return;
        }
        switch (_result) {
            case RESULT_OK: {
                player.sendMessage("Password succesfuly changed.");
                player.setVar("LastPwdChng", Long.toString(System.currentTimeMillis() / 1000L), -1L);
                break;
            }
            case RESULT_WRONG_OLD_PASSWORD: {
                player.sendMessage("Can't change password! Wrong old password.");
                break;
            }
            case RESULT_WRONG_NEW_PASSWORD: {
                player.sendMessage("Can't change password! Wrong new password.");
                break;
            }
            case RESULT_WRONG_ACCOUNT: {
                player.sendMessage("Can't change password! Wrong account.");
                break;
            }
            default: {
                player.sendMessage("Can't change password! System error. Try later.");
                break;
            }
        }
    }
}
