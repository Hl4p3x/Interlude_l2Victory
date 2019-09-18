package ru.j2dev.gameserver.idfactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.gameserver.database.DatabaseFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public abstract class IdFactory {
    private static Logger LOGGER = LoggerFactory.getLogger(IdFactory.class);

    public static final String[][] EXTRACT_OBJ_ID_TABLES = {
            {"characters", "obj_id"},
            {"items", "item_id"},
            {"clan_data", "clan_id"},
            {"ally_data", "ally_id"},
            {"couples", "id"}};

    protected boolean _initialized;

    public static final int FIRST_OID = 0x10000000;
    public static final int LAST_OID = 0x7FFFFFFF;
    public static final int FREE_OBJECT_ID_SIZE = LAST_OID - FIRST_OID;

    protected static final IdFactory _instance = new BitSetIDFactory();

    public static IdFactory getInstance() {
        return _instance;
    }

    protected IdFactory() {
        setAllCharacterOffline();
        cleanUpDB();
        cleanUpTimeStamps();
    }

    /**
     * Sets all character offline
     */
    private static void setAllCharacterOffline() {
        try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            Statement statement = con.createStatement();
            statement.executeUpdate("UPDATE characters SET online = 0");
            statement.close();

            LOGGER.info("Updated characters online status.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Cleans up Database
     */
    private static void cleanUpDB() {
        try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            int cleanCount = 0;
            Statement stmt = con.createStatement();

            // Character related
            cleanCount += stmt.executeUpdate("DELETE FROM character_blocklist WHERE character_blocklist.obj_Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_blocklist WHERE character_blocklist.target_Id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_effects_save WHERE character_effects_save.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_friends WHERE character_friends.friend_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_group_reuse WHERE character_group_reuse.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_hennas WHERE character_hennas.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_instances WHERE character_instances.obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_macroses WHERE character_macroses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_mail WHERE character_mail.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_post_friends WHERE character_post_friends.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_premium_items WHERE character_premium_items.charId NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_quests WHERE character_quests.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_recipebook WHERE character_recipebook.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_recommends WHERE character_recommends.objId NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_recommends WHERE character_recommends.targetId NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_shortcuts WHERE character_shortcuts.object_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_skills WHERE character_skills.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_skills_save WHERE character_skills_save.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_subclasses WHERE character_subclasses.char_obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM character_variables WHERE character_variables.obj_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM pets WHERE pets.item_obj_id NOT IN (SELECT item_id FROM items);");
            cleanCount += stmt.executeUpdate("DELETE FROM seven_signs WHERE seven_signs.char_obj_id NOT IN (SELECT obj_Id FROM characters);");

            // Olympiads & Heroes
            cleanCount += stmt.executeUpdate("DELETE FROM oly_heroes WHERE oly_heroes.char_id NOT IN (SELECT obj_Id FROM characters);");
            cleanCount += stmt.executeUpdate("DELETE FROM oly_nobles WHERE oly_nobles.char_id NOT IN (SELECT obj_Id FROM characters);");

            // Items
            cleanCount += stmt.executeUpdate("DELETE FROM items WHERE items.owner_id NOT IN (SELECT obj_Id FROM characters) AND items.owner_id NOT IN (SELECT clan_id FROM clan_data);");

            stmt.close();
            LOGGER.info("Cleaned " + cleanCount + " elements from database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void cleanUpTimeStamps() {
        try (Connection con = DatabaseFactory.getInstance().getConnection()) {
            int cleanCount = 0;
            PreparedStatement stmt = con.prepareStatement("DELETE FROM character_skills_save WHERE end_time <= ?");
            stmt.setLong(1, System.currentTimeMillis());
            cleanCount += stmt.executeUpdate();
            stmt.close();

            LOGGER.info("Cleaned " + cleanCount + " expired timestamps from database.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    protected static List<Integer> extractUsedObjectIDTable() throws SQLException {
        final List<Integer> temp = new ArrayList<>();
        try (Connection con = DatabaseFactory.getInstance().getConnection(); Statement s = con.createStatement()) {

            StringBuilder extractUsedObjectIdsQuery = new StringBuilder();

            for (final String[] tblClmn : EXTRACT_OBJ_ID_TABLES) {
                extractUsedObjectIdsQuery.append("SELECT ").append(tblClmn[1]).append(" FROM ").append(tblClmn[0]).append(" UNION ");
            }

            extractUsedObjectIdsQuery = new StringBuilder(extractUsedObjectIdsQuery.substring(0, extractUsedObjectIdsQuery.length() - 7)); // Remove the last " UNION "
            try (ResultSet rs = s.executeQuery(extractUsedObjectIdsQuery.toString())) {
                while (rs.next()) {
                    temp.add(rs.getInt(1));
                }
            }
        }
        temp.sort(Integer::compareTo);

        return temp;
    }

    public boolean isInitialized() {
        return _initialized;
    }

    public abstract int getNextId();

    public abstract void releaseId(int id);

    public abstract int size();
}