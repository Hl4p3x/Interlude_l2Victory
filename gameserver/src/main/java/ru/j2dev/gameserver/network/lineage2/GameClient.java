package ru.j2dev.gameserver.network.lineage2;

import org.apache.commons.lang3.mutable.MutableLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.commons.net.nio.impl.MMOClient;
import ru.j2dev.commons.net.nio.impl.MMOConnection;
import ru.j2dev.commons.net.nio.impl.SendablePacket;
import ru.j2dev.commons.threading.RunnableImpl;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.GameServer;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.cache.Msg;
import ru.j2dev.gameserver.dao.CharacterDAO;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.manager.BypassManager;
import ru.j2dev.gameserver.manager.BypassManager.BypassType;
import ru.j2dev.gameserver.manager.BypassManager.DecodedBypass;
import ru.j2dev.gameserver.model.CharSelectInfoPackage;
import ru.j2dev.gameserver.model.GameObjectsStorage;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.authcomm.AuthServerCommunication;
import ru.j2dev.gameserver.network.authcomm.SessionKey;
import ru.j2dev.gameserver.network.authcomm.gs2as.PlayerLogout;
import ru.j2dev.gameserver.network.lineage2.clientpackets.L2GameClientPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.L2GameServerPacket;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.network.lineage2.serverpackets.RequestNetPing;

import java.nio.ByteBuffer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.stream.IntStream;

public final class GameClient extends MMOClient<MMOConnection<GameClient>> {
    public static final String NO_IP = "?.?.?.?";
    private static final Logger LOGGER = LoggerFactory.getLogger(GameClient.class);
    public static int DEFAULT_PAWN_CLIPPING_RANGE = 2048;

    public GameCrypt _crypt;
    public GameClientState _state = GameClientState.CONNECTED;
    private String _login;
    private Player _activeChar;
    private SessionKey _sessionKey;
    private String _ip;
    private int revision;
    private int serverId;
    private String _hwid;
    private List<Integer> _charSlotMapping = new ArrayList<>();
    private List<String> _bypasses;
    private List<String> _bypasses_bbs;
    private Map<Class<? extends L2GameClientPacket>, MutableLong> _lastIncomePacketTimeStamp = new ConcurrentHashMap<>();
    private SecondPasswordAuth _secondPasswordAuth;
    private boolean _isSecondPasswordAuthed;
    private int _failedPackets;
    private int _unknownPackets;
    private int _pingTimestamp;
    private int _ping;
    private int _fps;
    private int _pawnClippingRange;
    private ScheduledFuture<?> _pingTaskFuture;

    public GameClient(final MMOConnection<GameClient> con) {
        super(con);
        _crypt = (CGMHelper.isActive() ? CGMHelper.getInstance().createCrypt() : new GameCrypt());
        _ip = con.getSocket().getInetAddress().getHostAddress();
    }

    public int getServerId() {
        return serverId;
    }

    public void setServerId(int serverId) {
        this.serverId = serverId;
    }

    @Override
    protected void onDisconnection() {
        if (_pingTaskFuture != null) {
            _pingTaskFuture.cancel(true);
            _pingTaskFuture = null;
        }
        setState(GameClientState.DISCONNECTED);
        final Player player = getActiveChar();
        setActiveChar(null);
        if (player != null) {
            player.setNetConnection(null);
            player.scheduleDelete();
        }
        if (getSessionKey() != null) {
            if (isAuthed()) {
                AuthServerCommunication.getInstance().removeAuthedClient(getLogin());
                AuthServerCommunication.getInstance().sendPacket(new PlayerLogout(getLogin()));
            } else {
                AuthServerCommunication.getInstance().removeWaitingClient(getLogin());
            }
        }
    }

    @Override
    protected void onForcedDisconnection() {
    }

    public void markRestoredChar(final int charslot) {
        final int objid = getObjectIdForSlot(charslot);
        if (objid < 0) {
            return;
        }
        if (_activeChar != null && _activeChar.getObjectId() == objid) {
            _activeChar.setDeleteTimer(0);
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET deletetime=0 WHERE obj_id=?");
            statement.setInt(1, objid);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void markToDeleteChar(final int charslot) {
        final int objid = getObjectIdForSlot(charslot);
        if (objid < 0) {
            return;
        }
        if (_activeChar != null && _activeChar.getObjectId() == objid) {
            _activeChar.setDeleteTimer((int) (System.currentTimeMillis() / 1000L));
        }
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("UPDATE characters SET deletetime=? WHERE obj_id=?");
            statement.setLong(1, (int) (System.currentTimeMillis() / 1000L));
            statement.setInt(2, objid);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("data error on update deletime char:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void deleteCharacterInSlot(final int charslot) {
        if (_activeChar != null) {
            return;
        }
        final int objid = getObjectIdForSlot(charslot);
        if (objid == -1) {
            return;
        }
        deleteCharacterByCharacterObjId(objid);
    }

    public void deleteCharacterByCharacterObjId(final int charObjId) {
        CharacterDAO.getInstance().deleteCharacterDataByObjId(charObjId, true);
    }

    public Player loadCharFromDisk(final int charslot) {
        final int objectId = getObjectIdForSlot(charslot);
        if (objectId == -1) {
            return null;
        }
        Player character = null;
        final Player oldPlayer = GameObjectsStorage.getPlayer(objectId);
        if (oldPlayer != null) {
            if (oldPlayer.isInOfflineMode() || oldPlayer.isLogoutStarted()) {
                oldPlayer.kick();
                return null;
            }
            oldPlayer.sendPacket(Msg.ANOTHER_PERSON_HAS_LOGGED_IN_WITH_THE_SAME_ACCOUNT);
            final GameClient oldClient = oldPlayer.getNetConnection();
            if (oldClient != null) {
                oldClient.setActiveChar(null);
                oldClient.closeNow(false);
            }
            oldPlayer.setNetConnection(this);
            character = oldPlayer;
        }
        if (character == null) {
            character = Player.restore(objectId);
        }
        if (character != null) {
            setActiveChar(character);
        } else {
            LOGGER.warn("could not restore obj_id: " + objectId + " in slot:" + charslot);
        }
        return character;
    }

    public int getObjectIdForSlot(final int charslot) {
        if (charslot < 0 || charslot >= _charSlotMapping.size()) {
            LOGGER.warn(getLogin() + " tried to modify Character in slot " + charslot + " but no characters exits at that slot.");
            return -1;
        }
        return _charSlotMapping.get(charslot);
    }

    public int getSlotForObjectId(final int objectId) {
        final List<Integer> charSlotMapping = _charSlotMapping;
        return IntStream.range(0, charSlotMapping.size()).filter(slotIdx -> Integer.valueOf(objectId).equals(charSlotMapping.get(slotIdx))).findFirst().orElse(-1);
    }

    public SecondPasswordAuth getSecondPasswordAuth() {
        if (getLogin() == null || !Config.USE_SECOND_PASSWORD_AUTH) {
            return null;
        }
        if (_secondPasswordAuth == null) {
            _secondPasswordAuth = new SecondPasswordAuth(getLogin());
        }
        return _secondPasswordAuth;
    }

    public boolean isSecondPasswordAuthed() {
        return _secondPasswordAuth != null && _isSecondPasswordAuthed;
    }

    public void setSecondPasswordAuthed(final boolean authed) {
        _isSecondPasswordAuthed = authed;
    }

    public Player getActiveChar() {
        return _activeChar;
    }

    public void setActiveChar(final Player player) {
        _activeChar = player;
        if (player != null) {
            player.setNetConnection(this);
        }
    }

    public SessionKey getSessionKey() {
        return _sessionKey;
    }

    public String getLogin() {
        return _login;
    }

    public void setLoginName(final String loginName) {
        _login = loginName;
    }

    public void setSessionId(final SessionKey sessionKey) {
        _sessionKey = sessionKey;
    }

    public void setCharSelection(final CharSelectInfoPackage[] chars) {
        _charSlotMapping.clear();
        Arrays.stream(chars).mapToInt(CharSelectInfoPackage::getObjectId).forEach(objectId -> _charSlotMapping.add(objectId));
    }

    public void setCharSelection(final int c) {
        _charSlotMapping.clear();
        _charSlotMapping.add(c);
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(final int revision) {
        this.revision = revision;
    }

    public String getHwid() {
        return _hwid;
    }

    public void setHwid(final String hwid) {
        _hwid = hwid;
    }

    private List<String> getStoredBypasses(final boolean bbs) {
        if (bbs) {
            if (_bypasses_bbs == null) {
                _bypasses_bbs = new ArrayList<>();
            }
            return _bypasses_bbs;
        }
        if (_bypasses == null) {
            _bypasses = new ArrayList<>();
        }
        return _bypasses;
    }

    public void cleanBypasses(final boolean bbs) {
        final List<String> bypassStorage = getStoredBypasses(bbs);
        synchronized (bypassStorage) {
            bypassStorage.clear();
        }
    }

    public String encodeBypasses(final String htmlCode, final boolean bbs) {
        final List<String> bypassStorage = getStoredBypasses(bbs);
        synchronized (bypassStorage) {
            return BypassManager.encode(htmlCode, bypassStorage, bbs);
        }
    }

    public DecodedBypass decodeBypass(final String bypass) {
        final BypassType bpType = BypassManager.getBypassType(bypass);
        final boolean bbs = bpType == BypassType.ENCODED_BBS || bpType == BypassType.SIMPLE_BBS;
        final List<String> bypassStorage = getStoredBypasses(bbs);
        if (bpType == BypassType.ENCODED || bpType == BypassType.ENCODED_BBS) {
            return BypassManager.decode(bypass, bypassStorage, bbs, this);
        }
        if (bpType == BypassType.SIMPLE || bpType == BypassType.SIMPLE_BBS) {
            return new DecodedBypass(bypass, bbs).trim();
        }
        LOGGER.warn("Direct access to bypass: " + bypass + " / " + toString());
        return null;
    }

    public long getLastIncomePacketTimeStamp(final Class<? extends L2GameClientPacket> pktCls) {
        MutableLong theVal = _lastIncomePacketTimeStamp.computeIfAbsent(pktCls, k -> new MutableLong(0L));
        return theVal.longValue();
    }

    public void setLastIncomePacketTimeStamp(final Class<? extends L2GameClientPacket> pktCls, final long val) {
        MutableLong theVal = _lastIncomePacketTimeStamp.computeIfAbsent(pktCls, k -> new MutableLong(0L));
        theVal.setValue(val);
    }

    @Override
    public boolean encrypt(final ByteBuffer buf, final int size) {
        _crypt.encrypt(buf.array(), buf.position(), size);
        buf.position(buf.position() + size);
        return true;
    }

    @Override
    public boolean decrypt(final ByteBuffer buf, final int size) {
        return _crypt.decrypt(buf.array(), buf.position(), size);
    }

    public void sendPacket(final L2GameServerPacket gsp) {
        if (!isConnected()) {
            return;
        }
        if (gsp instanceof NpcHtmlMessage) {
            final NpcHtmlMessage npcHtmlMessage = (NpcHtmlMessage) gsp;
            npcHtmlMessage.processHtml(this);
        }
        getConnection().sendPacket(gsp);
    }

    public void sendPacket(final L2GameServerPacket... gsps) {
        if (!isConnected()) {
            return;
        }
        Arrays.stream(gsps).filter(gsp -> gsp instanceof NpcHtmlMessage).map(gsp -> (NpcHtmlMessage) gsp).forEach(npcHtmlMessage -> npcHtmlMessage.processHtml(this));
        getConnection().sendPacket((SendablePacket<GameClient>[]) gsps);
    }

    public void sendPackets(final List<L2GameServerPacket> gsps) {
        if (!isConnected()) {
            return;
        }
        gsps.stream().filter(gsp -> gsp instanceof NpcHtmlMessage).map(gsp -> (NpcHtmlMessage) gsp).forEach(npcHtmlMessage -> npcHtmlMessage.processHtml(this));
        getConnection().sendPackets(gsps);
    }

    public void close(final L2GameServerPacket gsp) {
        if (!isConnected()) {
            return;
        }
        if (gsp instanceof NpcHtmlMessage) {
            final NpcHtmlMessage npcHtmlMessage = (NpcHtmlMessage) gsp;
            npcHtmlMessage.processHtml(this);
        }
        getConnection().close(gsp);
    }

    public String getIpAddr() {
        return _ip;
    }

    public byte[] enableCrypt() {
        final byte[] key = CGMHelper.isActive() ? CGMHelper.getInstance().getRandomKey() : BlowFishKeygen.getRandomKey();
        _crypt.setKey(key);
        return key;
    }

    public GameClientState getState() {
        return _state;
    }

    public void setState(final GameClientState state) {
        _state = state;
        switch (state) {
            case AUTHED: {
                onPing(0, 0, DEFAULT_PAWN_CLIPPING_RANGE);
                break;
            }
        }
    }

    public void onPacketReadFail() {
        if (_failedPackets++ >= 10) {
            LOGGER.warn("Too many client packet fails, connection closed : " + this);
            closeNow(true);
        }
    }

    public void onUnknownPacket() {
        if (_unknownPackets++ >= 10) {
            LOGGER.warn("Too many client unknown packets, connection closed : " + this);
            closeNow(true);
        }
    }

    public void logUnknownPacket(final int opcode) {
        LOGGER.warn("Unknown packet : Opcode : " + opcode + " to client" + this);
    }

    @Override
    public String toString() {
        return _state + " IP: " + getIpAddr() + ((_login == null) ? "" : (" Account: " + _login)) + ((_activeChar == null) ? "" : (" Player : " + _activeChar));
    }

    public void onPing(final int timestamp, final int fps, final int pawnClipRange) {
        if (_pingTimestamp == 0 || _pingTimestamp == timestamp) {
            final long nowMs = System.currentTimeMillis();
            final long serverStartTimeMs = GameServer.getInstance().getServerStartTime();
            _ping = ((_pingTimestamp > 0) ? ((int) (nowMs - serverStartTimeMs - timestamp)) : 0);
            _fps = fps;
            _pawnClippingRange = pawnClipRange;
            _pingTaskFuture = ThreadPoolManager.getInstance().schedule(new PingTask(this), 30000L);
        }
    }

    private void doPing() {
        final long nowMs = System.currentTimeMillis();
        final long serverStartTimeMs = GameServer.getInstance().getServerStartTime();
        final int timestamp = (int) (nowMs - serverStartTimeMs);
        _pingTimestamp = timestamp;
        sendPacket(new RequestNetPing(timestamp));
    }

    public int getPing() {
        return _ping;
    }

    public int getFps() {
        return _fps;
    }

    public int getPawnClippingRange() {
        return _pawnClippingRange;
    }

    public enum GameClientState {
        CONNECTED,
        AUTHED,
        IN_GAME,
        DISCONNECTED
    }

    private static class PingTask extends RunnableImpl {
        private final GameClient _client;

        private PingTask(final GameClient client) {
            _client = client;
        }

        @Override
        public void runImpl() {
            if (_client == null || !_client.isConnected()) {
                return;
            }
            _client.doPing();
        }
    }
}
