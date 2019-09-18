package ru.j2dev.gameserver.dao;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.j2dev.commons.dao.JdbcDAO;
import ru.j2dev.commons.dao.JdbcEntityState;
import ru.j2dev.commons.dao.JdbcEntityStats;
import ru.j2dev.commons.dbutils.DbUtils;
import ru.j2dev.gameserver.database.DatabaseFactory;
import ru.j2dev.gameserver.model.items.ItemInstance;
import ru.j2dev.gameserver.model.mail.Mail;
import ru.j2dev.gameserver.model.mail.Mail.SenderType;

import java.sql.*;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class MailDAO implements JdbcDAO<Integer, Mail> {
    private static final Logger LOGGER = LoggerFactory.getLogger(MailDAO.class);
    private static final String RESTORE_MAIL = "SELECT sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread FROM mail WHERE message_id = ?";
    private static final String STORE_MAIL = "INSERT INTO mail(sender_id, sender_name, receiver_id, receiver_name, expire_time, topic, body, price, type, unread) VALUES (?,?,?,?,?,?,?,?,?,?)";
    private static final String UPDATE_MAIL = "UPDATE mail SET sender_id = ?, sender_name = ?, receiver_id = ?, receiver_name = ?, expire_time = ?, topic = ?, body = ?, price = ?, type = ?, unread = ? WHERE message_id = ?";
    private static final String REMOVE_MAIL = "DELETE FROM mail WHERE message_id = ?";
    private static final String RESTORE_EXPIRED_MAIL = "SELECT message_id FROM mail WHERE expire_time <= ?";
    private static final String RESTORE_OWN_MAIL = "SELECT message_id FROM character_mail WHERE char_id = ? AND is_sender = ?";
    private static final String STORE_OWN_MAIL = "INSERT INTO character_mail(char_id, message_id, is_sender) VALUES (?,?,?)";
    private static final String REMOVE_OWN_MAIL = "DELETE FROM character_mail WHERE char_id = ? AND message_id = ? AND is_sender = ?";
    private static final String RESTORE_MAIL_ATTACHMENTS = "SELECT item_id FROM mail_attachments WHERE message_id = ?";
    private static final String STORE_MAIL_ATTACHMENT = "REPLACE INTO mail_attachments(message_id, item_id) VALUES (?,?)";
    private static final String REMOVE_MAIL_ATTACHMENTS = "DELETE FROM mail_attachments WHERE message_id = ?";
    private static final MailDAO instance = new MailDAO();

    private final Cache<Integer, Mail> cache;

    private final JdbcEntityStats stats;
    private final AtomicLong load;
    private final AtomicLong insert;
    private final AtomicLong update;
    private final AtomicLong delete;

    private MailDAO() {
        load = new AtomicLong();
        insert = new AtomicLong();
        update = new AtomicLong();
        delete = new AtomicLong();
        stats = new JdbcEntityStats() {
            @Override
            public long getLoadCount() {
                return load.get();
            }

            @Override
            public long getInsertCount() {
                return insert.get();
            }

            @Override
            public long getUpdateCount() {
                return update.get();
            }

            @Override
            public long getDeleteCount() {
                return delete.get();
            }
        };
        cache = Caffeine.newBuilder().maximumSize(100000).expireAfterWrite(8, TimeUnit.HOURS).build();
    }


    public static MailDAO getInstance() {
        return instance;
    }

    public Cache getCache() {
        return cache;
    }


    @Override
    public JdbcEntityStats getStats() {
        return stats;
    }

    private void save0(final Mail mail) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(STORE_MAIL, Statement.RETURN_GENERATED_KEYS);
            statement.setInt(1, mail.getSenderId());
            statement.setString(2, mail.getSenderName());
            statement.setInt(3, mail.getReceiverId());
            statement.setString(4, mail.getReceiverName());
            statement.setInt(5, mail.getExpireTime());
            statement.setString(6, mail.getTopic());
            statement.setString(7, mail.getBody());
            statement.setLong(8, mail.getPrice());
            statement.setInt(9, mail.getType().ordinal());
            statement.setBoolean(10, mail.isUnread());
            statement.execute();
            rset = statement.getGeneratedKeys();
            rset.next();
            mail.setMessageId(rset.getInt(1));
            if (!mail.getAttachments().isEmpty()) {
                DbUtils.close(statement);
                statement = con.prepareStatement(STORE_MAIL_ATTACHMENT);
                for (final ItemInstance item : mail.getAttachments()) {
                    statement.setInt(1, mail.getMessageId());
                    statement.setInt(2, item.getObjectId());
                    statement.addBatch();
                }
                statement.executeBatch();
            }
            DbUtils.close(statement);
            if (mail.getType() == SenderType.NORMAL) {
                statement = con.prepareStatement(STORE_OWN_MAIL);
                statement.setInt(1, mail.getSenderId());
                statement.setInt(2, mail.getMessageId());
                statement.setBoolean(3, true);
                statement.execute();
            }
            DbUtils.close(statement);
            statement = con.prepareStatement(STORE_OWN_MAIL);
            statement.setInt(1, mail.getReceiverId());
            statement.setInt(2, mail.getMessageId());
            statement.setBoolean(3, false);
            statement.execute();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        insert.incrementAndGet();
    }


    private Mail load0(final int messageId) throws SQLException {
        Mail mail = null;
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(RESTORE_MAIL);
            statement.setInt(1, messageId);
            rset = statement.executeQuery();
            if (rset.next()) {
                mail = new Mail();
                mail.setMessageId(messageId);
                mail.setSenderId(rset.getInt(1));
                mail.setSenderName(rset.getString(2));
                mail.setReceiverId(rset.getInt(3));
                mail.setReceiverName(rset.getString(4));
                mail.setExpireTime(rset.getInt(5));
                mail.setTopic(rset.getString(6));
                mail.setBody(rset.getString(7));
                mail.setPrice(rset.getLong(8));
                mail.setType(SenderType.VALUES[rset.getInt(9)]);
                mail.setUnread(rset.getBoolean(10));
                DbUtils.close(statement, rset);
                statement = con.prepareStatement(RESTORE_MAIL_ATTACHMENTS);
                statement.setInt(1, messageId);
                rset = statement.executeQuery();
                while (rset.next()) {
                    final int objectId = rset.getInt(1);
                    final ItemInstance item = ItemsDAO.getInstance().load(objectId);
                    if (item != null) {
                        mail.addAttachment(item);
                    }
                }
            }
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        load.incrementAndGet();
        return mail;
    }

    private void update0(final Mail mail) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(UPDATE_MAIL);
            statement.setInt(1, mail.getSenderId());
            statement.setString(2, mail.getSenderName());
            statement.setInt(3, mail.getReceiverId());
            statement.setString(4, mail.getReceiverName());
            statement.setInt(5, mail.getExpireTime());
            statement.setString(6, mail.getTopic());
            statement.setString(7, mail.getBody());
            statement.setLong(8, mail.getPrice());
            statement.setInt(9, mail.getType().ordinal());
            statement.setBoolean(10, mail.isUnread());
            statement.setInt(11, mail.getMessageId());
            statement.execute();
            if (mail.getAttachments().isEmpty()) {
                DbUtils.close(statement);
                statement = con.prepareStatement(REMOVE_MAIL_ATTACHMENTS);
                statement.setInt(1, mail.getMessageId());
                statement.execute();
            }
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        update.incrementAndGet();
    }

    private void delete0(final Mail mail) throws SQLException {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(REMOVE_MAIL);
            statement.setInt(1, mail.getMessageId());
            statement.execute();
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
        delete.incrementAndGet();
    }


    private List<Mail> getMailByOwnerId(final int ownerId, final boolean sent) {
        List<Integer> messageIds = Collections.emptyList();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(RESTORE_OWN_MAIL);
            statement.setInt(1, ownerId);
            statement.setBoolean(2, sent);
            rset = statement.executeQuery();
            messageIds = new ArrayList<>();
            while (rset.next()) {
                messageIds.add(rset.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.error("Error while restore mail of owner : " + ownerId, e);
            messageIds.clear();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return load(messageIds);
    }

    private boolean deleteMailByOwnerIdAndMailId(final int ownerId, final int messageId, final boolean sent) {
        Connection con = null;
        PreparedStatement statement = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(REMOVE_OWN_MAIL);
            statement.setInt(1, ownerId);
            statement.setInt(2, messageId);
            statement.setBoolean(3, sent);
            return statement.execute();
        } catch (SQLException e) {
            LOGGER.error("Error while deleting mail of owner : " + ownerId, e);
            return false;
        } finally {
            DbUtils.closeQuietly(con, statement);
        }
    }


    public List<Mail> getReceivedMailByOwnerId(final int receiverId) {
        return getMailByOwnerId(receiverId, false);
    }


    public List<Mail> getSentMailByOwnerId(final int senderId) {
        return getMailByOwnerId(senderId, true);
    }


    public Mail getReceivedMailByMailId(final int receiverId, final int messageId) {
        final List<Mail> list = getMailByOwnerId(receiverId, false);
        return list.stream().filter(mail -> mail.getMessageId() == messageId).findFirst().orElse(null);
    }


    public Mail getSentMailByMailId(final int senderId, final int messageId) {
        final List<Mail> list = getMailByOwnerId(senderId, true);
        return list.stream().filter(mail -> mail.getMessageId() == messageId).findFirst().orElse(null);
    }

    public boolean deleteReceivedMailByMailId(final int receiverId, final int messageId) {
        return deleteMailByOwnerIdAndMailId(receiverId, messageId, false);
    }

    public boolean deleteSentMailByMailId(final int senderId, final int messageId) {
        return deleteMailByOwnerIdAndMailId(senderId, messageId, true);
    }


    public List<Mail> getExpiredMail(final int expireTime) {
        List<Integer> messageIds = Collections.emptyList();
        Connection con = null;
        PreparedStatement statement = null;
        ResultSet rset = null;
        try {
            con = DatabaseFactory.getInstance().getConnection();
            statement = con.prepareStatement(RESTORE_EXPIRED_MAIL);
            statement.setInt(1, expireTime);
            rset = statement.executeQuery();
            messageIds = new ArrayList<>();
            while (rset.next()) {
                messageIds.add(rset.getInt(1));
            }
        } catch (SQLException e) {
            LOGGER.error("Error while restore expired mail!", e);
            messageIds.clear();
        } finally {
            DbUtils.closeQuietly(con, statement, rset);
        }
        return load(messageIds);
    }


    @Override
    public Mail load(final Integer id) {
        final Mail ce = cache.getIfPresent(id);
        if (ce != null) {
            return ce;
        }
        Mail mail;
        try {
            mail = load0(id);
            if (mail != null) {
                mail.setJdbcState(JdbcEntityState.STORED);
                cache.put(mail.getMessageId(), mail);
            }
        } catch (SQLException e) {
            LOGGER.error("Error while restoring mail : " + id, e);
            return null;
        }
        return mail;
    }


    public List<Mail> load(final Collection<Integer> messageIds) {
        if (messageIds.isEmpty()) {
            return Collections.emptyList();
        }
        return messageIds.stream().mapToInt(messageId -> messageId).mapToObj(this::load).filter(Objects::nonNull).collect(Collectors.toCollection(() -> new ArrayList<>(messageIds.size())));
    }

    @Override
    public void save(final Mail mail) {
        if (!mail.getJdbcState().isSavable()) {
            return;
        }
        try {
            save0(mail);
            mail.setJdbcState(JdbcEntityState.STORED);
        } catch (SQLException e) {
            LOGGER.error("Error while saving mail!", e);
            return;
        }
        cache.put(mail.getMessageId(), mail);
    }

    @Override
    public void update(final Mail mail) {
        if (!mail.getJdbcState().isUpdatable()) {
            return;
        }
        try {
            update0(mail);
            mail.setJdbcState(JdbcEntityState.STORED);
        } catch (SQLException e) {
            LOGGER.error("Error while updating mail : " + mail.getMessageId(), e);
            return;
        }
        cache.put(mail.getMessageId(), mail);
    }

    @Override
    public void saveOrUpdate(final Mail mail) {
        if (mail.getJdbcState().isSavable()) {
            save(mail);
        } else if (mail.getJdbcState().isUpdatable()) {
            update(mail);
        }
    }

    @Override
    public void delete(final Mail mail) {
        if (!mail.getJdbcState().isDeletable()) {
            return;
        }
        try {
            delete0(mail);
            mail.setJdbcState(JdbcEntityState.DELETED);
        } catch (SQLException e) {
            LOGGER.error("Error while deleting mail : " + mail.getMessageId(), e);
            return;
        }
        cache.invalidate(mail.getMessageId());
    }
}
