package ru.protection;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.configuration.PropertiesParser;
import ru.j2dev.commons.util.CRC16;
import ru.j2dev.commons.util.RC4;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.network.lineage2.GameClient;
import ru.j2dev.gameserver.network.lineage2.serverpackets.GameGuardQuery;
import ru.j2dev.gameserver.network.lineage2.serverpackets.LoginFail;

import java.io.File;
import java.io.FileWriter;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CatsGuard {
    private static final Logger LOGGER = LoggerFactory.getLogger(CatsGuard.class);

        private class CatsGuardReader implements GameClient.IExReader
        {
            private RC4 _crypt;
            private GameClient _client;
            private int _prevcode = 0;
            private byte[] buffer = new byte[4];
            private int _state;
            private boolean _checkChar;

            private CatsGuardReader(GameClient cl)
            {
                _state = 0;
                _client = cl;
            }

            @Override
            public void checkChar(Player cha)
            {
                if (!_checkChar || cha == null)
                {
                    return;
                }
                cha.getNetConnection().closeNow(false);
            }

            private int decryptPacket(byte[] packet)
            {
                packet = _crypt.rc4(packet);
                int crc = CRC16.calc(new byte[]
                        {
                                (byte) (_prevcode & 0xff), packet[1]
                        });
                int read_crc = (((packet[3] & 0xff) << 8) & 0xff00) | (packet[2] & 0xff);
                if (crc != read_crc)
                {
                    Action(_client, "CRC error");
                    return 0;
                }
                _prevcode = packet[1] & 0xff;
                return _prevcode;
            }

            @Override
            public int read(ByteBuffer buf)
            {
                int opcode = 0;
                if (_state == 0)
                {
                    opcode = buf.get() & 0xff;
                    if (opcode != 0xca)
                    {
                        Action(_client, "Invalid opcode on pre-auth state");
                        return 0;
                    }
                }
                else if (buf.remaining() < 4)
                {
                    Action(_client, "Invalid block size on authed state");
                }
                else
                {
                    buf.get(buffer);
                    opcode = decryptPacket(buffer) & 0xff;
                }
                return opcode;
            }

            private void setKey(int[] data)
            {
                String key = "";
                for (int i = 0; i < 10; i++)
                {
                    key += String.format("%X%X", data[1], _SERVER_KEY);
                }
                _crypt = new RC4(key, false);
                _state = 1;
            }
        }
        private static final Logger _log = LoggerFactory.getLogger(CatsGuard.class);
        private static boolean ENABLED = true;
    private static CatsGuard _instance;

    public static CatsGuard getInstance()
    {
        if (_instance == null)
        {
            _instance = new CatsGuard();
        }
        return _instance;
    }
        private Map<String, Integer> _connections;
        public Set<Hwid> _lockhwid;
        private List<String> _bannedhwid;
        private static int _SERVER_KEY = 7958915;
        private static String _hwid;
        private int MAX_SESSIONS;
        private String LOG_OPTION;
        private String ON_HACK_ATTEMP;
        private static boolean HWID;
        public static final String catsguard = "config/catsguard.properties";

        private CatsGuard()
        {
            try
            {
                if (_SERVER_KEY == 0)
                {
                    return;
                }
                PropertiesParser p = Config.load(catsguard);
                ENABLED = Boolean.parseBoolean(p.getProperty("Enabled", "true")) && ENABLED;
                if (!ENABLED)
                {
                    _log.info("CatsGuard: disabled");
                    return;
                }
                LOG_OPTION = p.getProperty("LogOption", "NOPROTECT HACK SESSIONS");
                HWID = Boolean.parseBoolean(p.getProperty("Hwid", "false"));
                MAX_SESSIONS = Integer.parseInt(p.getProperty("MaxSessionsFromHWID", "-1"));
                ON_HACK_ATTEMP = p.getProperty("OnHackAttempt", "kick");
                _connections = new ConcurrentHashMap<String, Integer>();
                _lockhwid = new HashSet<Hwid>();
                _bannedhwid = new ArrayList<String>();
                loadHwid();
                if (HWID)
                {
                    Hwid();
                }
                _log.info("CatsGuard: Ready");
                _log.info("CatsGuard: Loaded " + _bannedhwid.size() + " banned hwid(s)");
            }
            catch (Exception e)
            {
                _log.warn("CatsGuard: Error while loading config/catsguard.properties", e);
                ENABLED = false;
            }
        }

        public void doneSession(GameClient cl)
        {
            if (!ENABLED)
            {
                return;
            }
            if (cl.getHwid() != null)
            {
                if (_connections.containsKey(cl.getHwid()))
                {
                    int nwnd = _connections.get(cl.getHwid());
                    if (nwnd == 0)
                    {
                        _connections.remove(cl.getHwid());
                    }
                    else
                    {
                        _connections.put(cl.getHwid(), --nwnd);
                    }
                }
            }
            cl._reader = null;
        }

        private void Action(GameClient cl, String reason)
        {
            if (ON_HACK_ATTEMP.equals("hwidban") && cl.getHwid() != null)
            {
                if (!_bannedhwid.contains(cl.getHwid()))
                {
                    _bannedhwid.add(cl.getHwid());
                    saveHwidBan();
                }
            }
            else if (ON_HACK_ATTEMP.equals("jail") && cl.getActiveChar() != null)
            {
                cl.getActiveChar().setVar("jailed", "Зашита", 600);
            }
            else if (ON_HACK_ATTEMP.equals("ban") && cl.getActiveChar() != null)
            {
                cl.getActiveChar().setAccessLevel(-100);
            }
            _log.info("CatsGuard: Client " + cl + " use illegal software and will " + ON_HACK_ATTEMP + "ed. Reason: " + reason);
            cl.closeNow(false);
        }

        public void initSession(GameClient cl)
        {
            if (!ENABLED)
            {
                return;
            }
            cl.sendPacket(new GameGuardQuery());
            cl._reader = new CatsGuardReader(cl);
        }

        public void initSession(GameClient cl, int[] data)
        {
            if (!ENABLED)
            {
                return;
            }
            if (data[0] != _SERVER_KEY)
            {
                if (LOG_OPTION.contains("NOPROTECT"))
                {
                    _log.info("CatsGuard: Client " + cl + " try to log with no CatsGuard");
                }
                cl.closeNow(false);
                return;
            }
            String hwid = String.format("%x", data[3]);
            if (cl._reader == null)
            {
                if (LOG_OPTION.contains("HACK"))
                {
                    _log.info("CatsGuard: Client " + cl + " has no pre-authed state");
                }
                cl.closeNow(false);
                return;
            }
            if (_bannedhwid.contains(hwid))
            {
                ((CatsGuardReader) cl._reader)._checkChar = true;
            }
            if (!_connections.containsKey(hwid))
            {
                _connections.put(hwid, 0);
            }
            int nwindow = _connections.get(hwid);
            int max = MAX_SESSIONS;
            if (max > 0 && ++nwindow > max)
            {
                if (LOG_OPTION.contains("SESSIONS"))
                {
                    _log.info("CatsGuard: To many sessions from hwid " + hwid);
                }
                cl.closeNow(false);
                return;
            }
            for (Hwid announce : _lockhwid)
            {
                if (announce.getLogin().equals(cl.getLogin()))
                {
                    _hwid = announce.getHwid();
                }
            }
            if (HWID && !isSameHWID(hwid, _hwid))
            {
                cl.close(new LoginFail(LoginFail.ACCESS_FAILED_TRY_LATER));
                return;
            }
            _connections.put(hwid, nwindow);
            cl.setHwid(hwid);
            ((CatsGuardReader) cl._reader).setKey(data);
        }

        public boolean isEnabled()
        {
            return ENABLED;
        }

        public boolean isSameHWID(String hwid1, String hwid2)
        {
            if (hwid2 == null)
            {
                return true;
            }
            if (isEnabled())
            {
                return hwid1.equals(hwid2);
            }
            return true;
        }

        ;
        public void Hwid()
        {
            if (HWID && !ENABLED)
            {
                return;
            }
            try
            {
                new File("guard").mkdir();
                File f = new File("guard/Hwid.txt");
                if (!f.exists())
                {
                    f.createNewFile();
                }
                List<String> lines = Arrays.asList(FileUtils.readFileToString(new File("guard/Hwid.txt"), "UTF-8").split("\n"));
                for (String line : lines)
                {
                    if (line.length() == 0)
                    {
                        continue;
                    }
                    StringTokenizer token = new StringTokenizer(line, ",");
                    if (token.countTokens() > 1)
                    {
                        addHwid(token.nextToken(), token.nextToken());
                    }
                }
            }
            catch (Exception e)
            {
                _log.error("Error while saving guard/Hwid.txt!", e);
            }
        }

        public void loadHwid()
        {
            _bannedhwid.clear();
            try
            {
                new File("guard").mkdir();
                File f = new File("guard/BanHwid.txt");
                if (!f.exists())
                {
                    f.createNewFile();
                }
                List<String> lines = Arrays.asList(FileUtils.readFileToString(new File("guard/BanHwid.txt"), "UTF-8").split("\n"));
                for (String line : lines)
                {
                    if (line.length() == 0)
                    {
                        continue;
                    }
                    _bannedhwid.add(line);
                }
            }
            catch (Exception e)
            {
                _log.error("Error while loading guard/BanHwid.txt!");
            }
        }

        private void saveHwidBan()
        {
            try
            {
                new File("guard").mkdir();
                File f = new File("guard/BanHwid.txt");
                if (!f.exists())
                {
                    f.createNewFile();
                }
                FileWriter writer = new FileWriter(f, false);
                for (int i = 0; i < _bannedhwid.size(); i++)
                {
                    writer.write(_bannedhwid.get(i) + "\n");
                }
                writer.close();
            }
            catch (Exception e)
            {
                _log.error("Error while saving guard/BanHwid.txt!", e);
            }
        }

        public void saveHwid()
        {
            try
            {
                new File("guard").mkdir();
                File f = new File("guard/Hwid.txt");
                if (!f.exists())
                {
                    f.createNewFile();
                }
                FileWriter writer = new FileWriter(f, false);
                for (Hwid announce : _lockhwid)
                {
                    writer.write(announce.getLogin() + "," + announce.getHwid() + "\n");
                }
                writer.close();
            }
            catch (Exception e)
            {
                _log.error("Error while saving guard/Hwid.txt!", e);
            }
        }

        public class Hwid
        {
            private final String _login;
            private final String _hwid;

            public Hwid(String l, String h)
            {
                _login = l;
                _hwid = h;
            }

            public String getLogin()
            {
                return _login;
            }

            public String getHwid()
            {
                return _hwid;
            }
        }

        public void addHwid(String val, String text)
        {
            Hwid _Hwid = new Hwid(val, text);
            _lockhwid.add(_Hwid);
        }

        public Set<Hwid> getHwid()
        {
            return _lockhwid;
        }

        public void delHwid(Hwid line)
        {
            _lockhwid.remove(line);
        }
    }
