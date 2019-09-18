package ru.j2dev.gameserver.network.lineage2.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.database.mysql;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.SecondPasswordAuth.SecondPasswordAuthUI;
import ru.j2dev.gameserver.network.lineage2.SecondPasswordAuth.SecondPasswordAuthUI.SecondPasswordAuthUIType;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterDeleteFail;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterDeleteSuccess;
import ru.j2dev.gameserver.network.lineage2.serverpackets.CharacterSelectionInfo;

public class RequestCharacterDelete extends L2GameClientPacket {
    private static final Logger LOGGER = LoggerFactory.getLogger(RequestCharacterDelete.class);

    private int _charSlot;

    @Override
    protected void readImpl() {
        _charSlot = readD();
    }

    @Override
    protected void runImpl() {
        final int clan = clanStatus();
        final int online = onlineStatus();
        if (clan > 0 || online > 0) {
            if (clan == 2) {
                sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_CLAN_LEADERS_MAY_NOT_BE_DELETED));
            } else if (clan == 1) {
                sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_YOU_MAY_NOT_DELETE_CLAN_MEMBER));
            } else if (online > 0) {
                sendPacket(new CharacterDeleteFail(CharacterDeleteFail.REASON_DELETION_FAILED));
            }
            return;
        }
        final GameClient client = getClient();
        final Runnable doDelete = new RunnableImpl() {
            @Override
            public void runImpl() {
                try {
                    if (Config.DELETE_DAYS == 0) {
                        client.deleteCharacterInSlot(_charSlot);
                    } else {
                        client.markToDeleteChar(_charSlot);
                    }
                    client.sendPacket(new CharacterDeleteSuccess());
                } finally {
                    final CharacterSelectionInfo cl = new CharacterSelectionInfo(client.getLogin(), client.getSessionKey().playOkID1);
                    client.sendPacket(cl);
                    client.setCharSelection(cl.getCharInfo());
                }
            }
        };
        if (Config.USE_SECOND_PASSWORD_AUTH && !client.isSecondPasswordAuthed()) {
            if (client.getSecondPasswordAuth().isSecondPasswordSet()) {
                if (client.getSecondPasswordAuth().getUI() == null) {
                    client.getSecondPasswordAuth().setUI(new SecondPasswordAuthUI(SecondPasswordAuthUIType.VERIFY));
                }
            } else if (client.getSecondPasswordAuth().getUI() == null) {
                client.getSecondPasswordAuth().setUI(new SecondPasswordAuthUI(SecondPasswordAuthUIType.CREATE));
            }
            client.getSecondPasswordAuth().getUI().verify(client, doDelete);
        } else {
            ThreadPoolManager.getInstance().execute(doDelete);
        }
    }

    private int clanStatus() {
        final int obj = getClient().getObjectIdForSlot(_charSlot);
        if (obj == -1) {
            return 0;
        }
        if (mysql.simple_get_int("clanid", "characters", "obj_Id=" + obj) <= 0) {
            return 0;
        }
        if (mysql.simple_get_int("leader_id", "clan_subpledges", "leader_id=" + obj + " AND type = " + 0) > 0) {
            return 2;
        }
        return 1;
    }

    private int onlineStatus() {
        final int obj = getClient().getObjectIdForSlot(_charSlot);
        if (obj == -1) {
            return 0;
        }
        if (mysql.simple_get_int("online", "characters", "obj_Id=" + obj) > 0) {
            return 1;
        }
        return 0;
    }
}
