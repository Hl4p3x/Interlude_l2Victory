package ru.j2dev.gameserver.model.actor.instances.player;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.Player;
import ru.j2dev.gameserver.model.actor.instances.player.Macro.L2MacroCmd;
import ru.j2dev.gameserver.network.lineage2.serverpackets.SendMacroList;
import ru.j2dev.gameserver.utils.Strings;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class MacroList {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacroList.class);

    private final Player player;
    private final Map<Integer, Macro> _macroses = new HashMap<>();
    private int _revision = 1;
    private int _macroId = 1000;

    public MacroList(final Player player) {
        this.player = player;
    }

    public int getRevision() {
        return _revision;
    }

    public Collection<Macro> getAllMacroses() {
        return _macroses.values();
    }

    public Macro getMacro(final int id) {
        return _macroses.get(id - 1);
    }

    public void registerMacro(final Macro macro) {
        if (macro.id == 0) {
            macro.id = _macroId++;
            while (_macroses.get(macro.id) != null) {
                macro.id = _macroId++;
            }
            _macroses.put(macro.id, macro);
            registerMacroInDb(macro);
        } else {
            final Macro old = _macroses.put(macro.id, macro);
            if (old != null) {
                deleteMacroFromDb(old);
            }
            registerMacroInDb(macro);
        }
        sendUpdate();
    }

    public void deleteMacro(final int id) {
        final Macro toRemove = _macroses.get(id);
        if (toRemove != null) {
            deleteMacroFromDb(toRemove);
        }
        _macroses.remove(id);
        sendUpdate();
    }

    public void sendUpdate() {
        ++_revision;
        final Collection<Macro> all = getAllMacroses();
        if (all.size() == 0) {
            player.sendPacket(new SendMacroList(_revision, all.size(), null));
        } else {
            all.stream().map(m -> new SendMacroList(_revision, all.size(), m)).forEach(player::sendPacket);
        }
    }

    private void registerMacroInDb(final Macro macro) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("REPLACE INTO character_macroses (char_obj_id,id,icon,name,descr,acronym,commands) values(?,?,?,?,?,?,?)");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, macro.id);
            statement.setInt(3, macro.icon);
            statement.setString(4, macro.name);
            statement.setString(5, macro.descr);
            statement.setString(6, macro.acronym);
            final StringBuilder sb = new StringBuilder();
            for (final L2MacroCmd cmd : macro.commands) {
                sb.append(cmd.type).append(',');
                sb.append(cmd.d1).append(',');
                sb.append(cmd.d2);
                if (cmd.cmd != null && cmd.cmd.length() > 0) {
                    sb.append(',').append(cmd.cmd);
                }
                sb.append(';');
            }
            statement.setString(7, sb.toString());
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("could not store macro: " + macro, e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    private void deleteMacroFromDb(final Macro macro) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("DELETE FROM character_macroses WHERE char_obj_id=? AND id=?");
            statement.setInt(1, player.getObjectId());
            statement.setInt(2, macro.id);
            statement.execute();
        } catch (Exception e) {
            LOGGER.error("could not delete macro:", e);
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }

    public void restore() {
        _macroses.clear();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement("SELECT char_obj_id, id, icon, name, descr, acronym, commands FROM character_macroses WHERE char_obj_id=?");
            statement.setInt(1, player.getObjectId());
            rset = statement.executeQuery();
            while (rset.next()) {
                final int id = rset.getInt("id");
                final int icon = rset.getInt("icon");
                final String name = Strings.stripSlashes(rset.getString("name"));
                final String descr = Strings.stripSlashes(rset.getString("descr"));
                final String acronym = Strings.stripSlashes(rset.getString("acronym"));
                final List<L2MacroCmd> commands = new ArrayList<>();
                final StringTokenizer st1 = new StringTokenizer(rset.getString("commands"), ";");
                while (st1.hasMoreTokens()) {
                    final StringTokenizer st2 = new StringTokenizer(st1.nextToken(), ",");
                    final int type = Integer.parseInt(st2.nextToken());
                    final int d1 = Integer.parseInt(st2.nextToken());
                    final int d2 = Integer.parseInt(st2.nextToken());
                    String cmd = "";
                    if (st2.hasMoreTokens()) {
                        cmd = st2.nextToken();
                    }
                    final L2MacroCmd mcmd = new L2MacroCmd(commands.size(), type, d1, d2, cmd);
                    commands.add(mcmd);
                }
                final Macro m = new Macro(id, icon, name, descr, acronym, commands.toArray(new L2MacroCmd[0]));
                _macroses.put(m.id, m);
            }
        } catch (Exception e) {
            LOGGER.error("could not restore shortcuts:", e);
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
    }
}
