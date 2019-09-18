package ru.j2dev.gameserver.network.lineage2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.Config;
import ru.j2dev.gameserver.ThreadPoolManager;
import ru.j2dev.gameserver.data.xml.holder.StringHolder;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.network.lineage2.components.SystemMsg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.ConfirmDlg;
import ru.j2dev.gameserver.network.lineage2.serverpackets.NpcHtmlMessage;
import ru.j2dev.gameserver.utils.Language;
import ru.j2dev.gameserver.utils.Util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

public class SecondPasswordAuth {
    private static final Logger LOG = LoggerFactory.getLogger(SecondPasswordAuth.class);

    private final String _login;
    private String _secondPassword;
    private int _tryLine;
    private long _blockEndTime;
    private SecondPasswordAuthUI _ui;

    public SecondPasswordAuth(final String login) {
        _login = login;
        _secondPassword = null;
    }

    private String getSecondPassword() {
        if (_secondPassword != null) {
            return _secondPassword;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rset = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("SELECT `password`, `tryLine`, `blockEndTime` FROM `second_auth` WHERE `login` = ?");
            pstmt.setString(1, _login);
            rset = pstmt.executeQuery();
            if (rset.next()) {
                _secondPassword = rset.getString("password");
                _tryLine = Math.min(Config.SECOND_AUTH_MAX_TRYS, rset.getInt("tryLine"));
                _blockEndTime = rset.getLong("blockEndTime");
            }
        } catch (SQLException se) {
            LOG.warn("Database error on retreiving second password for login '" + _login + "' :", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt, rset);
        }
        return _secondPassword;
    }

    private void store() {
        if (_secondPassword == null) {
            return;
        }
        Connection conn = null;
        PreparedStatement pstmt = null;
        try {
            conn = DatabaseFactory.getInstance().getConnection();
            pstmt = conn.prepareStatement("REPLACE INTO `second_auth`(`login`, `password`, `tryLine`, `blockEndTime`) VALUES (?, ?, ?, ?)");
            pstmt.setString(1, _login);
            pstmt.setString(2, _secondPassword);
            pstmt.setInt(3, getTrysCount());
            pstmt.setLong(4, _blockEndTime);
            pstmt.executeUpdate();
        } catch (SQLException se) {
            LOG.warn("Database error on storing second password for login '" + _login + "' :", se);
        } finally {
            DbUtils.closeQuietly(conn, pstmt);
        }
    }

    public boolean isSecondPasswordSet() {
        return getSecondPassword() != null;
    }

    public boolean isBlocked() {
        if (_blockEndTime == 0L) {
            return false;
        }
        if (_blockEndTime * 1000L < System.currentTimeMillis()) {
            _blockEndTime = 0L;
            _tryLine = 0;
            store();
            return false;
        }
        return true;
    }

    public int getBlockTimeLeft() {
        return (int) Math.max(0L, _blockEndTime - System.currentTimeMillis() / 1000L);
    }

    public int getTrysCount() {
        return Math.min(Config.SECOND_AUTH_MAX_TRYS, _tryLine);
    }

    public boolean isValidSecondPassword(final String checkSecondPassword) {
        if (checkSecondPassword == null && getSecondPassword() == null) {
            return true;
        }
        if (checkSecondPassword.equalsIgnoreCase(getSecondPassword())) {
            _blockEndTime = 0L;
            _tryLine = 0;
            store();
            return true;
        }
        ++_tryLine;
        if (_tryLine >= Config.SECOND_AUTH_MAX_TRYS) {
            _blockEndTime = System.currentTimeMillis() / 1000L + Config.SECOND_AUTH_BLOCK_TIME;
            _tryLine = Config.SECOND_AUTH_MAX_TRYS;
        }
        store();
        return false;
    }

    public boolean changePassword(final String oldSecondPassword, final String newSecondPassword) {
        if (!isValidSecondPassword(oldSecondPassword)) {
            return false;
        }
        _secondPassword = newSecondPassword;
        store();
        return true;
    }

    public SecondPasswordAuthUI getUI() {
        return _ui;
    }

    public void setUI(final SecondPasswordAuthUI ui) {
        _ui = ui;
    }

    public static class SecondPasswordAuthUI {
        private static final Language SPA_UI_LANG;
        private static final Random RND = new Random();

        static {
            Language lang = Language.ENGLISH;
            for (final Language lang2 : Language.VALUES) {
                if (lang2.getShortName().equals(Config.DEFAULT_LANG)) {
                    lang = lang2;
                }
            }
            SPA_UI_LANG = lang;
        }

        private SecondPasswordAuthUIType _type;
        private SPAUIPINInputData[] _inputs;
        private SPAUIPINInputData _inputFocus;
        private SecondPasswordAuthUIResult _result;
        private ArrayList<Integer> _numpad;
        private Runnable _runOnVerify;

        public SecondPasswordAuthUI(final SecondPasswordAuthUIType type) {
            Collections.shuffle(_numpad = new ArrayList<>(Arrays.asList(0, 1, 2, 3, 4, 5, 6, 7, 8, 9)), RND);
            _type = type;
            switch (_type) {
                case CREATE: {
                    (_inputs = new SPAUIPINInputData[2])[0] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.CREATE.PIN"), 0);
                    _inputs[1] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.CREATE.PINConfirm"), 1);
                    _inputFocus = _inputs[0];
                    break;
                }
                case VERIFY: {
                    (_inputs = new SPAUIPINInputData[1])[0] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.VERIFY.PIN"), 0);
                    _inputFocus = _inputs[0];
                    break;
                }
                case CHANGE: {
                    (_inputs = new SPAUIPINInputData[3])[0] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.CHANGE.PINOld"), 0);
                    _inputs[1] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.CHANGE.PINNew"), 1);
                    _inputs[2] = new SPAUIPINInputData(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.CHANGE.PINNewConfirm"), 2);
                    _inputFocus = _inputs[0];
                    break;
                }
            }
            _result = null;
        }

        public void setRunOnVerify(final Runnable runOnVerify) {
            _runOnVerify = runOnVerify;
        }

        private void handleArg(final GameClient client, final String args) {
            if ("cnl".equals(args)) {
                return;
            }
            if (args.startsWith("af")) {
                final int inputFieldidx = args.charAt(2) - '0';
                _inputFocus = _inputs[inputFieldidx];
            } else if (args.startsWith("np")) {
                if (_inputFocus != null) {
                    switch (args) {
                        case "npc":
                            _inputFocus.clear();
                            break;
                        case "npb":
                            _inputFocus.back();
                            break;
                        default:
                            final int digit = args.charAt(2) - '0';
                            if (digit >= 0 && digit <= 9 && _inputFocus.getLen() < 8) {
                                _inputFocus.add(digit);
                            }
                            break;
                    }
                }
            } else {
                if ("hlp".equals(args)) {
                    final NpcHtmlMessage html = new NpcHtmlMessage(5);
                    html.setFile("spahelp.htm");
                    client.sendPacket(html);
                    return;
                }
                if ("hlb".equals(args)) {
                    client.getSecondPasswordAuth().getUI().handle(client, "");
                    return;
                }
                if ("cgh".equals(args)) {
                    final SecondPasswordAuthUI changeUI = new SecondPasswordAuthUI(SecondPasswordAuthUIType.CHANGE);
                    changeUI.setRunOnVerify(_runOnVerify);
                    client.getSecondPasswordAuth().setUI(changeUI);
                    changeUI.handle(client, "");
                    return;
                }
                if ("okk".equals(args)) {
                    final SecondPasswordAuth spa = client.getSecondPasswordAuth();
                    if (spa == null) {
                        return;
                    }
                    switch (_type) {
                        case CREATE: {
                            if (_inputs[0].getLen() < 6 || _inputs[1].getLen() < 6) {
                                _result = SecondPasswordAuthUIResult.TOO_SHORT;
                                break;
                            }
                            if (!_inputs[0].toString().equals(_inputs[1].toString())) {
                                _result = SecondPasswordAuthUIResult.NOT_MATCH;
                                break;
                            }
                            if (!_inputs[0].isStrongPin()) {
                                _result = SecondPasswordAuthUIResult.TOO_SIMPLE;
                                break;
                            }
                            if (spa.isSecondPasswordSet()) {
                                _result = SecondPasswordAuthUIResult.ERROR;
                                break;
                            }
                            final String pin = _inputs[0].toString();
                            spa.changePassword(null, pin);
                            final SecondPasswordAuthUI verifyUI = new SecondPasswordAuthUI(SecondPasswordAuthUIType.VERIFY);
                            verifyUI.setRunOnVerify(_runOnVerify);
                            client.getSecondPasswordAuth().setUI(verifyUI);
                            verifyUI.handle(client, "");
                            return;
                        }
                        case CHANGE: {
                            if (!spa.isSecondPasswordSet()) {
                                _result = SecondPasswordAuthUIResult.ERROR;
                                break;
                            }
                            if (_inputs[0].getLen() < 6 || _inputs[1].getLen() < 6 || _inputs[2].getLen() < 6) {
                                _result = SecondPasswordAuthUIResult.TOO_SHORT;
                                break;
                            }
                            final String oldPin = _inputs[0].toString();
                            if (!_inputs[1].toString().equals(_inputs[2].toString())) {
                                _result = SecondPasswordAuthUIResult.NOT_MATCH;
                                break;
                            }
                            if (!_inputs[1].isStrongPin()) {
                                _result = SecondPasswordAuthUIResult.TOO_SIMPLE;
                                break;
                            }
                            final String newPin = _inputs[1].toString();
                            if (!spa.isBlocked() && spa.changePassword(oldPin, newPin)) {
                                final SecondPasswordAuthUI verifyUI2 = new SecondPasswordAuthUI(SecondPasswordAuthUIType.VERIFY);
                                verifyUI2.setRunOnVerify(_runOnVerify);
                                client.getSecondPasswordAuth().setUI(verifyUI2);
                                verifyUI2.handle(client, "");
                                return;
                            }
                            if (spa.isBlocked()) {
                                _result = SecondPasswordAuthUIResult.BLOCK_HOMEPAGE;
                                String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Result.BLOCK_HOMEPAGE");
                                blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(spa.getTrysCount()));
                                blockMsg = blockMsg.replace("%time%", Util.formatTime(spa.getBlockTimeLeft()));
                                client.close((new ConfirmDlg(SystemMsg.S1, -1)).addString(blockMsg));
                            } else {
                                _result = SecondPasswordAuthUIResult.FAIL_VERIFY;
                                verifyFail(client);
                            }
                            return;
                        }
                        case VERIFY: {
                            if (!spa.isSecondPasswordSet()) {
                                _result = SecondPasswordAuthUIResult.ERROR;
                                break;
                            }
                            final String pin = _inputs[0].toString();
                            if (!spa.isBlocked() && spa.isValidSecondPassword(pin)) {
                                if (_runOnVerify != null) {
                                    client.setSecondPasswordAuthed(true);
                                    ThreadPoolManager.getInstance().execute(_runOnVerify);
                                }
                                return;
                            }
                            if (spa.isBlocked()) {
                                _result = SecondPasswordAuthUIResult.BLOCK_HOMEPAGE;
                                String blockMsg2 = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Result.BLOCK_HOMEPAGE");
                                blockMsg2 = blockMsg2.replace("%tryCnt%", Integer.toString(spa.getTrysCount()));
                                blockMsg2 = blockMsg2.replace("%time%", Util.formatTime(spa.getBlockTimeLeft()));
                                client.close(new ConfirmDlg(SystemMsg.S1, -1).addString(blockMsg2));
                            } else {
                                _result = SecondPasswordAuthUIResult.FAIL_VERIFY;
                                verifyFail(client);
                            }
                            return;
                        }
                    }
                }
            }
            final NpcHtmlMessage html = new NpcHtmlMessage(5);
            html.setHtml(format());
            client.sendPacket(html);
        }

        public void handle(final GameClient client, final String args) {
            if (args != null) {
                try {
                    handleArg(client, args);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        public boolean verify(final GameClient client, final Runnable runOnSuccess) {
            final SecondPasswordAuth spa = client.getSecondPasswordAuth();
            if (spa == null) {
                return false;
            }
            setRunOnVerify(runOnSuccess);
            if (spa.isBlocked()) {
                _result = SecondPasswordAuthUIResult.BLOCK_HOMEPAGE;
                String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Result.BLOCK_HOMEPAGE");
                blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(spa.getTrysCount()));
                blockMsg = blockMsg.replace("%time%", Util.formatTime(spa.getBlockTimeLeft()));
                client.close((new ConfirmDlg(SystemMsg.S1, -1)).addString(blockMsg));
                return false;
            }
            handle(client, "");
            return true;
        }

        private void verifyFail(final GameClient client) {
            final SecondPasswordAuth spa = client.getSecondPasswordAuth();
            if (spa == null) {
                return;
            }
            _result = SecondPasswordAuthUIResult.FAIL_VERIFY;
            String blockMsg = StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Result.FAIL_VERIFY");
            blockMsg = blockMsg.replace("%tryCnt%", Integer.toString(Config.SECOND_AUTH_MAX_TRYS - spa.getTrysCount()));
            client.close(new ConfirmDlg(SystemMsg.S1, -1).addString(blockMsg));
        }

        private String getTitle() {
            return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth." + _type.name() + ".Title");
        }

        private String getFormDescription() {
            return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth." + _type.name() + ".Description");
        }

        private String getNote() {
            if (_result == null) {
                return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Note");
            }
            return StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Result." + _result.name());
        }

        private String getInputDescription() {
            return "<font color=\"a2a0a2\">(Enter PIN - Mouse Click 6 to 8 digits)</font>";
        }

        private String format() {
            final StringBuilder sb = new StringBuilder(8192);
            sb.append("<html>");
            sb.append("<head><title>").append(getTitle()).append("</title></head>");
            sb.append("<body><center>");
            sb.append("<table width=270 border=0 cellspacing=0 cellpadding=0>");
            sb.append("<br>");
            sb.append("<tr><td fixwidth=270 align=center>");
            formatFormContent(sb);
            sb.append("</td></tr>");
            sb.append("<tr><td fixwidth=270 align=center>");
            sb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
            formatNote(sb);
            sb.append("</td></tr>");
            sb.append("<tr><td align=center>");
            sb.append("<img src=\"L2UI.SquareBlank\" width=270 height=10>");
            formatButtons(sb);
            sb.append("</td></tr>");
            sb.append("</table>");
            sb.append("</center></body>");
            sb.append("</html> ");
            return sb.toString();
        }

        private void formatFormContent(final StringBuilder sb) {
            sb.append("<table width=260 height=250 border=0 cellspacing=5 cellpadding=0 bgcolor=000000>");
            sb.append("<tr><td valign=TOP height=80>").append(getFormDescription()).append("</td></tr>");
            sb.append("<tr><td align=CENTER>").append(getInputDescription()).append("</td></tr>");
            sb.append("<tr><td valign=TOP>");
            formatInputs(sb);
            sb.append("<br>");
            sb.append("</td></tr>");
            sb.append("<tr><td valign=TOP align=center height=100>");
            sb.append("<img src=\"L2UI.SquareGray\" width=250 height=1><br>");
            formatNumPad(sb);
            sb.append("<br>");
            sb.append("</td></tr>");
            sb.append("</table>");
        }

        private void formatInputs(final StringBuilder sb) {
            sb.append("<table width=250 height=60 border=0 cellspacing=5 cellpadding=0>");
            for (final SPAUIPINInputData suid : _inputs) {
                suid.formatPINInput(sb, _inputFocus == suid);
            }
            sb.append("</table>");
        }

        private void formatNote(final StringBuilder sb) {
            sb.append("<table height=20 border=0 cellspacing=0 cellpadding=0 bgcolor=\"000000\">").append("<tr><td align=center valign=center fixwidth=264>").append(getNote()).append("</td></tr></table>");
        }

        private void formatButtons(final StringBuilder sb) {
            sb.append("<table width=260 border=0 cellspacing=0 cellpadding=0><tr>");
            sb.append("<td fixwidth=50>");
            if (_type.isCanChange()) {
                sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Change")).append("\" action=\"bypass -h spa_cgh\">");
            }
            sb.append("</td>");
            sb.append("<td width=60>&nbsp;</td>");
            sb.append("<td>");
            sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Help")).append("\" action=\"bypass -h spa_hlp\">");
            sb.append("</td>");
            sb.append("<td>");
            sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.OK")).append("\" action=\"bypass -h spa_okk\">");
            sb.append("</td>");
            sb.append("<td>");
            sb.append("<button width=47 height=21 fore=\"L2UI_CH3.smallbutton1\" back=\"L2UI_CH3.smallbutton1_down\" value=\"").append(StringHolder.getInstance().getNotNull(SPA_UI_LANG, "SecondPasswordAuth.Cancel")).append("\" action=\"bypass -h spa_cnl\">");
            sb.append("</td>");
            sb.append("</tr></table>");
        }

        public void formatNumPad(final StringBuilder sb) {
            sb.append("<table width=90 border=0 cellspacing=0 cellpadding=0>");
            for (int i = 0; i < 3; ++i) {
                sb.append("<tr>");
                for (int j = 0; j < 3; ++j) {
                    final int idx = i * 3 + j;
                    final int num = _numpad.get(idx);
                    sb.append("<td>");
                    sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_").append(num).append("\" back=\"L2UI_CH3.calculate2_").append(num).append("_down\" value=\"\" action=\"bypass spa_np").append(num).append("\">");
                    sb.append("</td>");
                }
                sb.append("</tr>");
            }
            sb.append("<tr><td>");
            final int num = _numpad.get(9);
            sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_c\" back=\"L2UI_CH3.calculate2_c_down\" action=\"bypass spa_npc\">");
            sb.append("</td><td>");
            sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_").append(num).append("\" back=\"L2UI_CH3.calculate2_").append(num).append("_down\" value=\"\" action=\"bypass spa_np").append(num).append("\">");
            sb.append("</td><td>");
            sb.append("<button width=35 height=24 fore=\"L2UI_CH3.calculate2_bs\" back=\"L2UI_CH3.calculate2_bs_down\" action=\"bypass spa_npb\">");
            sb.append("</td></tr>");
            sb.append("</table>");
        }

        public enum SecondPasswordAuthUIResult {
            TOO_SHORT,
            NOT_MATCH,
            TOO_SIMPLE,
            FAIL_VERIFY,
            BLOCK_HOMEPAGE,
            ERROR
        }

        public enum SecondPasswordAuthUIType {
            CREATE(false),
            VERIFY(true),
            CHANGE(false);

            private final boolean _canChange;

            SecondPasswordAuthUIType(final boolean canChange) {
                _canChange = canChange;
            }

            public boolean isCanChange() {
                return _canChange;
            }
        }

        private static class SPAUIPINInputData {
            private final Stack<Integer> _pin;
            private final String _label;
            private final int _inputFieldIdx;

            public SPAUIPINInputData(final String label, final int inputFieldIdx) {
                _pin = new Stack<>();
                _label = label;
                _inputFieldIdx = inputFieldIdx;
            }

            public String getLabel() {
                return _label;
            }

            public int getInputFieldIdx() {
                return _inputFieldIdx;
            }

            public void clear() {
                _pin.clear();
            }

            public void back() {
                if (!_pin.isEmpty()) {
                    _pin.pop();
                }
            }

            public void add(final int digit) {
                _pin.add(digit);
            }

            public boolean isEmpty() {
                return _pin.isEmpty();
            }

            public boolean isStrongPin() {
                return !isEmpty();
            }

            public int getLen() {
                return _pin.size();
            }

            private void formatPINInputBox(final StringBuilder sb, final boolean isActive, final int len, final String link) {
                final int dWidth = 8 * Math.min(8, len);
                final int cWidth = isActive ? 1 : 0;
                final int eWidth = 65 - (dWidth + cWidth);
                final String hTexture = isActive ? "L2UI_CH3.inputbox02_over" : "L2UI_CH3.M_inputbox02";
                final String vTexture = isActive ? "L2UI_CH3.inputbox04_over" : "L2UI_CH3.M_inputbox04";
                final String dTexture = isActive ? "L2UI_CH3.radar_tutorial1" : "L2UI_CH3.radar_tutorial2";
                sb.append("<table width=67 height=12 border=0 cellspacing=0 cellpadding=0>");
                sb.append("<tr><td>");
                sb.append("<img src=\"").append(hTexture).append("\" width=67 height=1>");
                sb.append("</td></tr>");
                sb.append("<tr><td>");
                sb.append("<table width=67 height=12 border=0 cellspacing=0 cellpadding=0><tr>");
                sb.append("<td><img src=\"").append(vTexture).append("\" width=1 height=12></td>");
                sb.append("<td>");
                sb.append("<img src=\"L2UI.SquareBlank\" width=65 height=4>");
                sb.append("<table border=0 cellspacing=0 cellpadding=0><tr>");
                if (dWidth > 0) {
                    sb.append("<td fixwidth=").append(dWidth).append(">");
                    sb.append("<button width=").append(dWidth).append(" height=8 ").append("fore=\"").append(dTexture).append("\" back=\"").append(dTexture).append("\" value=\" \"");
                    if (link != null) {
                        sb.append(" action=\"bypass spa_").append(link).append("\"");
                    }
                    sb.append(">");
                    sb.append("</td>");
                }
                if (cWidth > 0) {
                    sb.append("<td valign=TOP><img src=\"L2UI.SquareWhite\" width=1 height=8></td>");
                }
                if (eWidth > 0) {
                    sb.append("<td FIXWIDTH=").append(eWidth).append(">");
                    sb.append("<button width=").append(eWidth).append(" height=8 ").append("fore=\"L2UI.SquareBlank\" back=\"L2UI.SquareBlank\" value=\" \"");
                    if (link != null) {
                        sb.append(" action=\"bypass spa_").append(link).append("\"");
                    }
                    sb.append(">");
                    sb.append("</td>");
                }
                sb.append("</tr></table>");
                sb.append("</td>");
                sb.append("<td><img src=\"").append(vTexture).append("\" width=1 height=12></td>");
                sb.append("</tr></table>");
                sb.append("</td></tr>");
                sb.append("<tr><td>");
                sb.append("<img src=\"").append(hTexture).append("\" width=67 height=1>");
                sb.append("</td></tr>");
                sb.append("</table>");
            }

            public void formatPINInput(final StringBuilder sb, final boolean isActive) {
                sb.append("<tr>");
                sb.append("<td align=right valign=TOP fixwidth=100>");
                if (!isActive) {
                    sb.append("<font color=\"a2a0a2\">").append(getLabel()).append("</font>");
                } else {
                    sb.append(getLabel());
                }
                sb.append("</td>");
                sb.append("<td align=left>");
                formatPINInputBox(sb, isActive, getLen(), String.format("af%d", getInputFieldIdx()));
                sb.append("</td>");
                sb.append("</tr>");
            }

            @Override
            public String toString() {
                final StringBuilder pinText = new StringBuilder(8);
                for (final Integer digit : _pin) {
                    pinText.append((char) (48 + Math.min(9, Math.max(digit, 0))));
                }
                return pinText.toString();
            }
        }
    }
}
