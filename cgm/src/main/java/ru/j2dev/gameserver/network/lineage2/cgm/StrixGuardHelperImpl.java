package ru.j2dev.gameserver.network.lineage2.cgm;


import ru.j2dev.gameserver.network.lineage2.CGMHelper;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.GameCrypt;
import ru.j2dev.gameserver.network.lineage2.cgm.sxg.ISXGWrapper;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;

public class StrixGuardHelperImpl extends CGMHelper
{
    private final ISXGWrapper wrapper;
    
    private static ISXGWrapper getWrapper() {
        ISXGWrapper inst = null;
        try {
            inst = (ISXGWrapper) Class.forName("ru.j2dev.gameserver.network.lineage2.cgm.sxg.SXGWrapper").newInstance();
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return inst;
    }
    
    public StrixGuardHelperImpl() {
        (wrapper = getWrapper()).init();
    }
    
    @Override
    public L2GameClientPacket handle(final GameClient client, final int opcode) {
        if (!wrapper.isEnabled()) {
            return null;
        }
        return wrapper.handle(client, opcode);
    }
    
    @Override
    public GameCrypt createCrypt() {
        return wrapper.createCrypt();
    }
    
    @Override
    public byte[] getRandomKey() {
        return wrapper.getRandomKey();
    }
    
    @Override
    public void addHWIDBan(final String hwid, final String ip, final String account, final String comment) {
        wrapper.addHWIDBan(hwid, ip, account, comment);
    }
}
